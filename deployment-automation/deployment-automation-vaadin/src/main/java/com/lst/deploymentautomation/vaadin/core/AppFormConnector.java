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

import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.whitestein.lsps.lang.exec.RecordHolder;
import com.whitestein.lsps.vaadin.LspsFormConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple form connector that delegates most of the logic to the application UI
 * or to the view itself.
 * 
 * @author mhi
 */
public class AppFormConnector implements LspsFormConnector {
	private static final long serialVersionUID = 1L;
	
	private AppView view;
	
	/**
	 * Full constructor.
	 * @param view
	 */
	public AppFormConnector(AppView view) {
		this.view = view;
	}

	@Override
	public void setContent(Component content) {
		if (content == null) {
			//rendering failed; show a sad face to show how sorry we are
			VerticalLayout layout = new VerticalLayout();
 			Label fail = new Label(":-(");
 			fail.addStyleName("screen-failed-to-render");
			layout.addComponent(fail);
			layout.setExpandRatio(fail, 1);
			layout.setComponentAlignment(fail, Alignment.MIDDLE_LEFT);
			layout.setSizeFull();
			
			view.setContent(layout);
			
		} else {
			//if the component has either natural or absolute size, wrap it
			//in a scrollable panel with full size and nice margin
			if (content.getHeightUnits() != Unit.PERCENTAGE) {
				final Panel panel = new Panel();
				panel.addStyleName("l-border-none");
				panel.setContent(content);
				panel.setSizeFull();
				content = panel;
			}
			
			view.setContent(content);
		}
	}

	@Override
	public void setTitle(String title) {
		view.setTitle(title);
	}

	@Override
	public void showNotification(Notification notif) {
		notif.show(Page.getCurrent());
	}
	
	@Override
	public void handleError(Throwable t) {
		LspsUI ui = (LspsUI) view.getUI();
		ui.handleError(t);
	}
	
	@Override
	public void onProcessingEnd(ProcessingResult status) {
        if (!status.validationErrors.errors.isEmpty()) {
            log.debug("Event processing produced " + status.validationErrors.errors.size() + " validation error(s)");
        }
	}

    private static final Logger log = LoggerFactory.getLogger(AppFormConnector.class);

	@Override
	public void navigate(RecordHolder navigation, boolean closeForm) {
		LspsUI ui = (LspsUI) view.getUI();
		if (navigation == null) {
			ui.openHomePage();
		} else {
			String url = ui.getAppConnector().getUrlFactory().getNavigationUrl(navigation);
			if (url.startsWith("#!")) {
				//this works also if the location is the same
				ui.navigateTo(url.substring(2));
			} else {
				ui.getPage().setLocation(url);
			}
		}
	}

}
