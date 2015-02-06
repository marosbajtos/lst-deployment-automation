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

import javax.servlet.ServletException;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.UI;
import com.lst.deploymentautomation.vaadin.util.Constants;
import com.whitestein.lsps.vaadin.util.TransactionAccessorImpl;
import com.whitestein.lsps.vaadin.util.VaadinServletWithTransaction;

/**
 * Custom Vaadin servlet. An application that uses LSPS forms has either to use
 * {@link VaadinServletWithTransaction} or make sure that a DB transaction is
 * present in requests (using e.g. {@link TransactionAccessorImpl}).
 * 
 * @author mhi
 */
public class AppServlet extends VaadinServletWithTransaction {

	private static final long serialVersionUID = 1L;

	@Override
	protected void servletInitialized() throws ServletException {
		super.servletInitialized();

		//register a bootstrap listener that adds a meta header to the bootstrap page
		getService().addSessionInitListener(new SessionInitListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void sessionInit(SessionInitEvent event) {
				event.getSession().addBootstrapListener(new AppBootstrapListener());
				event.getSession().addUIProvider(new AppUIProvider());
			}
		});
	}

	/**
	 * Bootstrap listener that adds viewport meta header for better mobile support.
	 * 
	 * @author mhi
	 */
	public static class AppBootstrapListener implements BootstrapListener {

		private static final long serialVersionUID = 1L;

		@Override
		public void modifyBootstrapPage(BootstrapPageResponse response) {
			response.getDocument().head().append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1\" />");
		}

		@Override
		public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
			//nothing to do
		}
	}

	/**
	 * Custom UI provider.
	 * 
	 * @author mhi
	 */
	public static class AppUIProvider extends UIProvider {

		private static final long serialVersionUID = 1L;

		@Override
		public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
			boolean userLoggedIn = event.getRequest().isUserInRole("user");

			if (!userLoggedIn) {
				return LoginUI.class;
			} else {
				return LspsUI.class;
			}
		}

		@Override
		public String getTheme(UICreateEvent event) {
			Object theme = VaadinService.getCurrentRequest().getWrappedSession().getAttribute("theme");
			if (theme != null) {
				if (Constants.THEMES.contains(theme)) {
					return theme.toString();
				} else {
					return Constants.DEFAULT_THEME;
				}
			} else {
				return Constants.DEFAULT_THEME;
			}
		}
	}
}
