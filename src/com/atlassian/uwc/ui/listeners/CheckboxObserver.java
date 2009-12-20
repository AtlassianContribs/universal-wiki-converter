package com.atlassian.uwc.ui.listeners;

import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractButton;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.UWCForm3;
import com.atlassian.uwc.ui.UWCUserSettings;
import com.atlassian.uwc.ui.UWCUserSettings.Setting;

/**
 * listens for changes to a checkbox, and loads the associated setting 
 * into the desired element
 */
public class CheckboxObserver implements Observer {

	Logger log = Logger.getLogger(this.getClass());
	private UWCForm3 form;
	/**
	 * ui element that will be affected by observed changes
	 */
	private AbstractButton uiElement;
	/**
	 * setting representing the value that should be loaded
	 */
	private Setting setting;

	public CheckboxObserver(UWCForm3 form, AbstractButton uiElement, Setting setting) {
		this.form = form;
		this.uiElement = uiElement;
		this.setting = setting;
	}

	/**
	 * loads the setting.
	 * Note: this is called when an observable calls notifyObservers 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object obj) {
		switch (this.setting) {
		case ATTACHMENT_SIZE:
			form.loadBooleanSettingFromString(this.uiElement, this.setting, UWCUserSettings.DEFAULT_ATTACHMENT_SIZE);
			break;
		default:
			form.loadSetting(this.uiElement, this.setting);
			break;
		}
	}

}
