package com.atlassian.uwc.converters;

import com.atlassian.uwc.ui.ConverterEngine;

/**
 * Sometimes we really need the engine. Use sparingly!
 * @author Laura Kolker
 */
public abstract class RequiresEngineConverter extends BaseConverter {

	ConverterEngine engine;
	
	public void setEngine(ConverterEngine engine) {
		this.engine = engine;
	}
}
