package com.atlassian.uwc.ui.listeners;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.UWCGuiModel;

/**
 * Listener that handles dragging and dropping into the add pages pane.
 * Based heavily on Confluence File Uploader's class SelectFileToUploadStep.
 * See http://confluence.atlassian.com/display/CONFEXT/Confluence+File+Uploader
 */
public class SelectFileDropTargetListener extends PageHandler implements DropTargetListener {
	Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * creates drag and drop listener for add pages elements
	 * @param ui jscrollpane component listing the pages
	 * @param model object controlling the underlying data
	 */
	public SelectFileDropTargetListener(JScrollPane ui, UWCGuiModel model) {
		super(ui, model);
	}
	
	public void dragEnter(DropTargetDragEvent event) {
		highlightUI(true);
		validateDrag(event);
	}

	public void dragExit(DropTargetEvent event) {
		highlightUI(false);
	}

	public void dragOver(DropTargetDragEvent event) {
		validateDrag(event);
	}

	public void drop(DropTargetDropEvent event) {
		 if (!event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
             event.rejectDrop();
             return;
         }
         event.acceptDrop(event.getDropAction());
         try {
        	 List<File> data = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
        	 Vector<String> pages = this.model.addWikiPages((File[])data.toArray());
             JList pagelist = new JList(pages);
             this.updateUI(this.ui, pagelist);
             event.dropComplete(true);
         }
         catch (Exception e) {
        	 event.dropComplete(false);
             log.error("Failed to accept drag n drop", e);
         }
         highlightUI(false);
	}

	public void dropActionChanged(DropTargetDragEvent event) {
		validateDrag(event);
	}

	/* Helper Methods */
	/**
	 * accepts the event if the the data is of flavor javaFileListFlavor, otherwise
	 * rejectes the event
	 * @param event
	 */
	private void validateDrag(DropTargetDragEvent event) {
		if (event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			event.acceptDrag(event.getDropAction());
			return;
		}
		event.rejectDrag();
	}

    /**
     * @param highlight if true, highlights the ui elements. if false,
     * unhighlights them. 
     */
    private void highlightUI(boolean highlight) {
        if (highlight) {
            this.ui.setBorder(BorderFactory.createLineBorder(Color.yellow, 1));
            this.ui.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        else {
            this.ui.setBorder(BorderFactory.createEmptyBorder());
            this.ui.setCursor(Cursor.getDefaultCursor());
        }
        this.ui.getParent().validate();
        this.ui.repaint();
    }

}
