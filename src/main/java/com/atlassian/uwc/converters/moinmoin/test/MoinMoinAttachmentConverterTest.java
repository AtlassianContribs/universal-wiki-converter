package com.atlassian.uwc.converters.moinmoin.test;

import com.atlassian.uwc.converters.moinmoin.MoinMoinAttachmentConverter;
import com.atlassian.uwc.ui.ConfluenceSettingsForm;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.UWCForm2;
import com.atlassian.uwc.prep.MoinMoinPreparation;

import junit.framework.TestCase;
import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.util.Set;

/**
 * Unit tests for {@link MoinMoinAttachmentConverter}.
 * @author Rolf Staflin (rstaflin)
 */
public class MoinMoinAttachmentConverterTest extends TestCase {
    private MoinMoinAttachmentConverter converter;
    private Page page;
    private static final String ATTACHMENT_DIR = "X:\\folder";

    protected void setUp() throws Exception {
        super.setUp();
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        converter = new MoinMoinAttachmentConverter();
        page = new Page(new File("dummy.txt"));
        page.setOriginalText("");
        page.setName("");
        page.setPath("");

        converter.setAttachmentDirectory(ATTACHMENT_DIR);
    }

    public void testConvertEmptyString() {
        page.setOriginalText("");
        converter.convert(page);
        assertEquals("", page.getConvertedText());
        assertEquals(0, page.getAttachments().size());
    }

    public void testConvertNoAttachment() {
        page.setOriginalText("foo");
        converter.convert(page);
        assertEquals("foo", page.getConvertedText());
        assertEquals(0, page.getAttachments().size());
    }

    public void testConvertPlainImage() {
        page.setOriginalText("attachment:foo.jpg");
        converter.convert(page);
        assertEquals("!foo.jpg!", page.getConvertedText());
        checkAttachment("attachments" + File.separator + "foo.jpg");
    }

    public void testConvertInlineImage() {
        page.setOriginalText("inline:foo.jpg");
        converter.convert(page);
        assertEquals("!foo.jpg!", page.getConvertedText());
        checkAttachment("attachments" + File.separator + "foo.jpg");
    }

    public void testConvertDrawing() {
        page.setOriginalText("drawing:foo");
        converter.convert(page);
        assertEquals("!foo.png!", page.getConvertedText());
        checkAttachment("attachments" + File.separator + "foo.png");
    }

    public void testConvertLinkedImage() {
        page.setOriginalText("inline:foo/bar.jpg");
        converter.convert(page);
        assertEquals("!foo^bar.jpg!", page.getConvertedText());
        assertEquals(0, page.getAttachments().size());
    }

    public void testConvertLinkedImageWithBrackets() {
        page.setOriginalText("before inline:[FooBar]/[BazQuux]/[helpImOutOfMetasyntacticVariables].jpg after");
        converter.convert(page);
        assertEquals("before !BazQuux^helpImOutOfMetasyntacticVariables.jpg! after", page.getConvertedText());
        assertEquals(0, page.getAttachments().size());
    }

    public void testConvertBoldFilename() {
        page.setOriginalText("before *attachment:helpImOutOfMetasyntacticVariables.pdf* after");
        converter.convert(page);
        assertEquals("before *[^helpImOutOfMetasyntacticVariables.pdf]* after", page.getConvertedText());
        checkAttachment("attachments" + File.separator + "helpImOutOfMetasyntacticVariables.pdf");
    }

    public void testConvertCharEndedFilename() {
    	for (char endingChar : MoinMoinAttachmentConverter.ATTACHMENT_ENDING_CHARS) {
    		page.setOriginalText("before attachment:helpImOutOfMetasyntacticVariables.pdf"
    				+ endingChar + " after");
    		converter.convert(page);
    		assertEquals("before [^helpImOutOfMetasyntacticVariables.pdf]"
    				+ endingChar + " after", page.getConvertedText());
    		checkAttachment("attachments" + File.separator + "helpImOutOfMetasyntacticVariables.pdf");
    	}
    }

    public void testConvertNamedAttachement() {
        page.setOriginalText("before [Better Builds with Maven|attachment:BetterBuildsWithMaven.pdf] after");
        converter.convert(page);
        assertEquals("before [Better Builds with Maven|^BetterBuildsWithMaven.pdf] after", page.getConvertedText());
        checkAttachment("attachments" + File.separator + "BetterBuildsWithMaven.pdf");
    }

    public void testConvertDotEndedFilename() {
        page.setOriginalText("before attachment:helpImOutOfMetasyntacticVariables.pdf. after dot");
        converter.convert(page);
        assertEquals("before [^helpImOutOfMetasyntacticVariables.pdf]. after dot", page.getConvertedText());
        checkAttachment("attachments" + File.separator + "helpImOutOfMetasyntacticVariables.pdf");
    }

    public void testConvertSubpageImage() {
        page.setOriginalText("inline:foo/bar/baz/quux.jpg");
        converter.convert(page);
        assertEquals("!baz^quux.jpg!", page.getConvertedText());
        assertEquals(0, page.getAttachments().size());
    }

    public void testConvertPlainPdf() {
        page.setOriginalText("a attachment:foo.pdf b");
        converter.convert(page);
        assertEquals("a [^foo.pdf] b", page.getConvertedText());
        checkAttachment("attachments" + File.separator + "foo.pdf");
    }

    //-----------------
    // Tests for the name and path conversion
    public void testConvertNameNormalCase() {
        page.setName("Test" +  MoinMoinPreparation.EXTENSION);
        converter.convert(page);
        assertEquals("", page.getPath());
        assertEquals("Test", page.getName());
    }

    public void testConvertNameEmptyString() {
        page.setName("");
        converter.convert(page);
        assertEquals("", page.getPath());
        assertEquals("", page.getName());
    }

    public void testConvertNameAndPath() {
        page.setName("foo(2f)bar" + MoinMoinPreparation.EXTENSION);
        converter.convert(page);
//        assertEquals("foo", page.getPath());
//        assertEquals("Bar", page.getName());
        assertEquals("", page.getPath());
        assertEquals("Foo bar", page.getName());
    }

    public void testConvertNameAndLongPath() {
        page.setName("fee(2f)fie(2f)foe(2f)foo(2f)fum" + MoinMoinPreparation.EXTENSION);
        converter.convert(page);
//        assertEquals("fee" + File.separator + "fie" + File.separator + "foe" + File.separator + "foo", page.getPath());
//        assertEquals("Fum", page.getName());
        assertEquals("", page.getPath());
        assertEquals("Fee fie foe foo fum", page.getName());
    }

    public void testConvertDeuxPoints() {
        page.setName("Something: blabla" + MoinMoinPreparation.EXTENSION);
        converter.convert(page);
        assertEquals("", page.getPath());
        assertEquals("Something - blabla", page.getName());
    }

    /**
     * This helper method checks that the page has an attachment with the supplied
     * file name in the correct directory.
     * @param filename The name of the file that's been attached
     */
    private void checkAttachment(String filename) {
        Set<File> files = page.getAttachments();
        assertEquals(1, files.size());
        for (File file : files) {
            assertEquals(ATTACHMENT_DIR + File.separator + filename, file.getPath());
        }
    }

    public void testConvertPageNameToUnicode() {
		assertEquals("é",
				MoinMoinAttachmentConverter.convertPageNameToUnicode("(c3a9)"));
		assertEquals("JeanBaptisteCatté",
				MoinMoinAttachmentConverter.convertPageNameToUnicode("JeanBaptisteCatt(c3a9)"));
		assertEquals("Jérôme_Topczenski",
				MoinMoinAttachmentConverter.convertPageNameToUnicode("J(c3a9)r(c3b4)me_Topczenski"));
		assertEquals("WLS_8.1_to_9.0_Migration's_Steps",
				MoinMoinAttachmentConverter.convertPageNameToUnicode("WLS_8(2e)1_to_9(2e)0_Migration(27)s_Steps"));
		assertEquals("Wiki/Make_Screenshots",
				MoinMoinAttachmentConverter.convertPageNameToUnicode("Wiki(2f)Make_Screenshots"));
	}
}
