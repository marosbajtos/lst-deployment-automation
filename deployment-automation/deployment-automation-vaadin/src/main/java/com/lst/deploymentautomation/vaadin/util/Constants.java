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

import java.util.Arrays;
import java.util.List;

/**
 * @author oko
 * Class for storing app constants.
 */
public final class Constants {

	/**
	* Default theme setting for vaadin application.
	*/
	public static final String DEFAULT_THEME = "modern";

	/**
	 * Themes that does support font and layout settings.
	 */
	public static final List<String> CUSTOMISABLE_THEMES = Arrays.asList("modern", "blue");

	/**
	 * Other than default themes. This is used in user settings as options to default.
	 * Themes are stored in deployment-automation-vaadin-war/src/main/webapp/VAADIN/themes folder.
	 * If new theme is added it has to be compiled from SASS.
	 */
	public static final List<String> THEMES = Arrays.asList("modern", "classic", "blue");

	private Constants() {
		throw new AssertionError();
	}
}
