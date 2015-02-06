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

import java.util.List;
import java.util.Map;

import com.vaadin.ui.MenuBar;
import com.lst.deploymentautomation.vaadin.util.ModelRunner;

/**
 * Menu bar that supports localization and makes adding application views and
 * LSPS documents easier.
 */
public class NavigationMenu extends MenuBar {

	private static final long serialVersionUID = 1L;

	/**
	 * Updates the menu item titles to match the current locale.
	 */
	public void onLocaleChange() {
		for (MenuItem item : getItems()) {
			updateMenuItemLocale(item);
		}
	}

	private void updateMenuItemLocale(MenuItem item) {
		Command cmd = item.getCommand();
		if (cmd instanceof NavigationCommand) {
			String title = ((NavigationCommand) cmd).getTitle();
			item.setText(title);
		}

		List<MenuItem> children = item.getChildren();
		if (children != null) {
			for (MenuItem child : children) {
				updateMenuItemLocale(child);
			}
		}
	}

	/**
	 * Adds a new menu item for an application view.
	 * @param titleKey localization key for the menu item title
	 * @param viewId view ID
	 * @return created menu item
	 */
	public MenuItem addViewItem(String titleKey, String viewId) {
		OpenViewCommand cmd = new OpenViewCommand((LspsUI) getUI(), titleKey, viewId);
		return addItem(cmd.getTitle(), cmd);
	}

	/**
	 * Adds a new menu item for a document.
	 * @param titleKey localization key for the menu item title
	 * @param documentId document ID
	 * @param parameters document parameters (may be null)
	 * @return created menu item
	 */
	public MenuItem addDocumentItem(String titleKey, String documentId, Map<String, String> parameters) {
		OpenDocumentCommand cmd = new OpenDocumentCommand((LspsUI) getUI(), titleKey, documentId, parameters);
		return addItem(cmd.getTitle(), cmd);
	}

	/**
	 * Interface for commands to be shown in the navigation menu.
	 * @author mhi
	 */
	public interface NavigationCommand extends MenuBar.Command {

		/**
		 * Returns the command title.
		 * @return title
		 */
		String getTitle();
	}

	private abstract static class BaseCommand implements NavigationCommand {

		private static final long serialVersionUID = 1L;

		protected final LspsUI ui;
		private final String titleKey;

		public BaseCommand(LspsUI ui, String titleKey) {
			this.ui = ui;
			this.titleKey = titleKey;
		}

		@Override
		public String getTitle() {
			return ui.getMessage(titleKey);
		}
	}

	/**
	 * Menu command for opening application views.
	 * @author mhi
	 */
	public static class OpenViewCommand extends BaseCommand {

		private static final long serialVersionUID = 1L;

		private String viewId;

		/**
		 * Full constructor.
		 * @param ui
		 * @param titleKey
		 * @param viewId
		 */
		public OpenViewCommand(LspsUI ui, String titleKey, String viewId) {
			super(ui, titleKey);
			this.viewId = viewId;
		}

		@Override
		public void menuSelected(MenuBar.MenuItem selectedItem) {
			try {
				ui.navigateTo(viewId);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Menu command for opening documents.
	 * @author mhi
	 */
	public static class OpenDocumentCommand extends BaseCommand {

		private static final long serialVersionUID = 1L;

		private String documentId;
		private Map<String, String> parameters;

		/**
		 * Full constructor.
		 * @param ui
		 * @param titleKey
		 * @param documentId
		 * @param parameters 
		 */
		public OpenDocumentCommand(LspsUI ui, String titleKey, String documentId, Map<String, String> parameters) {
			super(ui, titleKey);
			this.documentId = documentId;
			this.parameters = parameters;
		}

		@Override
		public void menuSelected(MenuBar.MenuItem selectedItem) {
			try {
				ui.openDocument(documentId, parameters);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Menu command for running models.
	 * @author oko
	 */
	public static class RunModelCommand extends BaseCommand {

		private static final long serialVersionUID = 1L;

		private String modelName;
		private String modelVersion;

		/**
		 * Full constructor.
		 * @param ui 
		 * @param titleKey 
		 * @param modelName 
		 * @param modelVersion 
		 */
		public RunModelCommand(LspsUI ui, String titleKey, String modelName, String modelVersion) {
			super(ui, titleKey);
			this.modelName = modelName;
			this.modelVersion = modelVersion;
		}

		/**
		 * Constructor for command with unspecified version.
		 * Newest model with matching name is ran.
		 * 
		 * @param ui
		 * @param titleKey
		 * @param modelName
		 */
		public RunModelCommand(LspsUI ui, String titleKey, String modelName) {
			this(ui, titleKey, modelName, null);
		}

		@Override
		public void menuSelected(MenuBar.MenuItem selectedItem) {
			ModelRunner runner = new ModelRunner();
			runner.runModelByName(modelName, modelVersion, ui);
		}
	}
}
