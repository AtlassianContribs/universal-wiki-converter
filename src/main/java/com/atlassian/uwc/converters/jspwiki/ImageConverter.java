package com.atlassian.uwc.converters.jspwiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.listeners.FeedbackHandler.Feedback;

/**
 * converts Jspwiki image syntax to Confluence image syntax
 */
public class ImageConverter extends JspwikiLinkConverter {

	public static String FILE_SEP = File.separator; // we keep this in a seperate var, so we can change it for unit testing
	private static final int DEFAULT_VERSION = 1;
	private static final String ATTACHMENT_SUFFIX = "-dir";
	private static final String ATTACHMENT_PAGE_SUFFIX = "-att";
	private String tmpDir = System.getProperty("user.dir") + FILE_SEP + "tmp";
	Logger log = Logger.getLogger(this.getClass());
	
	public void convert(Page page) {
		log.info("Converting Images - start");
		
		String input = page.getOriginalText();
		
		//make the syntax conversions
		String converted = convertImages(input);
		converted = convertAttachments(converted, page.getName() != null?page.getName():"");
		
		//do the actual attaching
		try {
			String attachmentDirectory = this.getAttachmentDirectory();
			String pagename = page.getName();
			log.info("Attempting to find attachments.");
			Vector<String> files = getAttachableFiles(converted, attachmentDirectory, pagename);
			log.info("Attempting to attach files.");
			converted = attach(page, files, converted);
		} catch (NullPointerException e) {
			//XXX
			//this is only relevant for tests that don't have an
			//instantiated ConfluenceSettingsForm object
			//thus - don't do anything with it
			log.debug("NPE!: " + e.getMessage());
		}

		page.setConvertedText(converted);
		log.info("Converting Images - complete");
	}
		

	/**
	 * converts jspwiki image syntax to Confluence image syntax
	 * @param input
	 * @return
	 */
	protected String convertImages(String input) {
		String imagetypes = createOrString(getImageTypes());
		String jspImage = "\\[" +					// image syntax bracket start
							".*?" +					// possible display text
							"(https?:\\/\\/)" + 	// protocol (group 1)
							"(.*?\\.)" +			// everything until a dot (group 2)
							"(?i)" +				// make the following case insensitive
							"(" + imagetypes + ")" +// image types (group3)
							"\\]";					// image syntax bracket end
		String confImage = "!{group1}{group2}{group3}!";
		return RegexUtil.loopRegex(input, jspImage, confImage);
	}

	/**
	 * creates a series of uncaptured OR strings as a regex string
	 * @param items string array of text elements
	 * @return Example: 
	 * <br/>
	 * if items = { "A", "B" }
	 * <br/>
	 * then the return string will be (?:A)|(?:B) 
	 */
	protected String createOrString(String[] items) {
		if (items == null) return null;
		if (items.length < 1) return null;
		String orString = "";
		int counter = 0;
		for (String item : items) {
			counter++;
			if (counter > 1) {
				orString += "|";
			}
			orString += "(" + 	// start container
					"?:" +		// don't capture as group
					item +		// actual text
					")";		// end container
					
		}
		return orString;
	}

	/**
	 * image types that we look for when converting images
	 */
	private String[] types = {
			"JPEG",
			"JPG",
			"TIFF",
			"RAW",
		    "PNG",
		    "GIF",
		    "BMP",
		    "WDP",
		    "XPM",
		    "MrSID",
		    "SVG"
	};
	/**
	 * @return String array of supported image types
	 * Note: strings are in ALL CAPS, so case insensitive matching 
	 * is probably necessary to work with these
	 */
	protected String[] getImageTypes() {
		/* XXX alternatively, we could try to get this info from the jspwiki.properties
		 * by looking for patterns like:
		 * jspwiki.translatorReader.inlinePattern.1 
		 */
		return this.types;
	}
	
	String attachments = "\\[" +				//left bracket
					"(" +						//start capturing (group1)
						"[^\\]]+" +			//anything not a dot or right bracket
					")" +						//end capturing (group1)
					"(" +						//start capturing (group2)
						"\\." +					//one dot
						"[^\\]]+" +				//anything not a right bracket
					")" +						//end capturing (group2)
					"\\]";						//closing right bracket
	Pattern attachmentsPattern = Pattern.compile(attachments);
	String attachmentsWithAlias = "^" +			//start string
					"(" +						//start capturing (group1)
						"[^|\\]]+" +			//anything not a pipe or right bracket
						"\\|" +					//one pipe
					")" +						//end capturing (group1)
					"(" +						//start capturing (group2)
						".+" +					//everything until the end
					")" +						//end capturing (group2)
					"$";						//the end
	Pattern attachmentsWithAliasPattern = Pattern.compile(attachmentsWithAlias);
	String attachmentsWithPagename = "^" +		//start string
					"(" +						//start capturing (group1)
						"[^/\\\\\\]]+" +			//anything not a forward or back slash
					")" +						//end capturing (group1)
					"[/\\\\]" +					//one forward or back slash
					"(" +						//start capturing (group2)
						".+" +					//everything until the end
					")" +						//end capturing (group2)
					"$";						//the end
	Pattern attachmentsWithPagenamePattern = Pattern.compile(attachmentsWithPagename);
	Pattern extractExtension = Pattern.compile("" +
			"^(.*)\\.[^.]+$");
	
	/**
	 * @param input
	 * @return converts jspwiki attachment syntax to Confluence syntax
	 */
	protected String convertAttachments(String input) {
		return convertAttachments(input, "");
	}
	/**
	 * @param input
	 * @thisPagename the current page's name
	 * @return converts jspwiki attachment syntax to Confluence syntax
	 */
	protected String convertAttachments(String input, String thisPagename) {
		String pagedir = getPageDir();
		Matcher attachmentsFinder = attachmentsPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		//find attachment
		while (attachmentsFinder.find()) {
			found = true;
			String leftOfDot = attachmentsFinder.group(1);
			String extension = attachmentsFinder.group(2);
			//is this supposed to be escaped?
			if (pagedir != null && isPage(pagedir, leftOfDot+extension)) continue;
			if (extension.length() > 5) continue; //Extensions are period + 1-4 chars
			if (notLink(leftOfDot)) continue;
			//is there an alias?
			Matcher attachmentsWithAliasFinder = 
				attachmentsWithAliasPattern.matcher(leftOfDot);
			String alias = "";
			String filename = leftOfDot;
			if (attachmentsWithAliasFinder.matches()) {
				alias = attachmentsWithAliasFinder.group(1);
				filename = attachmentsWithAliasFinder.group(2);
			}
			if (hasProtocol(filename)) continue;
			//check for pagename
			String pagename = "";
			Matcher attachmentsWithPagenameFinder =
				attachmentsWithPagenamePattern.matcher(filename);
			if (attachmentsWithPagenameFinder.matches()) {
				pagename = attachmentsWithPagenameFinder.group(1);
				filename = attachmentsWithPagenameFinder.group(2);
				filename = filename.replaceAll("[+]", " ");
			}
			
			//check to see if we don't need the pagename
			Matcher extensionFinder = extractExtension.matcher(thisPagename);
			if (extensionFinder.matches()) thisPagename = extensionFinder.group(1);
			if (thisPagename != null && thisPagename.equals(pagename))
				pagename = "";
			
			//check for external link
			if (hasProtocol(pagename)) continue;
			
			//check for imageness
			String replacement = "";
			if (isImage(extension)) {
				filename += extension;
				if (!"".equals(pagename)) pagename += "^";
				replacement = "!" + pagename + filename + "!";
			}
			else {
				filename += extension;
				replacement = "[" + alias + pagename + "^" + filename + "]";
			}
			log.debug("replacement = " + replacement);
			attachmentsFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			attachmentsFinder.appendTail(sb);
			return sb.toString();
		}
 		return input;
	}


	Pattern protocol = Pattern.compile("" +
			"^(http)|(file)|(ftp)|(https)");
	private boolean hasProtocol(String input) {
		return protocol.matcher(input).lookingAt();
	}


	/**
	 * @param input
	 * @return true if not a link
	 */
	protected boolean notLink(String input) {
		return input.startsWith("[") || input.startsWith("{");
	}


	/**
	 * @param extension
	 * @return true if the given extension reflects an image
	 */
	protected boolean isImage(String extension) {
		extension = extension.replaceAll("\\W", "");
		extension = extension.toUpperCase();
		for (String type : types) {
			if (type.equals(extension)) return true;
		}
		return false;
	}

	/* Attachment Helper Methods */
	String confluenceImage = 
			"!" +				//exclamation point
			"(" +				//start capturing (group 1)
				"[^\n!.]+" +	//not an exclamation point, newline (uwc-210) or . until
				"(" +			//start capturing (group 2)
					"\\." +		//a . (dot)
					"[^\n!]+" +	//not an exclamation point or newline (uwc-210) until
				")" +			//end capturing (group 2)
			")" +				//end capturing (group 1)
			"!"; 				//an exclamation point
	Pattern confImagePattern = Pattern.compile(confluenceImage);
	String linkToConfAttachment = 
			"\\[" +					//left bracket
			"(" +					//start capturing (group 1)
				"[^\\^]*" +			//not a carat until
				"\\^" +				//carat symbol
				"(" +				//start capturing (group 2)
					"[^\\].]+" +	//not a right bracket or . until
					"(" +			//start capturing (group 3)
						"\\." +		//a . (dot)
						"[^\\]]+" +	//not a right bracket until
					")" +			//end capturing (group 3)
				")" +				//end capturing (group 2)
			")" +					//end capturing (group 1)
			"\\]"; 					//right bracket
	Pattern linkToAttachPattern = Pattern.compile(linkToConfAttachment);
	String postCarat = 
		"(.*)"+	//everything until
		"\\^" +	//carat
		"(.*)";	//everything after the carat
	
	
	/**
	 * @param confluenceSyntax confluence syntax that might
	 * contain images or other attachment references
	 * @param attachmentDirectory this should be the same as 
	 * defined in jspwiki.properties under the 
	 * jspwiki.basicAttachmentProvider.storageDir prop
	 * @param pagename name of the page containing the syntax
	 * @return Vector of file paths to attachments referred to in the syntax
	 * or if none, returns empty Vector
	 */
	protected Vector<String> getAttachableFiles(String confluenceSyntax, String attachmentDirectory, String pagename) {
		if ("".equals(attachmentDirectory)) {
			log.info("Attachment Directory is undefined. Cannot find attachments.");
			return new Vector<String>();
		}
		Vector<String> paths = new Vector<String>();
		String basePath = createBasePath(attachmentDirectory, pagename);
		if (getAll()) { //if the images-all property isn't false, just get them all
			File pageDir = new File(basePath);
			String[] children = pageDir.list();
			for (int i = 0; i < children.length; i++) {
				String child = children[i];
				if (!child.endsWith("-dir")) continue;
				String attachment = getAttachmentName(child);
				attachment = handleWS(attachment);
				attachment = attachment.replaceAll(" ", "+");
				String extension = getExtension(child);
				int version = getVersion(basePath, attachment);
				String path = createAttachmentPath(basePath, version, attachment, extension);
				paths.add(path);
			}
			paths = uniquify(paths);
			return paths;
		}
		
		//inline images
		Matcher confImageFinder = confImagePattern.matcher(confluenceSyntax);
		while (confImageFinder.find()) {
			String image = confImageFinder.group(1);
			image  = image.replaceAll(" ", "+");
			
			//attachments from other pages should be accessed from those pages
			if (getOtherPage(image) != null) continue; 
			//String otherPage = getOtherPage(image);
//			if (otherPage != null) {
//				basePath = createBasePath(attachmentDirectory, otherPage);
//				image = image.replaceAll(postCarat, "$2");
//			}
			
			String extension = confImageFinder.group(2);
			int version = getVersion(basePath, image);
			
			String path = createAttachmentPath(basePath, version, image, extension);
			log.debug("Found Attachable File: " + path);
			paths.add(path);
		}	
		//links to attachments
		Matcher linkToAttachFinder = linkToAttachPattern.matcher(confluenceSyntax);
		while (linkToAttachFinder.find()) {
			String aliasOrPage = linkToAttachFinder.group(1);
			//attachments from other pages should be accessed from those pages
			if (getOtherPage(aliasOrPage) != null) continue; 
//			String otherPage = getOtherPage(aliasOrPage);
//			if (otherPage != null) basePath = createBasePath(attachmentDirectory, otherPage);
			
			String attachment = linkToAttachFinder.group(2);
			attachment = attachment.replaceAll(" ", "+");
			
			String extension = linkToAttachFinder.group(3);
			int version = getVersion(basePath, attachment);

			String path = createAttachmentPath(basePath, version, attachment, extension);
			log.debug("Found Attachable File: " + path);
			paths.add(path);
		}
		paths = uniquify(paths);
		log.debug("The number of attachable files is: " + paths.size());
		return paths;
	}

	protected boolean getAll() {
		Properties props = getProperties();
		if (props.containsKey("images-all")) {
			String prop = props.getProperty("images-all");
			if (prop.startsWith("f") || prop.startsWith("s"))
				return Boolean.parseBoolean(prop);
		}
		return true;
	}

	String notCaratString = 
			"(?:" +			//start non-capture group 
				"[^|]*" +	//anything not a pipe until
				"\\|" +		//a pipe
			")" +			//end non-capture group
			"?" +			//previous group is optional
			"(" +			//start capturing (group 1)
				"[^\\^]*" +	//anything not a carat until
			")" + 			//end capturing (group2)
			"\\^";			//a carat!
	Pattern notCarat = Pattern.compile(notCaratString);
	/**
	 * @param input page^file confluence syntax
	 * @return pagename containing attachment or image
	 * or null if no such page was found
	 */
	protected String getOtherPage(String input) {
		Matcher otherPageFinder = notCarat.matcher(input);
		if (otherPageFinder.find()) {
			String page = otherPageFinder.group(1);
			if ("".equals(page)) return null;
			return page;
		}
		return null;
	}

	/**
	 * filters out .properties files
	 */
	FilenameFilter notPropertiesFilter = new FilenameFilter() {
		public boolean accept(File file, String name) {
            assert file != null;
            return !name.endsWith(".properties");
		}
	};
	
	Pattern imageFilename = Pattern.compile("" +
			"^(.*(\\.[^-]+))-dir$");
	
	protected String getAttachmentName(String input) {
		Matcher finder = imageFilename.matcher(input);
		if (finder.matches()) {
			return finder.group(1);
		}
		return input;
	}
	
	protected String getExtension(String input) {
		Matcher finder = imageFilename.matcher(input);
		if (finder.matches()) {
			return finder.group(2);
		}
		return input;
	}

	String versionStr = "\\d+";
	Pattern versionPattern = Pattern.compile(versionStr);
	/**
	 * @param dir
	 * @param file
	 * @return the latest version for a file in a jspwiki dir
	 */
	protected int getVersion(String dir, String file) {
		String attachmentDirectoryStr = createAttachmentDirectory(dir, file);
		File attachDir = new File(attachmentDirectoryStr);
		
		if (attachDir.exists() && attachDir.isDirectory()) {
			String[] versions = attachDir.list(notPropertiesFilter);
			int version = 0;
			for (String versionedFile : versions) {
				Matcher versionFinder = versionPattern.matcher(versionedFile);
				if (versionFinder.find()) {
					String versionStr = versionFinder.group();
					int newVersion = Integer.parseInt(versionStr);
					version = (newVersion > version)?newVersion:version;
				}
			}
			return version;
		}
		return DEFAULT_VERSION;
	}


	/**
	 * @param basePath directory containing page
	 * @param version attachment version
	 * @param filename	attachment filename
	 * @param extension	attachment extension
	 * @return a valid path to an attachment
	 */
	private String createAttachmentPath(String basePath, int version, String filename, String extension) {
		String path = createAttachmentDirectory(basePath, filename);
		path += version + extension;
		return path;
	}


	/**
	 * @param basePath directory containing page
	 * @param filename attachment filename
	 * @return directory that will contain attachment
	 */
	private String createAttachmentDirectory(String basePath, String filename) {
		String path = basePath + filename + ATTACHMENT_SUFFIX + FILE_SEP;
		return path;
	}

	/**
	 * @param dir directory containing page
	 * @param page given page
	 * @return path to attachment directory for a given page
	 */
	protected String createBasePath(String dir, String page) {
		if (!dir.endsWith(FILE_SEP)) dir += FILE_SEP;
		page = filterPageDirForEarlierJspwikis(dir, page);
		return dir + page + ATTACHMENT_PAGE_SUFFIX + FILE_SEP;
	}


	/**
	 * early jspwikis handled this directory naming convention differently.
	 * We're going to try to find the right one. emitting errors
	 * as appropriate. See uwc-194.
	 * @param page
	 * @return
	 */
	private String filterPageDirForEarlierJspwikis(String dir, String page) {
		String test1 = dir + page + ATTACHMENT_PAGE_SUFFIX + FILE_SEP;
		File testfile = new File(test1);
		if (!testfile.exists()) {
			log.debug("Permutation 1: Can't find attachment directory: " + test1 + "\n" +
					"Trying permutations.");
		}
		else {
			log.debug("Found attachment directory: " + test1);
			return page;
		}
		
		if (page.contains(".")) //if the page has a filetype, then
			page = page.replaceFirst("^(.*?)(\\.)[^.]*$", "$1$2"); //get rid of the filetype, but not the dot
		else {
			log.debug("No valid permutations could be found.");
			log.error("Can't generate valid attachment directory. WILL NOT BE ABLE TO ATTACH FILES.");
			return page;
		}
		
		String test2 = dir + page + ATTACHMENT_PAGE_SUFFIX + FILE_SEP;
		testfile = new File(test2);
		if (!testfile.exists()) {
			log.debug("Permutation 2: Can't find attachment directory: " + test2 + "\n" +
					"Trying permutations.");
		}
		else {
			log.debug("Found attachment directory: " + test2);
			return page;
		}
		
		page = page.replaceFirst("\\.$", ""); //try getting rid of the dot
		String test3 = dir + page + ATTACHMENT_PAGE_SUFFIX + FILE_SEP;
		testfile = new File(test3);
		if (!testfile.exists()) { //nothing left to try
			log.debug("Permutation 3: Can't find attachment directory: " + test3);
			log.error("Can't generate valid attachment directory. WILL NOT BE ABLE TO ATTACH FILES.");
		}
		return page;
	}

	/**
	 * require that all paths in the given vector are unique
	 * @param paths
	 * @return given vector without non-unique elements
	 */
	protected Vector<String> uniquify(Vector<String> paths) {
		Set<String> unique = new TreeSet<String>();
		unique.addAll(paths);
		Vector<String> uniquePaths = new Vector<String>();
		uniquePaths.addAll(unique);
		return uniquePaths;
	}

	/**
	 * attaches file to page
	 * @param page
	 * @param files absolute path to file
	 */
	protected String attach(Page page, Vector<String> files, String input) {
		String pagedir = this.getTmpDir() + File.separator + page.getName();
		FileUtils.deleteDir(new File(pagedir)); //so we don't get unnecessary attachment name changes
		for (String filepath : files) {
			String orig = filepath;
			filepath = checkUrlEncoding(filepath);
			File file = new File(filepath);
			log.debug("Attachment Candidate: " + file.getAbsolutePath());
			if (fromADifferentPage(filepath, page))
				input = changeReferences(input, filepath);
			if (file.exists() && file.isFile()) {
				File copy = copyFileToTmp(file, page); //we need to do this because the jspwiki files have numeric names (1.png)
				log.debug("Attaching: " + copy.getName());
				input = fixLinks(copy, orig, input); //if the filename got changed for some reason, fix the input links
				page.addAttachment(copy);
			}
			else {
				log.debug("Could not attach: " + file.getAbsolutePath());
				if (!file.exists()) log.debug("File does not exist.");
				if (!file.isFile()) log.debug("Not a file.");
			}
		}
		return input;
	}

	Pattern filepathParts = Pattern.compile("" +
			"^(.*)\\" + File.separator + "(.*?-dir)\\" + File.separator + "(\\d.*)$");
	protected String checkUrlEncoding(String input) {
		Matcher filepathFinder = filepathParts.matcher(input);
		if (filepathFinder.matches()) {
			String dir = filepathFinder.group(1);
			String imagename = filepathFinder.group(2);
			String file = filepathFinder.group(3);
			File dirFile = new File(dir);
			if (dirFile.exists() && dirFile.isDirectory()) {
				String[] images = dirFile.list();
				for (String image : images) {
					if (image.contains("%")) {
						try {
							String candidate = URIUtil.decode(image, "utf-8");
							if (candidate.equals(imagename) || 
									candidate.equals(imagename.replaceAll("\\+", " "))) {
								imagename = image;
								break;
							}
						} catch (URIException e) {
							log.error("Problem decoding with charset: utf-8, input = " + input);
							e.printStackTrace();
						}
					}
				}
			}
			return dir + File.separator + imagename + File.separator + file;
		}
		return input;
	}
	
	/**
	 * fixes input links to changed filename
	 * @param tmpfile file that will be uploaded to Confluence
	 * @param origpath original path to jspwiki file
	 * @param input current input representing page content
	 * @return fixed input
	 */
	protected String fixLinks(File tmpfile, String origpath, String input) {
		String tmpname = tmpfile.getName();
		Matcher attachNameFinder = attachNamePattern.matcher(origpath);
		if (attachNameFinder.find()) {
			String filename = attachNameFinder.group(1);
			if (!filename.equals(tmpname)) {
				//fix input
				input = input.replaceAll("!\\Q" + filename + "\\E", "!" + tmpname);
				input = input.replaceAll("\\^\\Q" + filename + "\\E\\]", "^" + tmpname + "]");
			}
		}
		return input;
	}


	/**
	 * determines if the file we want to attach is not from the given page
	 * @param filepath
	 * @param page
	 * @return true if the file is not originally attached from the given page
	 */
	protected boolean fromADifferentPage(String filepath, Page page) {
		String name = page.getName();
		Pattern namePattern = Pattern.compile(name);
		Matcher nameFinder = namePattern.matcher(filepath);
		boolean found = nameFinder.find();
		return !found;
	}

	String attachName = "att[\\/\\\\](.*?)-dir";
	Pattern attachNamePattern = Pattern.compile(attachName);
	/**
	 * changes all image or link references to the given file so that
	 * the file can be attached to this page.  
	 * @param input current Confluence syntax with references to files attached
	 * to other pages
	 * @param filepath a valid path to a file that is attached to a different jspwiki page
	 * @return Confluence syntax that refers to the attached file with the current page:
	 * Example:<br/>
	 * input = "link to image: [OtherPage^image.png]"
	 * filepath = "mypath/OtherPage-att/image.png-dir/1.png"
	 * output: "link to image: [^image.png]"
	 */
	protected String changeReferences(String input, String filepath) {
		Matcher attachNameFinder = attachNamePattern.matcher(filepath);
		if (attachNameFinder.find()) {
			String filename = attachNameFinder.group(1);
			String regex = "(.)\\w+\\^" + filename;
			Pattern attachPagePattern = Pattern.compile(regex);
			Matcher attachPageFinder = attachPagePattern.matcher(input);
			StringBuffer sb = new StringBuffer();
			boolean found = true;
			while (attachPageFinder.find()) {
				found = true;
				String type = attachPageFinder.group(1);
				boolean inlineImage = ("!".equals(type))?true:false;
				boolean alias = ("|".equals(type))?true:false;
				//possibilities are !ed.png!, !alias|ed.png!, [^ed.png]. [alias|^ed.png]
				String prefix = (alias?"|":"") + (inlineImage?"!":(alias?"":"[")+"^"); 
				String replacement = prefix + filename;
				attachPageFinder.appendReplacement(sb, replacement);
			}
			if (found) {
				attachPageFinder.appendTail(sb);
				return sb.toString();
			}
			return input;
		}
		return input;
	}

	protected File copyFileToTmp(File file, Page page) {
		//make the tmp dir if it doesn't already exist
		String tmpDir = getTmpDir();
		createTmpDir(tmpDir);
	
		//create a new file
		String newpath = createCorrectPath(file.getAbsolutePath(), page);
		File newFile = new File(newpath);
		try {
			newFile = createNewFile(newFile);
		} catch (IOException e1) {
			log.error("Could not create new file: " + file.getAbsolutePath());
			e1.printStackTrace();
		} 
		
		//copy the file
		try {
	        copyFile(file, newFile);
		} catch (IOException e) {
			log.error("Could not copy from: " + file.getAbsolutePath() + " to " + newFile.getAbsolutePath());
			e.printStackTrace();
		}
		
		return newFile;
	}


	/**
	 * create dir at given location, unless it's already there.
	 * @param tmpDir
	 */
	protected void createTmpDir(String tmpDir) {
		File tmp = new File(tmpDir);
		if (!tmp.exists()) {
			log.info("Creating Tmp Directory");
			boolean success = tmp.mkdir();
			if (!success || !tmp.exists()) {
				log.debug("Could not mkdir: " + tmpDir + ".");
			}
			else {
				log.info("Tmp dir created successfully.");
			}
		}
	}


	/**
	 * copies file to newFile
	 * @param file
	 * @param newFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void copyFile(File file, File newFile) throws FileNotFoundException, IOException {
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
			String buffersizeStr = getProperties().getProperty("buffer-size", null);
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
	 * create a new file 
	 * @param newFile initial candidate file for creation.
	 * If this file already exists, then try a variant on the filename
	 * @return the new file object
	 * @throws IOException 
	 */
	protected File createNewFile(File newFile) throws IOException {
		log.debug("attempting to create: " + newFile.getAbsolutePath());
		String oldPath = newFile.getAbsolutePath();
		
		if (missingDirectories(oldPath)) {
			log.debug("Parent directories are missing... creating parents.");
			createParents(oldPath);
		}
		while(!newFile.createNewFile()) {
			oldPath = newFile.getAbsolutePath();	
			String newPath = createDifferentPath(oldPath);
			log.debug("Could not create new file at location: " + oldPath);
			log.debug("-- Trying new path: " + newPath);
			newFile = new File(newPath);
		}
		return newFile;
	}


	/**
	 * @param input filepath
	 * @return true if the given input string cannot be created 
	 * because parent directories do not exist
	 */
	protected boolean missingDirectories(String input) {
		String regex =
				"\\Q" +			//quote until \\E (important for backslashes)
				getTmpDir() +	//tmp dir
				"\\E" + 		//end quoting
				"(.*$)";		//everything after tmp dir
		String withoutTmp = input.replaceFirst(regex, "$1");
		String filesep = ((FILE_SEP.equals("\\/")?FILE_SEP:("\\"+FILE_SEP))); //backslashes need extra escaping 
		String[] pathParts = withoutTmp.split(filesep); 
		String accumulation = getTmpDir();
		for (int i = 0; i < pathParts.length; i++) {
			String directory = pathParts[i];
			//not directories
			if ("".equals(directory)) continue;
			if (i == pathParts.length - 1) continue;
			
			//create a file for the current parent dir
			accumulation += FILE_SEP + directory;
			File test = new File(accumulation);
			
			if (!test.exists()) return true;
		}
		return false;
	}


	protected void createParents(String input) {
		String regex =
			"\\Q" +			//quote until \\E (important for backslashes)
			getTmpDir() +	//tmp dir
			"\\E" + 		//end quoting
			"(.*$)";		//everything after tmp dir
		String withoutTmp = input.replaceFirst(regex, "$1");
		String filesep = ((FILE_SEP.equals("\\/")?FILE_SEP:("\\"+FILE_SEP))); //backslashes need extra escaping 
		String[] pathParts = withoutTmp.split(filesep); 
		String accumulation = getTmpDir();
		for (int i = 0; i < pathParts.length; i++) {
			String directory = pathParts[i];
			//not directories
			if ("".equals(directory)) continue;
			if (i == pathParts.length - 1) continue;
			
			//create a file for the current parent dir
			accumulation += FILE_SEP + directory;
			File parent = new File(accumulation);
			
			parent.mkdir();
			if (!parent.exists()) {
				log.error("Could not create parent directory: " + parent.getAbsolutePath());
				break;
			}
		}
	}


	public String getTmpDir() {
		//for unit testing purposes
		if (!FILE_SEP.equals(File.separator)) { //unit testing red flagz
			String tmp = this.tmpDir;
			Pattern p  = Pattern.compile("\\" + File.separator);
			Matcher m = p.matcher(tmp);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				String replacement = FILE_SEP;
				if (replacement.equals("\\")) replacement += "\\";
				m.appendReplacement(sb, replacement);
			}
			m.appendTail(sb);
			return sb.toString();
		}
		//what we normally do
		return this.tmpDir;
	}

	String jspAttachInfo = 
		"[\\/\\\\]" + //forward slash or backslash
		"(" +			//start capture (group1)
			"[^\\/\\\\]+" + 	//anything not a backslash or slash until
		")" +			//end capture (group1)
		"-" +			//dash
		attachName; 	//look for the filename (group2)
		
	Pattern jspAttachInfoPattern = Pattern.compile(jspAttachInfo);
	protected String createCorrectPath(String input, Page page) {
		String tmpdir = getTmpDir();
		Matcher jspInfoFinder = jspAttachInfoPattern.matcher(input);
		if (jspInfoFinder.find()) {
			String pagename = jspInfoFinder.group(1);
			Matcher filenameFinder = filenamePattern.matcher(page.getName());
			String extension = (filenameFinder.find())?filenameFinder.group(2):".txt";
			if (!pagename.endsWith(extension)) pagename += extension;
			String filename = jspInfoFinder.group(2);
			filename = handleWS(filename);
			return tmpdir + FILE_SEP + pagename + FILE_SEP + filename;
		}
		else {
			log.debug("Could not figure out correct path for: " + input);
		}
		return input;
	}
	
	protected String handleWS(String input) {
		if (input.contains("%")) {
			try {
				input = URIUtil.decode(input, "utf-8");
			} catch (URIException e) {
				log.error("Could not decode: " + input);
				e.printStackTrace();
			}
		}
		input = input.replaceAll("\\+", " ");
		return input;
	}

	String filenameStr = 
			"(" +				//start capture (group1)
				"[^\\/\\\\.]+" +	//not a slash, backslash or dot until
			")" +				//end capture (group1)
			"(" +				//start capture (group2)
				"\\." +			//a dot
				"[^\\/\\\\]+" +		//not a slash or backslash until
			")" +	 			//end capture (group2)
			"$"; 				//the end
	Pattern filenamePattern = Pattern.compile(filenameStr);
	protected String createDifferentPath(String input) {
		Matcher filenameFinder = filenamePattern.matcher(input);
		if (filenameFinder.find()) {
			String filename = filenameFinder.group(1);
			int num = getClosingNumber(filename);
			filename = removeClosingNumber(filename);

			num++;
			String extension = filenameFinder.group(2);
			String replacement = filename + num + extension;
			return filenameFinder.replaceFirst(replacement);
		}
		return null; //we can't return the input. This will be used in a loop
	}


	String fileNumber = "(\\d+)$";
	Pattern fileNumPattern = Pattern.compile(fileNumber);
	/**
	 * gets the number at the end of the string
	 * @param input
	 * @return Example:
	 * <br/>
	 * input: abc123
	 * <br/>
	 * output: 123 
	 * <br/>
	 * If no numbers present, return default number
	 */
	protected int getClosingNumber(String input) {
		Matcher fileNumFinder = fileNumPattern.matcher(input);
		if (fileNumFinder.find()) {
			String numStr = fileNumFinder.group(1);
			int num = Integer.parseInt(numStr);
			return num;
		}
		return 1; //default
	}

	String preFileNumber = "^(.*?)" + fileNumber;
	Pattern preFileNumPattern = Pattern.compile(preFileNumber);
	/**
	 * remove numbers at end of the given string
	 * @param input
	 * @return
	 * Example:
	 * <br/>
	 * input: abc123
	 * <br/>
	 * output: abc
	 * <br/>
	 * If no numbers present, return input
	 * 
	 */
	protected String removeClosingNumber(String input) {
		Matcher preFileNumberFinder = preFileNumPattern.matcher(input);
		if (preFileNumberFinder.find()) {
			String preFileNum = preFileNumberFinder.group(1);
			return preFileNum;
		}
		return input;
	}

	protected boolean isPage(String pagedir, String link) {
		File dir = new File(pagedir);
		String[] files = getPageFiles(dir);
		for (String name : files) {
			name = name.replaceAll("[+ ]", "");
			name = name.replaceFirst(".txt$", "");
			link = link.replaceAll("[+ ]", "");
			link = link.replaceFirst("^[^|]+\\|", "");
			if (name.equals(link)) return true;
		}
		return false;
	}
}
