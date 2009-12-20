package com.atlassian.uwc.converters.twiki;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Use in place of BaseConverter if you want some property handling convenience methods.
 *
 */
public abstract class PropertyConverter extends BaseConverter {

	public HashMap<String, String> getPropsWithPrefix(String prefix) {
		Properties props = getProperties();
		HashMap<String,String> vars = new HashMap<String, String>();
		for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			if (key.startsWith(prefix)) {
				String newkey = key.replaceFirst("^\\Q" + prefix + "\\E", "");            
				String value = props.getProperty(key);
				vars.put(newkey, value);
			}
		}
		return vars;
	}

}
