package com.atlassian.uwc.ui.organizer;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import com.atlassian.uwc.ui.organizer.DNDTreeNodeTransferContainer;

public class DNDTreeSourceListener extends TransferHandler {

    public static Logger logger = Logger.getLogger(DNDTreeSourceListener.class);

    public DNDTreeSourceListener() {
        super();
    }

    protected Transferable createTransferable(JComponent aComponent) {

        if (!(aComponent instanceof JTree)) {
            return null;
        }

        TreePath selectedPath = ((JTree)aComponent).getSelectionPath();
        DNDTreeNodeTransferContainer transferable = new DNDTreeNodeTransferContainer(selectedPath);

        // HACK
        // SET THE SOURCE IN THE TARGET LISTENER
        // FOR DRAWING THE IMAGE OF THE SOURCE NODE
        DNDTreeTargetListener treeTargetListener =
            (DNDTreeTargetListener)((JTree)aComponent).getDropTarget();
        treeTargetListener.setSourceNodePath(selectedPath);

        return transferable;
    }

    protected void exportDone(JComponent aSource, Transferable aTransferable, int anAction) {

        if (!(aSource instanceof JTree)) {
            super.exportDone(aSource, aTransferable, anAction);
            return;
        }

        JTree tree = (JTree)aSource;
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode targetParentNode = getTargetParentNode(tree);

        DefaultMutableTreeNode sourceNode = null;
        try {
            TreePath sourcePath = (TreePath)aTransferable.getTransferData(DataFlavor.stringFlavor);
            sourceNode = (DefaultMutableTreeNode)sourcePath.getLastPathComponent();

        } catch (Exception ex) {
            logger.error("Unable to unpackage drag and drop container for selected tree node.\n", ex);
        }

        //Check if source node is a child of target
        if (!sourceNode.isNodeDescendant(targetParentNode)) {
            model.removeNodeFromParent(sourceNode);
            model.insertNodeInto(sourceNode, targetParentNode, targetParentNode.getChildCount());

            tree.setSelectionPath(new TreePath(sourceNode.getPath()));
        }

        super.exportDone(aSource, aTransferable, anAction);
    }

    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }

    private DefaultMutableTreeNode getTargetParentNode(JTree aTree) {

        if (aTree.getSelectionPath() != null) {
            return (DefaultMutableTreeNode)aTree.getSelectionPath().getLastPathComponent();
        }

        Point location = ((DNDTreeTargetListener)aTree.getDropTarget()).getLastDragLocation();
        TreePath path = aTree.getClosestPathForLocation(location.x, location.y);
        MutableTreeNode targetNode = (MutableTreeNode)path.getLastPathComponent();
        return (DefaultMutableTreeNode)targetNode.getParent();
    }
}
