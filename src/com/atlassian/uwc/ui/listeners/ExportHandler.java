package com.atlassian.uwc.ui.listeners;

import java.util.Vector;

import javax.swing.JComboBox;

import com.atlassian.uwc.ui.UWCGuiModel;

/**
 * parent class used by export focused listeners
 */
public class ExportHandler {

	protected JComboBox wikitypes;
	protected UWCGuiModel model;
	protected String dir;

	/**
	 * @return true if the wikitype represented by this.wikitypes is exportable
	 */
	protected boolean isExportable() {
		if (fieldsAreNull())
			throw new IllegalStateException("Must set wikitypes, model, and directory before calling isExportable");
		String selection = getCurrentWikitype();
		Vector<String> exportableWikis = model.getExportTypes(this.dir);
		boolean isExportable = exportableWikis.contains(selection);
		return isExportable;
	}

	/**
	 * @return the wikitype represented by this.wikitypes
	 */
	protected String getCurrentWikitype() {
		String selection = (String) wikitypes.getSelectedItem();
		return selection;
	}

	/**
	 * @return true if any necessary fields are null
	 */
	private boolean fieldsAreNull() {
		return this.wikitypes == null || this.model == null || this.dir == null;
	}
	
	/**
	 * @return the directory where exporter.xxx.properties can be found 
	 */
	protected String getDirectory() {
		return this.dir;
	}

}
