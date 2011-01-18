package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;

public class DocDirectoryHierarchy extends FilepathHierarchy {


	private static final String PROP_ATT = "doc-directory-attachments";
	private static final String PROP_ROOT = "doc-directory-root";
	private static final String PROP_TEMPLATE = "doc-directory-template";
	private static final String PROP_EXCLUDE = "doc-directory-exclude";
	
	private static final String DEFAULT_ROOT = "Home";
	private static final String DEFAULT_TEMPLATE = "{attachments}\n";

	private File attdir;
	private String template;
	private FileFilter filter;
	
	private File getAttachmentsDirectory() {
		if (attdir == null) {
			String attdirpath = getProperties().getProperty(PROP_ATT, "");
			attdir = new File(attdirpath);
			if (!attdir.exists()) {
				throw new IllegalArgumentException(PROP_ATT + " does not exist.");
			}
			if (!attdir.isDirectory()) {
				throw new IllegalArgumentException(PROP_ATT + " is not a directory.");
			}
			if (attdir.list().length < 1) {
				throw new IllegalArgumentException(PROP_ATT + " is empty.");
			}
		}
		return attdir;
	}
	
	public HierarchyNode buildHierarchy(Collection<Page> pages) {
		File attachmentsDirectory = getAttachmentsDirectory(); 
		HierarchyNode root = getRootNode();
		log.info("Building Hierarchy.");

		HierarchyNode pen = getPenultimateNode(getRootName(), root);
		
		FileFilter filter = getExcludeFilter();
		File[] files = (filter == null)?attachmentsDirectory.listFiles():
										attachmentsDirectory.listFiles(filter);
		
		log.debug("foreach in attachmentsdirectory: " + attachmentsDirectory.getAbsolutePath());
		for (File child: files) {
			log.debug(".. file: " + child.getPath());
			buildRelationships(child, pen);
		}
		
		return root;
	}
	
	private HierarchyNode getPenultimateNode(String rootname, HierarchyNode root) {
		HierarchyNode pen;
		if (rootname != null && !"".equals(rootname)) {
			log.debug("Page root set to: " + rootname);
			pen = new HierarchyNode();
			pen.setName(rootname);
			root.addChild(pen);
		}
		else pen = root;
		return pen;
	}
	
	protected String getRootName() {//provided by the misc props framework
		return getProperties().getProperty(PROP_ROOT, DEFAULT_ROOT);
	}
	
	private FileFilter getExcludeFilter() {
		if (this.filter == null) {
			final String regex = getProperties().getProperty(PROP_EXCLUDE, null);
			if (regex == null) return null;
			this.filter = new FileFilter() {
				Pattern p = Pattern.compile(regex);
				public boolean accept(File file) {
					String name = file.getName();
					Matcher m = p.matcher(name);
					return !m.matches();
				}
			};
		}
		return this.filter;
	}

	protected void buildRelationships(File file, HierarchyNode root) {
		if (file.isDirectory()) { 
			//create Page and Node
			String name = file.getName();
			Page page = new Page(null);
            page.setName(name);
            page.setOriginalText(getTemplate());
            page.setConvertedText(getTemplate());
            page.setPath(name); //needed by auto-detect spacekeys feature
            HierarchyNode node = new HierarchyNode(page, root);
            
            //recurse
            FileFilter filter = getExcludeFilter();
    		File[] next = (filter == null)?file.listFiles():
    									   file.listFiles(filter);
    		for (File child : next) {
				buildRelationships(child, node);
			}
		}
		else { //assign as attachment
			Page page = root.getPage();
			if (page == null) {
				page = new Page(null);
				page.setName(root.getName());
				page.setPath(root.getName());
				page.setOriginalText(getTemplate());
				page.setConvertedText(getTemplate());
				root.setPage(page);
			}
			page.addAttachment(file);
		}
	}

	private String getTemplate() {
		if (template == null) {
			String templatepath = getProperties().getProperty(PROP_TEMPLATE, "");
			if ("".equals(templatepath)) {
				template = DEFAULT_TEMPLATE;
				return template;
			}
			File templatefile = new File(templatepath);
			if (!templatefile.exists()) {
				log.error("doc-directory-template file does not exist: " + templatepath + "\nUsing default.");
				template = DEFAULT_TEMPLATE;
			}
			if (!templatefile.isFile()) {
				log.error("doc-directory-template file is not a file: " + templatepath + "\nUsing default.");
				template = DEFAULT_TEMPLATE;
			}
			try {
				template = FileUtils.readTextFile(templatefile);
			} catch (IOException e) {
				log.error("Could not read doc-directory-template file: " + templatepath + "\nUsing default.");
				template = DEFAULT_TEMPLATE;
			}
		}
		return template;
	}
	
}
