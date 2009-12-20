package com.atlassian.uwc.converters.jotspot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Makes sure nested lists are in a parsable format:
 * <br/>Converts to:<br/>
 * &lt;ul&gt;
 * &lt;li&gt;a&lt;/li&gt;
 * &lt;ul&gt;&lt;li&gt;1&lt;/li&gt;
 * &lt;/ul&gt;
 * &lt;/ul&gt;
 * <br/>
 * From:
 * <br/>
 * &lt;ul&gt;
 * &lt;li&gt;a&lt;ul&gt;&lt;li&gt;1&lt;/li&gt;
 * &lt;/ul&gt;&lt;/li&gt;
 * &lt;/ul&gt;
 * 
 * @author Laura Kolker
 */
public class ListPreprocessor extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	String openingTagAttributes = "[^>]*";
	String itemTag = "<li" + openingTagAttributes + ">";
	String listTag = "<[uo]l" + openingTagAttributes + ">";
	String nlDelim = "\n";
	String nlReplace = "\n";

	
	public void convert(Page page) {
		log.debug("List PreProcessor - starting");
		String input = page.getOriginalText();
		String converted = input;
		
		//make sure incoming lists are parsable
		converted = preProcessLists(input);
		log.debug("converted = " + converted);
		
		page.setConvertedText(converted);
		log.debug("List PreProcessor - complete");
	}

	
	/**
	 * prepares list HTML for the list parser.
	 * <br/>
	 * Includes:
	 * <ul>
	 * <li/> adds newlines between ul, ol, and li elements
	 * <li/> requires closing &lt;/li&gt; tags before opening nested lists
	 * <li/> removes undesireable tags we're not converting within the context of a list item: 
	 * &lt;p&gt; &lt;b&gt; &lt;font&gt; &lt;br&gt;
	 * These should either be converted already, or are not useful as part of a list item.
	 * <li/> preserves non-list, non-bold uses of the * symbol
	 * </ul>
	 * @param input conversion file contents
	 * @return conversion file contents with parsable lists
	 */
	protected String preProcessLists(String input) {
		//Step 0 make sure nlDelim accurately reflects input
		determineNlDelim(input);
		
		//Step 1 add newlines before every ul and li.
		input = addNewlinesBeforeOpeners(input);
		
		//Step 1.5 - disallow multiple newlines
		input = disallowMultipleNewlines(input);
		
		//Step 2 disallow lines that start with li to not end with /li
		input = disallowLostClosingLI(input);

		//Step 2.5 disallow lines that are  </li>\n</li>
		input = disallowExtraNewlineLI(input);
		
		//Step 3 removing dangling closers
		input = removeExtraClosingLI(input);
		
		//Step 4 final newlines
		input = addFinalNewlines(input);
		
		//Step 4.5 make sure there are no </ul>\n<ul> constructs (converter can't chunk those)
		input = addChunkableWhitespace(input);
			
		//Step 5 clean up extra whitespace
		input = cleanWhitespace(input);

		//Step 6 make safe any non-list Jotspot syntax
		input = handleNonListNonBoldStars(input);

		//Step 7 paragraph & font tags do not belong here!
		input = removeForbiddenTags(input);
		
		return input;
	}
	

	Pattern rnl = Pattern.compile("\r\n");
	private void determineNlDelim(String input) {
		Matcher rnlFinder = rnl.matcher(input);
		if (rnlFinder.find()) { //using Windows delimiters!
			nlDelim = "(?:\r\n)"; 
			nlReplace = "\r\n";
			//need to recompile some patterns
		}
		recompilePatternsWithNewlines();
	}


	private void recompilePatternsWithNewlines() {
		nonlist = "(^|" + nlDelim + "|(?:<br\\s*\\/>))\\*(?=[^\n*]*(" + nlDelim + "|$))";
		listTransformNonList = Pattern.compile(nonlist);
		extraWS = "" + nlDelim + "?( )+";
		listTransformLessWS = Pattern.compile(extraWS);
		endTagNL = "(<\\/(?:(?:li)|(?:[uo]l))>)(?!" + nlDelim + ")";
		listTransformAddEnd = Pattern.compile(endTagNL);
		danglingClosersAfterNL = "<\\/li>" + nlDelim + "<\\/li>";
		listTransformClosingDanglersAfterNL = Pattern.compile(danglingClosersAfterNL);
		noListEnd = "(" + nlDelim + ""+ itemTag + ")(.*?)(?<!(?:<\\/li>))(" + nlDelim + ")";
		listTransformNoEnd = Pattern.compile(noListEnd);
		multNL = "" + nlDelim + "+";
		listTransformMultNL = Pattern.compile(multNL);
		noNL = "(?<!^|(?:" + nlDelim + "))((?:" + itemTag + ")|(?:" + listTag + "))";
		listTransformNoNL = Pattern.compile(noNL);
	}


	String nonlist = "(^|" + nlDelim + "|(?:<br\\s*\\/>))\\*(?=[^\n*]*(" + nlDelim + "|$))";
	Pattern listTransformNonList = Pattern.compile(nonlist);

	private String handleNonListNonBoldStars(String input) {
		String replacement;
		Matcher transformer;
		replacement = "\\\\*";
		transformer = listTransformNonList.matcher(input);
		if (transformer.find()) {
			String pre = transformer.group(1);
			input = transformer.replaceAll(pre + replacement);
		}
		return input;
	}
	
	
	String extraWS = "" + nlDelim + "?( )+";
	Pattern listTransformLessWS = Pattern.compile(extraWS);

	private String cleanWhitespace(String input) {
		String replacement;
		Matcher transformer;
		replacement = "$1";
		transformer = listTransformLessWS.matcher(input);
		if (transformer.find()) {
			input = transformer.replaceAll(replacement);
		}
		return input;
	}
	
	String endTagNL = "(<\\/(?:(?:li)|(?:[uo]l))>)(?!" + nlDelim + ")";
	Pattern listTransformAddEnd = Pattern.compile(endTagNL);

	private String addFinalNewlines(String input) {
		String replacement;
		Matcher transformer;
		replacement = "$1" + nlReplace + "";
		transformer = listTransformAddEnd.matcher(input);
		if (transformer.find()) {
			input = transformer.replaceAll(replacement);
		}
		return input;
	}
	
	String danglingClosers = "(<\\/[ou]l>)<\\/li>";
	Pattern listTransformClosingDanglers = Pattern.compile(danglingClosers);

	private String removeExtraClosingLI(String input) {
		String replacement;
		Matcher transformer;
		replacement = "" + nlDelim + "$1";
		transformer = listTransformClosingDanglers.matcher(input);
		if (transformer.find()) {
			input = transformer.replaceAll(replacement);
		}
		return input;
	}
	
	String danglingClosersAfterNL = "<\\/li>" + nlDelim + "<\\/li>";
	Pattern listTransformClosingDanglersAfterNL = Pattern.compile(danglingClosersAfterNL);

	private String disallowExtraNewlineLI(String input) {
		String replacement;
		Matcher transformer;
		replacement = "</li>";
		transformer = listTransformClosingDanglersAfterNL.matcher(input);
		if (transformer.find()) {
			input = transformer.replaceAll(replacement);
		}
		return input;
	}


	String noListEnd = "(" + nlDelim + ""+ itemTag + ")(.*?)(?<!(?:<\\/li>))(" + nlDelim + ")";
	Pattern listTransformNoEnd = Pattern.compile(noListEnd);
	Pattern endListTag = Pattern.compile("<\\/li>");

	private String disallowLostClosingLI(String input) {
		String replacement;
		Matcher transformer;
		replacement = "$1$2<\\/li>$3";
		transformer = listTransformNoEnd.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (transformer.find()) {
			String insides = transformer.group(2);
			//handles case where extra post text gets an accidental <li/> appended
			//see JavaRegexJunitTest.testTableEndProblem
			Matcher doubleCheckListEnd = endListTag.matcher(insides);
			if (!doubleCheckListEnd.find()) {
				transformer.appendReplacement(sb, replacement);
			}
		}
		transformer.appendTail(sb);
		input = sb.toString();
		return input;
	}
	
	String multNL = "" + nlDelim + "+";
	Pattern listTransformMultNL = Pattern.compile(multNL);
	
	private String disallowMultipleNewlines(String input) {
		String replacement;
		Matcher transformer;
		replacement = "" + nlReplace + "";
		transformer = listTransformMultNL.matcher(input);
		if (transformer.find()) {
			input = transformer.replaceAll(replacement);
		}
		return input;
	}
	
	String noNL = "(?<!^|(?:" + nlDelim + "))((?:" + itemTag + ")|(?:" + listTag + "))";
	Pattern listTransformNoNL = Pattern.compile(noNL);
	
	private String addNewlinesBeforeOpeners(String input) {
		String replacement = "" + nlReplace + "$1";
		Matcher transformer = listTransformNoNL.matcher(input);
		if (transformer.find()) {
			input = transformer.replaceAll(replacement);
		}
		return input;
	}
	
	Pattern listContents = Pattern.compile("<li>(.*?)</li>", Pattern.DOTALL);
	//forbidden tags are <b> <p> <br/> <font>
	Pattern forbiddenTags = Pattern.compile("<\\/?(?:p|b|(?:font)|(?:br))\\s*[^>]*>");
	private String removeForbiddenTags(String input) {

		Matcher listContentsFinder = listContents.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (listContentsFinder.find()) {
			found = true;
			String contents = listContentsFinder.group(1);
			Matcher forbiddenFinder = forbiddenTags.matcher(contents);
			String replacement = contents;
			if (forbiddenFinder.find()) {
				replacement = forbiddenFinder.replaceAll("");
			}
			replacement = "<li>" + replacement + "</li>";
			listContentsFinder.appendReplacement(sb, replacement);
		}
		listContentsFinder.appendTail(sb);
		if (found)
			input = sb.toString();
		return input;
	}
	
	String badChunk = "(<\\/[uo]l>)(" + nlDelim + "+" + listTag + ")";
	Pattern badChunkPattern = Pattern.compile(badChunk);
	/**
	 * lists that flow directly into other lists with no non-NL between
	 * will not be converted properly by the ListConverter.
	 * As a simple patch (it's a bit of a kludge), I'm adding a space 
	 * between these sorts of lists, so that they can be converted.
	 * @param input
	 * @return
	 */
	private String addChunkableWhitespace(String input) {
		Matcher badChunkFinder = badChunkPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (badChunkFinder.find()) {
			found = true;
			String openingTag = badChunkFinder.group(1);
			String nlsAndCloseTag = badChunkFinder.group(2);
			String replacement = openingTag + " " + nlsAndCloseTag;
			badChunkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			badChunkFinder.appendTail(sb);
			input = sb.toString();
		}
		
		return input;
	}
}
