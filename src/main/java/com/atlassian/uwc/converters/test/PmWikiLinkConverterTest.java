package com.atlassian.uwc.converters.test;
/**
 * Test to verify the PmWikiConverter is working. A little test first
 * coding is acutally a great way to get moving on these conversions.
 */

import junit.framework.*;
import com.atlassian.uwc.converters.pmwiki.PmWikiLinkConverter;
import com.atlassian.uwc.ui.Page;

import java.io.File;

public class PmWikiLinkConverterTest extends TestCase {
    PmWikiLinkConverter pmWikiLinkConverter;

    protected void setUp() throws Exception {
        super.setUp();
        pmWikiLinkConverter = new PmWikiLinkConverter();
    }

    /**
     * Tests that the Strings in the input array are properly converted the way
     * they should be in the output array
     * @throws Exception
     */
    public void testStandardizeLinkTarget() throws Exception {
        String inputText[] = {
                "who is your daddy",
                "aPage",
                " cowbell "
        };
        String standardizedText[] = {
                "WhoIsYourDaddy",
                "APage",
                "Cowbell"
        };

        for (int i = 0; i < standardizedText.length; i++) {
            String s = pmWikiLinkConverter.standardizeLinkTarget(inputText[i]);
            assertEquals(standardizedText[i], s);

        }
    }

    /**
     * Test all those PmWiki link types and make sure they get converted properly.
     * @throws Exception
     */
    public void testConvert() throws Exception {
        String inputText[] = {
                " here is some text with a link - [[ link to a page ]] yadda yadda",
                " here is some text with two links - [[ link to a page ]] yadda [[anotherLink]] yadda",
                " link to a page with some text - [[ link to a page | and some text ]] yadda [[anotherLink]] yadda",
                " link to a page with an arrow - [[ and some text -> link to a page ]] yadda [[anotherLink]] yadda",
                " link to an attachment - [[Attach:Yippe ki aye.doc]] yadda yadda",
                " link to an attachment also with text [[Attach:Yippe ki aye.doc|More fun]] yadda yadda",
        };
        String correctResultText[] = {
                " here is some text with a link - [LinkToAPage] yadda yadda",
                " here is some text with two links - [LinkToAPage] yadda [AnotherLink] yadda",
                " link to a page with some text - [and some text|LinkToAPage] yadda [AnotherLink] yadda",
                " link to a page with an arrow - [and some text|LinkToAPage] yadda [AnotherLink] yadda",
                " link to an attachment - [^Yippe ki aye.doc] yadda yadda",
                " link to an attachment also with text [More fun|^Yippe ki aye.doc] yadda yadda",
        };
        for (int i = 0; i < correctResultText.length; i++) {
            Page page = new Page(new File("dummy.txt"));
            page.setOriginalText(inputText[i]);
            pmWikiLinkConverter.convert(page);
            String converted = page.getConvertedText();
            assertEquals(correctResultText[i], converted);
        }
    }
}