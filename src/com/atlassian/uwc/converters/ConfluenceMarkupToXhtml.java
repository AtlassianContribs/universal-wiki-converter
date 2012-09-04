package com.atlassian.uwc.converters;

import com.atlassian.uwc.ui.Page;

/**
 * Transforms the confluence markup to xhtml. 
 * The engine will do this by default, so only use this class if you need the transformation as a middle step.
 * 
 * Should accompany this class with the property:
 * engine-markuptoxhtml.property=false
 * so that we don't run it twice.
 * @author Laura Kolker
 */
public class ConfluenceMarkupToXhtml extends RequiresEngineConverter {
	
	@Override
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = this.engine.markupToXhtml(input);
		if (converted != null)
			page.setConvertedText(converted);
	}
}
