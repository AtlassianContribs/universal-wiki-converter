package com.atlassian.uwc.ui.guisettings;

import java.awt.Component;
import java.util.HashMap;

import com.atlassian.uwc.ui.FeedbackWindow;
import com.atlassian.uwc.ui.UWCForm3;

/**
 * Default GuiDisabler. It disables no ui elements.
 * All other GuiDisablers should extend this object.
 */
public class DefaultGuiDisabler implements GuiDisabler {

	protected FeedbackWindow feedback = null;
	protected UWCForm3 gui = null;

	public DefaultGuiDisabler() {
		//do nothing here - needed to use Class.newInstance
	}
	
	public DefaultGuiDisabler(UWCForm3 gui, FeedbackWindow feedback) {
		this.gui = gui;
		this.feedback = feedback;
	}
	
	public void converterSelected() {
		//Do nothing - This is the default, so we want everything to stay enabled
	}
	
	public void converterUnselected() {
		reset();
	}

	/**
	 * sets conversion components to be enabled
	 */
	public void reset() {
		//set everything back to normal
		HashMap<String, Component> components = gui.getConversionSettingsComponents();
		for (String key: components.keySet()) {
			Component component = components.get(key);
			component.setEnabled(true);
		}
	}

	/* Setters */
	public void setGui(UWCForm3 gui) {
		this.gui = gui;
	}

	public void setFeedbackWindow(FeedbackWindow feedback) {
		this.feedback = feedback;
	}
}
