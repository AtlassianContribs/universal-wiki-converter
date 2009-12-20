package com.atlassian.uwc.ui.guisettings;

import java.awt.Component;
import java.util.HashMap;

import javax.swing.JTextField;

/**
 * GuiDisabler for the Sharepoint Converter
 */
public class SharepointGuiDisabler extends DefaultGuiDisabler {
	/**
	 * component that's going to be disabled
	 */
	Component spacekey;
	/**
	 * contents of spacekey component before its disabled.
	 * Used to repopulate the spacekey when it's enabled again.
	 */
	static String oldkey = "";
	
	public void converterSelected() {
		Component spacekey = getSpaceKeyComponent();
		JTextField spaceText = (JTextField) spacekey;
		oldkey = spaceText.getText();
		spaceText.setText(" "); //invisible but not empty - see HasAllConverterSettingsListener
		spacekey.setEnabled(false);
		this.feedback.updateFeedback("Disabling Spacekey Textfield. Sharepoint Converter will identify spacekeys automatically.\n");
	}

	public void converterUnselected() {
		Component spacekey = getSpaceKeyComponent();
		spacekey.setEnabled(true);
		JTextField spaceText = (JTextField) spacekey;
		spaceText.setText(oldkey);
		oldkey = "";
		this.feedback.updateFeedback("Enabling Spacekey Textfield.\n");
	}
	

	/**
	 * gets the spacekey component
	 * @return
	 */
	private Component getSpaceKeyComponent() {
		if (this.spacekey == null) {
			HashMap<String, Component> components = this.gui.getConversionSettingsComponents();
			this.spacekey = components.get("space");
		}
		return this.spacekey;
	}
}
