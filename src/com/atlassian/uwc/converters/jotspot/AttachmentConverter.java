package com.atlassian.uwc.converters.jotspot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.ConfluenceSettingsForm;
import com.atlassian.uwc.ui.Page;

/**
 * Prepares jotspot attachments for uploading to Confluence.
 * NOTE: This class was heavily influenced by TwikiPrepareAttachmentFilesConverter
 * @author Laura Kolker
 */
public class AttachmentConverter extends BaseConverter {
	private static final String JOTSPOT_IMAGE_DIR = "/_data/";
	private static final String JOTSPOT_IMAGE_DATA = "attach.URI.dat";
	Logger log = Logger.getLogger(this.getClass()); 

	public void convert(Page page) {
		log.debug("Converting Jotspot Attachments -- starting");
        // scan the page and create a list of attachments
        addAttachmentsToPage(page);
		log.debug("Converting Jotspot Attachments -- complete");

	}
	
    /**
     * looks for attachments and attaches them
     * @param page object to attach pages to
     */
    protected void addAttachmentsToPage(Page page) {
    	//two types of attachments, images and attachments

    	//images  
    	Vector<String> imagepaths = getImagePaths(page.getOriginalText());
    	attach(imagepaths, page);
    	
    	//attachments
    	Vector<String> attachmentPaths = getAttachmentPaths(page);
    	attach(attachmentPaths, page);
    	
    }

	/**
	 * attaches given String Vector of paths to page
	 * @param paths
	 * @param page
	 */
	private void attach(Vector<String> paths, Page page) {

		//foreach path in paths
		for (String path : paths) {
			//get the complete path to the file

			log.debug("complete path = " + path);
			
			//confirm existance of file
			File file = new File(path);
	        if (!file.exists() || file.isDirectory()) 
	        	continue;
			
			//attach the file
			log.debug("adding attachment: " + file.getName());
			page.addAttachment(file);
		}
	}
	
	String correctPath = "(.*\\/([^/]+)\\" + JOTSPOT_IMAGE_DIR + ")";
	Pattern correctPathPattern = Pattern.compile(correctPath);
	/**
	 * changes the jotspot default imagename (which is a .dat) to the
	 * actual image name, by examining the path to said image
	 * @param path default path
	 * <br>For example:
	 * /attachDirectory/System/TmpImageUpload/hobbespounce.gif/_data/attach.URI.dat
	 * @return useful path
	 * <br/>For example: 
	 * /attachDirectory/System/TmpImageUpload/hobbespounce.gif/_data/hobbespounce.gif
	 */
	private String getUsefulImagePath(String path) {
		log.debug("renaming path = " + path);
		Matcher correctPathFinder = correctPathPattern.matcher(path);
		if (correctPathFinder.lookingAt()) {
			String correctName = correctPathFinder.group(2);
			String correctDir = correctPathFinder.group(1);
			//check for ascii char encodings (only encode directory
			//as wiki syntax won't find encoded character filenames)
			correctDir = encodeChars(correctDir);
			path = correctDir + correctName; 
			log.debug("correct path = " + path);
		}
		
		return path;
	}

	Pattern nonfilenameChars = Pattern.compile("[^-\\w+ \\.\\/\\\\]");
	/**
	 * checks for non filename safe characters (like , (comma) for example)
	 * and encodes thems.
	 * @param string For example:
	 * thisStringHasA,comma
	 * @return encoded nonword characters, For example:
	 * thisStringHasA%2Ccomma
	 */
	protected String encodeChars(String string) {
		String encoding = "UTF-8";//FIXME long term, provide property for this?
		Matcher nonwordcharFinder = nonfilenameChars.matcher(string);
		StringBuffer sb = new StringBuffer();
		//find the nonfilename characters
		while (nonwordcharFinder.find()) {
			String badChar = nonwordcharFinder.group();
			String encoded = null;
			//and encode them.
			try {
				encoded = URLEncoder.encode(badChar, encoding);
			} catch (UnsupportedEncodingException e) {
				log.error("Problem with encoding: " + encoding);
				e.printStackTrace();
			}
			nonwordcharFinder.appendReplacement(sb, encoded);
		}
		nonwordcharFinder.appendTail(sb);
	    String encodedStr = sb.toString();
	    if (encodedStr == null || "".equals(encodedStr))
	    	encodedStr = string;
		return encodedStr;
	}


	String imgSrc = "<img src=\"([^\"]+)\"[^/]*/>";
	Pattern imgPattern = Pattern.compile(imgSrc);
	/**
	 * @param input page text
	 * @return list of absolute paths to images
	 */
	private Vector<String> getImagePaths(String input) {
		Vector<String> paths = new Vector<String>();
		
		//get paths from img src using regex
		Matcher imgFinder = imgPattern.matcher(input);
		String root = this.getAttachmentDirectory();
		while (imgFinder.find()) {
			String path = imgFinder.group(1);
			if (!path.startsWith("\\/")) path = "/" + path;
			path = root + path; //this is actually an image directory !
			String tmpPath = path + JOTSPOT_IMAGE_DIR + JOTSPOT_IMAGE_DATA; //here's where the image is
			String toPath = getUsefulImagePath(tmpPath); //but that's a useless name for the file, so we change it
			tmpPath = encodeChars(tmpPath); //only encode directory, as wiki syntax won't find encoded character filenames
			File tmpFile = new File(tmpPath);
			File toFile = new File(toPath);
			if (!tmpFile.renameTo(toFile)) { //this happens when there's something there already
				log.debug("Can't rename " + tmpFile + " to " + toFile);
			}
			
			log.debug("getImagePaths path = " + toPath);
			paths.add(toPath);
		}
		
		return paths;
	}

	/**
	 * figures out what attachments are associated with this page.
	 * @param page 
	 * @return String vector of absolute paths to attachments
	 */
	private Vector<String> getAttachmentPaths(Page page) {
		Vector<String> paths = new Vector<String>();
		
    	//get the directory with the same name as this page
    	File pageDir = getPageDir(page);
    	if (pageDir == null) 
    		return paths;
    	
    	//go down one level
    	File files[] = pageDir.listFiles();
    	
    	//open up the xml files
    	for (File file : files) {
    		//check for existence and non-directoryness
    		if (!file.exists() || file.isDirectory())
    			continue;
    		//slurp string
    		String fileContents = read(file);
//    		log.debug("fileContents = " + fileContents);
    		if (isAttachment(fileContents)) {
    			String path = getPath(fileContents);
    			String root = this.getAttachmentDirectory();
    			String basePath = encodeChars(root + path + JOTSPOT_IMAGE_DIR);
    			String tmpPath = basePath + JOTSPOT_IMAGE_DATA;
    			path = basePath + getFilename(path);
    			File tmpFile = new File(tmpPath);
    			File toFile = new File(path);
    			if (!tmpFile.renameTo(toFile)) { //this happens when there's already a file there
    				log.debug("Can't rename " + tmpPath + " to " + path);
    			}
    			log.debug("attachment path = " + path);
    			if (path == null)
    				continue;
    			paths.add(path);
    		}
    	}

    	return paths;
	}
	

	/**
	 * @param path
	 * @return filename for a given filepath.
	 * <br/>Example: 
	 * <br/>path = /Dirpath/myfile.txt
	 * <br/>return = myfile.txt
	 */
	private String getFilename(String path) {
		File file = new File(path);
		return file.getName();
	}

	Pattern attachmentPath = Pattern.compile("<node[^>]+?path=\"([^\"]*)\"");
	/**
	 * Uses the given file contents to determine the path to the attachment
	 * @param fileContents
	 * @return
	 */
	private String getPath(String fileContents) {
		log.debug("non image attachment!!!");
		Matcher pathFinder = attachmentPath.matcher(fileContents);
		if (pathFinder.find()) {
			String path = pathFinder.group(1);
			path = path.replaceAll(" ", "+");
			log.debug("path = " + path);
			return path;
		}
		return null;
	}

	Pattern attachmentClue = Pattern.compile("nodeClass=\"attachment\"");
	/**
	 * Uses the given file contents to determine if that file represents 
	 * an attachment
	 * @param fileContents
	 * @return true if fileContents represents an attachment
	 */
	private boolean isAttachment(String fileContents) {
		Matcher attachmentClueFinder = attachmentClue.matcher(fileContents);
		return attachmentClueFinder.find();
	}

	/**
	 * reads the contents of the file
	 * @param file file is assumed to be an existing non-directory file
	 * @return the contents of the file as a string or null if file could
	 * not be found
	 */
	private String read(File file) {

		String string = "";
		try {
			Scanner in = new Scanner(file);
			while (in.hasNext()) {
				string += in.next() + " ";
			}
		} catch (FileNotFoundException e) {
			log.debug("Problem opening file: " + file.getName());
			e.printStackTrace();
			return null;
		}
		return string;
	}

	Pattern extension = Pattern.compile("(.*)\\.\\w+$");
	/**
	 * figures out the associated directory for the given page
	 * @param page 
	 * @return page directory or null if none exists
	 * <br/>Example:
	 * <br/>If the page exists at: /SomeDirectory/My+Page.xml
	 * <br/>returns /SomeDirectory/My+Page if that represents an existing directory
	 */
	private File getPageDir(Page page) {
		String similarPath = page.getFile().getPath(); 
		log.debug("similar = " + similarPath);
		Matcher extFinder = extension.matcher(similarPath);
		if (extFinder.lookingAt()) {
			String dirPath = extFinder.group(1);
			log.debug("dirPath = " + dirPath);
			File file = new File(dirPath);
			if (file.exists() && file.isDirectory()) {
				return file;
			}
		}
		return null; 
	}

}
