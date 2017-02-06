package com.atlassian.uwc.ui;

import junit.framework.TestCase;

import org.apache.xmlrpc.*;

import java.util.Vector;
import java.util.*;
import java.io.*;

/**
 * This is kind of a junk test class at the moment. I was just trying
 * out different XMLRPC Confluence things to see if they'd work. Ultimately this should
 * class should be refactored or moved to documentation.
 *
 * For now it's a kind of handy example of making simple XMLRPC calls with Java
 *
 * This class is just for trying things out and not part of the test suite.
 */
public class XMLRPCClientTest extends TestCase {

    String token;
    public final static String testLoginID = "test";
    public final static String testLoginPassword = "test";
    private String testSpaceID = "test";
    XmlRpcClient client = null;

    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public String getUniqueString() {
        return String.valueOf((new Date()).getTime());

    }

    /**
     * Read from standard input and make an asynchronous XML-RPC call.
     */
    public void testAddingASinglePage() throws IOException

    {
        String url = "http://localhost:8080/rpc/xmlrpc";
        try {
            client = new XmlRpcClient(url);

            // Login and retrieve logon token
            Vector loginParams = new Vector(2);
            loginParams.add(testLoginID);
            loginParams.add(testLoginPassword);
            String loginToken;
            loginToken = (String) client.execute("confluence1.login", loginParams);
            Vector paramsVector = new Vector();
            paramsVector.add(loginToken);

            List results = (List) client.execute("confluence1.getGroups", paramsVector);
            for (int i = 0; i < results.size(); i++) {
                Object o = results.get(i);
                System.out.println("o= " + o);
            }

            for (int i = 0; i < 1; i++) {
                // add a unique page to space test
                Hashtable page = new Hashtable();
                page.put("space", "test");
                page.put("title", "test-" + getUniqueString());
                page.put("content", "test" + getUniqueString());
                paramsVector = new Vector();
                paramsVector.add(loginToken);
                paramsVector.add(page);
                Map resultPage = (Map) client.execute("confluence1.storePage", paramsVector);
                System.out.println("page written");

                //add an attachment
                paramsVector = new Vector();
                paramsVector.add(loginToken);
                Hashtable attachment = new Hashtable();
                String pageID = (String) resultPage.get("id");
                attachment.put("pageId", pageID);
                attachment.put("title", "fun");
                attachment.put("fileName", "fun.xml");
                attachment.put("contentType", "XML");
                attachment.put("comment", "funny that a comment is required");
                paramsVector.add(pageID);
                paramsVector.add(attachment);
                File file = new File("c:\\tmp\\fun.xml");
                byte fileBytes[] = getBytesFromFile(file);
                paramsVector.add(fileBytes);
                Map resultAttachment = (Map) client.execute("confluence1.addAttachment", paramsVector);
                System.out.println("attachment written");


            }


            System.out.println("cleaning up");
        } catch (XmlRpcException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

// Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}
