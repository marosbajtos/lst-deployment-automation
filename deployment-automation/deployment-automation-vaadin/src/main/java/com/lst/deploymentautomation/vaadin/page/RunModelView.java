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

import com.vaadin.data.util.BeanContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Table;
import com.lst.deploymentautomation.vaadin.core.AppView;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.util.ModelRunner;
import com.whitestein.lsps.engine.dto.Module;
import com.whitestein.lsps.engine.dto.ModuleCriteria;
import com.whitestein.lsps.engine.dto.ModuleList;
import com.whitestein.lsps.engine.ejb.ModelManagementServiceLocal;

import javax.ejb.EJB;

/**
 * Run model page.
 * 
 * @author mhi
 */
public class RunModelView extends AppView {
	private static final long serialVersionUID = 1L;

	/**
	 * View title localization key.
	 */
	public static final String TITLE = "nav.runProcess";
	
	/**
	 * View ID used for navigation.
	 */
	public static final String ID = "runProcess";
	
	private BeanContainer<Long,Module> container;
	
	@EJB 
	private ModelManagementServiceLocal modelManagementService;
	
	@Override
	public void attach() {
		super.attach();
		
		if (container == null) {
			container = new BeanContainer<Long,Module>(Module.class);
			container.setBeanIdProperty("id");
			
			initComponents();
		} else {
			container.removeAllItems();
		}
		
		loadModels();
	}
	
	private void loadModels() {
		ModuleCriteria criteria = new ModuleCriteria();
		criteria.setIncludeImports(false);
		criteria.setIncludeExecutableOnly(true);
		criteria.setIncludeLatestOnly(true);
		ModuleList models = modelManagementService.findModules(criteria);
		
		for (Module module: models.getData()) {
			container.addBean(module);
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
				if (event.getButton() != MouseButton.LEFT || event.isDoubleClick()) return;
				final Long modelId = (Long) event.getItem().getItemProperty("id").getValue();
				final LspsUI ui = (LspsUI) getUI();
				final ModelRunner runner = new ModelRunner();
				runner.runModelById(modelId, ui);
			}
		});
        
		table.setContainerDataSource(container);
		table.setVisibleColumns(new Object[] {
				"name", 
				"description",
				"version"
		});
        //table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
        table.setColumnHeaders(new String[] {
	    		ui.getMessage("process.model"),
	    		ui.getMessage("process.description"),
	    		""
		});
        table.setColumnAlignment("version", Table.Align.RIGHT);
        table.setColumnExpandRatio("name", 1);
        table.setColumnExpandRatio("description", 3);
        
		setContent(table);
	}
}
