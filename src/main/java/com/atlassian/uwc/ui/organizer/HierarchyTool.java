package com.atlassian.uwc.ui.organizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.UWCForm2;

public class HierarchyTool {

    protected UWCForm2 uwcForm;
    public JPanel hierarchyToolPanel;

    private JTree originalTree;
    private JTree modifiedTree;
    private JScrollPane originalTreeViewScrollPane;
    private JScrollPane modifiedTreeViewScrollPane;
    private JPanel originalTreePanel;
    private JPanel modifiedTreePanel;
    private JSplitPane treeSplitPane;

    private JPanel buttonPanel;
    private JButton finishButton;
    private JButton cancelButton;
    private JButton helpButton;

    private static final String FINISH_BUTTON_LABEL = "Finish";
    private static final String CANCEL_BUTTON_LABEL = "Cancel";
    private static final String HELP_BUTTON_LABEL = "Help";

    private static final String ORIGINAL_TREE_LABEL = "Original Structure";
    private static final String MODIFIED_TREE_LABEL = "Modified Structure";

    private static final String HELP_MSG =
        "Reorganize the wiki pages in the \'Modified Structure\' tree \n" +
        "by dragging and dropping. Click \'Finish\' to rearrange the wiki.\n" +
        "The \'Orignal Structure\' tree is to be used as a reference.";

    public static Logger logger = Logger.getLogger(HierarchyTool.class);

    public HierarchyTool(UWCForm2 aUWCForm) {
        uwcForm = aUWCForm;
        initialize();
    }

    private void initialize() {

        getHierarchyToolPanel().add(getTreeSplitPane(), BorderLayout.CENTER);
        getHierarchyToolPanel().add(getButtonPanel(), BorderLayout.SOUTH);

        getHierarchyToolPanel().setPreferredSize(new Dimension(500, 400));

        spaceSplitPanes();
    }

    private JPanel getHierarchyToolPanel() {

        if (hierarchyToolPanel == null) {
            hierarchyToolPanel = new JPanel(new BorderLayout());
            hierarchyToolPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

            spaceSplitPanes();
        }

        return hierarchyToolPanel;
    }

    private WikiPage getParent(List aHierarchicalList, int aParentID) {

        Iterator hierarchicalListIt = aHierarchicalList.iterator();
        while (hierarchicalListIt.hasNext()) {
            WikiPage page = (WikiPage)hierarchicalListIt.next();

            if (page.getPageID() == aParentID) {
                return page;
            }

            WikiPage possibleParent = getParent(page.getChildPages(), aParentID);
            if (possibleParent != null) {
                return possibleParent;
            }
        }

        return null;
    }

    private void constructWikiPageHierarchy(List aHierarchy, List aFlatList) {

        List minimizedFlatList = new ArrayList();

        Iterator flatListIt = aFlatList.iterator();
        while (flatListIt.hasNext()) {
            WikiPage page = (WikiPage)flatListIt.next();

            if (page.getParentID() == 0) {
                aHierarchy.add(page);
                continue;
            }

            WikiPage parent = getParent(aHierarchy, page.getParentID());
            if (parent != null) {
                parent.getChildPages().add(page);
            } else {
                minimizedFlatList.add(page);
            }
        }

        if (minimizedFlatList.size() == aFlatList.size()) {
            // There are orphan pages
            logger.error("There are " + minimizedFlatList.size() + " orphaned pages.");
            return;
        }

        if (minimizedFlatList.size() > 0) {
            constructWikiPageHierarchy(aHierarchy, minimizedFlatList);
        }
    }

    private void addPageNodes(DefaultMutableTreeNode aParent, WikiPage aWikiPage) {

        Iterator childPagesIt = aWikiPage.getChildPages().iterator();
        while (childPagesIt.hasNext()) {
            WikiPage childPage = (WikiPage)childPagesIt.next();

            DefaultMutableTreeNode childPageNode = new DefaultMutableTreeNode(childPage);
            aParent.add(childPageNode);

            addPageNodes(childPageNode, childPage);
        }
    }

    private TreeModel getOriginalTreeModel() {

        List wikiPageHierarchy = new ArrayList();
        constructWikiPageHierarchy(wikiPageHierarchy, WikiPage.getExampleFlatList());

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new DefaultMutableTreeNode());

        Iterator rootPagesIt = wikiPageHierarchy.iterator();
        while (rootPagesIt.hasNext()) {
            WikiPage rootPage = (WikiPage)rootPagesIt.next();

            DefaultMutableTreeNode rootPageNode = new DefaultMutableTreeNode(rootPage);
            rootNode.add(rootPageNode);

            addPageNodes(rootPageNode, rootPage);
        }

        return new DefaultTreeModel(rootNode);
    }

    private JTree getOriginalTree() {

        if (originalTree == null) {
            originalTree = new JTree();

            originalTree.setRowHeight(-1);
            originalTree.setBounds(0, 0, 78, 72);
            originalTree.setShowsRootHandles(true);
            originalTree.setRootVisible(false);
            originalTree.setEditable(false);
            originalTree.setBackground(getHierarchyToolPanel().getBackground());
            DefaultTreeCellRenderer renderer =
                (DefaultTreeCellRenderer)originalTree.getCellRenderer();
            renderer.setBackgroundNonSelectionColor(
                    getHierarchyToolPanel().getBackground());

            originalTree.setModel(getOriginalTreeModel());
        }

        return originalTree;
    }

    private JScrollPane getOriginalTreeViewScrollPane() {

        if (originalTreeViewScrollPane == null) {
            originalTreeViewScrollPane = new JScrollPane();
            originalTreeViewScrollPane.setViewportView(getOriginalTree());
        }

        return originalTreeViewScrollPane;
    }

    protected JPanel getOriginalTreePanel() {

        if (originalTreePanel == null) {
            originalTreePanel = new JPanel(new BorderLayout(2, 2));

            Box treeLabel = Box.createHorizontalBox();
            treeLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 0));
            treeLabel.add(new JLabel(ORIGINAL_TREE_LABEL));
            originalTreePanel.add(treeLabel, BorderLayout.NORTH);

            originalTreePanel.add(getOriginalTreeViewScrollPane(),
                    BorderLayout.CENTER);
        }

        return originalTreePanel;
    }

    private JTree getModifiedTree() {

        if (modifiedTree == null) {
            modifiedTree = new JTree();

            modifiedTree.setRowHeight(-1);
            modifiedTree.setBounds(0, 0, 78, 72);
            modifiedTree.setShowsRootHandles(true);
            modifiedTree.setRootVisible(false);
            modifiedTree.getSelectionModel().
                setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
            modifiedTree.setDragEnabled(true);

            modifiedTree.setTransferHandler(new DNDTreeSourceListener());
            modifiedTree.setDropTarget(new DNDTreeTargetListener());

            modifiedTree.setModel(getOriginalTreeModel());
        }

        return modifiedTree;
    }

    private JScrollPane getModifiedTreeViewScrollPane() {

        if (modifiedTreeViewScrollPane == null) {
            modifiedTreeViewScrollPane = new JScrollPane();
            modifiedTreeViewScrollPane.setViewportView(getModifiedTree());
        }

        return modifiedTreeViewScrollPane;
    }

    protected JPanel getModifiedTreePanel() {

        if (modifiedTreePanel == null) {
            modifiedTreePanel = new JPanel(new BorderLayout(2, 2));

            Box treeLabel = Box.createHorizontalBox();
            treeLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 0));
            treeLabel.add(new JLabel(MODIFIED_TREE_LABEL));
            modifiedTreePanel.add(treeLabel, BorderLayout.NORTH);

            modifiedTreePanel.add(getModifiedTreeViewScrollPane(),
                    BorderLayout.CENTER);
        }

        return modifiedTreePanel;
    }

    protected JSplitPane getTreeSplitPane() {

        if (treeSplitPane == null) {

            treeSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

            treeSplitPane.add(getOriginalTreePanel(), JSplitPane.LEFT);
            treeSplitPane.add(getModifiedTreePanel(), JSplitPane.RIGHT);

            treeSplitPane.setContinuousLayout(true);
        }
        return treeSplitPane;
    }

    public void spaceSplitPanes() {
        getTreeSplitPane().setDividerLocation(180);
    }

    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel(new BorderLayout(0, 10));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            // Align the buttons in a horizontal box
            Box buttonBox = Box.createHorizontalBox();
            buttonBox.add(Box.createHorizontalStrut(5));
            buttonBox.add(getFinishButton());
            buttonBox.add(Box.createHorizontalStrut(5));
            buttonBox.add(getCancelButton());
            buttonBox.add(Box.createHorizontalStrut(5));
            buttonBox.add(getHelpButton());
            buttonBox.add(Box.createHorizontalStrut(5));

            // Add a separator line to the panel
            buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

            // Add the horizontal button box to the button panel
            buttonPanel.add(buttonBox, BorderLayout.EAST);
        }

        return buttonPanel;
    }

    private JButton getFinishButton() {
        if (finishButton == null) {
            finishButton = new JButton(FINISH_BUTTON_LABEL);

            finishButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    commitHeirarchyChanges();
                }
            });
        }

        return finishButton;
    }

    private void commitHeirarchyChanges() {

        int answer = JOptionPane.showConfirmDialog(
                getHierarchyToolPanel(),
                "Would you like to commit the changes?",
                "Committing Hierarchy Changes",
                JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.NO_OPTION) {
            closeHierarchyTool();
            return;
        }

        DefaultMutableTreeNode rootNode =
            (DefaultMutableTreeNode)getModifiedTree().getModel().getRoot();
        List newHierarchy = new ArrayList();

        constructFlatHierarchy(rootNode, newHierarchy);

        Iterator flatHierarchyIt = newHierarchy.iterator();
        while (flatHierarchyIt.hasNext()) {
            WikiPage page = (WikiPage) flatHierarchyIt.next();

            System.out.println("Wiki Page: " + page);
        }

        closeHierarchyTool();
    }

    public void constructFlatHierarchy(DefaultMutableTreeNode aNode, List aFlatHierarchy) {

        if (aNode.getUserObject() instanceof WikiPage) {
            aFlatHierarchy.add(aNode.getUserObject());
        }

        if (aNode.getChildCount() >= 0) {

            for (Enumeration e = aNode.children(); e.hasMoreElements(); ) {
                DefaultMutableTreeNode childNode =
                    (DefaultMutableTreeNode)e.nextElement();
                constructFlatHierarchy(childNode, aFlatHierarchy);
            }
        }
    }


    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton(CANCEL_BUTTON_LABEL);

            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    closeHierarchyTool();
                }
            });
        }

        return cancelButton;
    }

    private void closeHierarchyTool() {
        UWCForm2.hierarchyToolFrame.setVisible(false);
        getModifiedTree().setModel(getOriginalTreeModel());
    }

    private JButton getHelpButton() {
        if (helpButton == null) {
            helpButton = new JButton(HELP_BUTTON_LABEL);

            helpButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showHelpScreen();
                }
            });
        }

        return helpButton;
    }

    private void showHelpScreen() {
        JOptionPane.showMessageDialog(
                getHierarchyToolPanel(),
                HELP_MSG,
                "Hierarchy Tool Help",
                JOptionPane.QUESTION_MESSAGE);
    }
}
