/*
 *********************************************************************

 $Id$


 Copyright (c) 2007-2014 Whitestein Technologies AG,
 Riedstrasse 13, CH-6330 Cham, Switzerland.
 All rights reserved.

 This software is confidential and proprietary information of
 Whitestein Technologies AG.
 You shall not disclose this confidential information and shall use
 it only in accordance with the terms of the license agreement you
 entered into with Whitestein Technologies AG.
 The use of this file in source or binary form requires a written
 license from Whitestein Technologies AG.
 *********************************************************************
 */
package com.lst.deploymentautomation.vaadin.core;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.whitestein.lsps.engine.lang.ExecutionContext;
import com.whitestein.lsps.lang.Decimal;
import com.whitestein.lsps.lang.exec.MapHolder;
import com.whitestein.lsps.lang.exec.RecordHolder;

/**
 * Custom {@link Navigator} subclass that remembers UI state when moving back/forward in browser history.
 * 
 * @author mhi
 */
public class AppNavigator extends Navigator {

	private static final long serialVersionUID = 1L;

	/** Delimiter used in the URL to keep the ID of the view in the UI history. */
	public static final String HISTORY_ID_DELIMITER = "/~";

	private static final int MAX_VIEW_COUNT = 10;
	private static final long ONE_YEAR = 1000 * 3600 * 24 * 365;

	private String historyId;

	private Map<String, AppView> views = new HashMap<String, AppView>();
	private List<String> history = new LinkedList<String>();
	private List<String> future = new LinkedList<String>();

	/**
	 * Full constructor.
	 * @param ui
	 * @param display
	 */
	public AppNavigator(UI ui, ViewDisplay display) {
		super(ui, new AppNavigationManager(ui.getPage()), display);
	}

	@Override
	public void navigateTo(String stateWithHistory) {
		historyId = getHistoryId(stateWithHistory);

		LspsUI ui = (LspsUI) UI.getCurrent();
		if (ui.getPage().getBrowserWindowWidth() < 768) {
			ui.addStyleName("l-menu-collapsed");
			ui.getUser().setSetting("collapsedMenu", true);
		}

		String navigationState;
		AppView view;
		if (historyId == null) {
			historyId = Long.toHexString(System.currentTimeMillis() % ONE_YEAR); //now modulo one year
			navigationState = stateWithHistory;
			view = null;

		} else {
			navigationState = getNavigationState(stateWithHistory);
			view = views.get(historyId);
		}

		if (view != null) {
			int historyIndex = history.indexOf(historyId);
			if (historyIndex >= 0) {
				//move back in history
				List<String> entriesToMove = history.subList(historyIndex + 1, history.size());
				future.addAll(0, entriesToMove); //add to the beginning of the future
				entriesToMove.clear(); //remove from the history
			} else {
				//move forward in history
				int futureIndex = future.indexOf(historyId);
				List<String> entriesToMove = future.subList(0, futureIndex + 1);
				history.addAll(entriesToMove); //add to the beginning of the history
				entriesToMove.clear(); //remove from the future
			}

			//return to an already existing view
			navigateTo(view, navigationState, "");

		} else {
			//create new view and navigate to it
			try {
				super.navigateTo(navigationState);
			} catch (AccessControlException e) {
				Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
			}
		}
	}

	@Override
	protected void navigateTo(View view, String viewName, String parameters) {
		AppView currentView = (view instanceof AppView) ? (AppView) view : null;

		boolean screenNotYetInHistory = currentView != null && views.get(historyId) == null;
		if (screenNotYetInHistory) {
			//remember the view for the given historyId
			views.put(historyId, currentView);
			history.add(historyId);

			//if the history is too long, remove oldest entry
			if (history.size() > MAX_VIEW_COUNT) {
				String oldHistoryId = history.remove(0);
				AppView old = views.remove(oldHistoryId);
				old.cleanup();
			}

			//clear the future and delete all related views
			for (String oldHistoryId : future) {
				AppView old = views.remove(oldHistoryId);
				old.cleanup();
			}
			future.clear();
		}
		try {
			super.navigateTo(view, viewName, parameters);
		} catch (AccessControlException e) {
			Notification.show(e.getMessage(), Type.ERROR_MESSAGE);
		}
	}

	private static String getHistoryId(String stateWithHistory) {
		if (stateWithHistory.contains(HISTORY_ID_DELIMITER)) {
			return stateWithHistory.substring(stateWithHistory.lastIndexOf(HISTORY_ID_DELIMITER) + 2);
		} else {
			return null;
		}
	}

	private static String getNavigationState(String stateWithHistory) {
		if (stateWithHistory.contains(HISTORY_ID_DELIMITER)) {
			return stateWithHistory.substring(0, stateWithHistory.lastIndexOf(HISTORY_ID_DELIMITER));
		} else {
			return stateWithHistory;
		}
	}

	/**
	 * Returns the current UI history entries as needed for the {@code human:getUIHistory()} function.
	 * @param context not null
	 * @return UI history
	 */
	public MapHolder getHistory(ExecutionContext context) {
		Map<Decimal, RecordHolder> uiHistory = new LinkedHashMap<Decimal, RecordHolder>();

		long i = history.size() - 1;
		for (String historyId : history) {
			AppView view = views.get(historyId);
			uiHistory.put(Decimal.valueOf(i--), view.getHistoryEntry(context, historyId));
		}
		for (String historyId : future) {
			AppView view = views.get(historyId);
			uiHistory.put(Decimal.valueOf(i--), view.getHistoryEntry(context, historyId));
		}
		return context.getNamespace().createMap(uiHistory);
	}

	/**
	 * Cleans up all views. Called when the UI is detached.
	 */
	public void cleanup() {
		for (AppView view : views.values()) {
			view.cleanup();
		}
	}

	private static class AppNavigationManager extends UriFragmentManager {

		private static final long serialVersionUID = 1L;

		private AppNavigator navigator;

		public AppNavigationManager(Page page) {
			super(page);
		}

		@Override
		public void setNavigator(Navigator navigator) {
			super.setNavigator(navigator);
			this.navigator = (AppNavigator) navigator;
		}

		@Override
		public void uriFragmentChanged(UriFragmentChangedEvent event) {
			//reset historyId first, otherwise getState would return new view with old historyId
			navigator.historyId = null;

			super.uriFragmentChanged(event);
		}

		@Override
		public String getState() {
			String stateWithHistory = super.getState();
			String historyId = getHistoryId(stateWithHistory);
			if (historyId == null && navigator.historyId != null) {
				stateWithHistory += HISTORY_ID_DELIMITER + navigator.historyId;
			}
			return stateWithHistory;
		}

		@Override
		public void setState(String state) {
			String historyId = navigator.historyId;
			String stateWithHistory = (historyId == null) ? state : state + HISTORY_ID_DELIMITER + historyId;
			super.setState(stateWithHistory);
		}

	}

}
