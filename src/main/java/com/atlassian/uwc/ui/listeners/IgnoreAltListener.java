package com.atlassian.uwc.ui.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

/**
 * Hides Alt-Based Key inputs to the textfield. Otherwise, using Alt 
 * to communicate with the Menu results in additional ugly unintended characters
 * in the textfield
 */
public class IgnoreAltListener implements KeyListener {

	boolean menutoggle = false;
	/**
	 * the textfield we are shielding from alt-triggered text characters
	 */
	private JTextField component;
	
	public IgnoreAltListener(JTextField component) {
		this.component = component;
	}
	
	/**
	 * sets the textfield to uneditable when the alt character is being pressed
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent event) {
		int keyCode = event.getKeyCode();
		this.component.setEditable(!menutoggle);
		if (keyCode == KeyEvent.VK_ALT) {
			menutoggle = true;
		}
	}

	/** 
	 * sets the textfield to editable when any key is released
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent event) {
		menutoggle = false;
		this.component.setEditable(true);
	}

	/* Key events we're not using */
	public void keyTyped(KeyEvent event) {/*Not using this method*/}

}
