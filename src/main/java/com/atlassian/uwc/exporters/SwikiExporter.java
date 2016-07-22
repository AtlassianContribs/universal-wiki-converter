/*
 * SwikiExporter.java
 *
 * Created on February 8, 2007, 3:02 PM
 *
 */

package com.atlassian.uwc.exporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.atlassian.uwc.util.PropertyFileManager;

/**
 *
 * @author Leonard Sitongia (sitongia@ucar.edu)
 *
 * $Id$
 *
 * This exporter converts Swiki XML files into text files.
 * Swiki stores content in XML files with a naming convention of numerics.
 * Links in the XML file refer to other files using the number.
 * It is convenient to convert these references to the actual page names,
 * and similarly, the file names from numerical to the page names, so that
 * the rest of UWC works simply.
 *
 * Thanks to Laura Kolker for a great example to work from, namely JotspotExporter.
 *
 */

public class SwikiExporter implements Exporter {
    private Logger log = Logger.getLogger(this.getClass());
    
    public static final String DEFAULT_PROPERTIES_LOCATION =   "exporter.swiki.properties";
    public static final String EXPORTER_PROPERTIES_INPUTDIR =  "exported.input.dir";
    public static final String EXPORTER_PROPERTIES_OUTPUTDIR = "exported.output.dir";
    public static final String EXPORTER_PROPERTIES_ATTACHMENTDIR_INPUT = "exported.input.attachment.dir";
    public static final String EXPORTER_PROPERTIES_ATTACHMENTDIR_OUTPUT = "exported.output.attachment.dir";
            
    public void export(Map properties) {
        log.info("Exporting Swiki...");
        
        // Set up the input directories
        String in =  (String) properties.get(EXPORTER_PROPERTIES_INPUTDIR);
        File in_dir = new File(in);
        if (!in_dir.exists()) {
            log.error("Input directory doesn't exist: " + in);
            return;
        } else if (!in_dir.isDirectory()) {
            log.error("Input is not a directory: " + in);
            return;
        }
        log.info("Reading from "+in);
        // Set up the attachments input directory
        String attachments_in = (String)properties.get(EXPORTER_PROPERTIES_ATTACHMENTDIR_INPUT);
        File attachmentsInDir = new File(attachments_in);
        if(!attachmentsInDir.exists()){
        	log.error("Attachments input directory not found: " + attachments_in);
        	return;
        }else if(!attachmentsInDir.isDirectory()) {
        	log.error("Attachments input is not a directory: " + attachments_in);
        	return;
        }
        
        // Set up the ioutput (exported)and attachment directories
        String out = (String) properties.get(EXPORTER_PROPERTIES_OUTPUTDIR);
        File out_dir = new File(out);
        String attachment = (String) properties.get(EXPORTER_PROPERTIES_ATTACHMENTDIR_OUTPUT);
        File attachment_dir = new File(attachment);
        if (!out_dir.exists()) {
            log.info("Creating output directory: " + out);
            out_dir.mkdir();
        } else if (out_dir.isDirectory()) {
            deleteDir(out_dir);
            out_dir.mkdir();
        }
        
        if(!attachment_dir.exists()) {
        	log.info("Creating attachment output directory " + attachment);
        	attachment_dir.mkdir();
        } else if (attachment_dir.isDirectory()) {
        	deleteDir(attachment_dir);
        	attachment_dir.mkdir();
        }
        log.info("Exporting to "+out);
        
        // Start with the home page, which is always named 1.xml
        
        SwikiFile homepage = new SwikiFile(in_dir, attachmentsInDir, out_dir, attachment_dir, "1.xml");
        
        log.info("Name of Home Page is "+homepage.getName());       
        homepage.convert();
        homepage.save();
        homepage.convertRemainingPages();
        homepage.copyAttachments();
        homepage.clearLinks();
        
        log.info("Export Swiki Complete...");
    }
    
    /**
     * deletes the given file. This method is used recursively.
     * @param file can be a directory or a file. Directory does not have to be empty.
     * @author Laura Kolker
     */
    private void deleteDir(File file) {
        //if file doesn't exist (shouldn't happen), just exit
        if (!file.exists()) return;
        String name = "";
        try {
            name = file.getCanonicalPath();
        } catch (IOException e) {
            log.error("Problem while deleting directory. No filename!");
            e.printStackTrace();
        }
        //delete the file
        if (file.delete()) {
            log.debug("Deleting " + name);
            return;
        } else { // or delete the directory
            File[] files = file.listFiles();
            for (File f : files) {
                deleteDir(f);
            }
            file.delete();
            log.debug("Deleting dir: " + name);
        }
        
    }
    
    public void cancel() {
    	log.error("Cancel hasn't been implemented. Contact developer.");
    }
    
   
    public static void main(String args[])
    {
    	if (args.length < 1)
    	{
    		System.out.println("Usage: <prop file name>");
    		System.exit(1);
    	}
    	String propFile=args[0];
    	
    	try
    	{
    		PropertyConfigurator.configure("log4j.properties");
    		SwikiExporter exporter=new SwikiExporter();
    		Map props=PropertyFileManager.loadPropertiesFile(propFile);
    		exporter.export(props);
    		System.exit(0);
    	}
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    		System.exit(1);
    	}
    	
    }
}
