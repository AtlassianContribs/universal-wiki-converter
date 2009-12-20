package com.atlassian.uwc.converters.pmwiki;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Note: !! This class is no longer used !!
 * it is only being left here as reference
 * 
 * Converter - convert PmWiki links to Confluence links. A Java
 * class seems necessary here (or at least more maintainable)
 * because of the numerous types of links and their complexity.
 *
 * This was initially copied from Rolf Staflins DokuWiki converter
 *
 */
public class PmWikiAttachmentReferenceConverter extends BaseConverter {

    private static final String LINK_START = "[[";
    private static final String LINK_END = "]]";
    private static final String SEPARATOR = "|";
    private static final String SEPARATOR_ARROW = "->";

    /**
     * Converts any links from the PmWiki format to Confluence's format. Any links pointing to
     * other documents in the wiki are massaged further so that they point to the correct page title.
     * @param page A page with text to be converted.
     */
    public void convert(Page page) {
        assert page != null;
        assert page.getOriginalText() != null;

        String text = page.getOriginalText();

        int linkStart = text.indexOf(PmWikiAttachmentReferenceConverter.LINK_START);
        while (linkStart >= 0) {
            int linkEnd = text.indexOf(PmWikiAttachmentReferenceConverter.LINK_END, linkStart);
            if (linkEnd < 0) {
                break;
            }
            // grab the [[ text inside the link ]]
            String link = text.substring(linkStart + 2, linkEnd);
            int separator = link.indexOf(PmWikiAttachmentReferenceConverter.SEPARATOR);
            String linkText = null;
            String linkTarget = link.trim();
            // if there is [[ link | the name to show ]] type then assign
            // linkText and linkTarget
            if (separator >= 0) {
                // PmWiki has [[ target | link text]]
                linkText = link.substring(separator + 1).trim();
                linkTarget = link.substring(0, separator).trim();
            }

            separator = link.indexOf(PmWikiAttachmentReferenceConverter.SEPARATOR_ARROW);
            if (separator >= 0) {
                // PmWiki has [[ target | link text]]
                linkTarget = link.substring(separator + PmWikiAttachmentReferenceConverter.SEPARATOR_ARROW.length()).trim();
                linkText = link.substring(0, separator).trim();
            }

            // convert linkTarget to Confluence page.
            // PmWiki targets have several different forms, here we
            // have to standardize on one form and move those
            linkTarget = standardizeLinkTarget(linkTarget);
            StringBuffer newText = new StringBuffer("[");
            if (linkText != null) {
                newText.append(linkText);
                newText.append(PmWikiAttachmentReferenceConverter.SEPARATOR).append(linkTarget);
            } else {
                newText.append(linkTarget);
            }

            newText.append("]");

            text = text.substring(0, linkStart) + newText.toString() + text.substring(linkEnd + PmWikiAttachmentReferenceConverter.LINK_END.length());
            linkStart = text.indexOf(PmWikiAttachmentReferenceConverter.LINK_START);
        }
        page.setConvertedText(text);
    }

    /**
     * PmWiki link targets can be written many different ways.
     * From the docs:
     * [[Wiki sandbox]], [[wiki sandbox]], and [[WikiSandbox]] all display
     * differently but link to the same page - which is titled WikiSandbox.
     *
     * So we're going to massage everything to one format which also matches
     * the page names.
     *
     * @param linkTarget
     * @return
     */
    public String standardizeLinkTarget(String linkTarget) {
        // if there is an Attach: in the link then handle that only
        if (linkTarget.indexOf("Attach:")>=0) {
            linkTarget = linkTarget.replaceFirst("Attach:","^");
            return linkTarget;
        }

        String strArray[] = linkTarget.split(" ");
        StringBuffer sb = new StringBuffer();
        for (String s : strArray) {
            if (s.length()==0) continue;
            s = s.trim();
            String firstChar = s.substring(0,1).toUpperCase();
            sb.append(firstChar + s.substring(1));
        }
        return sb.toString();  //To change body of created methods use File | Settings | File Templates.
    }
}
