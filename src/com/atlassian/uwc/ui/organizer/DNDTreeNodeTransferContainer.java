package com.atlassian.uwc.ui.organizer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class DNDTreeNodeTransferContainer implements Transferable {

	private Object node;

	private DataFlavor[] flavors;

	public DNDTreeNodeTransferContainer(Object aNode) {
		super();
		node = aNode;
	}

	public DataFlavor[] getTransferDataFlavors() {

		if (flavors == null) {
			flavors = new DataFlavor[1];

			flavors[0] = DataFlavor.stringFlavor;
		}
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return true;
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		return node;
	}
}
