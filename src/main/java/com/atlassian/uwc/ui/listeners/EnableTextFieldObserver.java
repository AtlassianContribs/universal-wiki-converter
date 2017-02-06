package com.atlassian.uwc.ui.listeners;

import java.awt.event.ItemEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextField;

import org.apache.log4j.Logger;

/**
 * listens for events that indicate a textfield should be enabled or disabled
 */
public class EnableTextFieldObserver implements Observer {

	private JTextField textfield;
	Logger log = Logger.getLogger(this.getClass());
	/**
	 * registers the given textfield. When the update method is called, this
	 * textfield will be the one affected by any changes.
	 * @param textfield
	 */
	public EnableTextFieldObserver(JTextField textfield) {
		this.textfield = textfield;
	}

	/**
	 * sets the associated textfield enabled or disabled, based on the given obj
	 * @param observable
	 * @param obj ItemEvent.SELECTED or ItemEvent.DESELECTED
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object obj) {
		if (obj instanceof Integer) {
			boolean enabled = false;
			Integer selected = (Integer) obj;
			switch (selected) {
			case ItemEvent.SELECTED:
				enabled = true;
				break;
			case ItemEvent.DESELECTED:
				enabled = false;
			}
			this.textfield.setEnabled(enabled);
		}
	}

}
