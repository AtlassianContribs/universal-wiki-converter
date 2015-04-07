package com.atlassian.uwc.converters.moinmoin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.prep.MoinMoinPreparation;
import com.atlassian.uwc.ui.ConverterEngine;
import com.atlassian.uwc.ui.Page;

/**
 * Pre-processes file names in images and attachments to that the ImageAttachmentConverter can
 * find them. This class handles attachments linked with "attachment:", "inline:" and "drawing:",
 * but note that the drawings will lose any image maps and you will not be able to edit them in
 * Confluence like in MoinMoin.
 *
 * <p>Examples:
 * <ul>
 *   <li> "foo attachment:image.png bar" is converted to "foo !image.png! bar"
 *   <li> "foo attachment:page/subpage/image.png bar" is converted to "foo !subpage^image.png! bar".
 *        There is no need to specify the path since page names in Confluence are unique within a space.
 * </ul>
 *
 * <h3>Notes</h3>
 * <p>This converter only stores attachments that appear on the page (as a link or image). Any other
 * attachments will <em>NOT</em> be stored in Confluence, even if they are linked to from other pages.
 *
 * <p>The converter assumes that file and path names don't contain whitespace and that links are always
 * followed by whitespace (unless they are right at the end of the file). If that is not true for your
 * wiki, you will have to change the method <code>findLinkEnd()</code>.
 *
 * @author Rolf Staflin (rstaflin)
 */
public class MoinMoinAttachmentConverter extends BaseConverter {
    private Logger log = Logger.getLogger(MoinMoinAttachmentConverter.class);

    private static final String ATTACHMENT = "attachment:";
    private static final String INLINE = "inline:";
    private static final String DRAWING = "drawing:";

    private static final String ATTACHMENT_DIR = "attachments";

    private static final String UTF8_FILE_SEPARATOR_REGEX = "\\(2f\\)";
    private static final String UTF8_FILE_SEPARATOR = "(2f)";

    /**
     * Converts any attachment links from the MoinMoin format to Confluence's format and
     * attaches them to the page.
     *
     * @param page A page with text to be converted.
     */
    public void convert(Page page) {
    	//OLD HANDLING -- COMMENTING FOR NOW
//        if (log.isDebugEnabled()) {
//            log.debug(">convert(" + page.getName() + ")");
//        }
//        assert page != null;
//        assert page.getOriginalText() != null;
//
//        StringBuffer text = new StringBuffer(page.getOriginalText());
//
//        // Convert all the "attachment:" links
//        int linkStart = text.indexOf(ATTACHMENT);
//        while (linkStart >= 0) {
//            handleAttachment(text, linkStart, page, null);
//            linkStart = text.indexOf(ATTACHMENT);
//        }
//
//        // Convert all the "inline:" links
//        linkStart = text.indexOf(INLINE);
//        while (linkStart >= 0) {
//            handleAttachment(text, linkStart, page, null);
//            linkStart = text.indexOf(INLINE);
//        }
//
//        // Convert all the "drawing:" links, forcing the extension to be ".png"
//        linkStart = text.indexOf(DRAWING);
//        while (linkStart >= 0) {
//            handleAttachment(text, linkStart, page, ".png");
//            linkStart = text.indexOf(DRAWING);
//        }
    	
    	String input = page.getOriginalText();
    	input = convertAttachments(input, page);
        page.setConvertedText(input);

        // Fix the name and path of the page.
        setupNameAndPath(page);
        
        log.debug("<convert(" + page.getName() + ")");

        
        String pagename = page.getName();
        // Remove the extension, if present
        if (pagename.endsWith(MoinMoinPreparation.EXTENSION)) {
            pagename = pagename.substring(0, pagename.length() - MoinMoinPreparation.EXTENSION.length());
        }
       
        String baseDir = this.getAttachmentDirectory() + File.separator + pagename + File.separator + ATTACHMENT_DIR;
        
        log.info("Attachment Path: " + baseDir);
        
        File base = new File(baseDir);
        log.info("Attachment File: " + (base == null ? "(null)" : base.toString()) );
        if (base != null && base.exists()){
	        for(File f : base.listFiles()){
	        	if(f.isFile()){ 
	        		log.info("Adding Attachment:  " + f.getAbsolutePath() );
	        		page.addAttachment(f);
	        	}
	        }
        }
        
    }
    
    Pattern attachment = Pattern.compile("([{\\[])"+
    		"\\1?" +
    		"(?:(?:attachment)|(?:inline)):"+
    		"([^}\\]]+)" +
    		"([}\\]])\\3?");
    Pattern pagedelim = Pattern.compile("\\/([^/]*)");
    
    private String convertAttachments(String input, Page page) {
		Matcher attachmentFinder = attachment.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (attachmentFinder.find()) {
			found = true;
			String type = attachmentFinder.group(1);
			String target = attachmentFinder.group(2);
			String replacement = "";
			Matcher pagedelimfinder = pagedelim.matcher(target);
			boolean haspagename = false;
			String filename = target;
			if (pagedelimfinder.find()) {
				filename = pagedelimfinder.group(1);
				target = pagedelimfinder.replaceFirst("^" +filename);
				haspagename = true;
			}
			if (type.startsWith("{")) { //inline 
				replacement = "!" + target + "!";
			}
			else { //link
				if (!haspagename) target = "^" + target;
				replacement = "[" + target + "]";
			}
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			attachmentFinder.appendReplacement(sb, replacement);
			attachfile(filename, page);
		}
		if (found) {
			attachmentFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}



	private void attachfile(String filename, Page page) {
		String pagename = page.getName();
		pagename = pagename.replaceFirst("\\.txt$", "");
		String filePath = getAttachmentDirectory() 
			+ File.separator + pagename + File.separator +  "attachments" + File.separator + filename;
		File file = new File(filePath);
		if (!file.exists()) {
			log.error("Could not find attachment: " + filePath);
		}
		else page.addAttachment(file);
	}



	/**
     * Handles converting a link of the form "protocol:path/to/page/filename.ext".
     *
     * If the path is not present ("protocol:filename.ext") it refers to an attachment of the
     * current page. The actual file resides in path(2f)to(2f)the(2f)current(2f)page/attachments.
     * It is added as an attachment to the Confluence page being converted.
     *
     * If the extension is an image, the link is converted to "!page^filename.ext!",
     * otherwise the link is converted to "[page^filename.ext]".
     *
     * @param text The page text. The contents of this buffer is altered by this method.
     * @param linkStart index of the first letter of the protocol name
     * @param page A page object corresponding to the page being converted
     * @param forcedExtension If not <code>null</code>, this string is appended to the end of the filename.
     *        Example: ".png"
     */
    private void handleAttachment(StringBuffer text, int linkStart, Page page, String forcedExtension) {
        if (log.isDebugEnabled()) {
            log.debug(">handleAttachment(" + linkStart + ")");
        }

        int fileNameStart = text.indexOf(":", linkStart) + 1;
        int linkEnd = findLinkEnd(fileNameStart, text);

        // newLink will hold the new link markup.
        StringBuffer newLink = new StringBuffer();

        String filePath = text.substring(fileNameStart, linkEnd);
        boolean namedAttachment = filePath.endsWith("]");

        // Append the forced extension, if any
        if (forcedExtension != null && forcedExtension.length() > 0) {
            filePath += forcedExtension;
        }

        // Get rid of brackets inserted by the link syntax regex converters.
        filePath = filePath.replaceAll("\\[", "");
        filePath = filePath.replaceAll("\\]", "");

        // If the link leads to another page, linkPage will hold its name
        String linkPage = null;

        if (filePath.contains("/")) {
            log.debug(filePath + " innehï¿½ll /!");
            // This is a link to an attachment on some other page.
            // Get the page name and file name from the path
            String path = filePath.substring(0, filePath.lastIndexOf("/"));
            linkPage = (path.contains("/") ?
                                  path.substring(path.lastIndexOf("/") + 1) :
                                  path);
        } else {
            // This is a link to an attachment on this page. We need to
            // move the attachment to Confluence!
            log.debug(filePath + " innehï¿½ll INTE /!");

            String pagename = page.getName();
            // Remove the extension, if present
            if (pagename.endsWith(MoinMoinPreparation.EXTENSION)) {
                pagename = pagename.substring(0, pagename.length() - MoinMoinPreparation.EXTENSION.length());
            }
            filePath = pagename + File.separator + ATTACHMENT_DIR + File.separator + filePath;

            //Add the file as an attachment to the Confluence page
            addAttachment(filePath, page);
        }


        // Now decide if this is an image or some other attachment that should be linked instead.
        // create a link according to the type.
        File file = new File(filePath);
        if (isImage(file)) {
            makeImageTag(newLink, linkPage, file.getName());
        } else {
            makeAttachmentTag(newLink, linkPage, file.getName(), !namedAttachment);
        }

        // Make the change
        text.replace(linkStart, linkEnd, newLink.toString());

        if (log.isDebugEnabled()) {
            log.debug("<handleAttachment() -- new link: " + newLink);
        }
    }

    /**
     * Figure out where a link file name ends, given it's beginning.
     * This method assumes that the filename ends with whitespace.
     *
     * @param fileNameStart Index of the first letter of the file name
     * @param text The text to search through
     * @return The index of the first letter past the end of the file name.
     *         This may be past the end of the text if the file name is at
     *         the end of it.
     */
    private int findLinkEnd(int fileNameStart, StringBuffer text) {
        int linkEnd = fileNameStart;

        while (linkEnd < text.length()
        		&& !Character.isWhitespace(text.charAt(linkEnd))
        		&& !ATTACHMENT_ENDING_CHARS.contains(text.charAt(linkEnd))) {
            linkEnd++;
        }
        if (text.charAt(linkEnd - 1) == '.') linkEnd--;
        return linkEnd;
    }

    public static final Set<Character> ATTACHMENT_ENDING_CHARS = Collections.unmodifiableSet(new HashSet<Character>(Arrays.asList(new Character[] {
    		'*', ',', ';', ')', '(', '|', ':', ';', '!'
    })));

    private boolean isImage(File file) {
        String mimetype = ConverterEngine.determineContentType(file);
        return mimetype.startsWith("image");
    }

    private void makeImageTag(StringBuffer newLink, String linkPage, String fileName) {
        newLink.append("!");
        if (linkPage != null && !"".equals(linkPage)) {
            newLink.append(linkPage).
                    append("^");
        }
        newLink.append(fileName).
                append("!");
    }

    private void makeAttachmentTag(StringBuffer newLink, String linkPage, String fileName, boolean needSquareBrackets) {
    	if (needSquareBrackets) newLink.append("[");
        if (linkPage != null && !"".equals(linkPage)) {
            newLink.append(linkPage);
        }
        newLink.append("^");
        newLink.append(fileName);
        newLink.append("]");
    }

    /**
     * Adds the attachment to the page object
     * @param filePath path to the file that is to be added
     * @param page the page that is to receive the attachment
     */
    private void addAttachment(String filePath, Page page) {
        String baseDir = this.getAttachmentDirectory();
        File attachment = new File(baseDir + File.separator + filePath);
        page.addAttachment(attachment);
    }

    /**
     * Changes the name of a page.
     * <code>MoinMoinPreparation</code> stores the page files
     * as "path(2f)to(2f)the(2f)page.uwc". This method changes the
     * page name to "page" and sets the page path to
     * "path/to/the".
     *
     * @param page the page object to set up.
     */
    private void setupNameAndPath(Page page) {
        if (log.isDebugEnabled()) {
            log.debug(">setupNameAndPath(" + page.getName() + ")");
        }
        String name = page.getName();
        if (name == null) {
            name = "";
        }

        if (name.endsWith(MoinMoinPreparation.EXTENSION)) {
            name = name.substring(0, name.length() - MoinMoinPreparation.EXTENSION.length());
        }

        // sub-pages managment
        if (name.contains(UTF8_FILE_SEPARATOR)) {
        	String newName = name.replaceAll(UTF8_FILE_SEPARATOR_REGEX, " ");
        	log.info(name.replaceAll(UTF8_FILE_SEPARATOR_REGEX, "/")
        			+ " renamed to: \"" + newName + "\".");
        	name = newName;
        }

        name = convertPageNameToUnicode(name);
        name = name.replaceAll("_", " ");

        // ':' is illegal in a Confluence page name.
        name = name.replaceAll(":", " -");

        String path = "";
        int pathEnd = name.lastIndexOf(File.separator);
        if (pathEnd >= 0) {
            path = name.substring(0, pathEnd);
            name = name.substring(pathEnd + 1);
        }
        page.setPath(path);

        // Finally start the page name with an uppercase character
        if (name.length() > 1) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        page.setName(name);

        if (log.isDebugEnabled()) {
            log.debug("<setupNameAndPath() Path: " + page.getPath() + ", Name: \"" + page.getName() + "\"");
        }
    }

    /** Needed for pages only: attachments names are already Unicode. */
    public static String convertPageNameToUnicode(String asciiName) {
    	char[] asciiNameChars = asciiName.toCharArray();
    	StringBuilder result = new StringBuilder(asciiName.length());
    	int i = 0;
    	try {
    		while (i < asciiNameChars.length) {
    			if (asciiNameChars[i] != '(') {
    				result.append(asciiNameChars[i++]);
    			}
    			else {
    				List<Byte> utf8Bytes = new ArrayList<Byte>();
    				while (asciiNameChars[++i] != ')') {
    					StringBuilder hexValue = new StringBuilder(4);
    					hexValue.append("0x");
    					hexValue.append(asciiNameChars[i]);
    					hexValue.append(asciiNameChars[++i]);
    					Integer intValue = Integer.decode(hexValue.toString());
    					utf8Bytes.add(intValue.byteValue());
    				}
    				byte[] utf8ByteArray = new byte[utf8Bytes.size()];
    				for (int k = 0; k < utf8ByteArray.length; k++) {
    					utf8ByteArray[k] = utf8Bytes.get(k);
					}
    				result.append(new String(utf8ByteArray, "utf-8"));
    				i++;
    			}
    		}
    		return result.toString();
    	} catch (Exception e) {
    		e.printStackTrace();
    		return asciiName;
    	}
    }

}
