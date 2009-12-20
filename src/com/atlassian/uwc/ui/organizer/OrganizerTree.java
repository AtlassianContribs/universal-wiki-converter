package com.atlassian.uwc.ui.organizer;

import net.antonioshome.swing.treewrapper.TreeWrapper;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import biz.artemis.confluence.xmlrpcwrapper.RemoteWikiBroker;
import biz.artemis.confluence.xmlrpcwrapper.ConfluenceServerSettings;
import biz.artemis.confluence.xmlrpcwrapper.PageForXmlRpc;
import com.atlassian.uwc.ui.UWCForm2;
import com.atlassian.uwc.ui.ConfluenceSettingsForm;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

/**
 * Created by IntelliJ IDEA.
 * User: brendan
 * Date: Jun 30, 2006
 * Time: 3:29:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrganizerTree {

    Logger log = Logger.getLogger("Organizer Tree");
    JTree organizerTree = new JTree();

    public JTree getJTree() {
        return organizerTree;
    }
    /**
     * create the organizer tree and populate it
     *
     * @return
     */
    public static OrganizerTree getOrganizerTree() {
        OrganizerTree ot = new OrganizerTree();
        ot.populate();
        return ot;
    }

    /**
     *
     */
    private void populate() {
        TreeWrapper organizerTreeWrapper = new TreeWrapper(organizerTree);


        // get confluence settings
        ConfluenceServerSettings confServerSettings = OrganizerUtils.getInstance().getConfluenceSettings();
        String space = UWCForm2.getInstance().confluenceSettingsForm.getSpaceName();
        // get all pages
        RemoteWikiBroker rwb = RemoteWikiBroker.getInstance();
//        Map pageMap = rwb.getAllServerPagesMapByTitle(confServerSettings, space);

        // check connection
        if (!rwb.connectionActive(confServerSettings)) {
            return;
        }

        // traverse the pages as a tree structure
        DefaultTreeModel organizerTreeModel = createJTreeModelFromConfluencePages(confServerSettings, space);

        // insert the pages into the model

//////////////////////////////////
//        DefaultMutableTreeNode oranges = new DefaultMutableTreeNode("oranges");
//        model.insertNodeInto(oranges, root, 0);
//
//        for (int i = 0; i < 5; i++) {
//            DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode("orange " + (i + 1));
//            model.insertNodeInto(dmtn, oranges, i);
//            DefaultMutableTreeNode dmtn2 = new DefaultMutableTreeNode("orange " + (i + 1));
//            model.insertNodeInto(dmtn2, dmtn, 0);
//
//        }
//
//        DefaultMutableTreeNode apples = new DefaultMutableTreeNode("apples");
//        model.insertNodeInto(apples, root, 1);
//        for (int i = 0; i < 3; i++)
//            model.insertNodeInto(new DefaultMutableTreeNode("apple " + (i + 1)), apples, i);
/////////////////////////////////////////

        organizerTree.setModel(organizerTreeModel);
    }

    /**
     * To create a JTree we use two passes. The first pass creates all
     * the nodes and indexes them in a map. The second pass hooks them
     * all together.
     *
     * @param pageMap
     * @return
     */
    private DefaultTreeModel createJTreeModelFromConfluencePages(ConfluenceServerSettings confServerSettings, String space) {
        DefaultMutableTreeNode pageNode = new DefaultMutableTreeNode("space", true);
        Map<String, DefaultMutableTreeNode> pageNodeMapById = new HashMap<String, DefaultMutableTreeNode>();
        // first pass - iterate through the pages, create the tree nodes
        //   and stick them into a map
        RemoteWikiBroker rwb = RemoteWikiBroker.getInstance();
        Collection<PageForXmlRpc> pages = null;
		try {
			pages = rwb.getAllServerPageSummaries(confServerSettings, space);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (XmlRpcException e1) {
			e1.printStackTrace();
		}
        for (PageForXmlRpc page : pages) {
            pageNode = new DefaultMutableTreeNode(page, true);
            pageNodeMapById.put(page.getId(), pageNode);
        }

        // create place for pages with no parent
        DefaultMutableTreeNode noParentNode = new DefaultMutableTreeNode("no parent", true);

        // create the model and add the root
        String homePageId = null;
		try {
			homePageId = rwb.getSpaceHomePageId(confServerSettings, space);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (XmlRpcException e1) {
			e1.printStackTrace();
		}
        DefaultMutableTreeNode root = pageNodeMapById.get(homePageId);
        if (root==null) {
            log.error("=================");
            log.error("No root node found!!!");
            log.error("=================");
            return null;
        }
        root.add(noParentNode);

        // second pass, connect all the nodes together into their
        //   child parent relationships
        for (PageForXmlRpc page : pages) {
            List children = null;
			try {
				children = rwb.getPageChildrenIds(confServerSettings, page.getId());
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (XmlRpcException e1) {
				e1.printStackTrace();
			}
            pageNode = pageNodeMapById.get(page.getId());
            for (Object aChildren : children) {
                String childId = (String) aChildren;
                pageNode.add(pageNodeMapById.get(childId));
                log.debug("adding to node: "+page.getTitle()+ "   child:  "+pageNodeMapById.get(childId).toString());
            }
            // add pages with no parent to the no parent group
            if (page.getParentId()==null||page.getParentId().equals("0")) {
                try {
                    noParentNode.add(pageNode);
                } catch (IllegalArgumentException e) {
                    log.debug(page.toString()+"  - probably the root node");
                }
            }
        }
        DefaultTreeModel model = new DefaultTreeModel(root);
        return model;  //To change body of created methods use File | Settings | File Templates.
    }

//    /**
//     * Grab a ConfluenceServerSettings object which is necessary
//     * to perform remote actions. The server settings in this
//     * case are populated by what's in the UWC Form
//     *
//     * @return
//     */
//    public ConfluenceServerSettings getConfluenceSettings() {
//        ConfluenceServerSettings css = new ConfluenceServerSettings();
//        ConfluenceSettingsForm csf = UWCForm2.getInstance().confluenceSettingsForm;
//
//        css.login = csf.getLogin();
//        css.password = csf.getPassword();
//        css.spaceName = csf.getLogin();
//        css.url = csf.getUrl();
//        return css;
//    }


}
