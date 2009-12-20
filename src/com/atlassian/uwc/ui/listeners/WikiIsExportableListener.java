package com.atlassian.uwc.ui.listeners;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.UWCGuiModel;

/**
 * handles changing the export button's usability based on the
 * currently selected wikitype
 */
public class WikiIsExportableListener extends ExportHandler implements ActionListener {

	Logger log = Logger.getLogger(this.getClass());
	private JButton exportButton;
	private JLabel exportAdvisory;
	private Changeable type;
	private JMenuItem exportMenu;
	private enum Changeable {
		BUTTON,
		LABEL, 
		MENU
	}
	
	/**
	 * for use with JButtons
	 * @param wikitypes combobox whose change will initiate this action
	 * @param exportButton this is the object that will be changed
	 * @param model used to get the valid export types
	 * @param dir directory that export properties files live in
	 */
	public WikiIsExportableListener(JComboBox wikitypes, JButton exportButton, UWCGuiModel model, String dir) {
		this.wikitypes = wikitypes;
		this.model = model;
		this.dir = dir;
		this.exportButton = exportButton;
		this.type = Changeable.BUTTON;
	}

	/**
	 * for use with JLabels
	 * @param wikitypes combobox whose change will initiate this action
	 * @param exportAdvisory this is the object that will be changed
	 * @param model used to get the valid export types 
	 * @param dir directory that export properties files live in
	 */
	public WikiIsExportableListener(JComboBox wikitypes, JLabel exportAdvisory, UWCGuiModel model, String dir) {
		this.wikitypes = wikitypes;
		this.model = model;
		this.dir = dir;
		this.exportAdvisory = exportAdvisory;
		this.type = Changeable.LABEL;
	}

	/**
	 * for use with JMenuItems
	 * @param wikitypes combobox whose changes will initiate this action
	 * @param exportMenu this is the object that will be changed
	 * @param model used to get the valid export types
	 * @param dir directory that export properties files live in
	 */
	public WikiIsExportableListener(JComboBox wikitypes, JMenuItem exportMenu, UWCGuiModel model, String dir) {
		this.wikitypes = wikitypes;
		this.model = model;
		this.dir = dir;
		this.exportMenu = exportMenu;
		this.type = Changeable.MENU;
	}

	/**
	 * checks to see if the wiki is exportable, and propogate changes
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		boolean isExportable = this.isExportable();
		if (type == Changeable.BUTTON)
			handleExportButton(isExportable);
		else if (type == Changeable.LABEL)
			handleExportLabel(isExportable);
		else if (type == Changeable.MENU)
			handleExportMenu(isExportable);
	}
	
	/**
	 * enable or disable export button 
	 * @param enabled if true, enable button
	 */
	private void handleExportButton(boolean enabled) {
		if (exportButton == null)
			log.error("exportButton is null! Cannot change enable setting.");
		else
			exportButton.setEnabled(enabled);
	}
	
	/**
	 * make visible or invisible export label
	 * @param visible if true, make the label visible
	 */
	private void handleExportLabel(boolean visible) {
		if (exportAdvisory == null) 
			log.error("exportAdvisory label is null! Cannot change enable setting.");
		else
			exportAdvisory.setVisible(visible); //FIXME is this what I want to do?
		
	}
	
	/**
	 * enable or disable export menu
	 * @param enabled if true, enable the menu
	 */
	private void handleExportMenu(boolean enabled) {
		if (exportMenu == null) 
			log.error("exportMenu is null! Cannot change enable setting.");
		else
			exportMenu.setEnabled(enabled);
	}
}
