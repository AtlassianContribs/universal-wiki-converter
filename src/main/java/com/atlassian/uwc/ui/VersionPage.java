package com.atlassian.uwc.ui;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

public class VersionPage extends Page {

	private Page parent;
	Logger log = Logger.getLogger(this.getClass());
	public VersionPage(File file, String path) {
		super(file, path);
	}
	
	public VersionPage(File file) {
		super(file);
	}
	
	public void addAncestor(VersionPage ancestor) {
		throw new IllegalArgumentException("VersionPage object does not accept ancestors");
	}
	
	public Vector<VersionPage> getAncestors() {
		return null;
	}
	
	public void setParent(Page page) {
		this.parent = page;
	}
	
	public Page getParent() {
		return this.parent;
	}
	
	@Override
	public int compareTo(Object o) {
		if (o instanceof Page) {
			Page p = (Page) o;
			if (this.getTimestamp() != null) { 
				return this.getTimestamp().compareTo(p.getTimestamp());
			}
			return super.compareTo(p);
		}
		else 
			return super.compareTo(o);
	}

}
