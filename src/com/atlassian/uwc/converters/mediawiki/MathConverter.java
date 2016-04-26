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

	Pattern math = Pattern.compile("<math>(.*?)<\\/math>(?=(..|.$|$))", Pattern.DOTALL);
	Pattern label = Pattern.compile("\\\\label\\{(\\w+)}", Pattern.DOTALL);
	protected String convertMath(String input) {
		Matcher mathFinder = math.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (mathFinder.find()) {
			found = true;
			String mathbits = mathFinder.group(1);
			mathbits = escapePercents(mathbits);

			int len = sb.length();
			boolean newlinesBefore = (len==0) ||
					((len==1) && (sb.substring(0,1).equals("\n")))
					|| (sb.substring(len-2,len).equals("\n\n"));

			String after = mathFinder.group(2);
			len = after.length();
			boolean newlinesAfter = (len==0) ||
					((len==1) && (after.equals("\n")))
					|| (after.equals("\n\n"));

			String replacement;
			if (newlinesBefore && newlinesAfter) {
				// Looks line a mathblock

				String anchor = "";
				Matcher labelFinder = label.matcher(mathbits);
				if (labelFinder.find()) {
					anchor = labelFinder.group(1).trim();
					replacement = labelFinder.replaceAll(" ");
				}
				if (!anchor.isEmpty()) {
					anchor = ":anchor="+anchor;
				}

				replacement = "{mathblock"+anchor+"}\n" +
						"\\begin{eqnarray}\n" +
						mathbits +"\n" +
						"\\end{eqnarray}\n" +
						"{mathblock}";
			} else {
				replacement = "{mathinline}" + mathbits + "{mathinline}";
			}

			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			mathFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			mathFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern percents = Pattern.compile("[%]");
	private String escapePercents(String input) {
		Matcher percFinder = percents.matcher(input);
		if (percFinder.find()) {
			return percFinder.replaceAll("\\\\%");
		}
		return input;
	}

}
