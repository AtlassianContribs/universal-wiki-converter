package com.atlassian.uwc.exporters.mindtouch;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MindtouchPage {
	public String id;
	public String title;
	public String content;
	public String tags;
	public String comments;
	public String attachments;
	private Vector<MindtouchPage> subpages;

	public MindtouchPage() {
		
	}
	
 	public MindtouchPage(String id, String title, String content, String tags, String comments, String attachments) {
 		this.id = id;
 		this.title = title;
 		this.content = content;
 		this.tags = tags;
 		this.comments = comments;
 		this.attachments = attachments;
 	}
	
	public Vector<MindtouchPage> getSubpages() {
		if (this.subpages == null)
			this.subpages = new Vector<MindtouchPage>();
		return this.subpages;
	}

	Pattern noatts = Pattern.compile("^<files count=\"0\"");
	public boolean hasAttachments() {
		if (attachments == null) return false;
		Matcher matcher = noatts.matcher(attachments);
		return !matcher.find();
	}

	/**
	 * search pages and its subpages for node containing given title, removes that node,
	 * and attaches that nodes subpages to its parent
	 * @param title node with this page should be removed, and it's subpages added to parents subpages
	 * @param pages set of pages that could contain title node
	 * @return
	 */
	public static Vector<MindtouchPage> removeNode(String title, Vector<MindtouchPage> pages) {
		for (MindtouchPage page : pages) {
			if (page == null) continue;
			if (page.title == null) continue;
			if (page.title.equals(title)) { //remove it!
				Vector<MindtouchPage> branch = page.getSubpages();
				pages.remove(page);
				pages.addAll(branch);
				break;
			}
			pages = removeNode(title, page.getSubpages());
		}
		return pages;
	}

}
