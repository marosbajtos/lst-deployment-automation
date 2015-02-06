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

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Set;

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.MultiSelectMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.VerticalLayout;
import com.lst.deploymentautomation.vaadin.core.AppView;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.popup.TodoAnnotation;
import com.lst.deploymentautomation.vaadin.popup.TodoDelegation;
import com.lst.deploymentautomation.vaadin.popup.TodoEscalation;
import com.lst.deploymentautomation.vaadin.popup.TodoRejection;
import com.lst.deploymentautomation.vaadin.util.Utils;
import com.whitestein.lsps.common.query.DtoList;
import com.whitestein.lsps.human.dto.Todo;
import com.whitestein.lsps.human.dto.TodoListCriteria;
import com.whitestein.lsps.human.ejb.TodoServiceLocal;
import com.whitestein.lsps.vaadin.util.LazyDtoContainer;
import com.whitestein.lsps.vaadin.util.LazyDtoContainer.DtoLoader;

/**
 * To-do list page.
 * 
 * @author mhi
 */
public class TodoListView extends AppView {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(TodoListView.class.getName());

	/**
	 * View title localization key.
	 */
	public static final String TITLE = "nav.todoList";

	/**
	 * View ID used for navigation.
	 */
	public static final String ID = "todos";

	private static final String SETTINGS_KEY = "table.todoList";
	private static final int PAGE_SIZE = 50;

	private Table table;
	private Button selectBtn;
	private Button cancelBtn;
	private MenuBar actionBtn;

	private LazyDtoContainer<Todo> container;
	private Set<Long> selection;
	private String originalSettings;

	@EJB
	private TodoServiceLocal todoService;

	public TodoListView() {
		container = new LazyDtoContainer<Todo>(new TodoLoader(), PAGE_SIZE, Todo.class, "id");
	}

	@Override
	public void attach() {
		super.attach();

		if (table == null) {
			//create view content
			createView();
		} else {
			//if reopening the view, refresh content
			container.refresh();
		}
	}

	@SuppressWarnings("serial")
	private void createView() {
		final LspsUI ui = (LspsUI) getUI();

		setTitle(ui.getMessage(TITLE));

		VerticalLayout layout = new VerticalLayout();
		setContent(layout);

		table = new Table();
		table.setSizeFull();
		table.setSelectable(true);
		table.setMultiSelectMode(MultiSelectMode.SIMPLE);
		table.setSortEnabled(false);
		table.setColumnReorderingAllowed(true);
		table.setColumnCollapsingAllowed(true);

		table.addValueChangeListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				final Object sel = event.getProperty().getValue();
				if (sel instanceof Set) {
					selection = (Set<Long>) sel;
				} else if (sel instanceof Long) {
					selection = Collections.singleton((Long) sel);
				} else {
					selection = Collections.emptySet();
				}

				//enable todo actions only if the sel is non-empty
				actionBtn.setEnabled(selection.size() > 0);
			}
		});
		table.addItemClickListener(new ItemClickListener() {

			@Override
			public void itemClick(ItemClickEvent event) {
				if (table.isMultiSelect())
				{
					return; //don't do anything if in selection mode
				}
				if (event.getButton() != MouseButton.LEFT || event.isDoubleClick())
				{
					return; //ignore right-clicks
				}

				final Item item = event.getItem();
				final Long todoId = (Long) item.getItemProperty("id").getValue();
				try {
					((LspsUI) getUI()).openTodo(todoId);
				} catch (Exception e) {
					Utils.log(e, "could not open to-do " + todoId, log);
					final LspsUI ui = (LspsUI) getUI();
					ui.showErrorMessage("app.unknownErrorOccurred", e); //todo.openFailed?
				}
			}
		});

		table.setContainerDataSource(container);

		Object[] defaultColumns = new Object[] {
				"title",
				"notes",
				"priority",
				"authorization",
				"modelInstanceId",
				"issuedDate"
		};
		//load table settings
		String settings = ui.getUser().getSettingString(SETTINGS_KEY, null);
		if (settings == null) {
			table.setVisibleColumns(defaultColumns);
			originalSettings = getColumnSettings();
		} else {
			originalSettings = settings;
			try {
				applyTableSettings(settings);
			} catch (Exception e) {
				table.setVisibleColumns(defaultColumns);
				Utils.log(e, "could not load todo list settings", log);
			}
		}

		table.setColumnHeader("title", ui.getMessage("todo.title"));
		table.setColumnHeader("notes", ui.getMessage("todo.notes"));
		table.setColumnHeader("priority", ui.getMessage("todo.priority"));
		table.setColumnHeader("authorization", ui.getMessage("todo.authorizationShort"));
		table.setColumnHeader("modelInstanceId", ui.getMessage("todo.process"));
		table.setColumnHeader("issuedDate", ui.getMessage("todo.issued"));

		table.setColumnAlignment("modelInstanceId", Table.Align.CENTER);
		table.setColumnExpandRatio("title", 2);
		table.setColumnExpandRatio("notes", 1);

		//localize todo titles
		table.addGeneratedColumn("title", new ColumnGenerator() {

			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				@SuppressWarnings("unchecked")
				BeanItem<Todo> item = (BeanItem<Todo>) source.getItem(itemId);
				return ui.localizeEngineText(item.getBean().getTitle());
			}
		});

		//show icons for authorization
		table.addGeneratedColumn("authorization", new ColumnGenerator() {

			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				@SuppressWarnings("unchecked")
				BeanItem<Todo> item = (BeanItem<Todo>) source.getItem(itemId);
				String icon;
				String text;
				switch (item.getBean().getAuthorization()) {
				case INITIAL_PERFORMER:
					icon = "auth_performer.gif";
					text = ui.getMessage("todo.authorizationPerformer");
					break;
				case DELEGATE:
					icon = "auth_delegate.gif";
					text = ui.getMessage("todo.authorizationDelegate");
					break;
				case SUBSTITUTE:
					icon = "auth_substitute.gif";
					text = ui.getMessage("todo.authorizationSubstitute");
					break;
				case NOT_PERMITTED:
				default:
					icon = "auth_unknown.gif";
					text = ui.getMessage("todo.authorizationUnknown");
					break;
				}
				Embedded authIcon = new Embedded(null, new ThemeResource("../icons/" + icon));
				authIcon.setDescription(text);

				if (item.getBean().getAllocatedTo() != null) {
					HorizontalLayout layout = new HorizontalLayout();
					layout.setSpacing(true);

					layout.addComponent(authIcon);

					Embedded lockedIcon = new Embedded(null, new ThemeResource("../icons/lock.gif"));
					lockedIcon.setDescription(ui.getMessage("todo.lockedBy", item.getBean().getAllocatedToFullName()));
					layout.addComponent(lockedIcon);
					return layout;
				} else {
					return authIcon;
				}
			}
		});

		//format date
		final SimpleDateFormat df = new SimpleDateFormat(ui.getMessage("app.dateTimeFormat"));
		table.addGeneratedColumn("issuedDate", new ColumnGenerator() {

			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				@SuppressWarnings("unchecked")
				BeanItem<Todo> item = (BeanItem<Todo>) source.getItem(itemId);
				return df.format(item.getBean().getIssuedDate());
			}
		});

		layout.addComponent(table);
		layout.setExpandRatio(table, 1);
	}

	@Override
	public void detach() {
		//there is currently no listener for column collapsing, therefore we save table settings on detach
		String columnSettings = getColumnSettings();
		if (!columnSettings.equals(originalSettings)) {
			LspsUI ui = (LspsUI) getUI();
			ui.getUser().setSetting(SETTINGS_KEY, columnSettings);
		}

		super.detach();
	}

	private void applyTableSettings(String settings) {
		String[] columns = settings.split("\n");
		String[] columnIds = new String[columns.length];
		boolean[] collapsed = new boolean[columns.length];

		int i = 0;
		for (String column : columns) {
			String[] parts = column.split("\t");
			columnIds[i] = parts[0];
			collapsed[i] = parts[1].equals("1");
			i++;
		}
		table.setVisibleColumns(columnIds);
		for (int j = 0; j < columnIds.length; j++) {
			table.setColumnCollapsed(columnIds[j], collapsed[j]);
		}
	}

	private String getColumnSettings() {
		StringBuilder settings = new StringBuilder();
		Object[] columns = table.getVisibleColumns();
		for (Object columnId : columns) {
			//int width = table.getColumnWidth(columnId);
			boolean collapsed = table.isColumnCollapsed(columnId);
			settings.append(columnId.toString()).append('\t').append(collapsed ? '1' : '0').append('\n');
		}
		return settings.toString();
	}

	private Set<Long> getSelectedTodoIds() {
		return selection;
	}

	@SuppressWarnings("serial")
	@Override
	protected Component createHeader(Component titleComponent) {
		LspsUI ui = (LspsUI) getUI();

		actionBtn = new MenuBar();
		actionBtn.addStyleName("menu-button-action");
		actionBtn.setVisible(false); //initially hidden
		MenuItem actions = actionBtn.addItem("", new ThemeResource("../icons/popup-menu.png"), null);

		final ViewAction refreshTodos = new ViewAction() {

			@Override
			public void invoke() {
				toggleSelectionMode(false);
				container.refresh();
			}
		};

		actions.addItem(ui.getMessage("action.lock"), new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				lock();
			}
		});
		actions.addItem(ui.getMessage("action.annotate") + "...", new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				getUI().addWindow(new TodoAnnotation(getSelectedTodoIds(), refreshTodos));
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
				getUI().addWindow(new TodoRejection(getSelectedTodoIds(), refreshTodos));
			}
		});
		actions.addItem(ui.getMessage("action.delegate") + "...", new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				getUI().addWindow(new TodoDelegation(getSelectedTodoIds(), refreshTodos));
			}
		});
		actions.addItem(ui.getMessage("action.escalate") + "...", new Command() {

			@Override
			public void menuSelected(MenuItem selectedItem) {
				getUI().addWindow(new TodoEscalation(getSelectedTodoIds(), refreshTodos));
			}
		});

		selectBtn = new Button(ui.getMessage("action.select"), new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				toggleSelectionMode(true);
			}
		});
		selectBtn.addStyleName("menu-button");

		cancelBtn = new Button(ui.getMessage("action.cancel"), new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				toggleSelectionMode(false);
			}
		});
		cancelBtn.setVisible(false); //initially hidden
		cancelBtn.addStyleName("menu-button");

		titleComponent.addStyleName("l-content-title");

		HorizontalLayout layout = new HorizontalLayout();
		HorizontalLayout btnLayout = new HorizontalLayout();
		layout.setWidth("100%");
		layout.addComponent(titleComponent);
		layout.addComponent(btnLayout);
		layout.setSpacing(true);
		btnLayout.addComponent(actionBtn);
		btnLayout.addComponent(cancelBtn);
		btnLayout.addComponent(selectBtn);
		layout.setComponentAlignment(btnLayout, Alignment.MIDDLE_RIGHT);
		return layout;
	}

	private void toggleSelectionMode(boolean active) {
		selectBtn.setVisible(!active);
		actionBtn.setVisible(active);
		cancelBtn.setVisible(active);
		table.setMultiSelect(active);
		table.setValue(null);
	}

	/**
	 * Locks selected to-dos.
	 * If lock failed, an error message is displayed to the user.
	 */
	public void lock() {
		try {
			for (Long todoId : selection) {
				todoService.lockTodo(todoId);
			}

			LspsUI ui = (LspsUI) getUI();
			ui.showInfoMessage("todo.lockSuccessful", selection.size());

			toggleSelectionMode(false);
			container.refresh();

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
			//				WebUtils.addExceptionMessage(e, "lockTodoFailed");
			//			}
		} catch (Exception e) {
			Utils.log(e, "could not lock to-dos " + selection, log);

			LspsUI ui = (LspsUI) getUI();
			ui.showErrorMessage("todo.lockFailed", e);
		}
	}

	/**
	 * Unlocks selected to-dos.
	 * If unlock failed, an error message is displayed to the user.
	 */
	public void unlock() {
		try {
			for (Long todoId : selection) {
				todoService.unlockTodo(todoId);
			}

			LspsUI ui = (LspsUI) getUI();
			ui.showInfoMessage("todo.unlockSuccessful", selection.size());

			toggleSelectionMode(false);
			container.refresh();

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
			Utils.log(e, "could not unlock to-do " + selection, log);

			LspsUI ui = (LspsUI) getUI();
			ui.showErrorMessage("todo.unlockFailed", e);
		}
	}

	private class TodoLoader implements DtoLoader<Todo> {

		private static final long serialVersionUID = 1L;

		@Override
		public DtoList<Todo> loadData(int startIndex, int count, Object[] sortPropertyIds, boolean[] sortStates) {
			TodoListCriteria criteria = new TodoListCriteria(startIndex, count);
			return todoService.getTodoList(criteria);
		}
	}

}
