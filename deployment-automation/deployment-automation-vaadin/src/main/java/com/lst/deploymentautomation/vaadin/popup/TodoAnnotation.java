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

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.lst.deploymentautomation.vaadin.core.AppView.ViewAction;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.util.LspsAppConnectorImpl;
import com.lst.deploymentautomation.vaadin.util.Utils;
import com.whitestein.lsps.human.dto.Todo;
import com.whitestein.lsps.human.ejb.TodoServiceLocal;

/**
 * Popup window for to-do annotation.
 * 
 * @author mhi
 */
public class TodoAnnotation extends Window {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(TodoAnnotation.class.getName());

	private Set<Long> todoIds;
	private ViewAction closeAction;

	private ObjectProperty<Integer> priority = new ObjectProperty<Integer>(null, Integer.class);
	private ObjectProperty<String> notes = new ObjectProperty<String>(null, String.class);

	@EJB
	private TodoServiceLocal todoService;
	private TextField priorityField;

	/**
	 * Constructor for the annotation of a single to-do.
	 * @param todo
	 * @param closeAction 
	 */
	public TodoAnnotation(Todo todo, ViewAction closeAction) {
		this(Collections.singleton(todo.getId()), closeAction);

		priority.setValue(todo.getPriority());
		notes.setValue(todo.getNotes());
	}

	/**
	 * Full constructor.
	 * @param todoIds
	 * @param closeAction 
	 */
	public TodoAnnotation(Set<Long> todoIds, ViewAction closeAction) {
		LspsAppConnectorImpl.INJECTOR_INSTANCE.inject(this);

		this.todoIds = todoIds;
		this.closeAction = closeAction;

		setWidth("400px");
		setHeight("400px");
		setModal(true);
		setClosable(true);
	}

	@Override
	public void attach() {
		super.attach();

		LspsUI ui = (LspsUI) getUI();
		setCaption(ui.getMessage("todo.annotationTitle"));

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		setContent(layout);

		Label help = new Label(ui.getMessage("todo.annotationHelp"));
		help.setStyleName("form-help");
		layout.addComponent(help);

		priorityField = new TextField(ui.getMessage("todo.priority"), priority);
		//TODO forbid negative priorities
		priorityField.setConverter(Integer.class);
		priorityField.setConversionError(ui.getMessage("app.invalidValueMessage"));
		priorityField.setNullRepresentation("");
		priorityField.setNullSettingAllowed(true);
		layout.addComponent(priorityField);

		TextArea notesField = new TextArea(ui.getMessage("todo.notes"), notes);
		notesField.setMaxLength(1024);
		notesField.setNullRepresentation("");
		priorityField.setNullSettingAllowed(true);
		notesField.setSizeFull();
		layout.addComponent(notesField);
		layout.setExpandRatio(notesField, 1);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		layout.addComponent(buttons);

		@SuppressWarnings("serial")
		Button submitButton = new Button(ui.getMessage("action.annotate"), new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				annotate();
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

	private void annotate() {
		try {
			priorityField.validate();
		} catch (InvalidValueException e) {
			return;
		}

		int failed = 0;
		for (Long todoId : todoIds) {
			if (!annotateTodo(todoId)) {
				failed++;
			}
		}

		//show success message if no error message was shown
		if (failed == 0) {
			LspsUI ui = (LspsUI) getUI();
			ui.showInfoMessage("todo.annotateSuccessful", todoIds.size() - failed);
		}

		close();
		if (closeAction != null) {
			closeAction.invoke();
		}
	}

	private boolean annotateTodo(Long todoId) {
		try {
			todoService.setNotes(todoId, notes.getValue());
			todoService.setPriority(todoId, priority.getValue());
			log.trace("annotated to-do " + todoId);
			return true;

		} catch (Exception e) {
			Utils.log(e, "could not annotate to-do " + todoId, log);

			LspsUI ui = (LspsUI) getUI();
			ui.showErrorMessage("todo.annotateFailed", e);

			return false;
		}
	}

}
