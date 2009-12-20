package com.atlassian.uwc.ui.guisettings;

import com.atlassian.uwc.ui.FeedbackWindow;
import com.atlassian.uwc.ui.UWCForm3;


/**
 * Interface used to disable gui elements on a per wiki basis
 */
public interface GuiDisabler {

	/**
	 * to be run when the converter is selected
	 */
	public void converterSelected();
	
	/**
	 * to be run when the converter is unselected
	 */
	public void converterUnselected();
	
	/**
	 * sets the gui parameter
	 * @param gui
	 */
	public void setGui(UWCForm3 gui);
	
	/**
	 * sets the feedback window parameter
	 * @param feedback
	 */
	public void setFeedbackWindow(FeedbackWindow feedback);
}
