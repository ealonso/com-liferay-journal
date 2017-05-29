/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.journal.demo.data.creator.internal;

import com.liferay.journal.demo.data.creator.JournalArticleDemoDataCreator;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.SAXReaderUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
public abstract class BaseJournalArticleDemoDataCreator
	implements JournalArticleDemoDataCreator {

	public JournalArticle createJournalArticle(
			long userId, long groupId, String title, String description,
			String content)
		throws PortalException {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGroupPermissions(true);
		serviceContext.setAddGuestPermissions(true);
		serviceContext.setScopeGroupId(groupId);

		Locale defaultLocale = LocaleUtil.getSiteDefault();

		Map<Locale, String> titleMap = new HashMap<>();

		titleMap.put(defaultLocale, title);

		Map<Locale, String> descriptionMap = new HashMap<>();

		descriptionMap.put(defaultLocale, description);

		String xml = _getStructuredContent("name", content, defaultLocale);

		JournalArticle journalArticle = journalArticleLocalService.addArticle(
			userId, groupId, 0, titleMap, descriptionMap, xml,
			"BASIC-WEB-CONTENT", "BASIC_WEB_CONTENT", serviceContext);

		entryIds.add(journalArticle.getId());

		return journalArticle;
	}

	@Override
	public void delete() throws PortalException {
		for (long entryId : entryIds) {
			journalArticleLocalService.deleteJournalArticle(entryId);
			entryIds.remove(entryId);
		}
	}

	@Reference(unbind = "-")
	protected void setJournalArticleLocalService(
		JournalArticleLocalService journalArticleLocalService) {

		this.journalArticleLocalService = journalArticleLocalService;
	}

	protected final List<Long> entryIds = new CopyOnWriteArrayList<>();
	protected JournalArticleLocalService journalArticleLocalService;

	private Document _createDocumentContent(String locale) {
		Document document = SAXReaderUtil.createDocument();

		Element rootElement = document.addElement("root");

		rootElement.addAttribute("available-locales", locale);
		rootElement.addAttribute("default-locale", locale);
		rootElement.addElement("request");

		return document;
	}

	private String _getStructuredContent(
		String name, String contents, Locale locale) {

		Document document = _createDocumentContent(locale.toString());

		Element rootElement = document.getRootElement();

		Element dynamicElementElement = rootElement.addElement(
			"dynamic-element");

		dynamicElementElement.addAttribute("index-type", "keyword");
		dynamicElementElement.addAttribute("name", name);
		dynamicElementElement.addAttribute("type", "text");

		Element element = dynamicElementElement.addElement("dynamic-content");

		element.addAttribute("language-id", LocaleUtil.toLanguageId(locale));
		element.addCDATA(contents);

		return document.asXML();
	}

}