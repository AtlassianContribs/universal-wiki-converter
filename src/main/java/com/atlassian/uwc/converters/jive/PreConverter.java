package com.atlassian.uwc.converters.jive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.IllegalNameConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class PreConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertPre(input);
		page.setConvertedText(converted);

	}

	Pattern preinpre = Pattern.compile("<pre[^>]*?__jive_macro_name=.code.>[<][!]\\[CDATA\\[<pre>([^<]+)<\\/pre>\\]\\]><\\/pre>");
	Pattern preincdata = Pattern.compile("([<][!]\\[CDATA\\[)?<pre[^>]*>");
	Pattern preincdata_end = Pattern.compile("<\\/pre>(\\]\\]>)?");
	Pattern cdatainpre = Pattern.compile("<pre[^>]*>[<][!]\\[CDATA\\[");
	Pattern cdatainpre_end = Pattern.compile("\\]\\]><\\/pre>");
	Pattern prewtitle = Pattern.compile("<pre[^>]*?title=\"([^\"]+)\"[^>]*>");
	protected String convertPre(String input) {
		
		input = RegexUtil.loopRegex(preinpre.matcher(input), input, "{{{group1}}}");
		input = removePreInPre(input);
		input = handleHtml(input);
		input = RegexUtil.loopRegex(prewtitle.matcher(input), input, "{code:title={group1}}");
		input = RegexUtil.loopRegex(cdatainpre.matcher(input), input, "{code}");
		input = RegexUtil.loopRegex(cdatainpre_end.matcher(input), input, "{code}");
		input = RegexUtil.loopRegex(preincdata.matcher(input), input, "{code}");
		input = RegexUtil.loopRegex(preincdata_end.matcher(input), input, "{code}");
		
		return input;
	}
	
	Pattern preinpre2 = Pattern.compile("(<\\/?pre[^>]*>)(.*?)(<\\/?pre[^>]*>)", Pattern.DOTALL);
	private String removePreInPre(String input) {
		Matcher preinpreFinder = preinpre2.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (preinpreFinder.find()) {
			String first = preinpreFinder.group(1);
			String contents = preinpreFinder.group(2);
			String second = preinpreFinder.group(3);
			String firstsub = first.substring(0,2);
			String secondsub = second.substring(0,2);
			if (firstsub.equalsIgnoreCase(secondsub)) {
				found = true;
				String replacement = (firstsub.endsWith("/"))?
						contents + second:
						first + contents;
				replacement = RegexUtil.handleEscapesInReplacement(replacement);
				preinpreFinder.appendReplacement(sb, replacement);
			}
		}
		if (found) {
			preinpreFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern precontents = Pattern.compile("(<pre[^>]*>)((?s).*?)(<\\/pre>)");
	Pattern tag = Pattern.compile("<(\\/?)(\\w+)([^>]*)>");
	Pattern inline = Pattern.compile("(b)|(strong)|(em)|(i)|(u)|(s)|(span)|(pre)", Pattern.CASE_INSENSITIVE);
	Pattern block = Pattern.compile("(p)|(div)|(br)|(blockquote)", Pattern.CASE_INSENSITIVE);
	private String handleHtml(String input) {
		Matcher preFinder = precontents.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (preFinder.find()) {
			found = true;
			String prefix = preFinder.group(1);//can't use zerowidth neg lookbehind for variable width
			String contents = preFinder.group(2);
			String suffix = preFinder.group(3);
			Matcher tagFinder = tag.matcher(contents);
			StringBuffer tagBuffer = new StringBuffer();
			boolean foundtag = false;
			String replacement = contents;
			while (tagFinder.find()) {
				foundtag = true;
				String end = tagFinder.group(1);
				String tagname = tagFinder.group(2);
				boolean isend = ((end != null) && (!"".equals(end))||"br".equalsIgnoreCase(tagname));
				if (block.matcher(tagname).matches()) {
					if (isend) tagFinder.appendReplacement(tagBuffer, "\n");
					else tagFinder.appendReplacement(tagBuffer, "");
				} else if (inline.matcher(tagname).matches()) {
					tagFinder.appendReplacement(tagBuffer, "");
				}
				//could be xml! could be other pre blocks! leave alone.
			}
			if (foundtag) {
				tagFinder.appendTail(tagBuffer);
				replacement = tagBuffer.toString();
			}
			replacement = prefix + replacement + suffix;
			replacement =  StringEscapeUtils.unescapeHtml(replacement); //handle html entities
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			preFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			preFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}


}
