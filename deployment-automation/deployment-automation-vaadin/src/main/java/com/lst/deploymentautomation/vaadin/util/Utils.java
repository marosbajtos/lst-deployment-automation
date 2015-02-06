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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.UserError;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.AbstractComponent;

/**
 * Utility methods.
 * 
 * @author mhi
 */
public class Utils {

	private static final String BUNDLE_ID = "com.whitestein.lsps.vaadin.webapp.localization";

	/**
	 * Returns localized string for given key and locale, replacing numbered tokens ({0}, {1}...) with the arguments.
	 * @param locale not null
	 * @param key message key not null
	 * @param args arguments may be null
	 * @return localized string, never null
	 */
	public static String getLocalizedString(Locale locale, String key, Object... args) {
		String text = getLocalizedString(locale, key, "???" + key + "???");
		try {
			return MessageFormat.format(text, args);
		} catch (Exception t) {
			log(t, "invalid translation '" + text + "'", LoggerFactory.getLogger(Utils.class.getName()));
			return text;
		}
	}

	/**
	 * Returns localized string for given key.
	 * @param locale not null
	 * @param key message key not null
	 * @param defaultValue default value, not null
	 * @return localized string, never null
	 */
	private static String getLocalizedString(Locale locale, String key, String defaultValue) {
		try {
			//TODO cache resource bundle
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_ID, locale, classLoader);
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return defaultValue;
		}
	}

	/**
	 * Logs the given exception. Stacktrace is logged on the FINER level, message from the exception is logged on the FINE level.
	 * @param x
	 * @param msg
	 * @param logger
	 */
	public static void log(Throwable x, String msg, Logger logger) {
		String cause = x.getMessage() == null ? x.toString() : x.getMessage();
		logger.error(msg + " (" + VaadinService.getCurrentRequest().getRemoteUser() + "): " + cause);
		logger.info("stacktrace", x);
	}

	/**
	 * Logs the given exception and rethrows it wrapped in a RuntimeException.
	 * @param e
	 * @param msg
	 * @param logger
	 */
	public static void rethrow(Exception e, String msg, Logger logger) {
		log(e, msg, logger);
		if (e instanceof RuntimeException) {
			throw (RuntimeException) e;
		} else {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Shows (or hides, if msg is null) localized error message on the given component. 
	 * @param component
	 * @param msg
	 * @param args
	 */
	public static void setComponentError(AbstractComponent component, String msg, Object... args) {
		if (msg == null) {
			component.setComponentError(null);
		} else {
			String localizedMsg = getLocalizedString(component.getUI().getLocale(), msg, args);
			component.setComponentError(new UserError(localizedMsg));
		}
	}

}
