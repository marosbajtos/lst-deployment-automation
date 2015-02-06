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
package com.lst.deploymentautomation.vaadin.util;

import java.util.Locale;

import javax.ejb.EJB;

import com.lst.deploymentautomation.vaadin.core.AppNavigator;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.whitestein.lsps.engine.dto.LocalizationResourceBundle;
import com.whitestein.lsps.engine.ejb.LocalizationServiceLocal;
import com.whitestein.lsps.engine.lang.ExecutionContext;
import com.whitestein.lsps.lang.exec.MapHolder;
import com.whitestein.lsps.vaadin.LspsAppConnector;
import com.whitestein.lsps.vaadin.LspsScreenFactory;
import com.whitestein.lsps.vaadin.LspsScreenFactoryImpl;
import com.whitestein.lsps.vaadin.ui.LspsConverterFactory;
import com.whitestein.lsps.vaadin.ui.LspsConverterFactoryImpl;
import com.whitestein.lsps.vaadin.ui.LspsLocalizer;
import com.whitestein.lsps.vaadin.ui.LspsLocalizerImpl;
import com.whitestein.lsps.vaadin.ui.LspsUrlFactory;
import com.whitestein.lsps.vaadin.ui.UIComponentFactory;
import com.whitestein.lsps.vaadin.ui.UIComponentFactoryImpl;
import com.whitestein.lsps.vaadin.util.Injector;
import com.whitestein.lsps.vaadin.util.TransactionAccessor;
import com.whitestein.lsps.vaadin.util.TransactionAccessorImpl;

/**
 * Application connector implementation for the default LSPS application.
 * 
 * @author mhi
 */
public class LspsAppConnectorImpl implements LspsAppConnector {

	private static final long serialVersionUID = 1L;

	/**
	 * Injector instance.
	 */
	public static final Injector INJECTOR_INSTANCE = com.whitestein.lsps.vaadin.util.Utils.createInjector();

	@EJB
	private LocalizationServiceLocal localizationService;

	private transient LspsLocalizer localizer;

	private Locale locale;

	private final LspsUI ui;

	/**
	 * Full constructor.
	 * @param ui not null
	 */
	public LspsAppConnectorImpl(LspsUI ui) {
		INJECTOR_INSTANCE.inject(this);
		this.ui = ui;
	}

	@Override
	public LspsScreenFactory getScreenFactory() {
		return new LspsScreenFactoryImpl();
	}

	@Override
	public UIComponentFactory getComponentFactory() {
		return new UIComponentFactoryImpl(this);
	}

	@Override
	public LspsConverterFactory getConverterFactory() {
		return new LspsConverterFactoryImpl();
	}

	@Override
	public LspsUrlFactory getUrlFactory() {
		return new LspsUrlFactoryImpl();
	}

	@Override
	public Injector getInjector() {
		return INJECTOR_INSTANCE;
	}

	@Override
	public TransactionAccessor getTransactionAccessor() {
		return TransactionAccessorImpl.instance();
	}

	@Override
	public LspsLocalizer getLocalizer() {
		if (localizer == null || !ui.getLocale().equals(locale)) {
			localizer = new LspsLocalizerImpl(new LocalizationResourceBundle(ui.getLocale(), localizationService));
			locale = ui.getLocale();
		}
		return localizer;
	}

	/**
	 * Looks into the application resource bundle (com.whitestein.lsps.vaadin.webapp.localization)
	 * of the current locale and returns value for the given key.
	 * 
	 * @param key not null
	 * @return value for the given key, never null
	 */
	@Override
	public String getApplicationMessage(String key) {
		return Utils.getLocalizedString(ui.getLocale(), key);
	}

	/**
	 *
	 * @param context not null
	 * @return not null
	 */
	@Override
	public MapHolder getHistory(ExecutionContext context) {
		final AppNavigator navigator = (AppNavigator) ui.getNavigator();
		return navigator.getHistory(context);
	}

}
