package com.atlassian.uwc.exporters;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.FileUtils;

public class PmwikiExporter implements Exporter {

	private boolean running = false;
	private Map properties;
	
	public final static String EXPORTDIR = "exported_pmwiki_pages"; 
	
	Logger log = Logger.getLogger(this.getClass());
	public void cancel() {
		this.running = false;
	}

	public void export(Map properties) {
		this.running = true;
		setProperties(properties);

		try {
			export();
		} catch (IOException e) {
			log.error("Problem while exporting. Exiting.");
			e.printStackTrace();
		}
		
		if (this.running) log.info("Export Complete.");
		this.running = false;
	}

	private void export() throws IOException {
		if (!this.running) return;
		File srcFile = getFileFromProperty("src");
		File outFile = getFileFromProperty("out");
		
		File exportdir = createExportDir(outFile);
		if (!this.running) return;
		File[] files = srcFile.listFiles();
		for (File file : files) {
			if (!this.running) return;
			exportFile(file, exportdir);
		}
	}

	private File createExportDir(File parent) {
		if (!this.running) return null;
		File export = new File(parent.getAbsolutePath() + File.separator + EXPORTDIR);
		if (export.exists()) {
			log.debug("Deleting existing export dir: " + export.getAbsolutePath());
			FileUtils.deleteDir(export);
		}
		if (!export.mkdir()) {
			log.error("Could not create export dir: " + export.getAbsolutePath());
		}
		return export;
	}

	Pattern pagename = Pattern.compile("^([^.]+)\\.(.*)$");
	//recursive
	private void exportFile(File file, File parent) throws IOException {
		if (!this.running) return;
		if (file == null) return;
		if (file.isFile()) {
			Matcher pageFinder = pagename.matcher(file.getName());
			if (pageFinder.find()) {
				String group = pageFinder.group(1);
				String pagename = pageFinder.group(2);
				File groupFile = getGroupFile(group, parent);
				File pageFile = getPageFile(pagename, groupFile);
				copyFile(file, pageFile);
			}
		}
		else if (file.isDirectory()) {
			File[] children = file.listFiles();
			File newparent = getNewParent(file, parent);
			for (File child : children) {
				if (!this.running) return;
				exportFile(child, newparent);
			}
		}
	}

	private File getGroupFile(final String group, File parent) {
		File[] files = parent.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return group.equals(file.getName());
			}
		});
		if (files == null || files.length < 1) { //create the file
			File newgroup = new File(parent.getAbsolutePath() + File.separator + group);
			if (!newgroup.mkdir()) {
				log.warn("Could not create group directory: " + newgroup.getAbsolutePath());
			}
			return newgroup; 
		}
		return files[0]; //or return the existing group directory
	}

	private File getPageFile(String pagename, File parent) {
		return new File(parent.getAbsolutePath() + File.separator + pagename);
	}

	private File getNewParent(File file, File parent) {
		String dirname = file.getName();
		File dir = getGroupFile(dirname, parent);
		return dir;
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
			String buffersizeStr = (String) getProperties().get("buffer-size");
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

	protected File getFileFromProperty(String key) {
		String path = (String) getProperties().get(key);
		if (path == null) {
			log.error("exporter property '" + key + "' must be set");
			throw new IllegalArgumentException();
		}
		File file = new File(path);
		if (!file.exists()) {
			log.error("'" + key + "' file does not exist: " + path);
			throw new IllegalArgumentException();
		}
		return file;
	}

	public void setProperties(Map props) {
		this.properties = props;
	}
	
	public Map getProperties() {
		if (this.properties == null)
			this.properties = new HashMap();
		return this.properties;
	}

	protected void start() { //used by junit
		this.running  = true;
	}


}
