package com.atlassian.uwc.converters.dokuwiki;

import com.atlassian.uwc.ui.ConverterEngine;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.converters.BaseConverter;
import org.apache.log4j.Logger;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * A custom converter to turn DokuWiki's links into Confluence page names.
 *
 * <strong>NOTE:</strong> This class is heavily dependent on the page name
 * set by ConverterEngine.setupPages(). Any change there will probably force a change here.
 *
 * @author Rex (Rolf Staflin)
 * @version $Id$
 */
public class DokuWikiLinkConverter extends BaseConverter {
    private static Logger log = Logger.getLogger(DokuWikiLinkConverter.class);

    private static final String LINK_START = "[[";
    private static final String LINK_END = "]]";
    private static final String SEPARATOR = "|";

    /**
     * These are assumed to be protocols rather than DokuWiki namespaces.
     */
    private static final String[] protocols = {
            "file",
            "http",
            "https",
            "ftp",
            "mailto",
            "svn"
    };

    /**
     * Converts any links from the DokuWiki format to Confluence's format. Any links pointing to
     * other documents in the wiki are massaged further so that they point to the correct page title.
     * @param page A page with text to be converted.
     */
    public void convert(Page page) {
        assert page != null;
        assert page.getOriginalText() != null;

        String text = page.getOriginalText();

        int linkStart = text.indexOf(LINK_START);
        while (linkStart >= 0) {
            int linkEnd = text.indexOf(LINK_END, linkStart);
            if (linkEnd < 0) {
                break;
            }
            String link = text.substring(linkStart + 2, linkEnd);
            int separator = link.indexOf(SEPARATOR);
            String linkText = null;
            String linkTarget = link.trim();
            if (separator >= 0) {
                linkText = link.substring(separator + 1).trim();
                linkTarget = link.substring(0, separator).trim();

                // Remove any line breaks from the link text
                linkText = linkText.replaceAll("\r\n", " ");
                linkText = linkText.replaceAll("\r", " ");
                linkText = linkText.replaceAll("\n", " ");
            }

            if (isPageReference(linkTarget)) {
                // First of all, this may be a local reference (e.g., from
                // foo:bar you can link to foo:baz with [[baz]], and we need
                // to change that into [foo -- baz] because that's what the
                // baz page will have been renamed to.
                if (linkTarget.indexOf(":") < 0) {
                    // Get the name space from the current page name.
                    int lastSeparator = page.getName().lastIndexOf(ConverterEngine.CONFLUENCE_SEPARATOR);
                    if (lastSeparator >= 0) {
                        linkTarget = page.getName().substring(0, lastSeparator +
                                                                 ConverterEngine.CONFLUENCE_SEPARATOR.length()) +
                                     linkTarget;
                    }
                } else {
                    // Replace colons with the separator used in naming the pages.
                    linkTarget = linkTarget.replaceAll(":", ConverterEngine.CONFLUENCE_SEPARATOR);
                }
                // Replace underscores with spaces
                linkTarget = linkTarget.replaceAll("_", " ");
            } else {
                linkTarget = normalizeLink(linkTarget);
            }

            StringBuffer newText = new StringBuffer("[");
            if (linkText != null) {
                newText.append(linkText);
            } else {
                newText.append(link);
            }

            newText.append(SEPARATOR).append(linkTarget);
            newText.append("]");

            text = text.substring(0, linkStart) + newText.toString() + text.substring(linkEnd + LINK_END.length());
            linkStart = text.indexOf(LINK_START);
        }
        page.setConvertedText(text);

        // Lastly, we update the page name
        formatPageName(page);
    }

    /**
     * "Normalizes" a link by doing the following:
     *
     *   <li>Replacing all backslashes with forward slashes
     *       (otherwise Confluence strips them from the links)
     *   <li>Replacing spaces with "+"
     *   <li>Changing the protocol file: into http: (file: does not seem to work)
     *   <li>Adding the protocol http: to links starting with "//"
     * </ul>
     * @param linkTarget the link to be normalized
     * @return The normalized string
     */
    public static String normalizeLink(String linkTarget) {
        assert linkTarget != null;

        linkTarget = linkTarget.replaceAll("\\\\", "/");
/*        linkTarget = linkTarget.replaceAll("¿", "%C3%A5");
        linkTarget = linkTarget.replaceAll("¿", "%C3%A4");
        linkTarget = linkTarget.replaceAll("¿", "%C3%B6");
        linkTarget = linkTarget.replaceAll("¿", "%C3%85");
        linkTarget = linkTarget.replaceAll("¿", "%C3%84");
        linkTarget = linkTarget.replaceAll("¿", "%C3%96");
        linkTarget = linkTarget.replaceAll(" ", "+");
*/
        if (linkTarget.startsWith("file:")) {
            linkTarget = "http:" + linkTarget.substring(5);
        }
        if (linkTarget.startsWith("//")) {
            linkTarget = "http:" + linkTarget;
        }

        try {
            linkTarget = URLEncoder.encode(linkTarget, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            log.error("Could not URL-encode target!", ignored);
        }
        // Now the encoder has ruined the colons and slashes :P. Fix that.
        linkTarget = linkTarget.replaceAll("%3A", ":");
        linkTarget = linkTarget.replaceAll("%2F", "/");

        return linkTarget;
    }

    /**
     * Makes the page name prettier by removing the file name extension,
     * replacing underscores with spaces and finally converting the first
     * character into upper case. E.g., "my_page.txt" is converted into "My page".
     * @param page A page with the name set.
     */
    private void formatPageName(Page page) {
        assert page != null;
        assert page.getName() != null;

        String name = page.getName();
        // Strip trailing file name extension.
        if (name.endsWith(".txt")) {
            name = name.substring(0, name.length()-4);
        }
        // Replace underscores with spaces
        name = name.replaceAll("_", " ");

        // Casify the name
        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        page.setName(name);
    }

    /**
     * Determines if a link is a DokuWiki page reference or not.
     * Page references have the form [dir:][name] with one or more
     * [dir:] components, e.g., "path:to:a:page".
     *
     * The problem is that regular URL:s also contain colons; "mailto:foo" is not
     * a page reference, but "mail:foo" is. This method checks for some standard
     * protocol names and assumes that links that contain colons but do not start with
     * one of the protocol names are page references.
     *
     * To find out what protocols your DokuWiki contains, run this in a command prompt
     * at the wiki base document directory:
     *     grep -ohr "\[\[[0-9a-zA-Z]\*:" * | sort | uniq
     * Look through the resulting list and eliminate the matches that are DokuWiki name spaces.
     * The rest are protocols.
     *
     * @param target The link text.
     * @return True if and only if the text is a page reference.
     */
    public static boolean isPageReference(String target) {
        assert target != null;
        int colon = target.indexOf(':');

        if (colon < 0) {
            return true; // No colon in the string -- must be a local reference!
        }
        for (String protocol : protocols) {
            if (target.startsWith(protocol)) {
                return false; // This target uses an approved protocol
            }
        }
        return true;
    }
}
