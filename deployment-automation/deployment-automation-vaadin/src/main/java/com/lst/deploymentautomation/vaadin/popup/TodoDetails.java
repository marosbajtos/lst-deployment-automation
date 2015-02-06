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

import java.text.SimpleDateFormat;

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.util.LspsAppConnectorImpl;
import com.lst.deploymentautomation.vaadin.util.Utils;
import com.whitestein.lsps.engine.dto.ModelInstanceInfo;
import com.whitestein.lsps.engine.ejb.ProcessServiceLocal;
import com.whitestein.lsps.human.dto.Todo;
import com.whitestein.lsps.human.dto.TodoAuthorization;
import com.whitestein.lsps.human.ejb.TodoServiceLocal;

/**
 * Popup window showing to-do details.
 * 
 * @author mhi
 */
public class TodoDetails extends Window {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(TodoDetails.class.getName());

	private Todo todo;

	@EJB
	private TodoServiceLocal todoService;

	@EJB
	private ProcessServiceLocal processService;

	/**
	 * Full constructor.
	 * @param todo 
	 */
	public TodoDetails(Todo todo) {
		LspsAppConnectorImpl.INJECTOR_INSTANCE.inject(this);

		this.todo = todo;
		setWidth("400px");
		setHeight("400px");
		setModal(true);
		setClosable(true);
	}

	@Override
	public void attach() {
		super.attach();

		LspsUI ui = (LspsUI) getUI();
		setCaption(ui.getMessage("todo.detailsTitle"));

		TabSheet tabs = new TabSheet();
		tabs.setSizeFull();
		tabs.addTab(createTodoInfo(ui), ui.getMessage("todo.header"));
		tabs.addTab(createProcessInfo(ui), ui.getMessage("process.header"));
		setContent(tabs);

		//		Button close = new Button("Close", new Button.ClickListener() {
		//            // inline click-listener
		//            public void buttonClick(ClickEvent event) {
		//                // close the window by removing it from the parent window
		//                (subwindow.getParent()).removeWindow(subwindow);
		//            }
		//        });
		//        // The components added to the window are actually added to the window's
		//        // layout; you can use either. Alignments are set using the layout
		//        layout.addComponent(close);
		//        layout.setComponentAlignment(close, Alignment.TOP_RIGHT);

	}

	private Component createProcessInfo(LspsUI ui) {
		ModelInstanceInfo instance = processService.getModelInstanceInfo(todo.getModelInstanceId());

		GridLayout grid = new GridLayout(2, 10);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setWidth("100%");
		grid.setColumnExpandRatio(0, 1);
		grid.setColumnExpandRatio(1, 2);

		grid.addComponent(new Label(ui.getMessage("process.id")));
		grid.addComponent(new Label(String.valueOf(instance.getId())));

		grid.addComponent(new Label(ui.getMessage("process.model")));
		grid.addComponent(new Label(instance.getModelName() + " - " + instance.getModelVersion()));

		grid.addComponent(new Label(ui.getMessage("process.started")));
		grid.addComponent(new Label(new SimpleDateFormat(ui.getMessage("app.dateTimeFormat")).format(instance.getStartedDate())));

		grid.addComponent(new Label(ui.getMessage("process.finished")));
		grid.addComponent(new Label(instance.getFinishedDate() == null ? "" : new SimpleDateFormat(ui.getMessage("app.dateTimeFormat")).format(instance.getFinishedDate())));

		String status = "???";
		switch (instance.getState()) {
		case CREATED:
			status = ui.getMessage("process.statusCreated");
			break;
		case FINISHED:
			status = ui.getMessage("process.statusFinished");
			break;
		case RUNNING:
			status = ui.getMessage("process.statusRunning");
			break;
		case SUSPENDED:
			status = ui.getMessage("process.statusSuspended");
			break;
		case MODEL_UPDATE_PREPROCESSING:
			status = ui.getMessage("process.statusPreprocessing");
			break;
		case MODEL_UPDATE_PREPROCESSED:
			status = ui.getMessage("process.statusPreprocessed");
			break;
		case MODEL_UPDATE_TRANSFORMED:
			status = ui.getMessage("process.statusTransformed");
			break;
		case MODEL_UPDATE_POSTPROCESSING:
			status = ui.getMessage("process.statusPostprocessing");
			break;
		case MODEL_UPDATE_UPDATED:
			status = ui.getMessage("process.statusUpdated");
			break;
		case MODEL_UPDATE_ABORTED:
			status = ui.getMessage("process.statusUpdateAborted");
			break;
		}
		grid.addComponent(new Label(ui.getMessage("process.status")));
		grid.addComponent(new Label(status));

		return grid;
	}

	private Component createTodoInfo(LspsUI ui) {
		GridLayout grid = new GridLayout(2, 10);
		grid.setMargin(true);
		grid.setSpacing(true);
		grid.setWidth("100%");
		grid.setColumnExpandRatio(0, 1);
		grid.setColumnExpandRatio(1, 2);

		grid.addComponent(new Label(ui.getMessage("todo.id")));
		grid.addComponent(new Label(String.valueOf(todo.getId())));

		//		grid.addComponent(new Label(ui.getMessage("todo.process")));
		//		grid.addComponent(new Label(String.valueOf(todo.getModelInstanceId())));

		//		grid.addComponent(new Label(ui.getMessage("todo.title")));
		//		grid.addComponent(new Label(ui.localizeEngineText(todo.getTitle())));

		grid.addComponent(new Label(ui.getMessage("todo.task")));
		grid.addComponent(new Label(todo.getTaskNamespace()));

		grid.addComponent(new Label(ui.getMessage("todo.issued")));
		grid.addComponent(new Label(new SimpleDateFormat(ui.getMessage("app.dateTimeFormat")).format(todo.getIssuedDate())));

		String status = "???";
		switch (todo.getStatus()) {
		case ALIVE:
			status = ui.getMessage("todo.statusAlive");
			break;
		case ACCOMPLISHED:
			status = ui.getMessage("todo.statusAccomplished", todo.getSubmittedDate());
			break;
		case INTERRUPTED:
			status = ui.getMessage("todo.statusInterrupted", todo.getSubmittedDate(), todo.getInterruptionReason());
			break;
		case SUSPENDED:
			status = ui.getMessage("todo.statusSuspended");
			break;
		}
		grid.addComponent(new Label(ui.getMessage("todo.status")));
		grid.addComponent(new Label(status));

		try {
			TodoAuthorization authorization = todoService.getAuthorization(todo.getId(), ui.getUser().getPerson().getId());
			String authMsg;
			switch (authorization) {
			case INITIAL_PERFORMER:
				authMsg = ui.getMessage("todo.authorizationPerformer");
				break;
			case DELEGATE:
				authMsg = ui.getMessage("todo.authorizationDelegate");
				break;
			case SUBSTITUTE:
				authMsg = ui.getMessage("todo.authorizationSubstitute");
				break;
			case NOT_PERMITTED:
			default:
				authMsg = ui.getMessage("todo.authorizationUnknown");
				break;
			}
			grid.addComponent(new Label(ui.getMessage("todo.authorization")));
			grid.addComponent(new Label(authMsg));
		} catch (Exception e) {
			Utils.rethrow(e, "could not get authorization for " + todo.getId(), log);
		}

		//currently it's always the current user; no need to show him
		//		grid.addComponent(new Label(ui.getMessage("todo.responsible")));
		//		grid.addComponent(new Label(todo.getAllocatedToFullName()));

		return grid;
	}

}
