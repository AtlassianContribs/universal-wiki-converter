package com.atlassian.uwc.ui.listeners;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.UWCGuiModel;
import com.atlassian.uwc.ui.UWCUserSettings.Setting;

/**
 * listens for events caused by changes to a checkbox's state
 */
public class ChangeCheckboxListener extends Observable implements ItemListener, FeedbackHandler {

	Logger log = Logger.getLogger(this.getClass());
	/**
	 * Setting that represents the ui element that uses this listener
	 */
	private Setting setting;
	private UWCGuiModel model;

	public ChangeCheckboxListener(UWCGuiModel model, Setting setting) {
		this.model = model;
		this.setting = setting;
	}

	/**
	 * saves the changed associated setting and notifies any observers
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		int selected = e.getStateChange();
		String value = null;
		Integer command = null;

		switch(this.setting) {
		// Attachment Size is special because we maintain the boolean and string values in the same setting
		case ATTACHMENT_SIZE:
			command = selected; //this will be used by the EnableTextFieldObserver
			saveFromText(selected);
			break;
		// we just want to translate the selection into a boolean value
		default: 
			value = getSelectedValue(selected);
			model.saveSetting(setting, value);
			break;
		}

		setChanged();
		notifyObservers(command);
	}

	/**
	 * saves state based on an associated textfield and the given selected criteria
	 * @param selected ItemEvent.SELECTED or ItemEvent.DESELECTED
	 */
	private void saveFromText(int selected) {
		//we have a subclass handle this
		if (this instanceof ChangeAttachmentCheckboxListener) {
			ChangeAttachmentCheckboxListener listener = (ChangeAttachmentCheckboxListener) this;
			listener.saveFromText(selected);
		}
	}

	/**
	 * @param selected ItemEvent.SELECTED or ItemEvent.DESELECTED
	 * @return true if item was selected
	 */
	private String getSelectedValue(int selected) {
		String value = null;
		switch(selected) {

		case ItemEvent.DESELECTED:
			value = "false";
			break;
		
		case ItemEvent.SELECTED:
			value = "true";
			break;
		}
//		log.debug("select = " + value);
		return value;
	}
	
	/**
	 * getter
	 */
	protected UWCGuiModel getModel() {
		return this.model;
	}
}
