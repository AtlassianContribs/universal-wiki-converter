package com.atlassian.uwc.converters.test;
/**
 * Created by IntelliJ IDEA.
 * User: brendan
 * Date: Jan 7, 2006
 * Time: 5:43:11 AM
 * To change this template use File | Settings | File Templates.
 */

import junit.framework.*;
import com.atlassian.uwc.converters.PerlConverter;
import com.atlassian.uwc.ui.Page;

import java.io.File;

public class PerlConverterTest extends TestCase {
    PerlConverter perlConverter;
    Page page;

    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
        page = new Page(new File("dummy"));
    }

    public void testConvert() throws Exception {
        String regex = "s/^---\\s*$/----/g";
        perlConverter = (PerlConverter) PerlConverter.getPerlConverter(regex);
        page.setOriginalText("---  ");
        perlConverter.convert(page);
        assertEquals("----", page.getConvertedText());
    }
}