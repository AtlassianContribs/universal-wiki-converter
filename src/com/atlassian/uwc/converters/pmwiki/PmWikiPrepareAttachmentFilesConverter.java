package com.atlassian.uwc.converters.pmwiki;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * This class prepares PmWiki attachments and images for
 * sending to Confluence, but does not actually convert the page
 * text.
 * <p/>
 * What's nice is that the ConverterEngine is smart enough to allow
 * you make all the preparations to send attachments to Confluence, but
 * it doesn't actually do so until you hit the 'yes' - send to confluence button.
 */
public class PmWikiPrepareAttachmentFilesConverter extends BaseConverter {

    Logger log = Logger.getLogger("PmWikiPrepareAttachmentFilesConverter");
    static Perl5Compiler compiler = new Perl5Compiler();
    public static int totalAttachmentsFoundInPageText = 0;
    public static int totalAttachmentsQueuedForSending = 0;
    public static List allFilesNotFound = new ArrayList();
    public static List allFilesFound = new ArrayList();

    public void convert(Page page) {


        // scan the page and create a list of attachments
//        addNonBracketedAttachmentsToPage(page);
//        addBracketedAttachmentsToPage(page);

        List<String> attachmentFilePath = new ArrayList();
        // gather the matching file paths
        attachmentFilePath.addAll(findNonBracketedAttachments(page));
        attachmentFilePath.addAll(findBracketedAttachments(page));
        // try to locate the actual files by making sure the path is valid,
        // striping invalid chars and looking in different dirs
        List<File> attachmentFilesLocated = findFileAttachments(attachmentFilePath, page);
        if (attachmentFilesLocated == null) return;
        // finally add the attachments actually located to the page
        addAttachmentsToPage(attachmentFilesLocated, page);

        allFilesFound.addAll(attachmentFilesLocated);
        log.info("::: total attachments found: "+allFilesFound.size());
        log.info("::: total attachments NOT found: "+allFilesNotFound.size());
    }

    public Collection findNonBracketedAttachments(Page page) {
        List filePaths = new ArrayList();
        String patternRegEx = "[^\\[]Attach:[^ (\\012)\\|\\[,]+";
        List textMatchesList = getMatches(patternRegEx, page.getOriginalText());
        for (int i = 0; i < textMatchesList.size(); i++) {
            totalAttachmentsFoundInPageText++;
            boolean attachmentFileLocFound = false;
            String attachmentFileName = (String) textMatchesList.get(i);
            // chop off 'Attach:'
            attachmentFileName = attachmentFileName.substring(8);
            filePaths.add(attachmentFileName);
        }
        return filePaths;  // @author brendan.patterson
    }

    public Collection findBracketedAttachments(Page page) {
        List filePaths = new ArrayList();
        String patternRegEx = "\\[Attach:[^(\\012)]+";
        List textMatchesList = getMatches(patternRegEx, page.getOriginalText());
        for (int i = 0; i < textMatchesList.size(); i++) {
            totalAttachmentsFoundInPageText++;
            boolean attachmentFileLocFound = false;
            String attachmentFileName = (String) textMatchesList.get(i);
            // chop off after '|'
            int chopAfterLoc = attachmentFileName.indexOf("|");
            if (chopAfterLoc > 0) {
                attachmentFileName = attachmentFileName.substring(0, chopAfterLoc);
            }
            if (attachmentFileName.endsWith("|")) {
                attachmentFileName = attachmentFileName.substring(0, attachmentFileName.length() - 1);
            }
            // chop off end ']'
            chopAfterLoc = attachmentFileName.indexOf("]");
            if (chopAfterLoc > 0) {
                attachmentFileName = attachmentFileName.substring(0, chopAfterLoc);
            }

            // chop off 'Attach:'
            attachmentFileName = attachmentFileName.substring(8);
            int attachmentFileNameLen = attachmentFileName.length();
            char lastChar = attachmentFileName.charAt(attachmentFileNameLen - 1);
            if (lastChar < 33) {
                attachmentFileName = attachmentFileName.substring(0, attachmentFileNameLen - 1);
            }
            filePaths.add(attachmentFileName);
        }
        return filePaths;
    }

    public List findFileAttachments(List<String> attachmentFileTargetTexts, Page page) {
        boolean fileFound = false;
        List filesFound = new ArrayList();
        for (String fileNameText : attachmentFileTargetTexts) {
            fileNameText = fileNameText.trim();
            // chop off non-alpha char
            char lastChar = fileNameText.charAt(fileNameText.length() - 1);
            while (isCharNonAlpha(lastChar) && fileNameText.length() > 0) {
                // chop off non-alpha char
                fileNameText = fileNameText.substring(0, fileNameText.length() - 1);
                lastChar = fileNameText.charAt(fileNameText.length() - 1);
            }
            if (fileNameText.contains("Design/AustinSiteMap.pdf")){
                log.debug("breakpoint");
            }

            File file;
            String attachmentDir = this.getAttachmentDirectory();
            File attdir = new File(attachmentDir);
            if (attachmentDir == null || 
            		!new File(attachmentDir).exists() || 
            		!new File(attachmentDir).isDirectory()) {
            	String note = "Attachment Directory does not exist or is not a directory: '" +
            		attachmentDir + "'";
            	log.error(note);
            	addError(Feedback.BAD_SETTING, note, true);
            	return null;
            }

            // look in group
            String groupDir = page.getFile().getParentFile().getName();
            String fileLocToTry = attachmentDir + File.separator + groupDir + File.separator + fileNameText;
            file = new File(fileLocToTry);
            if (file.exists()) {
                filesFound.add(file);
                continue;
            }
            // look in path if it exists
            String tryFileNametext = fileNameText.replace("/", File.separator);
            fileLocToTry = attachmentDir + File.separator + fileNameText;
            file = new File(fileLocToTry);
            if (file.exists()) {
                filesFound.add(file);
                continue;
            }

            // feel around the rest of the path
            String pathParts[] = fileNameText.split("/");
            fileLocToTry = attachmentDir + File.separator + "Main" + File.separator + pathParts[pathParts.length-1];
            file = new File(fileLocToTry);
            if (file.exists()) {
                filesFound.add(file);
                continue;
            }
            fileLocToTry = attachmentDir + File.separator + pathParts[0] + File.separator + pathParts[pathParts.length-1];
            file = new File(fileLocToTry);
            if (file.exists()) {
                filesFound.add(file);
                continue;
            }
            if (pathParts.length==3) {
                fileLocToTry = attachmentDir + File.separator + pathParts[1] + File.separator + pathParts[pathParts.length-1];
            }
            file = new File(fileLocToTry);
            if (file.exists()) {
                filesFound.add(file);
                continue;
            }


            // look in Main
            fileLocToTry = attachmentDir + File.separator + "Main" + File.separator + fileNameText;
            file = new File(fileLocToTry);
            if (file.exists()) {
                filesFound.add(file);
                continue;
            }

            // the file was not found
            allFilesNotFound.add(fileNameText);
        }
        return filesFound;  // @author brendan.patterson
    }

    public boolean isCharNonAlpha(char c) {
        if ('A' <= c && c <= 'Z' || 'a' <= c && c <= 'z') {
            // this IS a valid alpha char so return false for the method
            return false;
        }
        return true;  // @author brendan.patterson
    }

    public void addAttachmentsToPage(List<File> attachmentFilesLocated, Page page) {

        for (File file : attachmentFilesLocated) {
            page.addAttachment(file);
        }
    }

    /**
     * There seem to be mainly 2 types of attachment patterns used in PmWiki
     * Attach:someFileName.jpeg   (if an image display, if a doc or something link)
     * [[Attach:someFileName.doc|Called something else]]
     * Here are some more examples:
     * Attach:resume.pdf
     * [[Attach:Test Test Doc.doc| i18n Bundling and Installer README ]]
     * in a table
     * ||Attach:JianfengR7aTaskBreakdown.xls Attach:lelong_R7_Breakdown.xls||1.5|| || ||3.5|| || ||
     *
     * @param page
     * @return
     */
//    protected void addNonBracketedAttachmentsToPage(Page page) {
//        Set retSet = new TreeSet();
//        // find attachment text which is not bracketed...just standalone Attach:
//        String patternRegEx = "[^\\[]Attach:[^ (\\012)\\|\\[,]+";
//        List textMatchesList = getMatches(patternRegEx, page.getOriginalText());
//        for (int i = 0; i < textMatchesList.size(); i++) {
//            totalAttachmentsFoundInPageText++;
//            boolean attachmentFileLocFound = false;
//            String attachmentFileName = (String) textMatchesList.get(i);
//            // chop off 'Attach:'
//            attachmentFileName = attachmentFileName.substring(8);
//            // file attachment paths can be really wacky - save the possible locations to search
//            String possibleLocations[] = attachmentFileName.split("/");
//            int attachmentFileNameLen = attachmentFileName.length();
//            if (attachmentFileName.endsWith(System.getProperty("line.separator"))) {
//                attachmentFileName = attachmentFileName.substring(0, attachmentFileNameLen - 2);
//            }
//            char lastChar = attachmentFileName.charAt(attachmentFileNameLen - 1);
//
//            if (lastChar < 33) attachmentFileName = attachmentFileName.substring(0, attachmentFileNameLen - 1);
//            String attachmentDir = confSettings.getAttachmentDirectory();
////            String fileName = page.getFile().getName();
//            String groupDir = page.getFile().getName();
//            groupDir = groupDir.substring(0, groupDir.indexOf("."));
//            String fileLoc = attachmentDir + File.separator + groupDir + File.separator + attachmentFileName;
//            fileLoc = replaceWithFileCorrectFileSeperator(fileLoc);
//            File attachmentFileToAdd = new File(fileLoc);
//            if (attachmentFileToAdd.exists()) {
//                attachmentFileLocFound = true;
//                totalAttachmentsQueuedForSending++;
//                page.addAttachment(attachmentFileToAdd);
//                continue;
//            } else {
//                log.info("Could not locate attachment: " + fileLoc + " for page: " + page.getName());
//            }
//            // try looking for the file in any part of the path
//            attachmentFileName = stripToJustFile(attachmentFileName);
//            for (String possibleLocation : possibleLocations) {
//                fileLoc = attachmentDir + File.separator + possibleLocation + File.separator + attachmentFileName;
//                attachmentFileToAdd = new File(fileLoc);
//                if (attachmentFileToAdd.exists()) {
//                    log.info("attachment file found at:  " + fileLoc);
//                    attachmentFileLocFound = true;
//                    totalAttachmentsQueuedForSending++;
//                    page.addAttachment(attachmentFileToAdd);
//                    break;
//                }
//            }
//            // still haven't found attachment try looking for the file under the Main dir.
//            fileLoc = attachmentDir + File.separator + "Main" + File.separator + attachmentFileName;
//            attachmentFileToAdd = new File(fileLoc);
//            if (attachmentFileToAdd.exists()) {
//                attachmentFileLocFound = true;
//                log.info("attachment file found at:  " + fileLoc);
//                totalAttachmentsQueuedForSending++;
//                page.addAttachment(attachmentFileToAdd);
//            }
//            if (!attachmentFileLocFound) {
//                log.info("Could not locate attachment: " + fileLoc + " for page: " + page.getName());
//            }
//        }
//    }

    /**
     * This method makes sure that if we're running the UWC on windows where
     * the paths are back slashes \  but the links
     * in the wiki sytax are forward slashes....make sure the UWC can find the files
     * <p/>
     * This is kind of a lot of code to do little work, could probably be minimized.
     *
     * @param fileLoc
     * @return
     */
//    private String replaceWithFileCorrectFileSeperator(String fileLoc) {
//        // use whatever setting is in the attachment dir. setting
//        boolean useBackslash = false;
//        boolean userForwardSlash = false;
//        int backSlashLoc = fileLoc.indexOf("\\");
//        int forwardSlashLoc = fileLoc.indexOf("/");
//        if (backSlashLoc >= 0 && forwardSlashLoc == -1) {
//            useBackslash = true;
//        } else if (backSlashLoc == -1 && forwardSlashLoc >= 0) {
//            userForwardSlash = true;
//        } else if (backSlashLoc >= 0 && forwardSlashLoc >= 0) {
//            if (backSlashLoc < forwardSlashLoc) {
//                useBackslash = true;
//            } else {
//                userForwardSlash = true;
//            }
//        }
//        String retString = fileLoc;
//        if (useBackslash) retString = fileLoc.replace('/', '\\');
//        if (userForwardSlash) retString = fileLoc.replace('\\', '/');
//        return retString;
//    }

//    protected void addBracketedAttachmentsToPage(Page page) {
//        Set retSet = new TreeSet();
//        // find attachment text
//        String patternRegEx = "\\[Attach:[^(\\012)]+";
//        List textMatchesList = getMatches(patternRegEx, page.getOriginalText());
//        for (int i = 0; i < textMatchesList.size(); i++) {
//            totalAttachmentsFoundInPageText++;
//            boolean attachmentFileLocFound = false;
//            String attachmentFileName = (String) textMatchesList.get(i);
//            // chop off after '|'
//            int chopAfterLoc = attachmentFileName.indexOf("|");
//            if (chopAfterLoc > 0) {
//                attachmentFileName = attachmentFileName.substring(0, chopAfterLoc);
//            }
//            if (attachmentFileName.endsWith("|")) {
//                attachmentFileName = attachmentFileName.substring(0, attachmentFileName.length() - 1);
//            }
//            // chop off end ']'
//            chopAfterLoc = attachmentFileName.indexOf("]");
//            if (chopAfterLoc > 0) {
//                attachmentFileName = attachmentFileName.substring(0, chopAfterLoc);
//            }
//
//            // chop off 'Attach:'
//            attachmentFileName = attachmentFileName.substring(8);
//            int attachmentFileNameLen = attachmentFileName.length();
//            char lastChar = attachmentFileName.charAt(attachmentFileNameLen - 1);
//            if (lastChar < 33) {
//                attachmentFileName = attachmentFileName.substring(0, attachmentFileNameLen - 1);
//            }
//            String attachmentDir = confSettings.getAttachmentDirectory();
//            String groupDir = page.getFile().getName();
//            groupDir = groupDir.substring(0, groupDir.indexOf("."));
//            String fileLoc = attachmentDir + File.separator + groupDir + File.separator + attachmentFileName;
//            fileLoc = replaceWithFileCorrectFileSeperator(fileLoc);
//            File attachmentFileToAdd = new File(fileLoc);
//            if (!attachmentFileToAdd.exists()) {
//                attachmentFileName = stripToJustFile(attachmentFileName);
//                fileLoc = attachmentDir + File.separator + "Main" + File.separator + attachmentFileName;
//                attachmentFileToAdd = new File(fileLoc);
//                if (attachmentFileToAdd.exists()) {
//                    log.info("attachment file found at:  " + fileLoc);
//                }
//            }
//            if (attachmentFileToAdd.exists()) {
//                attachmentFileLocFound = true;
//                totalAttachmentsQueuedForSending++;
//                page.addAttachment(attachmentFileToAdd);
//            }
//            if (!attachmentFileLocFound) {
//                log.info("Could not locate attachment: " + fileLoc + " for page: " + page.getName());
//            }
//        }
//    }

    /**
     * strip a file name including a path or whatever down to just the file name
     *
     * @param attachmentFileName
     * @return
     */
    public String stripToJustFile(String attachmentFileName) {
        String split[] = attachmentFileName.split("/");
        if (split.length > 1) {
            attachmentFileName = split[split.length - 1];
        }
        return attachmentFileName;
    }


    /**
     * Adapted from the Apache ORO examples.matchResultExample class
     * <p/>
     * Takes a regular expression and string as input and reports all the
     * pattern matches in the string.
     * <p/>
     */
    public List getMatches(String patternStr, String inputStr) {
        int groups;
        PatternMatcher matcher;
        org.apache.oro.text.regex.Pattern pattern = null;
        PatternMatcherInput input;
        org.apache.oro.text.regex.MatchResult result;
        List resultList = new ArrayList();

        // Create Perl5Compiler and Perl5Matcher instances.

        matcher = new Perl5Matcher();

        // Attempt to compile the pattern.  If the pattern is not valid,
        // report the error and exit.
        try {
            pattern = PmWikiPrepareAttachmentFilesConverter.compiler.compile(patternStr);
        } catch (MalformedPatternException e) {

            System.err.println("Bad pattern.");
            System.err.println(e.getMessage());
            return null;
        }

        // Create a PatternMatcherInput instance to keep track of the position
        // where the last match finished, so that the next match search will
        // start from there.  You always create a PatternMatcherInput instance
        // when you want to search a string for all of the matches it contains,
        // and not just the first one.
        input = new PatternMatcherInput(inputStr);

        // Loop until there are no more matches left.
        while (matcher.contains(input, pattern)) {
            // Since we're still in the loop, fetch match that was found.
            result = matcher.getMatch();

            // Perform whatever processing on the result you want.
            // Here we just print out all its elements to show how the
            // MatchResult methods are used.

            // The toString() method is provided as a convenience method.
            // It returns the entire match.  The following are all equivalent:
            //     System.out.println("Match: " + result);
            //     System.out.println("Match: " + result.toString());
            //     System.out.println("Match: " + result.group(0));
            String resultStr = result.toString();
            resultList.add(resultStr);
            log.debug("Match: " + resultStr);

            // Print the length of the match.  The length() method is another
            // convenience method.  The lengths of subgroups can be obtained
            // by first retrieving the subgroup and then calling the string's
            // length() method.
            log.debug("Length: " + result.length());

            // Retrieve the number of matched groups.  A group corresponds to
            // a parenthesized set in a pattern.
            groups = result.groups();
            log.debug("Groups: " + groups);

            // Print the offset into the input of the beginning and end of the
            // match.  The beinOffset() and endOffset() methods return the
            // offsets of a group relative to the beginning of the input.  The
            // begin() and end() methods return the offsets of a group relative
            // the to the beginning of a match.
            log.debug("Begin offset: " + result.beginOffset(0));
            log.debug("End offset: " + result.endOffset(0));
            log.debug("Groups: ");

            // Print the contents of each matched subgroup along with their
            // offsets relative to the beginning of the entire match.

            // Start at 1 because we just printed out group 0
            for (int group = 1; group < groups; group++) {
                log.debug(group + ": " + result.group(group));
                log.debug("Begin: " + result.begin(group));
                log.debug("End: " + result.end(group));
            }
        }
        return resultList;
    }
}
