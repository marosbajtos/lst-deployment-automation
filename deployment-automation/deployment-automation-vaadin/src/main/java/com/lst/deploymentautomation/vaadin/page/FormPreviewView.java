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
package com.lst.deploymentautomation.vaadin.page;

import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.lst.deploymentautomation.vaadin.core.AppFormConnector;
import com.lst.deploymentautomation.vaadin.core.AppView;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.util.Utils;
import com.whitestein.lsps.vaadin.FormPreviewContextHolder;
import com.whitestein.lsps.vaadin.LspsFormConnector;

/**
 * Application view for displaying form preview.
 * 
 * @author mhi
 */
public class FormPreviewView extends AppView {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(FormPreviewView.class.getName());

	private FormPreviewContextHolder previewContextHolder;
	
	
	/**
	 * View ID used for navigation.
	 */
	public static final String ID = "preview";
	
	/**
	 * Default constructor.
	 */
	public FormPreviewView() {
		setSizeFull();
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		//if reopening a view, there is nothing to do
		if (previewContextHolder != null) {
			previewContextHolder.reactivate();
			
		} else {
			super.enter(event);
			
			LspsUI ui = (LspsUI) event.getNavigator().getUI();
			String parameters = event.getParameters();
			
			try {
				//parse parameters
				int idx = parameters.indexOf('/');
				long modelId = Long.parseLong(parameters.substring(0, idx));
				String formExpression = URLDecoder.decode(parameters.substring(idx + 1), "UTF-8");
				
				String userId = ui.getUser().getPerson().getId();
				LspsFormConnector formConnector = new AppFormConnector(this);
				previewContextHolder = new FormPreviewContextHolder(modelId, formExpression, ui.getAppConnector(), formConnector, userId);
				previewContextHolder.reloadContent();
				
			} catch (Exception e) {
				ui.showErrorMessage("app.unknownErrorOccurred", e);
				Utils.log(e, "could not render form preview: "+parameters, log);
			}
		}
	}
	
	@Override
	public void cleanup() {
		previewContextHolder.invalidate();
	}
	
}
