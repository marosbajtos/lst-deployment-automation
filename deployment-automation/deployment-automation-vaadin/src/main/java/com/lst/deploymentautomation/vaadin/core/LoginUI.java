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

import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.lst.deploymentautomation.vaadin.util.Utils;

/**
 * Vaadin UI for the login page.
 * 
 * @author mhi
 */
public class LoginUI extends UI {

	private static final long serialVersionUID = 1L;

	@Override
	public void init(VaadinRequest request) {
		//The application itself is not covered by container security - that would break bookmarkability
		//(the view info is kept in the URL fragment, which is not sent to server, thus it's lost
		//when the container redirects the browser to the login page).

		//Therefore only one JSP page is currently covered by container security (ui.jsp). If no user
		//is logged in, we show ui.jsp in a full-sized iframe; since no user is logged in, login page
		//is shown instead - once user logs in, content of ui.jsp is returned, which refreshes the top
		//window - and thus the correct view is shown, since the URL fragment was kept.

		//using a label for this is not very nice, but it's simplest
		String src = request.getContextPath() + "/ui.jsp";
		Label iframe = new Label("<iframe src='" + src + "' style='width: 100%; height: 100%; border: 0' />", ContentMode.HTML);
		iframe.setSizeFull();
		setContent(iframe);

		//set page title
		getPage().setTitle(Utils.getLocalizedString(getLocale(), "application.title"));
	}

}
