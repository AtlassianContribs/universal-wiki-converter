package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class MathConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertMath(input);
		page.setConvertedText(converted);
	}

	Pattern math = Pattern.compile("<math>(.*?)<\\/math>", Pattern.DOTALL);
	protected String convertMath(String input) {
		Matcher mathFinder = math.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (mathFinder.find()) {
			found = true;
			String mathbits = mathFinder.group(1);
			String replacement = "{latex}\n" +
					"\\begin{eqnarray}\n" +
					"{\n" + mathbits +"\n}\n" +
					"\\end{eqnarray}\n" +
					"{latex}";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			mathFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			mathFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
