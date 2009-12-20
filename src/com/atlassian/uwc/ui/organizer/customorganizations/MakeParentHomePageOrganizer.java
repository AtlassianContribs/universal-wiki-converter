package com.atlassian.uwc.ui.organizer.customorganizations;

import biz.artemis.confluence.xmlrpcwrapper.ConfluenceServerSettings;
import biz.artemis.confluence.xmlrpcwrapper.PageForXmlRpc;
import biz.artemis.confluence.xmlrpcwrapper.RemoteWikiBroker;
import com.atlassian.uwc.ui.UWCForm2;
import com.atlassian.uwc.ui.organizer.OrganizerUtils;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: brendan
 * Date: Jul 7, 2006
 * Time: 11:40:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class MakeParentHomePageOrganizer {

    /**
     * point all pages without parents at the homepage of the
     * current space as a parent
     */
    public void runOrganizer() {
        Logger log = Logger.getLogger(this.getClass().getName());
        // pop up window to say 'are you sure you want to do this'?
        // it cannot be undone, backup your data!
        String message = "The parent of all parentless pages in the space \nwill have their parent set to the home page. \nPlease backup before conducting this operation. \nThis operation cannot be undone. \n\nAre you absolutely sure you want to do this?";
        JTextArea text = new JTextArea(message);
        text.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(200, 100));
        int choice = JOptionPane.showInternalConfirmDialog(UWCForm2.getInstance().getMainPanel(),
                text,
                "information",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // get home page info
        ConfluenceServerSettings cs = OrganizerUtils.getInstance().getConfluenceSettings();
        RemoteWikiBroker rwb = RemoteWikiBroker.getInstance();
        String homePageId = null;
		try {
			homePageId = rwb.getSpaceHomePageId(cs, cs.spaceKey);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}

        // get all pages
        Map<String, PageForXmlRpc> allPagesMap = null;
		try {
			allPagesMap = rwb.getAllServerPagesMapById(cs, cs.spaceKey);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		}
        Collection pages = allPagesMap.values();
        // iterate through and point those without parents at the home page
        int totalPages = pages.size();
        int count=0;
        for (Object page : pages) {
            PageForXmlRpc pageForXmlRpc = (PageForXmlRpc) page;
            if (pageForXmlRpc.getParentId() == null || pageForXmlRpc.getParentId().equals("0")) {
                count++; log.info("re-parenting page "+count+"/"+totalPages);
                pageForXmlRpc.setParentId(homePageId);
                // send the pages to Confluence
                try {
					rwb.storeNewOrUpdatePage(cs, cs.spaceKey, pageForXmlRpc);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlRpcException e) {
					e.printStackTrace();
				}
            }
        }

    }
}
