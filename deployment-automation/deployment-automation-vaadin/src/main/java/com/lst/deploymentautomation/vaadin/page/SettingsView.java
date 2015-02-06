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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.lst.deploymentautomation.vaadin.core.AppView;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.util.Constants;
import com.lst.deploymentautomation.vaadin.util.Utils;
import com.whitestein.lsps.common.Locales;
import com.whitestein.lsps.os.OsRights;
import com.whitestein.lsps.os.dto.Person;
import com.whitestein.lsps.os.dto.PersonCriteria;
import com.whitestein.lsps.os.ejb.PersonManagementServiceLocal;
import com.whitestein.lsps.os.ejb.PersonServiceLocal;

/**
 * User settings page.
 * 
 * @author mhi
 */
public class SettingsView extends AppView {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(SettingsView.class.getName());

	/**
	 * View title localization key.
	 */
	public static final String TITLE = "nav.settings";

	/**
	 * View ID used for navigation.
	 */
	public static final String ID = "settings";

	private static final Boolean BUTTON_TYPE_SAVE = true;

	//components
	private PasswordField passwordField;
	private PasswordField confirmation;
	private TextField email;
	private TextField telephone;
	private OptionGroup languages;
	private OptionGroup themes;
	private OptionGroup layoutSettings;
	private OptionGroup fontSettings;
	private CheckBox substitutionActive;
	private OptionGroup substitutes;

	private Set<String> userRights;

	private boolean requestReload = false;

	@EJB
	private PersonServiceLocal personService;

	@EJB
	private PersonManagementServiceLocal personManagementService;

	@Override
	public void attach() {
		super.attach();

		if (passwordField == null) {
			//create view content
			createView();
		}
	}

	private void createView() {
		LspsUI ui = (LspsUI) getUI();
		Person user = ui.getUser().getPerson();
		userRights = user.getRights();

		setTitle(ui.getMessage(TITLE));

		Panel panel = new Panel();
		panel.addStyleName("l-border-none");
		setContent(panel);

		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		panel.setContent(layout);

		//user data and settings will be laid out next to each other
		HorizontalLayout topSection = new HorizontalLayout();
		topSection.setSpacing(true);
		topSection.setWidth("100%");
		layout.addComponent(topSection);

		VerticalLayout userData = createUserDataSection(ui, user);
		topSection.addComponent(userData);
		topSection.setExpandRatio(userData, 1);

		Label spacer = new Label();
		spacer.setWidth("20px");
		topSection.addComponent(spacer);

		VerticalLayout settings = createSettingsSection(ui);
		topSection.addComponent(settings);
		topSection.setExpandRatio(settings, 1);

		//substitution section
		VerticalLayout substitution = createSubstitutionSection(ui, user);
		layout.addComponent(substitution);

		Label spacer2 = new Label();
		spacer2.setHeight("10px");
		layout.addComponent(spacer2);

		//buttons
		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setSpacing(true);
		Button saveButton = new Button(ui.getMessage("action.save"), new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (save()) {
					if (requestReload) {
						JavaScript.getCurrent().execute("window.location.reload()");
					} else {
						close();
					}
				}
			}
		});
		saveButton.setData(BUTTON_TYPE_SAVE);
		buttons.addComponent(saveButton);
		Button cancelButton = new Button(ui.getMessage("action.cancel"), new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				close();
			}
		});
		buttons.addComponent(cancelButton);
		layout.addComponent(buttons);
	}

	private VerticalLayout createUserDataSection(LspsUI ui, Person user) {
		VerticalLayout userData = new VerticalLayout();

		Label userDataHeader = new Label("<h2>" + ui.getMessage("settings.userSection") + "</h2>", ContentMode.HTML);
		userData.addComponent(userDataHeader);

		FormLayout userDataContent = new FormLayout();

		if ((userRights.contains(OsRights.CHANGE_OWN_PASSWORD)) || (userRights.contains(OsRights.MANAGE_PERSON))) {
			this.passwordField = new PasswordField(ui.getMessage("settings.password"));
			passwordField.setWidth("100%");
			userDataContent.addComponent(passwordField);

			this.confirmation = new PasswordField(ui.getMessage("settings.passwordVerification"));
			confirmation.setWidth("100%");
			userDataContent.addComponent(confirmation);
		}

		this.email = new TextField(ui.getMessage("settings.email"));
		email.setWidth("100%");
		email.setValue(user.getEmail());
		email.setNullRepresentation("");
		userDataContent.addComponent(email);

		this.telephone = new TextField(ui.getMessage("settings.phoneNumber"));
		telephone.setWidth("100%");
		telephone.setValue(user.getPhone());
		telephone.setNullRepresentation("");
		userDataContent.addComponent(telephone);

		userData.addComponent(userDataContent);
		return userData;
	}

	private VerticalLayout createSettingsSection(LspsUI ui) {
		VerticalLayout settings = new VerticalLayout();
		settings.setSpacing(true);

		Label settingsHeader = new Label("<h2>" + ui.getMessage("settings.applicationSection") + "</h2>", ContentMode.HTML);
		settings.addComponent(settingsHeader);

		this.languages = new OptionGroup(ui.getMessage("settings.language"));
		languages.addStyleName("ui-spacing");
		languages.addItem("en_US");
		languages.setItemCaption("en_US", "English");
		languages.addItem("de_DE");
		languages.setItemCaption("de_DE", "Deutsch");
		languages.addItem("sk_SK");
		languages.setItemCaption("sk_SK", "Slovensky");
		languages.setValue(ui.getLocale().toString());
		settings.addComponent(languages);

		this.themes = new OptionGroup(ui.getMessage("settings.theme"));
		themes.addStyleName("ui-spacing");
		for (String s : Constants.THEMES) {
			themes.addItem(s);
		}
		String userTheme = ui.getUser().getSettingString("theme", Constants.DEFAULT_THEME);
		themes.setValue(userTheme);
		themes.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if (Constants.CUSTOMISABLE_THEMES.contains(themes.getValue().toString())) {
					layoutSettings.setVisible(true);
					fontSettings.setVisible(true);
				} else {
					layoutSettings.setVisible(false);
					fontSettings.setVisible(false);
				}
			}
		});
		settings.addComponent(themes);

		layoutSettings = new OptionGroup(ui.getMessage("settings.layout"));
		layoutSettings.addStyleName("ui-spacing");
		layoutSettings.addItem("compact");
		layoutSettings.setItemCaption("compact", ui.getMessage("settings.layout.compact"));
		layoutSettings.addItem("standard");
		layoutSettings.setItemCaption("standard", ui.getMessage("settings.layout.standard"));
		layoutSettings.addItem("generous");
		layoutSettings.setItemCaption("generous", ui.getMessage("settings.layout.generous"));
		layoutSettings.setValue(ui.getUser().getSettingString("layoutSetting", "compact"));

		settings.addComponent(layoutSettings);

		fontSettings = new OptionGroup(ui.getMessage("settings.font"));
		fontSettings.addStyleName("ui-spacing");
		fontSettings.addItem("small");
		fontSettings.setItemCaption("small", ui.getMessage("settings.font.small"));
		fontSettings.addItem("standard");
		fontSettings.setItemCaption("standard", ui.getMessage("settings.font.standard"));
		fontSettings.addItem("large");
		fontSettings.setItemCaption("large", ui.getMessage("settings.font.large"));
		fontSettings.addItem("huge");
		fontSettings.setItemCaption("huge", ui.getMessage("settings.font.huge"));
		fontSettings.setValue(ui.getUser().getSettingString("fontSetting", "standard"));

		settings.addComponent(fontSettings);

		if (!Constants.CUSTOMISABLE_THEMES.contains(userTheme)) {
			layoutSettings.setVisible(false);
			fontSettings.setVisible(false);
		}

		return settings;
	}

	private VerticalLayout createSubstitutionSection(LspsUI ui, Person user) {
		VerticalLayout substitution = new VerticalLayout();
		substitution.setSpacing(true);

		Label substitutionHeader = new Label("<h2>" + ui.getMessage("settings.substitutionSection") + "</h2>", ContentMode.HTML);
		substitution.addComponent(substitutionHeader);

		Label substitutionHelp = new Label(ui.getMessage("settings.substitutionHelp"));
		substitutionHelp.setStyleName("form-help");
		substitution.addComponent(substitutionHelp);

		this.substitutionActive = new CheckBox(ui.getMessage("settings.substitutionActive"));
		substitutionActive.addStyleName("ui-spacing");
		substitutionActive.setValue(user.isSubstitutionActive());
		substitution.addComponent(substitutionActive);

		//substitutes
		Set<Person> selectedPersons = user.getDirectSubstitutes();
		//all persons except the user himself
		Collection<Person> persons = new ArrayList<Person>(personService.findPersons(new PersonCriteria()).getData());

		this.substitutes = new OptionGroup(ui.getMessage("settings.substitutes"));
		substitutes.setMultiSelect(true);
		substitutes.addStyleName("ui-spacing");
		for (Person substitute : persons) {
			if (!substitute.equals(user)) {
				substitutes.addItem(substitute);
				substitutes.setItemCaption(substitute, substitute.getFullName());
			}
		}
		substitutes.setValue(selectedPersons);
		substitution.addComponent(substitutes);

		return substitution;
	}

	private boolean save() {
		LspsUI ui = (LspsUI) getUI();

		boolean active = Boolean.TRUE.equals(substitutionActive.getValue());
		@SuppressWarnings("unchecked")
		Set<Person> selection = (Set<Person>) substitutes.getValue();

		String pass = passwordField.getValue();
		String pass2 = confirmation.getValue();

		boolean valid = true;
		if (active && selection.size() == 0) {
			Utils.setComponentError(substitutes, "settings.noSubstitutesError");
			valid = false;
		} else {
			Utils.setComponentError(substitutes, null);
		}
		if (pass != null && !pass.equals(pass2)) {
			Utils.setComponentError(confirmation, "settings.passwordDoesNotMatchError");
			valid = false;
		} else {
			Utils.setComponentError(confirmation, null);
		}

		if (valid) {
			try {
				//save locale and update UI
				Locale locale = Locales.toLocale((String) languages.getValue());
				if (!locale.equals(ui.getUser().getLocale())) {
					requestReload = true;
					ui.getUser().setLocale(locale);
					UI.getCurrent().setLocale(locale);
				}

				// change theme if it is different than current
				String newTheme = themes.getValue().toString();
				if (!ui.getTheme().equals(newTheme)) {
					((LspsUI) UI.getCurrent()).setTheme(newTheme);
				}

				String layoutSetting = "l-layout-" + ui.getUser().getSettingString("layoutSetting", "compact");
				JavaScript.getCurrent().execute(""
						+ "var classes = document.body.className.split(/\\s+/);"
						+ "classes.pop();"
						+ "document.body.className = classes.join(\" \");"
						+ "");
				ui.getUser().setSetting("layoutSetting", layoutSettings.getValue().toString());
				layoutSetting = "l-layout-" + layoutSettings.getValue().toString();
				JavaScript.getCurrent().execute(""
						+ "if (!Array.prototype.indexOf) {"
						+ "Array.prototype.indexOf = function(obj, start) {for (var i = (start || 0), j = this.length; i < j; i++) {if (this[i] === obj) { return i; }}return -1;}"
						+ "}"
						+ "var setting = '" + layoutSetting + "';"
						+ "var classes = document.body.className.split(/\\s+/);"
						+ "var index = classes.indexOf(setting);"
						+ "~index || classes.push(setting);"
						+ "document.body.className = classes.join(\" \");"
						+ "");
				ui.getUser().setSetting("fontSetting", fontSettings.getValue().toString());
				String fontSetting = "l-font-" + fontSettings.getValue().toString();
				JavaScript.getCurrent().execute(""
						+ "document.documentElement.className='" + fontSetting + "'"
						+ "");

				//reload person to avoid stale object exceptions
				Person person = ui.getUser().reload();

				//update user details
				person.setDirectSubstitutes(selection);
				person.setSubstitutionActive(active);
				person.setEmail(email.getValue());
				person.setPhone(telephone.getValue());

				person = personManagementService.updatePerson(person);
				ui.getUser().setPerson(person);

				//update password
				if (pass != null && !"".equals(pass)) {
					personManagementService.updatePersonPassword(person.getId(), pass);
				}

			} catch (Exception e) {
				Utils.rethrow(e, "could not save settings", log);
			}
		}

		return valid;
	}
}
