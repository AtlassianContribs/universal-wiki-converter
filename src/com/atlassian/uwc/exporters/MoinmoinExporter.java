package com.atlassian.uwc.exporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.uwc.prep.MoinMoinPreparation.PageDirFileFilter;
import com.atlassian.uwc.ui.FileUtils;

/**
 * Exporter code based on MoinMoinPreparation class. 
 * This is partially a refactor, but also contains some new properties and improvements.
 */
public class MoinmoinExporter implements Exporter {

	private boolean running = false;
	Logger log = Logger.getLogger(this.getClass());
	
	private Map properties;
	private static final String CURRENT = "current";
    private static final String REVISIONS = "revisions";
    private static final String BADCONTENT = "BadContent";
    private static final String EXTENSION = ".txt";
	
	public void cancel() {
		log.info("Cancelling Moinmoin Export");
		this.running = false;
	}

	public void export(Map propertiesMap) throws ClassNotFoundException,
			SQLException {
		this.running = true;
		log.info("Beginning Moinmoin Export");
		this.properties = propertiesMap;

		if (validDirectories()) {
			exportPages();
		}
		
		if (this.running)
			log.info("Moinmoin Export Complete");
		this.running = false;
	}

	/**
	 * @return true if the src and out directories are valid directories
	 */
	protected boolean validDirectories() {
		if (this.running == false) return false;
		
		String src = getSrc();
		String out = getOut();
		if (src == null || out == null) {
			log.error("src and out properties must be set in conf/exporter.moinmoin.properties");
			return false;
		}
        File pagesDir = new File(src);
        File destinationDir = new File(out);

        if (pagesDir.isFile() || !pagesDir.exists()) {
        	log.error("src directory is not a valid directory: " + src);
        	return false;
        }


        if (destinationDir.isFile()) {
        	log.error("out property is not a directory: " + out);
        	return false;
        }

        if (!destinationDir.exists()) {
        	if (!destinationDir.mkdirs()) {
        		log.error("Impossible to create out directory: \"" + out + "\".");
            	return false;
        	}
        }
        log.debug("src and out directories are valid");
		return true;
	}

	private String getSrc() {
		if (this.properties == null) return "";
		return (String) this.properties.get("src");
	}

	private String getOut() {
		if (this.properties == null) return "";
		return (String) this.properties.get("out");
	}

	/**
	 * export moinmoin pages to out directory
	 */
	private void exportPages() {
		if (this.running == false) return;
		
		log.debug("src directory: " + getSrc());
		log.debug("out directory: " + getOut());
		
		File pagesDir = new File(getSrc());
		String[] pages = pagesDir.list(new PageDirFileFilter());
        String current = null;

        for (int i = 0; i < pages.length; i++) {
            if (!pages[i].startsWith(BADCONTENT)) { //ignore BADCONTENT page
					log.debug("page: " + pages[i]);
            	if (exportHistory()) {
            		String revisiondir = getSrc() + File.separator + pages[i] + File.separator + REVISIONS;
						log.debug("revision dir: " + revisiondir);
            		String[] revisions = new File(revisiondir).list();
						if (revisions == null) {
							log.error("Revisions directory was null. Skipping.");
							continue;
						}
            		for (String revision : revisions) {
            			if (!revision.matches("\\d+")) continue; //ignore non-numbers
						int num = Integer.parseInt(revision);
						try {
	            			File srcfile = new File(getSrc() + File.separator + pages[i] + 
								            							File.separator + REVISIONS + File.separator + revision);
	            			File outfile = new File(getOut() + File.separator + pages[i] + "-" + num + EXTENSION);
							copyFile(srcfile,outfile);
							addTitleData(outfile, pages[i]);
	            		} catch (FileNotFoundException e) {
	            			log.info("Page \"" + pages[i] + "\" has been deleted and will be ignored.");
	            		} catch (Exception e) {
	            			e.printStackTrace();
	            		}
					}
            		
            	}
            	else { //only export current
	            	current = getCurrentRevision(getSrc() + File.separator + pages[i]);
	            	log.debug(pages[i] + "\ncurrent revision: " + current);
	            	if (current != null) {
	            		try {
	            			File srcfile = new File(getSrc() + File.separator + pages[i] + 
								            							File.separator + REVISIONS + File.separator + current);
							File outfile = new File(getOut() + File.separator + pages[i] + EXTENSION);
							copyFile(srcfile, outfile);
							addTitleData(outfile, pages[i]);
	            		} catch (FileNotFoundException e) {
	            			log.info("Page \"" + pages[i] + "\" has been deleted and will be ignored.");
	            		} catch (Exception e) {
	            			e.printStackTrace();
	            		}
	            	}
            	}
            }
        }
		
	}
	
	protected void addTitleData(File outfile, String title) {
		title = title.replaceAll("\\(2f\\)", "/");
		String titledata = "{orig-title:" + title + "}\n";
		String filecontents;
		try {
			filecontents = FileUtils.readTextFile(outfile);
		} catch (IOException e) {
			log.error("Could not read output file: " + outfile.getAbsolutePath());
			e.printStackTrace();
			return;
		}
		String newcontents = titledata + filecontents;
		FileUtils.writeFile(newcontents, outfile.getAbsolutePath());
	}

	private boolean exportHistory() {
		if (this.properties == null) return false;
		return Boolean.parseBoolean((String) this.properties.get("history"));
	}

	/**
	 * copies file to newFile
	 * @param file
	 * @param newFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void copyFile(File file, File newFile) throws FileNotFoundException, IOException {
		if (!this.running) return;
		log.debug("Copying '" + file.getAbsolutePath() + "' to '" + newFile.getAbsolutePath() + "'");
		if (!file.exists()) log.error("File doesn't exist. Cannot copy: " + file.getAbsolutePath());
		// Create channel on the source
		FileChannel srcChannel = new FileInputStream(file.getAbsolutePath()).getChannel();
   
		// Create channel on the destination
		FileChannel dstChannel = new FileOutputStream(newFile.getAbsolutePath()).getChannel();
   
		// Copy file contents from source to destination
		int buffersize = -1;
		try {
			//see if the user specified a buffer size
			String buffersizeStr = (String) this.properties.get("buffer-size");
			if (buffersizeStr != null) {
				try { 
					buffersize = Integer.parseInt(buffersizeStr); 
				}
				catch (NumberFormatException en) {
					log.error("Property buffer-size is not an integer. Using filesize.");
				}
			}
			if (buffersize > 0) { //user set buffersize - see Michael Grove's code in UWC-349
				long size = srcChannel.size();
	            long position = 0;
	            while (position < size) {
	               position += srcChannel.transferTo(position, buffersize, dstChannel);
	            }
			}
			else { //if no user specified buffer size, use filesize
				dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e2) {
			throw e2;
		} catch (RuntimeException e3) {
			throw e3;
		} finally {
			// Close the channels
			srcChannel.close(); 
			dstChannel.close();
			if (!newFile.exists()) log.error("Copying file unsuccessful. New file does not exist: " + newFile.getAbsolutePath());
		}
	}
	
	  /**
     * getCurrentRevision
     *
     * @param pagePath
     * @return current revision filename
     */
    private String getCurrentRevision(String pagePath) {

        String current = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(pagePath + File.separator + CURRENT));
            current = br.readLine();
        } catch (FileNotFoundException e) {
        	log.info("Page \"" + pagePath + "\" has been deleted and will be ignored.");
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return current;
    }
	
	//used with unit testing
	protected void setProperties(Map properties) {
		this.properties = properties;
	}

	//used with unit testing
	protected void setRunning(boolean running) {
		this.running = running;
	}

}
