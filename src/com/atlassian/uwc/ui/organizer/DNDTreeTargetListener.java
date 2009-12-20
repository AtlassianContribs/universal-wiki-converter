package com.atlassian.uwc.ui.organizer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class DNDTreeTargetListener extends DropTarget {

	private TreePath sourceNodePath;

	private Rectangle lastDragOverRowBounds;

	private Rectangle lastDragImageArea;

	private Point lastDragLocation;

	public DNDTreeTargetListener() {
		super();
	}

	private TreePath getSourceNodePath() {
		return sourceNodePath;
	}

	public void setSourceNodePath(TreePath aPath) {
		sourceNodePath = aPath;
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

	public void dragOver(DropTargetDragEvent anEvent) {

		JTree tree = (JTree)anEvent.getDropTargetContext().getComponent();
		Point eventLocation = anEvent.getLocation();
		
		DefaultMutableTreeNode targetParentNode = getTargetParentNode(tree);
		DefaultMutableTreeNode sourceNode = 
			(DefaultMutableTreeNode)getSourceNodePath().getLastPathComponent();
		
		// Check if source node is a child of target
		if (sourceNode.isNodeDescendant(targetParentNode)) {
			anEvent.rejectDrag();
			clearImage(tree);
		} else {
			anEvent.acceptDrag(DnDConstants.ACTION_MOVE);
			updateDragImage(tree, eventLocation);
		}

		
		updateScrolling(tree, eventLocation);
		updateDragMarker(tree, eventLocation);

		super.dragOver(anEvent);
	}

	private void updateDragImage(JTree aTree, Point aPoint) {

		BufferedImage dragImage = getDragImage(aTree, getSourceNodePath());

		if (dragImage == null) {
			return;
		}

		//Clear the last drag image.
		aTree.paintImmediately(getLastDragImageArea().getBounds());

		getLastDragImageArea().setRect(
				(int)aPoint.getX(),
				(int)aPoint.getY() - 17,
				dragImage.getWidth(),
				dragImage.getHeight());

		aTree.getGraphics().drawImage(
				dragImage,
				(int)aPoint.getX(),
				(int)aPoint.getY() - 17,
				aTree);
	}

	public BufferedImage getDragImage(JTree aTree, TreePath aNodeToDraw) {

		BufferedImage image = null;
		try {
			Rectangle nodeBounds = aTree.getPathBounds(aNodeToDraw);
			TreeCellRenderer treeRenderer = aTree.getCellRenderer();
			DefaultTreeModel treeModel = (DefaultTreeModel)aTree.getModel();
			boolean nIsLeaf = treeModel.isLeaf(aNodeToDraw.getLastPathComponent());

			JComponent node = (JComponent)treeRenderer.getTreeCellRendererComponent(
					aTree,
					aNodeToDraw.getLastPathComponent(),
					false,
					aTree.isExpanded(aNodeToDraw),
					nIsLeaf,
					0,
					false);

			node.setBounds(nodeBounds);
			image = new BufferedImage(
					node.getWidth(),
					node.getHeight(),
					java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);

			Graphics2D graphics = image.createGraphics();
			graphics.setComposite(AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 0.75f));
			node.setOpaque(false);
			node.paint(graphics);
			graphics.dispose();

		} catch (RuntimeException re) {
			return null;
		}

		return image;
	}

	private Rectangle getLastDragImageArea() {

		if (lastDragImageArea == null) {
			lastDragImageArea = new Rectangle();
		}

		return lastDragImageArea;
	}

	private void updateScrolling(JTree aTree, Point aCursorLocation) {

		Insets autoScrollInsets = new Insets(20, 20, 20, 20);

		Rectangle outer = aTree.getVisibleRect();
		Rectangle inner = new Rectangle(
				outer.x + autoScrollInsets.left,
				outer.y + autoScrollInsets.top,
				outer.width - (autoScrollInsets.left + autoScrollInsets.right),
				outer.height - (autoScrollInsets.top + autoScrollInsets.bottom));

		if (!inner.contains(aCursorLocation)) {
			Rectangle scrollRect = new Rectangle(
					aCursorLocation.x - autoScrollInsets.left,
					aCursorLocation.y - autoScrollInsets.top,
					autoScrollInsets.left + autoScrollInsets.right,
					autoScrollInsets.top + autoScrollInsets.bottom);

			aTree.scrollRectToVisible(scrollRect);
		}
	}

	public void updateDragMarker(JTree aTree, Point aLocation) {

		setLastDragLocation(aLocation);

		int row = aTree.getRowForPath(
				aTree.getClosestPathForLocation(
					aLocation.x,
					aLocation.y));

		TreePath path = aTree.getPathForRow(row);
		if (path == null) {
			return;
		}

		Rectangle rowBounds = aTree.getPathBounds(path);

		int rby = rowBounds.y;
		int topBottomDist = 6;

		Point topBottom = new Point(
				rby - topBottomDist,
				rby	+ topBottomDist);

		if (topBottom.x <= aLocation.y &&
			topBottom.y >= aLocation.y) {

			paintInsertMarker(aTree, aLocation);

		} else {
			markNode(aTree, aLocation);
		}
	}

	private void markNode(JTree aTree, Point aLocation) {

		TreePath path = aTree.getClosestPathForLocation(aLocation.x, aLocation.y);
		if (path == null) {
			return;
		}

		if (getLastDragOverRowBounds() != null) {
			Graphics g = aTree.getGraphics();
			g.setColor(Color.white);
			g.drawLine(
					getLastDragOverRowBounds().x,
					getLastDragOverRowBounds().y,
					getLastDragOverRowBounds().x + getLastDragOverRowBounds().width,
					getLastDragOverRowBounds().y);
		}

		aTree.setSelectionPath(path);
		aTree.expandPath(path);
	}

	private void paintInsertMarker(JTree aTree, Point aLocation) {

		Graphics g = aTree.getGraphics();
		aTree.clearSelection();

		int row = aTree.getRowForPath(aTree.getClosestPathForLocation(
				aLocation.x,
				aLocation.y));

		TreePath path = aTree.getPathForRow(row);
		if (path == null) {
			return;
		}

		Rectangle rowBounds = aTree.getPathBounds(path);

		if (getLastDragOverRowBounds() != null) {
			g.setColor(Color.white);
			g.drawLine(
					getLastDragOverRowBounds().x,
				getLastDragOverRowBounds().y,
				getLastDragOverRowBounds().x + getLastDragOverRowBounds().width,
				getLastDragOverRowBounds().y);
		}

		if (rowBounds != null) {
			g.setColor(Color.black);
			g.drawLine(
				rowBounds.x,
				rowBounds.y,
				rowBounds.x	+ rowBounds.width,
				rowBounds.y);
		}

		setLastDragOverRowBounds(rowBounds);
	}

	private Rectangle getLastDragOverRowBounds() {
		return lastDragOverRowBounds;
	}

	private void setLastDragOverRowBounds(Rectangle aRowBounds) {
		lastDragOverRowBounds = aRowBounds;
	}

	public Point getLastDragLocation() {
		return lastDragLocation;
	}

	private void setLastDragLocation(Point aLocation) {
		lastDragLocation = aLocation;
	}

	public void dragExit(DropTargetDragEvent anEvent) {
		clearImage((JTree)anEvent.getDropTargetContext().getComponent());

		super.dragExit(anEvent);
	}

	public void drop(DropTargetDropEvent anEvent) {
		clearImage((JTree)anEvent.getDropTargetContext().getComponent());

		super.drop(anEvent);
	}

	private final void clearImage(JTree tree) {
		tree.paintImmediately(getLastDragImageArea().getBounds());
	}
}
