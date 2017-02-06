package com.atlassian.uwc.converters.jspwiki;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * converts Jspwiki Definition List syntax to comparable Confluence syntax.
 * Note: Confluence doesn't have a special definition list syntax, but 
 * I can make something similar by using other existing syntax.
 * <br/>Example:
 * <br/>input = <br/>;term:definition
 * <br/>output = <br/>* _term_<br/>definition
 */
public class DefinitionListConverter extends BaseConverter {

	private static final String DEFVAL_EMPHCHAR = "_";
	private static final String DEFVAL_USEBULLET = "true";
	private static final String DEFVAL_USEINDENT = "false";
	private static final String PROPKEY_EMPHCHAR = "definition-lists-emphchar";
	private static final String PROPKEY_USEBULLET = "definition-lists-usebullet";
	private static final String PROPKEY_USEINDENT = "definition-lists-useindent";
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Definition Lists - start");
		
		String input = page.getOriginalText();
		String converted = convertDefinitionLists(input);
		page.setConvertedText(converted);
		
		log.info("Converting Definition Lists - complete");
	}
	String deflistJSP = "(?<=^|\n)" +	//zero width beginning of string or newline
						";" +			//semicolon delimiter 
						"\\s*" +		//optional whitespace
						"([^:]+?)" +	//not a colon until (group1)
						"\\s*" +		//optional whitespace
						":" +			//colon delimiter
						"\\s*" +		//optional whitespace
						"([^\n]+)";		//greedily capture until just before a newline (group2)
	String deflistConf = "* _{group1}_\n{group2}";
	protected String convertDefinitionLists(String input) {
		String deflistConf = createDeflistConf(shouldUseIndent(), shouldUseBullet(), getEmphChar());
		return RegexUtil.loopRegex(input, deflistJSP, deflistConf);
	}
	private String createDeflistConf(boolean useIndent, boolean useBullet, String emphChar) {
		String string = "";
		string += useBullet?"* ":"";
		string += emphChar + "{group1}" + emphChar + "\n";
		string += useIndent?"{indent}{group2}{indent}":"{group2}";
		return string;
	}
	private boolean shouldUseIndent() {
		return Boolean.parseBoolean(getProperties().getProperty(PROPKEY_USEINDENT, DEFVAL_USEINDENT));
	}
	private boolean shouldUseBullet() {
		return Boolean.parseBoolean(getProperties().getProperty(PROPKEY_USEBULLET, DEFVAL_USEBULLET));
	}
	private String getEmphChar() {
		return getProperties().getProperty(PROPKEY_EMPHCHAR, DEFVAL_EMPHCHAR);
	}

}
