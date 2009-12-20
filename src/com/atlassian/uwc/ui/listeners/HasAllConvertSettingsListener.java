package com.atlassian.uwc.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.atlassian.uwc.ui.UWCForm3;

/**
 * controls the usability of a ui element, by checking to see
 * that all necessary data has been provided by the user, every time the element
 * that this listener listens to has changed.
 * 
 * To use:
 * <ol>
 * <li>instantiate the object with The Form containing the relevent elements
 * <br/>Example:
 * <pre>
 * public class UWCForm3 { 
 * ...
 * this.hasAllConvertSettingsListener = new HasAllConvertSettingsListener(this);
 * ...
 * } 
 * </pre>
 * </li>
 * <li>register the ui elements whose enabled state will be controlled by this listener. 
 * <br/>Example: <pre>this.hasAllConvertSettingsListener.registerComponent(getJButtonConvert());</pre></li>
 * <li>Add the instantiated object as an actionlistener, focuslistener, or observer 
 * to any ui element that should trigger a check-in to
 * see if the registered components should be enabled or disabled.
 * <br/>Examples: 
 * <pre>this.removePagesListener.addObserver(getHasAllListener());</pre>
 * <pre>jTextFieldSpace.addFocusListener(getHasAllListener());</pre>
 * <pre>jComboBox_WikiType.addActionListener(getHasAllListener());</pre>
 * </li>
 * <li>For observables, notify observers as appropriate:
 * <br/>Example:
 * <pre>//do something
        setChanged();
		notifyObservers();
 * </pre>
 * </li>
 * </ol>
 */
public class HasAllConvertSettingsListener implements ActionListener, FocusListener, DocumentListener, Observer {

	private UWCForm3 form;
	/**
	 * set of ui elements that will be disabled or enabled based on the
	 * existence of all necessary data
	 */
	private Set<JComponent> components; //set preserves uniqueness
	
	public HasAllConvertSettingsListener(UWCForm3 form) {
		this.form = form;
		components = new HashSet<JComponent>();
	}
	
	/**
	 * registers the given component with the listener. registered components will be
	 * enabled or disabled based on the settings necessary to convert.
	 * @param component
	 */
	public void registerComponent(JComponent component) {
		components.add(component);
	}
	/**
	 * @see HasAllConvertSettingsListener#actionPerformed()
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		actionPerformed();
	}

	/**
	 * checks to see if all conditions have been met.
	 * If so, enables each component from this.components.
	 * If not, disables each component.
	 */
	public void actionPerformed() {
		boolean hasAllSettings = this.form.hasSetAllConverterSettings();
		for (JComponent component : this.components) {
			component.setEnabled(hasAllSettings); 
		}
	}

	
	/**
	 * trigger this actionPerformed every time the focus is lost.
	 * @see HasAllConvertSettingsListener#actionPerformed()
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent event) {
		if (event.getID() == FocusEvent.FOCUS_LOST) {
			actionPerformed(new ActionEvent(
					event.getSource(), event.getID(), ""));
		}
	}

	/** 
	 * trigger this actionPerformed every time a change has been observed
	 * @see HasAllConvertSettingsListener#actionPerformed()
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		actionPerformed();
	}
	
	public void insertUpdate(DocumentEvent e) {
		actionPerformed();
	}
	
	public void removeUpdate(DocumentEvent e) {
		actionPerformed();
	}
	
	/* Listener methods we're not using */
	
	public void focusGained(FocusEvent arg0) { /*Not using this method*/ }

	public void changedUpdate(DocumentEvent e) { /*Not using this method*/ }

}
