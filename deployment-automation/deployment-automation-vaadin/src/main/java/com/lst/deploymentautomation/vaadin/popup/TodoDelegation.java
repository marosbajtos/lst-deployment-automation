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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.lst.deploymentautomation.vaadin.core.AppView.ViewAction;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.util.LspsAppConnectorImpl;
import com.lst.deploymentautomation.vaadin.util.Utils;
import com.whitestein.lsps.human.ejb.TodoServiceLocal;
import com.whitestein.lsps.os.dto.Person;
import com.whitestein.lsps.os.dto.PersonCriteria;
import com.whitestein.lsps.os.ejb.PersonServiceLocal;

/**
 * Popup window for to-do delegation.
 * 
 * @author mhi
 */
public class TodoDelegation extends Window {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(TodoDelegation.class.getName());

	private Set<Long> todoIds;
	private ViewAction closeAction;
	private OptionGroup delegates;

	@EJB
	private TodoServiceLocal todoService;

	@EJB
	private PersonServiceLocal personService;

	/**
	 * Constructor for the delegation of a single to-do.
	 * @param todoId
	 * @param closeAction 
	 */
	public TodoDelegation(Long todoId, ViewAction closeAction) {
		this(Collections.singleton(todoId), closeAction);
	}

	/**
	 * Full constructor.
	 * @param todoIds
	 * @param closeAction 
	 */
	public TodoDelegation(Set<Long> todoIds, ViewAction closeAction) {
		LspsAppConnectorImpl.INJECTOR_INSTANCE.inject(this);

		this.todoIds = todoIds;
		this.closeAction = closeAction;
		setWidth("400px");
		setHeight("350px");
		setModal(true);
		setClosable(true);
	}

	@Override
	public void attach() {
		super.attach();

		LspsUI ui = (LspsUI) getUI();
		setCaption(ui.getMessage("todo.delegationTitle"));

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setSizeFull();
		setContent(layout);

		Label help = new Label(ui.getMessage("todo.delegationHelp"));
		help.setStyleName("form-help");
		layout.addComponent(help);

		Collection<Person> allUsers = new ArrayList<Person>(personService.findPersons(new PersonCriteria()).getData());
		Set<Person> substitutes = ui.getUser().getPerson().getDirectSubstitutes();
		Set<String> substitutesIds = new HashSet<String>();
		for (Person p : substitutes) {
			substitutesIds.add(p.getId());
		}

		delegates = new OptionGroup(ui.getMessage("todo.delegates"));
		delegates.setMultiSelect(true);
		delegates.addStyleName("ui-spacing");
		delegates.setRequired(true);
		delegates.setSizeFull();
		for (Person p : allUsers) {
			delegates.addItem(p.getId());
			delegates.setItemCaption(p.getId(), p.getFullName());
		}
		delegates.setValue(substitutesIds);
		layout.addComponent(delegates);
		layout.setExpandRatio(delegates, 1);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		layout.addComponent(buttons);

		@SuppressWarnings("serial")
		Button delegateButton = new Button(ui.getMessage("action.delegate"), new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				delegate();
			}
		});
		buttons.addComponent(delegateButton);

		@SuppressWarnings("serial")
		Button cancelButton = new Button(ui.getMessage("action.cancel"), new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				close();
			}
		});
		buttons.addComponent(cancelButton);
	}

	private void delegate() {
		@SuppressWarnings("unchecked")
		Set<String> selection = (Set<String>) delegates.getValue();
		if (selection == null || selection.size() == 0) {
			Utils.setComponentError(delegates, "app.requiredMessage");
			return;
		} else {
			Utils.setComponentError(delegates, null);
		}

		int failed = 0;
		for (Long todoId : todoIds) {
			if (!delegateTodo(todoId, selection)) {
				failed++;
			}
		}

		//show success message if no error message was shown
		if (failed == 0) {
			LspsUI ui = (LspsUI) getUI();
			ui.showInfoMessage("todo.delegateSuccessful", todoIds.size() - failed);
		}

		close();
		if (closeAction != null) {
			closeAction.invoke();
		}
	}

	private boolean delegateTodo(Long todoId, Set<String> delegates) {
		try {
			todoService.delegate(todoId, delegates);
			log.trace("delegated to-do " + todoId);
			return true;

		} catch (Exception e) {
			Utils.log(e, "could not delegate to-do " + todoId, log);

			LspsUI ui = (LspsUI) getUI();
			ui.showErrorMessage("todo.delegateFailed", e);

			return false;
		}
	}

}
