package com.atlassian.uwc.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import com.atlassian.uwc.hierarchies.HierarchyBuilder;
import com.atlassian.uwc.ui.FeedbackWindow;
import com.atlassian.uwc.ui.UWCForm3;
import com.atlassian.uwc.ui.guisettings.GuiDisabler;

/**
 * Provides a way to disable gui elements based on which converter has currently
 * been selected. (Example: When the Sharepoint Converter is selected, we disable
 * the spacekey element, as the spacekeys will be chosen by the Sharepoint wiki names.)
 */
public class GuiDisablingListener implements ActionListener {

	private static final String DEFAULT_GUI_DISABLER = "com.atlassian.uwc.ui.guisettings.DefaultGuiDisabler";
	private static final String GUISETTINGS_PROPERTIES_FILENAME = "guisettings.properties";
	private JComboBox wikitypes;
	private String propsDir;
	private FeedbackWindow feedbackWindow;
	private UWCForm3 gui;
	String lastwiki;
	Logger log = Logger.getLogger(this.getClass());
	
	
	public GuiDisablingListener(
			JComboBox wikitypes, 
			String propsDir, 
			FeedbackWindow feedbackWindow, 
			UWCForm3 gui) {
		this.wikitypes = wikitypes;
		this.propsDir = propsDir;
		this.feedbackWindow = feedbackWindow;
		this.gui = gui;
	}

	public void actionPerformed(ActionEvent event) {
		//get the wikitype
		String wikitype = getCurrentWikitype();
		//load the guisettings props file
		Properties props = getGuiSettingsProperties();
		GuiDisabler disabler = getDisabler(wikitype, props);
		if (lastwiki != null) { //undo previous disabler's work
			GuiDisabler lastDisabler = getDisabler(lastwiki, props);
			if (lastDisabler != null)
				lastDisabler.converterUnselected();
		}
		if (disabler != null) 
			disabler.converterSelected(); //use the disabler
		else {
			String error = "Could not create disabler. Ignoring.\n";
			log.warn(error);
			this.feedbackWindow.updateFeedback(error);
		}
		lastwiki = wikitype;
	}

	/**
	 * instantiates the disabler for the given wikitype, using the
	 * properties from the given props
	 * @param wikitype
	 * @param props
	 * @return
	 */
	private GuiDisabler getDisabler(String wikitype, Properties props) {
		//get the class for the given wikitype
		String classname = getGuiDisablerClassname(wikitype, props);
		//instantiate the class for the given wikitype, passing it necessary fields
		GuiDisabler disabler = instantiateGuiDisabler(classname);
		return disabler;
	}
	

	/**
	 * @return the wikitype represented by this.wikitypes
	 */
	/**
	 * @return currently selected wikitype
	 */
	protected String getCurrentWikitype() {
		String selection = (String) wikitypes.getSelectedItem();
		return selection;
	}
	
	/**
	 * @return properties representing contents of guisettings.properties file
	 */
	private Properties getGuiSettingsProperties() {
		String propFilename = this.propsDir + File.separator + GUISETTINGS_PROPERTIES_FILENAME;
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propFilename));
		} catch (IOException e) {
			log.error("Could not load properties: " + propFilename);
		}
		return properties;
	}

	/**
	 * @param wikitype desired wikitype. This will be used as a key to the props file.
	 * @param props properties from guisettings.properties file.
	 * @return class associated with wikitype, from props, or if no
	 * such class is defined, the default one
	 */
	private String getGuiDisablerClassname(String wikitype, Properties props) {
		String classname = props.getProperty(wikitype);
		if (classname == null) classname = DEFAULT_GUI_DISABLER;
		return classname;
	}

	/**
	 * instantiates the disabler class using the given classname,
	 * or the default disabler if the given classname doesn't work
	 * @param classname full name of a class implementing GuiDisabler.
	 * @return GuiDisabler instance or null, if problems were encountered
	 */
	private GuiDisabler instantiateGuiDisabler(String classname) {
		Class c = null;
		String error = null;
		try {
			c = Class.forName(classname);
		} catch (ClassNotFoundException e) {
			error = "Class '" + classname + "' does not exist. Will use default disabler.\n";
			this.feedbackWindow.updateFeedback(error);
			log.error(error);
			classname = DEFAULT_GUI_DISABLER;
			try {
				c = Class.forName(classname);
			} catch (ClassNotFoundException e1) {
				error = "Error while creating default gui disabler class.\n";
				log.error(error);
				this.feedbackWindow.updateFeedback(error);
				e1.printStackTrace();
				return null;
			}
		}
		GuiDisabler disabler = null;
		try {
			disabler = (GuiDisabler) c.newInstance();
			disabler.setGui(this.gui);
			disabler.setFeedbackWindow(this.feedbackWindow);
		} catch (InstantiationException e) {
			error = "Error while instantiating gui disabler class.\n";
			log.error(error);
			this.feedbackWindow.updateFeedback(error);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			error = "Error while accessing gui disabler class.\n";
			log.error(error);
			this.feedbackWindow.updateFeedback(error);
			e.printStackTrace();
		}
		return disabler;
	}


}
