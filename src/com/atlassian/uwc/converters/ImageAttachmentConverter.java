package com.atlassian.uwc.converters;

import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.ConfluenceSettingsForm;
import org.apache.log4j.Logger;
import org.apache.oro.text.regex.*;

import java.io.File;

/**
 * This converter scans the page and adds all images on it as attachments
 *
 * @todo - This is DokuWiki specific and should be changed to DokuImageAttachmentConverter
 * I'm not changing it just now because I don't want to break Doku wiki - BP
 *
 * @author Rex (Rolf Staflin)
 * @version $Id$
 */
public class ImageAttachmentConverter extends BaseConverter {

    static Logger log = Logger.getLogger(ImageAttachmentConverter.class);

    /**
     * Scans the page for image "tags" and attaches the images found.
     * The attachment directory field in ConfluenceSettingForm is used to find
     * the image files.
     * <p/>
     * If a file is not found the converter ignores it. This makes it possible to specify the .image
     * converter several times, in case the images are spread over several directories.
     *
     * @param page
     */
    public void convert(Page page) {

        Pattern pattern;
        try {
            PatternCompiler compiler = new Perl5Compiler();
            // TODO: This pattern needs to be more generic!
            // Unfortunately, the following does not work:
            // pattern = compiler.compile("!\\s*[^\\n\\r\\|!]+?\\s*[\\|!]");
            pattern = compiler.compile("!(\\s*[a-zA-Z0-9åäöÅÄÖ%+.:/\\\\_-]+?\\s*)(\\|[^\\!]*)?!");
        } catch (MalformedPatternException e) {
            log.error("Bad pattern.", e);
            return;
        }

        PatternMatcher matcher = new Perl5Matcher();
        String pageText = page.getOriginalText();
        PatternMatcherInput input = new PatternMatcherInput(pageText);

        while (matcher.contains(input, pattern)) {
            MatchResult result = matcher.getMatch();
            if (log.isDebugEnabled()) {
                log.debug("Match: \"" + result + "\", " +
                          "Group 1: \"" + result.group(1) + "\", " +
                          "Group 2: \"" + result.group(2) + "\"");
            }
            // Get the file name and trim off any whitespace at the ends
            // (it is important to do it here and not by shrinking the group
            // in the pattern, because we want to get rid of the spaces later.
            String filePath = result.group(1).trim();
            // TODO: This should be implemented cleaner:
            if (filePath.contains(":") && !File.separator.equals(":")) {
                // This is probably an external file accessed with a protocol like "http:"
                // Leave this match as it is!
                continue;
            }
            String baseDir = this.getAttachmentDirectory();
            File attachment = new File(baseDir + File.separator + filePath);
            page.addAttachment(attachment);

            // Remove any paths and spaces from the page links
            String quotedGroup = java.util.regex.Pattern.quote(result.group(1));
            //log.info("Replacing \"" + quotedGroup + "\" with \"" + attachment.getName() + "\"");
            pageText = pageText.replaceAll(quotedGroup, attachment.getName());
        }
        // Store the converted text.
        page.setConvertedText(pageText);
    }
}
