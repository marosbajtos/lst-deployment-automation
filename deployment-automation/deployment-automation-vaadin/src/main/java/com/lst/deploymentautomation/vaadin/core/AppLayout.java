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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.lst.deploymentautomation.vaadin.core.NavigationMenu.NavigationCommand;
import com.lst.deploymentautomation.vaadin.page.SettingsView;
import com.lst.deploymentautomation.vaadin.page.TodoListView;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.whitestein.lsps.human.HumanRights;
import com.whitestein.lsps.vaadin.util.UserInfo;

/**
 * The main layout of the application. Uses {@link CustomLayout} so that the
 * layout is easily customizable and themeable. Implements {@link ViewDisplay}
 * so that it can be used directly with {@link Navigator}.
 * 
 * @author mhi
 */
public class AppLayout extends CustomComponent implements ViewDisplay {

	private static final long serialVersionUID = 1L;

	//main layout
	private CustomLayout layout;

	//components
	private NavigationMenu navigation;
	private NavigationMenu userMenu;

	//active menu item tracking
	private MenuItem activeMenuItem;
	private Map<String, MenuItem> viewItems = new HashMap<String, MenuItem>();

	@Override
	public void attach() {
		super.attach();

		initLayout();
	}

	private void initLayout() {
		addStyleName("app-layout");
		setSizeFull();

		//main page layout
		layout = new CustomLayout("page");
		layout.setSizeFull();
		setCompositionRoot(layout);

		//navigation menu
		VerticalLayout navLayout = new VerticalLayout();
		navLayout.setSizeFull();
		layout.addComponent(navLayout, "usermenu");
		navigation = new NavigationMenu();
		navigation.addStyleName("navigation-menu");
		layout.addComponent(navigation, "navigation");

		final UserInfo user = ((LspsUI) UI.getCurrent()).getUser();
		LspsUI ui = (LspsUI) UI.getCurrent();

		if (user.hasRight(HumanRights.READ_ALL_TODO) || user.hasRight(HumanRights.READ_OWN_TODO)) {
			addViewItem(navigation, TodoListView.TITLE, TodoListView.ID, FontAwesome.LIST);
		}
		/*if (user.hasRight(HumanRights.ACCESS_DOCUMENTS)) {
			addViewItem(navigation, DocumentsView.TITLE, DocumentsView.ID, FontAwesome.FILE_TEXT_O);
		}
		if (user.hasRight(EngineRights.READ_MODEL) && user.hasRight(EngineRights.CREATE_MODEL_INSTANCE)) {
			addViewItem(navigation, RunModelView.TITLE, RunModelView.ID, FontAwesome.CARET_SQUARE_O_RIGHT);
		}*/

		addNavigationCommandItem(navigation, "Initialize deployment", new NavigationMenu.OpenDocumentCommand(ui, "", "'deployment-automation-ui'::InitiateDeploymentDoc", null), FontAwesome.PLAY);
		addNavigationCommandItem(navigation, "Deployments list", new NavigationMenu.OpenDocumentCommand(ui, "", "'deployment-automation-ui'::DeploymentsList", null), FontAwesome.LIST_OL);

		//user menu
		userMenu = new NavigationMenu();
		userMenu.addStyleName("navigation-menu");
		navLayout.addComponent(userMenu);

		addViewItem(userMenu, SettingsView.TITLE, SettingsView.ID, FontAwesome.COG);

		NavigationCommand logoutCmd = new LogoutCommand((LspsUI) getUI());
		MenuItem logout = addNavigationCommandItem(userMenu, logoutCmd.getTitle(), logoutCmd, FontAwesome.POWER_OFF);
		String fullName = user.getPerson().getFullName();
		logout.setDescription(ui.getMessage("nav.logout", fullName));

		boolean collapsed = user.getSettingBoolean("collapsedMenu", false);

		if (collapsed == true) {
			UI.getCurrent().addStyleName("l-menu-collapsed");
		}

		Button button = new Button("");
		button.addStyleName("l-menu-expander");
		button.addStyleName("link");
		navLayout.addComponent(button);
		button.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				boolean collapsed = user.getSettingBoolean("collapsedMenu", false);
				if (collapsed) {
					UI.getCurrent().removeStyleName("l-menu-collapsed");
				}
				else {
					UI.getCurrent().addStyleName("l-menu-collapsed");
				}
				user.setSetting("collapsedMenu", !collapsed);
			}
		});
	}

	private void addViewItem(NavigationMenu menu, String title, String viewId, FontAwesome icon) {
		MenuItem item = menu.addViewItem(title, viewId);
		item.setIcon(icon);
		item.setStyleName("inactive");
		viewItems.put(viewId, item);
	}

	private MenuItem addNavigationCommandItem(NavigationMenu menu, String title, NavigationCommand command, FontAwesome icon) {
		MenuItem item = menu.addItem(title, command);
		item.setIcon(icon);
		item.setStyleName("inactive");
		return item;
	}

	/**
	 * Should be called when the active view has changed.
	 * 
	 * @param viewId
	 */
	public void viewChanged(String viewId) {
		//update the active menu item
		setActiveMenuItem(viewItems.get(viewId));
	}

	/**
	 * Should be called when the locale has changed so that any content that
	 * needs to be localized can be redrawn.
	 * 
	 * @param locale
	 */
	public void localeChanged(Locale locale) {
		//let the menu know that locale has changed
		navigation.onLocaleChange();
		userMenu.onLocaleChange();
	}

	@Override
	public void showView(View view) {
		//update the page content
		layout.addComponent((Component) view, "content");
	}

	private void setActiveMenuItem(MenuItem menuItem) {
		if (activeMenuItem != null) {
			activeMenuItem.setStyleName(activeMenuItem.getStyleName().replace("active", "inactive"));

		}
		activeMenuItem = menuItem;
		if (menuItem != null) {
			menuItem.setStyleName(menuItem.getStyleName().replace("inactive", "active"));
		}
	}

	private static final class LogoutCommand implements NavigationCommand {

		private static final long serialVersionUID = 1L;

		private final LspsUI ui;

		private LogoutCommand(LspsUI ui) {
			this.ui = ui;
		}

		@Override
		public void menuSelected(MenuBar.MenuItem selectedItem) {
			ui.logout();
		}

		@Override
		public String getTitle() {
			return ui.getMessage("nav.logout", "");
		}
	}

}
