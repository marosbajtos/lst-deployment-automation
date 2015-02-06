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

import com.lst.deploymentautomation.vaadin.core.AppNavigator;
import com.lst.deploymentautomation.vaadin.page.*;
import com.whitestein.lsps.lang.Decimal;
import com.whitestein.lsps.lang.exec.MapHolder;
import com.whitestein.lsps.lang.exec.RecordHolder;
import com.whitestein.lsps.vaadin.LspsContextHolder;
import com.whitestein.lsps.vaadin.ui.LspsUrlFactory;
import com.whitestein.lsps.vaadin.ui.components.UIFieldNames;
import com.whitestein.lsps.vaadin.ui.components.UINavigationNames;
import com.whitestein.lsps.vaadin.util.Variant;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link LspsUrlFactory}
 * 
 * @author mbj
 *
 */
public class LspsUrlFactoryImpl implements LspsUrlFactory {

	@Override
	public String getUrlForTodo(long todoId) {
		return "#!" + TodoView.ID + "/" + todoId;
	}

	@Override
	public String getUrlForDocument(String documentId, Map<String, String> parameters) {
		return "#!" + DocumentView.getViewId(documentId, parameters);
	}

	@Override
	public String getUrlForSavedDocument(long savedDocumentId) {
		return "#!" + DocumentView.getViewId(savedDocumentId);
	}

	@Override
	public String getNavigationUrl(RecordHolder navigation) {
        final Variant.RecordVariant r = Variant.ofUnknownOrigin(navigation, (LspsContextHolder) null);
		final String navigationType = r.getTypeFullName();
		
		if (UINavigationNames.URL_NAVIGATION.equals(navigationType)) {
			return r.getPropertyValue(UIFieldNames.URL).string().get();
		}
		
		if (UINavigationNames.APP_NAVIGATION.equals(navigationType) || UINavigationNames.HISTORICAL_APP_NAVIGATION.equals(navigationType)) {
			final String code = r.getPropertyValue(UIFieldNames.CODE).string().get();

			final String viewId;
			if ("todoList".equals(code)) {
				viewId = TodoListView.ID;
			} else if ("documents".equals(code)) {
				viewId = DocumentsView.ID;
			} else if ("runModel".equals(code)) {
				viewId = RunModelView.ID;
			} else if ("settings".equals(code)) {
				viewId = SettingsView.ID;
			} else {
				viewId = code;
				//throw new RuntimeException("Navigation code '" + code + "' is not supported");
			}
			
			String url = "#!" + viewId;
			if (UINavigationNames.HISTORICAL_APP_NAVIGATION.equals(navigationType)) {
				url += AppNavigator.HISTORY_ID_DELIMITER + navigation.getProperty(UIFieldNames.ID);
			}
			return url;
		}
		
		if (UINavigationNames.TODO_NAVIGATION.equals(navigationType) || UINavigationNames.HISTORICAL_TODO_NAVIGATION.equals(navigationType)) {
			final Decimal todoId = r.getPropertyValue(UIFieldNames.TODO).record().getPropertyValue(UIFieldNames.ID).decimal().get();

			//create base url
			String url = getUrlForTodo(todoId.longValue());
			
			if (Boolean.TRUE.equals(navigation.getProperty(UIFieldNames.OPEN_AS_READONLY))) {
				url += TodoView.READONLY_SUFFIX;
			}
			
			if (UINavigationNames.HISTORICAL_TODO_NAVIGATION.equals(navigationType)) {
				url += AppNavigator.HISTORY_ID_DELIMITER + navigation.getProperty(UIFieldNames.ID);
			}
			return url;
		}
		
		if (UINavigationNames.DOCUMENT_NAVIGATION.equals(navigationType) || UINavigationNames.HISTORICAL_DOCUMENT_NAVIGATION.equals(navigationType)) {
			final String name = r.getPropertyValue(UIFieldNames.DOCUMENT_TYPE).record().getPropertyValue(UIFieldNames.NAME).string().get();

			MapHolder params = (MapHolder) navigation.getProperty(UIFieldNames.PARAMETERS);
			
			Map<String,String> paramsMap = new HashMap<String, String>();
			if (params != null){
				for (Map.Entry<Object,Object> entry : params.entrySet()){
					paramsMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
				}		
			}
			
			String url = getUrlForDocument(name, paramsMap);
			if (UINavigationNames.HISTORICAL_DOCUMENT_NAVIGATION.equals(navigationType)) {
				url += AppNavigator.HISTORY_ID_DELIMITER + navigation.getProperty(UIFieldNames.ID);
			}
			return url;
		}
		
		if (UINavigationNames.SAVED_DOCUMENT_NAVIGATION.equals(navigationType) || UINavigationNames.HISTORICAL_SAVED_DOCUMENT_NAVIGATION.equals(navigationType)) {
			final Decimal savedDocId = r.getPropertyValue(UIFieldNames.SAVED_DOCUMENT).record().getPropertyValue(UIFieldNames.ID).decimal().get();

			String url = getUrlForSavedDocument(savedDocId.longValue());
			if (UINavigationNames.HISTORICAL_SAVED_DOCUMENT_NAVIGATION.equals(navigationType)) {
				url += AppNavigator.HISTORY_ID_DELIMITER + navigation.getProperty(UIFieldNames.ID);
			}
			return url;
		}
		
		throw new RuntimeException("Navigation of type " + navigationType + " is not supported");
	}

}
