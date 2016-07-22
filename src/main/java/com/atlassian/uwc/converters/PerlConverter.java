package com.atlassian.uwc.converters;

import org.apache.oro.text.perl.Perl5Util;
import com.atlassian.uwc.ui.Page;

import java.util.HashMap;

/**
 * This PERLConverter handles all the regular expressions whose 'key values'
 * end with .perl. 
 */
public class PerlConverter extends BaseConverter {

    private static HashMap perlConverterCache = new HashMap();

    Perl5Util util = new Perl5Util();

    private PerlConverter() {
    }

    public void convert(Page page) {
        String original = page.getOriginalText();
        String result = util.substitute(value, original);
        page.setConvertedText(result);
    }

    /**
     * here we're handing back an existing class if it's in the
     * cache and creating it if not.
     *
     * @return
     */
    public static Converter getPerlConverter(String value) {
        if (perlConverterCache.containsKey(value)) {
            return (Converter) perlConverterCache.get(value);
        }

        PerlConverter instance = new PerlConverter();
        instance.setValue(value);
        perlConverterCache.put(value, instance);


        return instance;  //To change body of created methods use File | Settings | File Templates.
    }
}
