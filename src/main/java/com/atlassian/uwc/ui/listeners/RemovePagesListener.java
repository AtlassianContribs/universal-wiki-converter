package com.atlassian.uwc.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;

import com.atlassian.uwc.ui.UWCGuiModel;

/**
 * removes pages from the page ui when triggered by an event
 */
public class RemovePagesListener extends PageHandler implements ActionListener {

	public RemovePagesListener(JScrollPane ui, UWCGuiModel model) {
		super(ui, model);
	}

	/**
	 * removes the unwanted pages and updates the ui
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		Vector<String> filenames = removeWikiPages();
		JList pagelist = new JList(filenames);
		this.updateUI(this.ui, pagelist);
	}

	/**
	 * examines the highlighted list of pages
	 * and removes them from the model
	 * @return
	 */
	private Vector<String> removeWikiPages() {
		JList currentList = (JList) this.ui.getViewport().getView();
        Object[] files = currentList.getSelectedValues();
		return this.model.removeWikiPages(files);
	}

}
