package com.atlassian.uwc.converters.dokuwiki;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.ConfluenceSettingsForm;
import com.atlassian.uwc.ui.ConverterEngine;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.UWCForm2;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Pre-processes file names in images and attachments to that the ImageAttachmentConverter can
 * find them.
 * @author Rex
 * @version $Id$
 */
public class DokuWikiImageConverter extends BaseConverter {
    private Logger log = Logger.getLogger(DokuWikiLinkConverter.class);

    private static final String IMAGE_START = "{{";
    private static final String IMAGE_END = "}}";
    private static final String SEPARATOR = "|";
    private static final String QUALIFIER = "?";

    /**
     * Converts any file names in image tags from the DokuWiki format to Confluence's format.
     * The converter also handles other types of attachments.
     *
     * @param page A page with text to be converted.
     */
    public void convert(Page page) {
        assert page != null;
        assert page.getOriginalText() != null;

        if (log.isDebugEnabled()) {
            log.debug(">convert(" + page.getName() + ")");
        }

        String text = page.getOriginalText();

        int linkStart = text.indexOf(IMAGE_START);
        while (linkStart >= 0) {
            int linkEnd = text.indexOf(IMAGE_END, linkStart);
            if (linkEnd < 0) {
                break; // No end tag found
            }
            String link = text.substring(linkStart + IMAGE_START.length(), linkEnd);
            String filePath = link.trim();
            String linkText = "";
            int separator = link.indexOf(SEPARATOR);
            if (separator >= 0) {
                filePath = link.substring(0, separator).trim();
                linkText = link.substring(separator + 1).trim();

                // Remove any line breaks from the link text
                linkText = linkText.replaceAll("\r\n", " ");
                linkText = linkText.replaceAll("\r", " ");
                linkText = linkText.replaceAll("\n", " ");
            }
            String qualifiers = "";
            int qualifierIndex = link.indexOf(QUALIFIER);
            if (qualifierIndex >= 0) {
                filePath = link.substring(0, qualifierIndex);
                qualifiers = link.substring(qualifierIndex + 1);
            }

//            log.info("Link: \"" + link + "\", " +
//                    "Path: \"" + filePath + "\", " +
//                    "Linktext: \"" + linkText + "\", " +
//                    "Qualifiers: \"" + qualifiers + "\"");

            StringBuffer newLink = new StringBuffer();
            if (DokuWikiLinkConverter.isPageReference(filePath)) {
                // Replace colons (namespace separators) with the directory separator, since DokuWiki stores files in
                // subdirectories named after the namespace.
                String fileSeparator = File.separator;
                if ("\\".equals(fileSeparator)) {
                    // Escape the backslash
                    fileSeparator = "\\\\";
                }
                filePath = filePath.replaceAll(":", fileSeparator);
                addAttachment(filePath, page);

                // Now decide if this is an image or some other attachment that should be linked in stead
                File file = new File(filePath);
                if (isImage(file)) {
                    makeImageTag(newLink, file.getName(), qualifiers);
                } else {
                    // Not an image! Make this a link in stead!
                    filePath = makeAttachmentTag(filePath, newLink, linkText);
                }
            } else {
                // this is an external link. Is is an image?
                filePath = DokuWikiLinkConverter.normalizeLink(filePath);
                File file = new File(filePath.substring(Math.max(0, filePath.lastIndexOf("/"))));
                if (isImage(file)) {
                    makeImageTag(newLink, filePath, qualifiers);
                } else {
                    makeLinkTag(filePath, newLink, linkText);
                }
            }

            StringBuffer newText = new StringBuffer(filePath);
            newText.append(filePath).append(qualifiers);
            // log.info("New Link: " + newLink);
            text = text.substring(0, linkStart) +
                    newLink.toString() +
                    text.substring(linkEnd + IMAGE_END.length());
            linkStart = text.indexOf(IMAGE_START);
        }

        page.setConvertedText(text);
        log.debug("<convert()");
    }

    private boolean isImage(File file) {
    	String mimetype = ConverterEngine.determineContentType(file);
        return mimetype.startsWith("image");
    }

    private void makeLinkTag(String filePath, StringBuffer newLink, String linkText) {
        newLink.append("[");
        if (!"".equals(linkText)) {
            newLink.append(linkText).append("|");
        }
        newLink.append(filePath);
        newLink.append("]");
    }

    private void makeImageTag(StringBuffer newLink, String location, String qualifiers) {
        newLink.append("!").
                append(location);
        appendQualifiers(qualifiers, newLink);
        newLink.append("!");
    }

    private String makeAttachmentTag(String filePath, StringBuffer newLink, String linkText) {
        File file;
        filePath = DokuWikiLinkConverter.normalizeLink(filePath.trim());
        file = new File(filePath);
        newLink.append("[");
        if ("".equals(linkText)) {
            newLink.append("^")
                   .append(file.getName());
        } else {
            newLink.append(linkText)
                   .append("|^")
                   .append(file.getName());
        }
        newLink.append("]");
        return filePath;
    }

    private void appendQualifiers(String qualifiers, StringBuffer newLink) {
        if (qualifiers == null || "".equals(qualifiers)) {
            return;
        }
        newLink.append("|width=");
        int x = qualifiers.indexOf("x");
        if (x < 0) {
            newLink.append(qualifiers);
        } else {
            newLink.append(qualifiers.substring(0, x))
                   .append(", height=")
                   .append(qualifiers.substring(x + 1));
        }
    }

    private void addAttachment(String filePath, Page page) {
        // Add the attachment to the page object
        String baseDir = this.getAttachmentDirectory();
        File attachment = new File(baseDir + File.separator + filePath);
        page.addAttachment(attachment);
    }
}
