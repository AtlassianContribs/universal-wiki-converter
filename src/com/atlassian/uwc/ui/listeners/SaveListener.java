package com.atlassian.uwc.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.FeedbackWindow;
import com.atlassian.uwc.ui.UWCGuiModel;
import com.atlassian.uwc.ui.UWCUserSettings;
import com.atlassian.uwc.ui.UWCUserSettings.Setting;

/**
 * saves settings on event triggers
 */
public class SaveListener implements ActionListener, FocusListener, FeedbackHandler, Observer {

	private static final String PATH_DELIMITER = "::";

	public static final String DEFAULT_COMMAND = "calling save listener action";

	Logger log = Logger.getLogger(this.getClass());

	private static final String COMMAND_BASE = "Saving:";
	
	/**
	 * type of ui element
	 */
	public enum Component {
		UNKNOWN,
		COMBOBOX,
		TEXTFIELD,
		PASSWORD, 
		PAGES,
	}
	
	private JComponent component = null;
	private Component type = Component.UNKNOWN;
	private UWCUserSettings.Setting setting;
	private UWCGuiModel model;

	private FeedbackWindow feedbackWindow;

	/**
	 * instantiates the object,
	 * and notes the component type as COMBOBOX
	 * @param component
	 * @param model
	 * @param setting
	 * @param feedbackWindow
	 */
	public SaveListener(JComboBox component, UWCGuiModel model, Setting setting, FeedbackWindow feedbackWindow) {
		this.component = component;
		this.setting = setting;
		this.model = model;
		this.feedbackWindow = feedbackWindow;
		
		this.type = Component.COMBOBOX;
	}

	/**
	 * instantiates the object,
	 * and notes the component type as TEXTFIELD
	 * @param component
	 * @param model
	 * @param setting
	 * @param feedbackWindow
	 */
	public SaveListener(JTextField component, UWCGuiModel model, Setting setting, FeedbackWindow feedbackWindow) {
		this.component = component;
		this.setting = setting;
		this.model = model;
		this.feedbackWindow = feedbackWindow;
		
		this.type = Component.TEXTFIELD;
	}


	/**
	 * instantiates the object,
	 * and notes the component type as PASSWORD
	 * @param component
	 * @param model
	 * @param setting
	 * @param feedbackWindow
	 */
	public SaveListener(JPasswordField component, UWCGuiModel model, Setting setting, FeedbackWindow feedbackWindow) {
		this.component = component;
		this.setting = setting;
		this.model = model;
		this.feedbackWindow = feedbackWindow;
		
		this.type = Component.PASSWORD;
	}
	
	public SaveListener(JList component, UWCGuiModel model, Setting setting, FeedbackWindow feedbackWindow) {
		this.component = component;
		this.setting = setting;
		this.model = model;
		this.feedbackWindow = feedbackWindow;
		
		this.type = Component.PAGES;
	}

	/**
	 * does one of two things:
	 * (2) identifies that the event has all the necessary info for saving, and saves the setting
	 * (1) retriggers the event with additional necessary info saved in the command
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		if (command.startsWith(COMMAND_BASE)) { //re-sent event with useful info
			saveComponent(this.setting, command);
		}
		else { //original event
			String newCommand = COMMAND_BASE;
			switch(type) {
			case COMBOBOX:
				newCommand += ((JComboBox)component).getSelectedItem();
				break;
			case TEXTFIELD:
				newCommand += ((JTextField)component).getText();
				break;
			case PASSWORD:
				newCommand += getPasswordData((JPasswordField)component);
				break;
			case PAGES:
				newCommand += getPagesString(this.model.getPageNames());
				break;
			default:
				log.error("Component Type Unknown! Could not save.");
				return;
			}
			ActionEvent newEvent = new ActionEvent(
					event.getSource(),
					event.getID(),
					newCommand, 			//this is the part that's different
					event.getModifiers());
			this.actionPerformed(newEvent);
		}
		
	}
	
	/**
	 * @param component
	 * @return the password saved in the given component as a String
	 */
	public static String getPasswordData(JPasswordField component) {
		char[] passCh = component.getPassword();
		return String.copyValueOf(passCh);
	}
	
	private String getPagesString(Vector<String> pages) {
		String pagesString = "";
		for (int i = 0; i < pages.size(); i++) {
			String page = (String) pages.get(i);
			if (i > 0) pagesString += PATH_DELIMITER;
			pagesString += page;
		}
		return pagesString;
	}

	/**
	 * saves the setting by parsing the given command
	 * @param setting
	 * @param command should be COMMAND_BASE + the value of the setting
	 */
	private void saveComponent(Setting setting, String command) {
		String value = command.replaceFirst(COMMAND_BASE, ""); //get rid of header

		if (!valid(setting, value)) {
			revert();
			String instructions = getSettingSpecificInstructions(setting);
			String message = "Value '" + value + "'" +
					" for Setting '" + setting.toString() + "'" +
					" is invalid. " +
					instructions;
			this.feedbackWindow.launch();
			this.feedbackWindow.updateFeedback(message);
			log.error(message);
		}
		
		if (this.model == null) {
			log.error("Could not save. Model is null.");
			return;
		}

		//log it but hide passwords
		String printVal = value;
		if (setting == Setting.PASSWORD)
			printVal = "*******";
		log.debug("Saving Setting '" +setting + "' = '" + printVal +"'");
		
		//save it
		this.model.saveSetting(setting, value);
	}
	
	static String validAttachment = "\\d+[BKMG]";
	static Pattern validAttachmentPattern = Pattern.compile(validAttachment, Pattern.CASE_INSENSITIVE);

	/**
	 * validates the value for the given setting
	 * @param setting
	 * @param value
	 * @return true if value is allowed
	 */
	public static boolean valid(Setting setting, String value) {
		switch (setting) {
		case ATTACHMENT_SIZE:
			if (UWCUserSettings.DEFAULT_ATTACHMENT_SIZE.equals(value)) return true;
			Matcher validAttachmentFinder = validAttachmentPattern.matcher(value);
			return validAttachmentFinder.matches();
		case URL:
			return (UWCUserSettings.isValid(setting, value));
		default:
			return true;
		}
	}

	/**
	 * getter
	 * @return current setting
	 */
	public Setting getSetting() {
		return this.setting;
	}

	/**
	 * retrieves use instructions appropriate for the given setting
	 * @param setting
	 * @return use instructions
	 */
	public static String getSettingSpecificInstructions(Setting setting) {
		switch (setting) {
		case ATTACHMENT_SIZE:
			return "Value must be in this format: number[BKMG]. For example: 15M";
		case URL:
			return "Value must be in this format: [https?://]something.com[:port][/contextpath]\n";
		}
		return "";
	}
	
	/**
	 * reverts any changes to the component to the previous value 
	 */
	private void revert() {
		switch (this.type) {
		case TEXTFIELD:
			JTextField textfield = (JTextField) component;
			textfield.setText(this.model.getSetting(this.setting));
		}
	}
	
	/**
	 * Identifies the current setting as "unsaved".
	 * Note: Saving occurs after the focus is lost. (See this.focusLost.) 
	 * So we keep note of which setting the user is currently focused on
	 * so that if we start an operation (conversion, export, etc), we know which
	 * setting still needs to be saved before we actually do the op.
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent arg0) {
		this.model.setUnsaved(this.setting);
	}

	/**
	 * Saves this component when the focus is lost.
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent event) {
		//save when the focus is lost
		if (event.getID() == FocusEvent.FOCUS_LOST) {
			actionPerformed(new ActionEvent(
					event.getSource(), event.getID(), DEFAULT_COMMAND));
		}
	}

	public void update(Observable obs, Object obj) {
		//save when pages are added (when PageHandler.updateUI notifies observers) 
		if (obs instanceof AddPagesListener || 
				obs instanceof SelectFileDropTargetListener ||
				obs instanceof RemovePagesListener) {
			actionPerformed(new ActionEvent(obs, 1, DEFAULT_COMMAND));
		}
		else {
			log.error("We haven't implemented use of this observer with the SaveListener: " + obs.getClass().toString());
		}
	}

}
