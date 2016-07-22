package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.artemis.util.FileUtils;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Comment;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.VersionPage;

public class DiscussionConverter extends DokuwikiUserDate {

	@Override
	public void convert(Page page) {
		if (page instanceof VersionPage) return;
//		log.debug("Discussion Conversion --- Start");
		String input = page.getOriginalText();
		if (hasDiscussion(input)) {
			String commentData = getCommentData(page.getFile());
			int numComments = getNumComments(commentData);
			if (numComments > 0) {
				Vector<String> comments = seperateComments(commentData);
				for (String commentString : comments) {
					Comment comment = parseComment(commentString);
					page.addComment(comment);
				}
			}
		}
		log.debug("Discussion Conversion --- Completed");
	}

	Pattern discussionP = Pattern.compile("~~DISCUSSION[^~]*~~");
	/**
	 * 
	 * @param input page content
	 * @return true if the page indicates that there is a discussion
	 */
	protected boolean hasDiscussion(String input) {
		Matcher discussionFinder = discussionP.matcher(input);
		return discussionFinder.find();
	}
	
	/**
	 * @param pagefile
	 * @return the contents of the comments file associated with the given page file
	 */
	protected String getCommentData(File pagefile) {
		String metaFilename = getMetaFilename(pagefile.getPath(), ".comments");
		if (metaFilename == null) return "";
		File meta = new File(metaFilename);
		try {
			return FileUtils.readTextFile(meta);
		} catch (IOException e) {
			String message = "Problem reading meta file: " + metaFilename;
			log.error(message, e);
			addError(Feedback.BAD_FILE, message, false);
			return "";
		}
	}

	Pattern numCommentsP = Pattern.compile(";s:8:\"comments\";a:(\\d+):\\{");
	/**
	 * 
	 * @param input contents of a comments file
	 * @return number of comments in the file
	 */
	protected int getNumComments(String input) {
		Matcher numFinder = numCommentsP.matcher(input);
		if (numFinder.find()) {
			String numS = numFinder.group(1);
			return new Integer(numS);
		}
		return 0;
	}

	Pattern commentsStringP = Pattern.compile(";s:8:\"comments\";a:\\d+:\\{(.*?\\})s:6:\"number\"", Pattern.DOTALL);
	Pattern eachComment = Pattern.compile("(.*?s:3:\"cid\";s:32:\"\\w{32,32}\";\\})", Pattern.DOTALL);
	/**
	 * @param input
	 * @return each string in the vector contains all the data for an individual comment
	 */
	protected Vector<String> seperateComments(String input) {
		Vector<String> all = new Vector<String>();
		Matcher commentFinder = commentsStringP.matcher(input);
		while (commentFinder.find()) {
			String commentData = commentFinder.group(1);
			Matcher eachFinder = eachComment.matcher(commentData);
			while (eachFinder.find()) {
				String each = eachFinder.group(1);
				all.add(each);
			}
		}
		
		return all;
	}

	Pattern commentP = Pattern.compile("" +
			"s:32:\"\\w{32,32}\";a:8:\\{s:4:\"user\";a:\\d+:\\{" +
			"(?:s:2:(\"id\");s:\\d+:\"([^\"]+))?" + //id = username?
			".*?s:4:(\"mail\");s:\\d+:\"([^\"]+)" + //email = username?
			".*?\\}s:4:\"date\\\";a:\\d:\\{.*?s:7:\"created\";i:(\\d+)" + //timestamp
			".*?s:5:\"xhtml\";s:\\d+:\"(.*?)\";s:6:\"parent\";N;", //text 
			Pattern.DOTALL);
	protected Comment parseComment(String input) {
		Matcher commentFinder = commentP.matcher(input);
		while (commentFinder.find()) {
			String creator = commentFinder.group(2); //username from id
			String email = null;
			if (creator == null) {
				email = commentFinder.group(4); //no id? username from mail
			}
			String timestamp = commentFinder.group(5);
			String text = commentFinder.group(6);
			if (creator == null && email != null) {
				text = addEmailAsOrigCommenter(email, text);
			}
			timestamp = formatTimestamp(timestamp);
			boolean isXhtml = true;
			return new Comment(text, creator, timestamp, isXhtml);
		}
		addError(Feedback.CONVERTER_ERROR, "Problem finding comment for: " + input, false);
		return null;
	}

	Pattern lastpara = Pattern.compile("(</p>\\s*)$");
	private String addEmailAsOrigCommenter(String email, String text) {
		Matcher paraEndFinder = lastpara.matcher(text);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (paraEndFinder.find()) {
			found = true;
			String replacement = paraEndFinder.group(1) + "<p><b>Original Dokuwiki Commenter:</b> "+email+"</p>\n";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			paraEndFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			paraEndFinder.appendTail(sb);
			return sb.toString();
		}
		return text;
	}


	private String formatTimestamp(String timestamp) {
		long timestring = Long.parseLong(timestamp);
		Date date = new Date(timestring*1000);
		DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss:SS");
		return dateFormat.format(date);
	}

}
