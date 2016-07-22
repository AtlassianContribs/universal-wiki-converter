package com.atlassian.uwc.converters;

import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.util.TokenMap;

/**
 * Simply run the detokenizer
 */
public class DetokenizerConverter extends BaseConverter {
    public void convert(Page page) {
    	log.debug("Running Detokenizer");
        String wikiText = page.getOriginalText();
        wikiText = TokenMap.detokenizeText(wikiText);
        page.setConvertedText(wikiText);
    }
}
