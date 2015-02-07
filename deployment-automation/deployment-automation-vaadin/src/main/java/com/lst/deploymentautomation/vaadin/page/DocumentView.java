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

import static com.google.gwt.thirdparty.guava.common.base.Preconditions.checkNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lst.deploymentautomation.vaadin.core.AppFormConnector;
import com.lst.deploymentautomation.vaadin.core.AppView;
import com.lst.deploymentautomation.vaadin.core.LspsUI;
import com.lst.deploymentautomation.vaadin.util.Utils;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.whitestein.lsps.engine.lang.ExecutionContext;
import com.whitestein.lsps.human.ejb.GenericDocumentServiceLocal;
import com.whitestein.lsps.lang.exec.RecordHolder;
import com.whitestein.lsps.vaadin.AbstractDocumentHolder;
import com.whitestein.lsps.vaadin.LspsCustomUIDocumentHolder;
import com.whitestein.lsps.vaadin.LspsDocumentHolder;
import com.whitestein.lsps.vaadin.LspsFormConnector;

/**
 * Application view for displaying a document.
 * 
 * @author mhi
 */
public class DocumentView extends AppView {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(DocumentView.class.getName());

	@EJB
	private GenericDocumentServiceLocal documentService;

	private AbstractDocumentHolder documentHolder;

	/**
	 * View ID used for navigation.
	 */
	public static final String ID = "document";

	/**
	 * Default constructor.
	 */
	public DocumentView() {
		setSizeFull();
	}

	@Override
	public void enter(ViewChangeEvent event) {
		//if reopening a view, there is nothing to do
		if (documentHolder != null) {
			//if reopening a view, screen might want to reinitialize
			documentHolder.reactivate();

		} else {
			super.enter(event);

			LspsUI ui = (LspsUI) event.getNavigator().getUI();
			String parameters = event.getParameters();

			try {
				String documentId;
				Map<String, String> params = new HashMap<String, String>();

				//parse document parameters
				int idx = parameters.indexOf('?');
				if (idx > 0) {
					documentId = decode(parameters.substring(0, idx));

					String[] tuples = parameters.substring(idx + 1).split("&");
					for (String tuple : tuples) {
						idx = tuple.indexOf('=');
						String key = idx >= 0 ? tuple.substring(0, idx) : "";
						String value = idx >= 0 ? tuple.substring(idx + 1) : "";
						params.put(decode(key), decode(value));
					}
				} else {
					documentId = decode(parameters);
				}

				String userId = ui.getUser().getPerson().getId();

				//decide if we are opening a new document or a saved one
				LspsFormConnector formConnector = new AppFormConnector(this);
				if (documentId.matches("[0-9]+")) {
					long savedDocumentId = Long.parseLong(documentId);
					boolean isCustomGUIDocument = documentService.isCustomGUIDocument(savedDocumentId);
					if (isCustomGUIDocument) {
						documentHolder = new LspsCustomUIDocumentHolder(savedDocumentId, ui.getAppConnector(), formConnector, userId);
					} else {
						documentHolder = new LspsDocumentHolder(savedDocumentId, ui.getAppConnector(), formConnector, userId);
					}
				} else {
					boolean isCustomGUIDocument = documentService.isCustomGUIDocument(documentId);
					if (isCustomGUIDocument) {
						documentHolder = new LspsCustomUIDocumentHolder(documentId, params, ui.getAppConnector(), formConnector, userId);
					} else {
						documentHolder = new LspsDocumentHolder(documentId, params, ui.getAppConnector(), formConnector, userId);
					}
				}
				documentHolder.reloadContent();

			} catch (Exception e) {
				ui.showErrorMessage("app.unknownErrorOccurred", e);
				Utils.log(e, "could not render document " + parameters, log);
			}
		}
	}

	@Override
	public void cleanup() {
		if (documentHolder != null) {
			documentHolder.invalidate();
		}
	}

	@Override
	protected RecordHolder createHistoryEntry(ExecutionContext context,
			Map<String, Object> defaultValues) {
		if (documentHolder == null) {
			return super.createHistoryEntry(context, defaultValues);
		} else {
			return documentHolder.getHistoryEntry(context, defaultValues);
		}
	}

	/**
	 * Returns the view ID that can be used for navigation to the given document.
	 * @param documentId the document ID, not null.
	 * @param parameters parameters, may be null.
	 * @return view ID
	 */
	public static String getViewId(String documentId, Map<String, String> parameters) {
		checkNotNull(documentId, "documentId");
		String encodedDocumentId = encode(documentId)
				.replace("%3A", ":").replace("%27", "'"); //don't escape ":" and "'" so that page refresh works in most cases

		String viewId = DocumentView.ID + "/" + encodedDocumentId;

		if (parameters != null) {
			//encode document parameters
			StringBuilder params = new StringBuilder();
			for (Entry<String, String> entry : parameters.entrySet()) {
				if (params.length() > 0) {
					params.append('&');
				}
				String key = encode(entry.getKey());
				String value = encode(entry.getValue());
				params.append(key).append('=').append(value);
			}

			if (params.length() > 0) {
				viewId += "?" + params.toString();
			}
		}

		return viewId;
	}

	/**
	 * Returns the view ID that can be used for navigation to the given saved document.
	 * @param savedDocumentId
	 * @return view ID
	 */
	public static String getViewId(long savedDocumentId) {
		return DocumentView.ID + "/" + savedDocumentId;
	}

	/**
	 * Delimiters in view parameters need to be escaped so that the parameters can be parsed unambiguously. 
	 * @param rawString the string, not null.
	 * @return encoded string
	 */
	private static String encode(String rawString) {
		try {
			return URLEncoder.encode(rawString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Decodes strings encoded by {@link #encode(String)}.
	 * @param encodedString
	 * @return decoded string
	 */
	private static String decode(String encodedString) {
		try {
			return URLDecoder.decode(encodedString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
