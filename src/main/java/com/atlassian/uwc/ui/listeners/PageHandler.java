package com.atlassian.uwc.ui.listeners;

import java.awt.Point;
import java.util.Observable;

import javax.swing.JList;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.UWCGuiModel;
import com.atlassian.uwc.ui.UWCLabel;

/**
 * parent class controlling page object ui, extended by AddPagesListener
 * and RemovePagesListener
 */
public class PageHandler extends Observable {

	
	/**
	 * pane displaying the pages chosen by the user
	 */
	protected JScrollPane ui;
	protected UWCGuiModel model;
	
	Logger log = Logger.getLogger(this.getClass());
	
	public PageHandler(JScrollPane ui, UWCGuiModel model) {
		this.ui = ui;
		this.model = model;
	}

	/**
	 * updates the scrollpane with the given list of pages,
	 * and notifies observers
	 * @param ui
	 * @param pagelist 
	 */
	public void updateUI(JScrollPane ui, JList pagelist) {
		//fix font
		pagelist = setPageListUI(pagelist);
		//drag n drop feature
		JList currentlist = (JList) ui.getViewport().getView();
		pagelist.setDropTarget(currentlist.getDropTarget());
		//update the page list on the scrollpane
		ui.getViewport().setView(pagelist); 
		ui.getViewport().setViewPosition(new Point(500, 0));
		//notify observers like the remove and convert buttons
		setChanged();
		notifyObservers();
	}
	
	/**
	 * sets up any JList ui parameters like font 
	 * @param pagelist
	 * @return
	 */
	private JList setPageListUI(JList pagelist) {
		pagelist.setFont(UWCLabel.getUWCFont());
		return pagelist;
	}
}
