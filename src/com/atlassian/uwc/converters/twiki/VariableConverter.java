package com.atlassian.uwc.converters.twiki;

import java.util.HashMap;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class VariableConverter extends PropertyConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertVariable(input);
		page.setConvertedText(converted);
	}

	protected String convertVariable(String input) {
		HashMap<String,String> vars = getVariables();
		input = replace(input, "%WEB%", vars.get("web"));
		input = replace(input, "%PUBURL%", vars.get("puburl"));
		input = replace(input, "%PUBURLPATH%", vars.get("puburlpath"));
		input = replace(input, "%ATTACHURL%", vars.get("attachurl"));
		input = replace(input, "%ATTACHURLPATH%", vars.get("attachurlpath"));
		input = replace(input, "~UWCTOKENURL~", vars.get("url"));
		input = replace(input, "~UWCTOKENCURRENTSPACE~", vars.get("spacekey"));
		
		return input;
	}

	private HashMap<String, String> getVariables() {
		String prefix = "vars-";
		HashMap<String, String> vars = getPropsWithPrefix(prefix);
		vars.put("spacekey", getProperties().getProperty("spacekey"));
		return vars;
	}

	protected String replace(String input, String before, String replace) {
		return RegexUtil.loopRegex(input, "\\Q" + before + "\\E", replace);
	}

}
