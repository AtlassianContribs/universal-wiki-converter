package com.atlassian.uwc.ui.xmlrpcwrapperOld.test;
/**
 * Testing operations provided by the RemoteWikiBrokerOld. Eventually
 * this test class should verify that all the communications to
 * and from Confluence are being properly handled. This will be
 * an important class to run against new versions of Confluence
 * as certain parts of their APIs do change.
 *
 *      * The setup method is currently hard coded to assume you're running
 * these tests with:
 * - a local instance of Confluence
 * - running on port 8080
 * - which has a test space called 'test'
 * - with a user named 'test'
 * - with the user password being 'test'
 * - the user 'test' must have write access to the space 'test'
 */

import junit.framework.*;
import com.atlassian.uwc.ui.xmlrpcwrapperOld.RemoteWikiBrokerOld;
import com.atlassian.uwc.ui.xmlrpcwrapperOld.PageForXmlRpcOld;
import com.atlassian.uwc.ui.xmlrpcwrapperOld.AttachmentForXmlRpcOld;
import com.atlassian.uwc.ui.ConfluenceSettingsForm;
import com.atlassian.uwc.ui.UWCForm2;

import java.util.Date;
import java.io.File;

public class RemoteWikiBrokerTestOld extends TestCase {
    RemoteWikiBrokerOld remoteWikiBroker;

    /**
     * The setup method is currently hard coded to assume you're running
     * these tests with:
     * - a local instance of Confluence
     * - running on port 8080
     * - which has a test space called 'test'
     * - with a user named 'test'
     * - with the user password being 'test'
     * @throws Exception
     */
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        ConfluenceSettingsForm confSettings = UWCForm2.getInstance().getConfluenceSettingsForm();
        confSettings.setLogin("test");
        confSettings.setPassword("test");
        confSettings.setSpaceName("test");
        confSettings.setUrl("localhost:8080");
    }

    public void testStoreNewOrUpdatePage() throws Exception {
        ConfluenceSettingsForm confSettings = UWCForm2.getInstance().getConfluenceSettingsForm();
        RemoteWikiBrokerOld rwb = RemoteWikiBrokerOld.getInstance();
        PageForXmlRpcOld page = new PageForXmlRpcOld();
        page.setTitle("test-"+getUniqueString());
        page.setContent("this is some content "+getUniqueString());
        rwb.storeNewOrUpdatePage(page, "test");

    }

    /**
     * Test the ability to first add a new page and then update
     * that page.
     *
     * @todo - currently verification of a successfull test here
     * involves looking at the wiki, this should be more automated
     * thought that will probably always be needed unless we implement some
     * kind of screen scraping or trust the XMLRPC API enough to
     * do its own verification
     * @throws Exception
     */
    public void testAddThenUpdatePage() throws Exception {
        ConfluenceSettingsForm confSettings = UWCForm2.getInstance().getConfluenceSettingsForm();
        RemoteWikiBrokerOld rwb = RemoteWikiBrokerOld.getInstance();
        PageForXmlRpcOld page = new PageForXmlRpcOld();
        String uniqueId = getUniqueString();
        page.setTitle("testing-update-"+uniqueId);
        page.setContent("this is some content "+uniqueId);
        rwb.storeNewOrUpdatePage(page, "test");

        page = new PageForXmlRpcOld();
        page.setTitle("testing-update-"+uniqueId);
        page.setContent("this is some UPDATED content "+uniqueId);
        rwb.storeNewOrUpdatePage(page, "test");

    }

    /**
     * This test adds a page and then uploads an attachment to that page twice.
     *
     * Verify working: Go to Confluence and check that the new page was added
     * successfully, also verify that the attachment was added twice and it
     * contains the image file in the devel/sampleData dir.
     * @throws Exception
     */
    public void testAddPageThenAddAttachment() throws Exception {
        ConfluenceSettingsForm confSettings = UWCForm2.getInstance().getConfluenceSettingsForm();
        RemoteWikiBrokerOld rwb = RemoteWikiBrokerOld.getInstance();
        PageForXmlRpcOld page = new PageForXmlRpcOld();
        String uniqueId = getUniqueString();
        page.setTitle("testing-attachment-"+uniqueId);
        page.setContent("this is some content "+uniqueId);
        page = rwb.storeNewOrUpdatePage(page, "test");

        // add attachment to page
        //add an attachment
        AttachmentForXmlRpcOld attachment = new AttachmentForXmlRpcOld();
        attachment.setFileName("testFile.jpeg");
        attachment.setContentType("jpeg");
        attachment.setComment("RemoteWikiBrokerTestOld");
        attachment.setFileLocation("sampleData"+File.separator+"testFile.jpeg");
        attachment = rwb.storeAttachment(page.getId(), attachment);

        // write the attachment a second time with the same input data
        // just to verify it's working
        attachment = new AttachmentForXmlRpcOld();
        attachment.setFileName("testFile.jpeg");
        attachment.setContentType("jpeg");
        attachment.setComment("RemoteWikiBrokerTestOld");
        attachment.setFileLocation("sampleData"+File.separator+"testFile.jpeg");
        attachment = rwb.storeAttachment(page.getId(), attachment);



    }

    public String getUniqueString() {
        return String.valueOf((new Date()).getTime());
    }

    public void testPopulatePageXmlRpcData() throws Exception {
        fail("Test is not implemented");
    }
}