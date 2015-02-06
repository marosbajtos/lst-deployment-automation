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

import java.util.Map;

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.lst.deploymentautomation.vaadin.core.AppFormConnector;
import com.lst.deploymentautomation.vaadin.core.AppView;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.popup.TodoAnnotation;
import com.lst.deploymentautomation.vaadin.popup.TodoDelegation;
import com.lst.deploymentautomation.vaadin.popup.TodoDetails;
import com.lst.deploymentautomation.vaadin.popup.TodoEscalation;
import com.lst.deploymentautomation.vaadin.popup.TodoRejection;
import com.lst.deploymentautomation.vaadin.util.Utils;
import com.whitestein.lsps.engine.lang.ExecutionContext;
import com.whitestein.lsps.human.dto.Todo;
import com.whitestein.lsps.human.ejb.TodoServiceLocal;
import com.whitestein.lsps.lang.exec.RecordHolder;
import com.whitestein.lsps.vaadin.AbstractTodoHolder;
import com.whitestein.lsps.vaadin.LspsCustomUITodoHolder;
import com.whitestein.lsps.vaadin.LspsFormConnector;
import com.whitestein.lsps.vaadin.LspsTodoHolder;

/**
 * Application view for displaying a to-do.
 * 
 * @author mhi
 */
public class TodoView extends AppView {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(TodoView.class.getName());

	private AbstractTodoHolder todoHolder;

	//components
	private HorizontalLayout annotations;
	private Label priority;
	private Label notes;

	@EJB
	private TodoServiceLocal todoService;

	/**
	 * View ID used for navigation.
	 */
	public static final String ID = "todo";

	/**
	 * Suffix to be appended to the todo ID parameter if the screen should be opened in read-only mode.
	 */
	public static final String READONLY_SUFFIX = "ro";

	/**
	 * Default constructor.
	 */
	public TodoView() {
		setSizeFull();

		notes = new Label();
		notes.addStyleName("todo-notes");
		priority = new Label();
		priority.setSizeUndefined();
		priority.addStyleName("todo-priority");

		annotations = new HorizontalLayout();
		annotations.addStyleName("todo-annotations");
		annotations.setMargin(true);
		annotations.setWidth("100%");
		annotations.addComponent(notes);
		annotations.setExpandRatio(notes, 5);
		annotations.addComponent(priority);
		annotations.setExpandRatio(priority, 1);
		annotations.setComponentAlignment(priority, Alignment.TOP_RIGHT);
		annotations.setVisible(false);
	}

	@Override
	public void enter(ViewChangeEvent event) {
		if (todoHolder != null) {
			//if reopening a view, screen might want to reinitialize
			todoHolder.reactivate();

		} else {
			super.enter(event);

			LspsUI ui = (LspsUI) event.getNavigator().getUI();
			String todoIdString = event.getParameters();
			boolean readOnly = false;
			if (todoIdString.endsWith(READONLY_SUFFIX)) {
				readOnly = true;
				todoIdString = todoIdString.substring(0, todoIdString.length() - 2);
			}

			try {
				long todoId = Long.parseLong(todoIdString);

				String userId = ui.getUser().getPerson().getId();
				LspsFormConnector formConnector = new AppFormConnector(this);

				boolean isCustomGUITodo = todoService.isCustomGUITodo(todoId);

				if (isCustomGUITodo) {
					todoHolder = new LspsCustomUITodoHolder(todoId, ui.getAppConnector(), formConnector, userId, readOnly);
				} else {
					todoHolder = new LspsTodoHolder(todoId, ui.getAppConnector(), formConnector, userId, readOnly);
				}
				todoHolder.reloadContent();

				if (todoHolder.isValid()) {
					refreshAnnotations(todoHolder.getTodo());
				}

			} catch (Exception e) {
				ui.showErrorMessage("app.unknownErrorOccurred", e);
				Utils.log(e, "could not render todo '" + todoIdString + "'", log);
			}
		}
	}

	@Override
	public void detach() {
		super.detach();

		if (todoHolder != null) {
			todoHolder.deactivate();
		}
	}

	@Override
	protected void setContent(Component content) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.addComponent(annotations);
		layout.addComponent(content);
		layout.setExpandRatio(content, 1);
		super.setContent(layout);
	}

	@SuppressWarnings("serial")
	@Override
	protected Component createHeader(Component titleComponent) {
		LspsUI ui = (LspsUI) getUI();

		MenuBar menu = new MenuBar();
		menu.addStyleName("menu-button-action");
		MenuItem actions = menu.addItem("", new ThemeResource("../icons/popup-menu.png"), null);

		final ViewAction closeView = new ViewAction() {

			@Override
			public void invoke() {
				close(); //maybe close only if the to-do is not allocated to the user anymore
			}
		};
		final ViewAction refreshAnnotations = new ViewAction() {

			@Override
			public void invoke() {
				Todo todo = todoService.getTodo(todoHolder.getTodo().getId());
				refreshAnnotations(todo);
			}
		};

		actions.addItem(ui.getMessage("action.info"), new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				//get fresh info
				Todo todo = todoService.getTodo(todoHolder.getTodo().getId());
				getUI().addWindow(new TodoDetails(todo));
			}
		});
		actions.addSeparator();
		actions.addItem(ui.getMessage("action.annotate") + "...", new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				//get fresh info
				Todo todo = todoService.getTodo(todoHolder.getTodo().getId());
				getUI().addWindow(new TodoAnnotation(todo, refreshAnnotations));
			}
		});
		actions.addSeparator();
		actions.addItem(ui.getMessage("action.unlock"), new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				unlock();
			}
		});
		actions.addItem(ui.getMessage("action.reject") + "...", new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				getUI().addWindow(new TodoRejection(todoHolder.getTodo().getId(), closeView));
			}
		});
		actions.addItem(ui.getMessage("action.delegate") + "...", new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				getUI().addWindow(new TodoDelegation(todoHolder.getTodo().getId(), closeView));
			}
		});
		actions.addItem(ui.getMessage("action.escalate") + "...", new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				getUI().addWindow(new TodoEscalation(todoHolder.getTodo().getId(), closeView));
			}
		});

		HorizontalLayout layout = new HorizontalLayout();
		layout.setWidth("100%");
		layout.addComponent(titleComponent);
		layout.setExpandRatio(titleComponent, 1);
		layout.addComponent(menu);
		layout.setComponentAlignment(menu, Alignment.MIDDLE_RIGHT);
		return layout;
	}

	private void refreshAnnotations(Todo todo) {
		boolean visible = todo.getPriority() != null || todo.getNotes() != null && todo.getNotes().length() > 0;

		priority.setValue(todo.getPriority() == null ? null : todo.getPriority().toString());
		notes.setValue(todo.getNotes());
		annotations.setVisible(visible);
	}

	/**
	 * Unlocks and closes the to-do.
	 * If unlock failed, an error message is displayed to the user.
	 */
	public void unlock() {
		LspsUI ui = (LspsUI) getUI();

		long todoId = todoHolder.getTodo().getId();
		try {
			//invalidate context
			todoHolder.invalidate();

			//unlock to-do
			todoService.unlockTodo(todoId);

			ui.showInfoMessage("todo.unlockSuccessful", 1);

			close();

			//		} catch (TodoAllocatedException e) {
			//			WebUtils.addErrorMessage(null, "todoAllocatedError", e.getAllocatedTo());
			//		} catch (InvalidTodoStatusException e) {
			//			if (e.getStatus() == TodoStatus.ACCOMPLISHED) {
			//				WebUtils.addErrorMessage(null, "todoAccomplishedStatusError");
			//			} else if (e.getStatus() == TodoStatus.INTERRUPTED) {
			//				WebUtils.addErrorMessage(null, "todoInterruptedStatusError");
			//			} else if (e.getStatus() == TodoStatus.SUSPENDED) {
			//				WebUtils.addErrorMessage(null, "todoSuspendedStatusError");
			//			} else {
			//				WebUtils.addExceptionMessage(e, "unlockTodoFailed");
			//			}
		} catch (Exception e) {
			Utils.log(e, "could not unlock to-do " + todoId, log);

			ui.showErrorMessage("todo.unlockFailed", e);
		}
	}

	@Override
	public void cleanup() {
		if (todoHolder != null) {
			todoHolder.invalidate();
		}
	}

	@Override
	protected RecordHolder createHistoryEntry(ExecutionContext context, Map<String, Object> defaultValues) {
		if (todoHolder == null) {
			return super.createHistoryEntry(context, defaultValues);
		} else {
			return todoHolder.getHistoryEntry(context, defaultValues);
		}
	}

}
