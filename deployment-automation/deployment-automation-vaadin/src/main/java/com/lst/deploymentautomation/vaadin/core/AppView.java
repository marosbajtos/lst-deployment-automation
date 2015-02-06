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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.lst.deploymentautomation.vaadin.util.LspsAppConnectorImpl;
import com.whitestein.lsps.engine.lang.ExecutionContext;
import com.whitestein.lsps.lang.exec.Namespace;
import com.whitestein.lsps.lang.exec.RecordHolder;
import com.whitestein.lsps.vaadin.ui.components.UIFieldNames;
import com.whitestein.lsps.vaadin.ui.components.UINavigationNames;

/**
 * Base class for application views.
 * @author mhi
 */
public abstract class AppView extends CustomComponent implements View {

	private static final long serialVersionUID = 1L;
	private String viewCode;
	private Date created;
	private Label title;
	private VerticalLayout layout;

	/**
	 * Simple constructor.
	 */
	public AppView() {
		this(null);
	}

	/**
	 * Constructor with a fixed title.
	 * @param viewTitle
	 */
	public AppView(String viewTitle) {
		LspsAppConnectorImpl.INJECTOR_INSTANCE.inject(this);
		created = new Date();

		layout = new VerticalLayout();
		layout.setSizeFull();
		setCompositionRoot(layout);
		setSizeFull();

		//title = new Label("<h1>" + title + "</h1>", Label.CONTENT_XHTML); -- don't want to use here b/c of XSS
		title = new Label(viewTitle);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		String parameters = event.getParameters();
		boolean hasParameters = parameters != null && !parameters.isEmpty();
		this.viewCode = event.getViewName() + (hasParameters ? "/" + parameters : "");
	}

	@Override
	public void attach() {
		super.attach();

		if (title.getParent() == null) {
			try {
				CustomLayout titleWrapper = new CustomLayout(new ByteArrayInputStream("<h1 location='title'></h1>".getBytes("UTF-8")));
				titleWrapper.addComponent(title, "title");
				Component header = createHeader(titleWrapper);
				header.addStyleName("view-header");
				layout.addComponent(header);
			} catch (IOException e) {
				//should not happen; rethrow
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Creates the header component for the view. By default shows just the view title,
	 * override if you need to add some components in the header.
	 * 
	 * @param titleComponent
	 * @return header component
	 */
	protected Component createHeader(Component titleComponent) {
		return titleComponent;
	}

	/**
	 * Returns the view title.
	 * @return view title
	 */
	public String getTitle() {
		return title.getValue();
	}

	/**
	 * Sets the view title.
	 * @param title
	 */
	protected void setTitle(String title) {
		this.title.setValue(title);
	}

	/**
	 * Sets the view content.
	 * @param content
	 */
	protected void setContent(Component content) {
		while (layout.getComponentCount() > 1) {
			layout.removeComponent(layout.getComponent(1));
		}

		try {
			layout.addComponent(content);
			layout.setExpandRatio(content, 1);
			content.setSizeFull();

		} catch (RuntimeException e) {
			//remove the invalid content
			layout.removeComponent(content);

			//add a sad face to show how sorry we are
			Label fail = new Label(":-(");
			fail.addStyleName("screen-failed-to-render");
			layout.addComponent(fail);
			layout.setExpandRatio(fail, 1);
			layout.setComponentAlignment(fail, Alignment.MIDDLE_CENTER);

			throw e;
		}
	}

	/**
	 * Closes this view, opens the home page.
	 */
	protected void close() {
		((LspsUI) getUI()).openHomePage();
	}

	/**
	 * Called before the view is closed. By default does nothing, override if needed.
	 */
	public void cleanup() {
		//nothing to do
	}

	/**
	 * Returns the navigation history entry for this view.
	 * @param context not null
	 * @param historyId not null
	 * @return navigation history entry
	 */
	public RecordHolder getHistoryEntry(ExecutionContext context, String historyId) {

		Map<String, Object> defaultValues = new HashMap<String, Object>();
		defaultValues.put(UIFieldNames.ID, historyId);
		defaultValues.put(UIFieldNames.TITLE, title.getValue());
		defaultValues.put(UIFieldNames.FIRST_DISPLAY, created);

		RecordHolder entry = createHistoryEntry(context, defaultValues);

		return entry;
	}

	/**
	 * Returns a history entry (see {@code human:Navigation}) for the current view.
	 * 
	 * @param context not null
	 * @param defaultValues not null
	 * @return history entry
	 */
	protected RecordHolder createHistoryEntry(ExecutionContext context, Map<String, Object> defaultValues) {
		Namespace namespace = context.getNamespace();
		RecordHolder entry = namespace.createRecord(
				UINavigationNames.HISTORICAL_APP_NAVIGATION, Collections.singletonMap(UIFieldNames.CODE, viewCode));
		return entry;
	}

	/**
	 * Interface for serializable view actions.
	 * 
	 * @author mhi
	 */
	public interface ViewAction extends Serializable {

		/**
		 * Invokes the action.
		 */
		void invoke();
	}
}
