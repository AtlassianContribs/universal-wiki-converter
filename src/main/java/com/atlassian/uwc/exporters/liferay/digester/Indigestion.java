package com.atlassian.uwc.exporters.liferay.digester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.digester3.Digester;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

public class Indigestion {
	private static Logger _log = Logger.getLogger(Indigestion.class);

	public Manifest readManifestXml(String directory) throws IOException, SAXException {
		Manifest retval = null;
		File file = new File(directory, "manifest.xml");

		Digester digester = new Digester();
		digester.setValidating(false);

		digester.addObjectCreate("root", Manifest.class);
		digester.addObjectCreate("root/portlet", ManifestPortlet.class);
		digester.addSetProperties("root/portlet");
		digester.addSetNext("root/portlet", "setPortlet");

		retval = (Manifest) digester.parse(file);

		_log.debug(retval.toString());

		return retval;
	}

//	@formatter:off
/*
	<portlet portlet-id="36" root-portlet-id="36" old-plid="113518" scope-layout-type="" scope-layout-uuid="">
		<portlet-data path="/groups/113333/portlets/36/113518/portlet-data.xml"/>
*/
//	@formatter:on
	public Portlet readPortletXml(File file) throws IOException, SAXException {
		Portlet retval = null;

		Digester digester = new Digester();
		digester.setValidating(false);

		digester.addObjectCreate("portlet", Portlet.class);
		digester.addObjectCreate("portlet/portlet-data", PortletData.class);
		digester.addSetProperties("portlet/portlet-data");
		digester.addSetNext("portlet/portlet-data", "setPortlet");

		retval = (Portlet) digester.parse(file);

		_log.debug(retval.toString());

		return retval;
	}

//	@formatter:off
/*
<wiki-data group-id="113333">
	<pages>
		<page image-path="/groups/113333/portlets/36/page/028b08d8-1a74-4542-a3ec-9e429374f36c/1.0/" path="/groups/113333/portlets/36/pages/320552.xml"/>
		<page image-path="/groups/113333/portlets/36/page/0f07a009-8107-49bb-8612-48fb7b52c37a/1.0/" path="/groups/113333/portlets/36/pages/347437.xml">
			<attachment name="java-proxy-settings.jpg" bin-path="/groups/113333/portlets/36/bin/347437/java-proxy-settings.jpg"/>
		</page>
*/
//	@formatter:on
	public WikiData readPortletDataXml(File file) throws IOException, SAXException {
		WikiData retval = null;

		Digester digester = new Digester();
		digester.setValidating(false);

		digester.addObjectCreate("wiki-data", WikiData.class);
		//element <pages>
		digester.addObjectCreate("wiki-data/pages", Pages.class);
		digester.addSetProperties("wiki-data/pages");
		digester.addSetNext("wiki-data/pages", "setPages");

		//element <page>
		digester.addObjectCreate("wiki-data/pages/page", Page.class);
		digester.addSetProperties("wiki-data/pages/page");
		digester.addSetNext("wiki-data/pages/page", "setPage");

		//element <attachment>
		digester.addObjectCreate("wiki-data/pages/page/attachment", Attachment.class);
		digester.addSetProperties("wiki-data/pages/page/attachment", "bin-path", "binPath");
		digester.addSetNext("wiki-data/pages/page/attachment", "setAttachment");

		retval = (WikiData) digester.parse(file);

		_log.debug(retval.toString());

		return retval;
	}

/* @formatter:off
	<WikiPage>
	<__new>false</__new>
	...
*/
//	@formatter:on
	public WikiPage readWikiPageXml(File file, ArrayList<Attachment> attachments) throws IOException, SAXException {
		WikiPage retval = null;

		Digester digester = new Digester();
		digester.setValidating(false);

		digester.addObjectCreate("WikiPage", WikiPage.class);
		digester.addBeanPropertySetter("WikiPage/__title");
		digester.addBeanPropertySetter("WikiPage/__parentTitle");
		digester.addBeanPropertySetter("WikiPage/__redirectTitle");
		digester.addBeanPropertySetter("WikiPage/__format");
		digester.addBeanPropertySetter("WikiPage/__version");
		digester.addBeanPropertySetter("WikiPage/__content");

		retval = (WikiPage) digester.parse(file);		
		retval.setAttachments(attachments);
		retval.setFile(file);

		// _log.debug(retval.toString());

		return retval;
	}

}
