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

import java.util.List;

import javax.ejb.EJB;

import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.lst.deploymentautomation.vaadin.core.AppView;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.whitestein.lsps.common.ErrorException;
import com.whitestein.lsps.human.dto.DocumentInfo;
import com.whitestein.lsps.human.ejb.GenericDocumentServiceLocal;

/**
 * Documents page.
 * 
 * @author mhi
 */
public class DocumentsView extends AppView {

	private static final long serialVersionUID = 1L;

	/**
	 * View title localization key.
	 */
	public static final String TITLE = "nav.documents";

	/**
	 * View ID used for navigation.
	 */
	public static final String ID = "documents";

	private BeanContainer<String, DocumentInfo> container;

	@EJB
	private GenericDocumentServiceLocal genericDocumentService;

	@Override
	public void attach() {
		super.attach();

		if (container == null) {
			container = new BeanContainer<String, DocumentInfo>(DocumentInfo.class);
			container.setBeanIdProperty("id");

			initComponents();
		} else {
			container.removeAllItems();
		}

		load();
	}

	private void load() {
		try {
			List<DocumentInfo> documents = genericDocumentService.getAllDocuments();

			for (DocumentInfo document : documents) {
				container.addBean(document);
			}
		} catch (ErrorException e) {
			((LspsUI) getUI()).showErrorMessage("app.unknownErrorOccurred", e);
		}
	}

	private void initComponents() {
		final LspsUI ui = (LspsUI) getUI();

		setTitle(ui.getMessage(TITLE));

		Table table = new Table();
		table.setSizeFull();
		table.setSelectable(true);
		//		table.setColumnReorderingAllowed(true);
		//		table.setColumnCollapsingAllowed(true);

		table.addItemClickListener(new ItemClickListener() {

			@Override
			public void itemClick(ItemClickEvent event) {
				if (event.getButton() != MouseButton.LEFT || event.isDoubleClick()) {
					return;
				}
				final LspsUI ui = (LspsUI) getUI();
				final String documentId = (String) event.getItem().getItemProperty("id").getValue();
				ui.openDocument(documentId, null);
			}
		});

		table.setContainerDataSource(container);
		table.setVisibleColumns(new Object[] {
				"title"
				//"description"
		});
		//table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		table.setColumnHeaders(new String[] {
				ui.getMessage("document.title")
		});
		table.setColumnExpandRatio("title", 1);
		//table.setColumnExpandRatio("description", 3);

		//localize document titles
		table.addGeneratedColumn("title", new ColumnGenerator() {

			private static final long serialVersionUID = 1L;

			@Override
			public Object generateCell(Table source, Object itemId, Object columnId) {
				@SuppressWarnings("unchecked")
				BeanItem<DocumentInfo> item = (BeanItem<DocumentInfo>) source.getItem(itemId);
				return ui.localizeEngineText(item.getBean().getTitle());
			}
		});

		setContent(table);
	}
}
