package com.atlassian.uwc.converters.vqwiki;

	import java.io.File;
import java.util.HashSet;
	import java.util.Set;
	import java.util.TreeSet;
	import java.util.Vector;
	import java.util.regex.Matcher;
	import java.util.regex.Pattern;

	import org.apache.log4j.Logger;

	import com.atlassian.uwc.converters.BaseConverter;
	import com.atlassian.uwc.ui.ConfluenceSettingsForm;
import com.atlassian.uwc.ui.Page;
//deprecated : import com.atlassian.uwc.ui.UWCForm2;

	/**
	 * New 1.
	 * @author P.Hunter
	 *
	 */
	public class AttachmentConverter extends BaseConverter {
		Logger log = Logger.getLogger(this.getClass()); 
		ConfluenceSettingsForm confSettings = null;

		//CONSTANTS - regex patterns
		protected static final String DOTALL = "(?s)"; // flag to enable dotall mode ("." includes newline in search)

		// - need to work out how to capture attachments in tables::
//		private static Pattern attachPattern = Pattern.compile("attach:([^#|]*)[#]*[\n]");

		//private static Pattern attachPattern = Pattern.compile("attach:(.*)[\n]");
		private static Pattern attachPattern = Pattern.compile("attach:(\\w.*\\w)");
		private static final String FILE_SEP = System.getProperty("file.separator");
		

		public void convert(Page vqwikiPage) {

			log.info("Converting VQwiki Attachments -- starting");

			String pageAsTextString = vqwikiPage.getOriginalText();   // input (original) page text
			String convertedPage = "";  // write page to this output string, after successful conversion

			Matcher vqAttachFinder = attachPattern.matcher( pageAsTextString );	
			StringBuffer sb  = new StringBuffer();
			
			String attachmentDir = this.getAttachmentDirectory();
			//Set<File> pageAttachments = new HashSet<File>();

			// scan the page and create a list of attachments
	        // addAttachmentsToPage(vqwikiPage, attachmentDir);

			log.info("Converting VQwiki Attachments -- about to loop...");
			// for each attachment encountered ...
			while (vqAttachFinder.find()) {
				String attachmentName = vqAttachFinder.group(1);
				log.info("** -> attachment:" + attachmentName );
				// write some code to find the attachments for this page as Files
				File attachmentFile = new File(attachmentDir + FILE_SEP + attachmentName);
				// add the attachments to the page, this queues the files to be attached to the Confluence page upon conversion
				vqAttachFinder.appendReplacement(sb, "[^" + attachmentName + "]");
				//TODO
				vqwikiPage.addAttachment(attachmentFile);
				log.info("** -> attached:" + attachmentName );
			}

			// terminate converted text string and pass back to calling program
			vqAttachFinder.appendTail(sb);
			convertedPage = sb.toString();

			vqwikiPage.setConvertedText(convertedPage);
			log.info("Converting VQwiki Attachments -- complete");

		}
		
	    /**
	     * determines which attachments are sought by the page, and attaches them
	     * @param page object to attach pages to
	     */
	    protected void addAttachmentsToPage(Page page, String attachmentDir) {
	    	//what attachments are we looking for?
	    	log.debug("Finding Attachments...");
	    	Vector soughtAttachments = getSoughtAttachmentNames(page);
	    	if (soughtAttachments == null || soughtAttachments.size() == 0)
	    		 return;
	    	
	    	//get filelist from the attachment directory
	    	log.debug("Examining File Directory");
	        File attachmentPageDir = new File(attachmentDir);
	        File files[] = attachmentPageDir.listFiles();
	        if (files==null) {
	            log.info("no attachment files found in directory: "+attachmentDir);
	            return;
	        }
	        
	        // add sought after attachments. This is a recursive method.
	        log.debug("Attaching files to page");
	        addAttachments(page, files, soughtAttachments);
	    }

		/**
		 * recursively look through the images directory for the desired files
		 * and attach them to the given page
		 * @param page object files will be attached to
		 * @param files Array of a directory's files
		 * @param soughtFilenames filenames we are looking to attach
		 */
		private void addAttachments(Page page, File[] files, Vector soughtFilenames) {
			for (File file : files) {
				//check for existence
	            if (!file.exists()) continue;
	            //check for recursion
	            if (file.isDirectory()) {
	            	File moreFiles[] = file.listFiles();
	            	if (moreFiles == null)
	            		continue;
	            	addAttachments(page, moreFiles, soughtFilenames);
	            	continue;
	            }
	            //existing non-Directory file?
	            String filename = file.getName();
	            //is it a file we want to attach?
	            if (foundFile(soughtFilenames, filename)) {
	            	//attach the file
		            log.debug("adding attachment: " + file.getName());
		            page.addAttachment(file);
	            }
	        }
		}

		/**
		 * checks if the given filename can be found in the given vector.
		 * The search is case insensitive. 
		 * @param soughtFilenames
		 * @param filename
		 * @return true if filename is found to be in soughtFilenames
		 */
		protected boolean foundFile(Vector<String> soughtFilenames, String filename) {
			boolean found = soughtFilenames.contains(filename); 
			if (found) return found;
			// check for case insensitivity 
			Pattern caseInsensitiveFilename = Pattern.compile(filename, Pattern.CASE_INSENSITIVE);
			for (String soughtFile : soughtFilenames) {
				Matcher fileFinder = caseInsensitiveFilename.matcher(soughtFile);
				if (fileFinder.matches()) return true;
			}
			return false;
		}

		/**
		 * Determine which images the page is going to need.
		 * Mediawiki images are not associated with a particular page.
		 * @param page the given page
		 * @return String Vector of image names
		 */
		protected Vector<String> getSoughtAttachmentNames(Page page) {
			Vector<String> names = new Vector<String>();
			Set<String> nameSet = new TreeSet<String>();
			String pageText = page.getOriginalText();
			nameSet = getNamesFromImageSyntax(nameSet, pageText);
			nameSet = getNamesFromLinkSyntax(nameSet, pageText);
			names.addAll(nameSet);
			log.debug("found attachment names: " +names.toString());
			return names;
		}

		protected Set<String> getNamesFromImageSyntax(Set<String> nameSet, String pageText) {
			Pattern image = Pattern.compile("!([^!|]+)(?:\\|[^!]+)?!");
			Matcher imageFinder = image.matcher(pageText);
			while (imageFinder.find()) {
				String name = imageFinder.group(1);
				nameSet.add(name);
			}
			return nameSet;
		}
		
		String linkSyntax = 
			"\\[" +			//opening left bracket
			"(" +			//start group
				"?:" +		//but don't capture it
				"[^^]" +	//not a carat
				"[^|]+" +	//anything but a pipe until
				"\\|" +		//a pipe
			")" +			//end group
			"?" +			//and make that group optional
			"\\^" +			//a carat as a part of the string
			"(" +			//start capturing (group1)
				"[^\\]]+" +	//anything but a right bracket until
			")" +			//end capturing (group1)
			"\\]";			//right bracket
		protected Set<String> getNamesFromLinkSyntax(Set<String> nameSet, String pageText) {
			Pattern image = Pattern.compile(linkSyntax);
			Matcher imageFinder = image.matcher(pageText);
			while (imageFinder.find()) {
				String name = imageFinder.group(1);
				nameSet.add(name);
			}
			return nameSet;
		}
	}

