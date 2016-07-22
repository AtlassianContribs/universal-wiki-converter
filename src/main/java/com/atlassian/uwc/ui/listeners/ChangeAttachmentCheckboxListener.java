package com.atlassian.uwc.ui.listeners;

import javax.swing.JTextField;
import java.awt.event.ItemEvent;
import com.atlassian.uwc.ui.UWCGuiModel;
import com.atlassian.uwc.ui.UWCUserSettings;
import com.atlassian.uwc.ui.UWCUserSettings.Setting;

/**
 * saves the setting of the Attachment Size option when it is changed.
 */
public class ChangeAttachmentCheckboxListener extends ChangeCheckboxListener {

	
	private JTextField textfield;

	public ChangeAttachmentCheckboxListener(UWCGuiModel model, Setting setting, JTextField textfield) {
		super(model, setting);
		this.textfield = textfield;
	}

	/**
	 * saves the appropriate max value based on the given selected
	 * criteria
	 * @param selected ItemEvent.SELECTED or ItemEvent.DESELECTED
	 */
	protected void saveFromText(int selected) { 
		String value = this.textfield.getText();
		UWCGuiModel model = getModel();
		switch (selected) {
		case ItemEvent.SELECTED:
			model.saveSetting(Setting.ATTACHMENT_SIZE, value);
			break;
		case ItemEvent.DESELECTED:
			model.saveSetting(Setting.ATTACHMENT_SIZE, UWCUserSettings.DEFAULT_ATTACHMENT_SIZE);
			break;
		}
	}
}
