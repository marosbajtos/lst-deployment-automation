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

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.Navigator.EmptyView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.lst.deploymentautomation.vaadin.page.DocumentView;
import com.lst.deploymentautomation.vaadin.page.DocumentsView;
import com.lst.deploymentautomation.vaadin.page.FormPreviewView;
import com.lst.deploymentautomation.vaadin.page.RunModelView;
import com.lst.deploymentautomation.vaadin.page.SettingsView;
import com.lst.deploymentautomation.vaadin.page.TodoListView;
import com.lst.deploymentautomation.vaadin.page.TodoView;
import com.lst.deploymentautomation.vaadin.util.Constants;
import com.lst.deploymentautomation.vaadin.util.LspsAppConnectorImpl;
import com.lst.deploymentautomation.vaadin.util.Utils;
import com.whitestein.lsps.human.dto.TodoStatus;
import com.whitestein.lsps.human.exception.InvalidTodoStatusException;
import com.whitestein.lsps.human.exception.TodoAllocatedException;
import com.whitestein.lsps.human.exception.TodoNotAllocatedException;
import com.whitestein.lsps.vaadin.LspsAppConnector;
import com.whitestein.lsps.vaadin.util.UserInfo;

/**
 * Vaadin UI object for the LSPS Process Application.
 * 
 * @author mhi
 */

@Widgetset("com.whitestein.lsps.vaadin.widgets.WidgetSet")
public class LspsUI extends UI implements ErrorHandler {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(LspsUI.class.getName());

	private AppLayout appLayout;

	private UserInfo userInfo;

	private LspsAppConnector connector;

	@Override
	public void init(VaadinRequest request) {
		//register self for error handling
		setErrorHandler(this);

		connector = new LspsAppConnectorImpl(this);

		initLayout();
	}

	@Override
	public void detach() {
		super.detach();

		//cleanup any views in history
		((AppNavigator) getNavigator()).cleanup();
	}

	private void initLayout() {
		//load current user info

		boolean themeChange = false;

		userInfo = (UserInfo) getSession().getAttribute("user");
		if (userInfo == null) {
			userInfo = new UserInfo(LspsAppConnectorImpl.INJECTOR_INSTANCE);
			getSession().setAttribute("user", userInfo);

			String theme = userInfo.getSettingString("theme", Constants.DEFAULT_THEME);

			// if user have set other than default theme
			if (!theme.equals(Constants.DEFAULT_THEME)) {
				if (Constants.THEMES.contains(theme)) {
					this.setTheme(theme);
				} else {
					this.setTheme(Constants.DEFAULT_THEME);
				}
				themeChange = true;
			}
		}

		//get browser info
		WebBrowser browser = getPage().getWebBrowser();

		//set layout style
		String layoutSetting = "l-layout-" + userInfo.getSettingString("layoutSetting", "compact");
		JavaScript.getCurrent().execute(""
				+ "if (!Array.prototype.indexOf) {"
				+ "Array.prototype.indexOf = function(obj, start) {for (var i = (start || 0), j = this.length; i < j; i++) {if (this[i] === obj) { return i; }}return -1;}"
				+ "}"
				+ "var setting = '" + layoutSetting + "';"
				+ "var classes = document.body.className.split(/\\s+/);"
				+ "var index = classes.indexOf(setting);"
				+ "~index || classes.push(setting);"
				+ "document.body.className = classes.join(\" \");"
				+ "");

		String fontSetting = "l-font-" + userInfo.getSettingString("fontSetting", "standard");
		JavaScript.getCurrent().execute(""
				+ "document.documentElement.className='" + fontSetting + "';"
				+ "");

		//set locale to the one preferred by the user
		Locale userLocale = userInfo.getLocale();
		if (userLocale == null) {
			userLocale = Locale.US;
		}
		setLocale(userLocale);

		//initialize the layout
		appLayout = new AppLayout();
		if (themeChange) {
			appLayout.setVisible(false);
		} else {
			if (browser.isTouchDevice()) {
				getUI().addStyleName("v-touch");
			} else {
				//use fixed-width layout by default
				getUI().addStyleName("fixed");
			}
		}
		setContent(appLayout);

		//setup navigation, register available views
		initNavigation();

		if (getPage().getUriFragment() == null) {
			openHomePage();
		}

		//if substitution is active, show warning
		if (getUser().getPerson().isSubstitutionActive()) {
			showWarningMessage("settings.substitutionActiveMessage");
		}
	}

	private void initNavigation() {
		Navigator navigator = new AppNavigator(getUI(), appLayout);
		navigator.addView(TodoListView.ID, TodoListView.class);
		navigator.addView(DocumentsView.ID, DocumentsView.class);
		navigator.addView(RunModelView.ID, RunModelView.class);
		navigator.addView(SettingsView.ID, SettingsView.class);
		navigator.addView(TodoView.ID, TodoView.class); //expects to-do ID as parameter
		navigator.addView(DocumentView.ID, DocumentView.class); //expects document ID as parameter + optional document parameters aded as ?key=val&key=val
		navigator.addView(FormPreviewView.ID, FormPreviewView.class); //expects model instance ID + '/' + form expression as parameter

		navigator.setErrorProvider(new ViewProvider() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getViewName(String viewAndParameters) {
				return viewAndParameters;
			}

			@Override
			public View getView(String viewName) {
				return new EmptyView();
			}
		});

		navigator.addViewChangeListener(new ViewChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				//update navigation menu
				String parameters = event.getParameters();
				boolean hasParameters = parameters != null && !parameters.isEmpty();
				appLayout.viewChanged(event.getViewName() + (hasParameters ? "/" + parameters : ""));

				//update page title
				View view = event.getNewView();
				Page page = event.getNavigator().getUI().getPage();
				String title = (view instanceof AppView) ? ((AppView) view).getTitle() : null;
				page.setTitle(getMessage("application.title") + (title == null ? "" : ": " + title));
			}
		});
	}

	/**
	 * Returns the current user info.
	 * @return informations about the current user
	 */
	public UserInfo getUser() {
		return userInfo;
	}

	/**
	 * Returns the LSPS connector object.
	 * @return LSPS connector
	 */
	public LspsAppConnector getAppConnector() {
		return connector;
	}

	@Override
	public void setLocale(Locale locale) {
		super.setLocale(locale);
		Cookie localeCookie = new Cookie("l-locale-cookie", locale.toString());
		localeCookie.setMaxAge(60 * 60 * 24 * 365);
		VaadinService.getCurrentResponse().addCookie(localeCookie);

		//redraw the navigation and user menu 
		if (appLayout != null) {
			appLayout.localeChanged(locale);
		}
	}

	/**
	 * Opens a view for the given viewId. See {@link Navigator#navigateTo(String)}.
	 * @param viewId
	 */
	public void navigateTo(String viewId) {
		getNavigator().navigateTo(viewId);
	}

	/**
	 * Opens the home page. By default opens the to-do list view, override (or change)
	 * if you want to use another view as the home page.
	 */
	public void openHomePage() {
		navigateTo(TodoListView.ID);
	}

	/**
	 * Opens the to-do with the given ID.
	 * @param todoId
	 */
	public void openTodo(long todoId) {
		navigateTo(TodoView.ID + "/" + todoId);
	}

	/**
	 * Opens the document with the given ID and parameters.
	 * @param documentId
	 * @param parameters
	 */
	public void openDocument(String documentId, Map<String, String> parameters) {
		navigateTo(DocumentView.getViewId(documentId, parameters));
	}

	/**
	 * Logs out the current user.
	 */
	public void logout() {
		//close current view so that detach() on given component is called
		navigateTo("");

		//redirect to logout page
		getPage().setLocation(VaadinService.getCurrentRequest().getContextPath() + "/logout.jsp");
	}

	// Common functionality to be used in various views

	/**
	 * Returns localized string for given key, replacing numbered tokens ({0}, {1}...) with the arguments.
	 * @param key message key
	 * @param args arguments
	 * @return localized string
	 */
	public String getMessage(String key, Object... args) {
		return Utils.getLocalizedString(getLocale(), key, args);
	}

	/**
	 * Shows informational message to the user.
	 * @param key
	 * @param args
	 */
	public void showInfoMessage(String key, Object... args) {
		String msg = getMessage(key, args);
		Notification.show(msg);
	}

	/**
	 * Shows a warning message to the user.
	 * @param key
	 * @param args
	 */
	public void showWarningMessage(String key, Object... args) {
		String msg = getMessage(key, args);
		Notification.show(msg, Notification.Type.WARNING_MESSAGE);
	}

	/**
	 * Shows an error message to the user. If an exception is passed as an argument, it's message is used as the error message detail.
	 * @param key
	 * @param e
	 * @param args
	 */
	public void showErrorMessage(String key, Throwable e, Object... args) {
		String msg = getMessage(key, args);
		String detail = e == null ? null : e.getMessage() == null ? e.toString() : e.getMessage();
		Notification notification = new Notification(msg, detail, Notification.Type.ERROR_MESSAGE);
		notification.show(getPage());
	}

	/**
	 * Returns localized version of the given text. Should be called on all strings coming from
	 * the LSPS engine that will be shown in the UI.
	 * 
	 * @param text
	 * @return localized text
	 */
	public String localizeEngineText(String text) {
		return connector.getLocalizer().getLocalizedString(text, null);
	}

	/**
	 * Shows error message to the user. If the error is not a well-known error
	 * (e.g. invalid to-do status), the error details are also logged in the
	 * server log.
	 * 
	 * @param t
	 */
	public void handleError(Throwable t) {
		if (t instanceof TodoAllocatedException) {
			showErrorMessage("todo.allocatedError", null, ((TodoAllocatedException) t).getAllocatedTo());
		} else if (t instanceof TodoNotAllocatedException) {
			showErrorMessage("todo.notAllocatedError", null);
		} else if (t instanceof InvalidTodoStatusException) {
			InvalidTodoStatusException e = (InvalidTodoStatusException) t;
			if (e.getStatus() == TodoStatus.ACCOMPLISHED) {
				showErrorMessage("todo.accomplishedStatusError", null);
			} else if (e.getStatus() == TodoStatus.INTERRUPTED) {
				showErrorMessage("todo.interruptedStatusError", null);
			} else if (e.getStatus() == TodoStatus.SUSPENDED) {
				showErrorMessage("todo.suspendedStatusError", null);
			} else {
				showErrorMessage("app.unknownErrorOccurred", t);
				Utils.log(t, "unknown error occurred", log);
			}
		} else {
			showErrorMessage("app.unknownErrorOccurred", t);
			Utils.log(t, "unknown error occurred", log);
		}
	}

	@Override
	public void error(com.vaadin.server.ErrorEvent event) {
		handleError(event.getThrowable());
	}

	/**
	 * Sets theme for UI and then refreshes page to propagate the change.
	 * @param theme
	 */
	@Override
	public void setTheme(String theme) {
		this.getUser().setSetting("theme", theme);
		VaadinService.getCurrentRequest().getWrappedSession().setAttribute("theme", theme);
		// store theme in browser local storage, so if default theme is changed, next time
		// user will see his chosen theme even before login.. and refresh
		JavaScript.getCurrent().execute(""
				+ "var storage = window.localStorage;"
				+ "if(typeof(Storage)!=='undefined')"
				+ "storage.setItem('deployment-automationlication-theme-name', '" + theme + "');"
				+ "window.location.reload();"
				+ "");
	}

}
