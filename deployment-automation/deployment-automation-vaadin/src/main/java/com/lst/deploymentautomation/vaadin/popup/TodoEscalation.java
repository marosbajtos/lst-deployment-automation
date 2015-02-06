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
package com.lst.deploymentautomation.vaadin.popup;

import java.util.Collections;
import java.util.Set;

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.lst.deploymentautomation.vaadin.core.AppView.ViewAction;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.util.LspsAppConnectorImpl;
import com.lst.deploymentautomation.vaadin.util.Utils;
import com.whitestein.lsps.human.ejb.TodoServiceLocal;

/**
 * Popup window for to-do escalation.
 * 
 * @author mhi
 */
public class TodoEscalation extends Window {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(TodoEscalation.class.getName());

	private Set<Long> todoIds;
	private ViewAction closeAction;
	private TextArea reason;

	@EJB
	private TodoServiceLocal todoService;

	/**
	 * Constructor for the escalation of a single to-do.
	 * @param todoId
	 * @param closeAction 
	 */
	public TodoEscalation(Long todoId, ViewAction closeAction) {
		this(Collections.singleton(todoId), closeAction);
	}

	/**
	 * Full constructor.
	 * @param todoIds
	 * @param closeAction 
	 */
	public TodoEscalation(Set<Long> todoIds, ViewAction closeAction) {
		LspsAppConnectorImpl.INJECTOR_INSTANCE.inject(this);

		this.todoIds = todoIds;
		this.closeAction = closeAction;
		setWidth("400px");
		setHeight("300px");
		setModal(true);
		setClosable(true);
	}

	@Override
	public void attach() {
		super.attach();

		LspsUI ui = (LspsUI) getUI();
		setCaption(ui.getMessage("todo.escalationTitle"));

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		setContent(layout);

		Label help = new Label(ui.getMessage("todo.escalationHelp"));
		help.setStyleName("form-help");
		layout.addComponent(help);

		reason = new TextArea(ui.getMessage("todo.escalationReason"));
		reason.setMaxLength(1024);
		reason.setRequired(true);
		reason.setSizeFull();
		layout.addComponent(reason);
		layout.setExpandRatio(reason, 1);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		layout.addComponent(buttons);

		@SuppressWarnings("serial")
		Button submitButton = new Button(ui.getMessage("action.escalate"), new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				escalate();
			}
		});
		buttons.addComponent(submitButton);

		@SuppressWarnings("serial")
		Button cancelButton = new Button(ui.getMessage("action.cancel"), new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				close();
			}
		});
		buttons.addComponent(cancelButton);
	}

	private void escalate() {
		String reasonText = reason.getValue();
		if (reasonText == null || reasonText.trim().length() == 0) {
			Utils.setComponentError(reason, "app.requiredMessage");
			return;
		} else {
			Utils.setComponentError(reason, null);
		}

		int failed = 0;
		for (Long todoId : todoIds) {
			if (!escalateTodo(todoId, reasonText)) {
				failed++;
			}
		}

		//show success message if no error message was shown
		if (failed == 0) {
			LspsUI ui = (LspsUI) getUI();
			ui.showInfoMessage("todo.escalateSuccessful", todoIds.size() - failed);
		}

		close();
		if (closeAction != null) {
			closeAction.invoke();
		}
	}

	private boolean escalateTodo(Long todoId, String reasonText) {
		try {
			todoService.escalateTodo(todoId, reasonText);
			log.trace("escalated to-do " + todoId);
			return true;

		} catch (Exception e) {
			Utils.log(e, "could not escalate to-do " + todoId, log);

			LspsUI ui = (LspsUI) getUI();
			ui.showErrorMessage("todo.escalateFailed", e);

			return false;
		}
	}

}
