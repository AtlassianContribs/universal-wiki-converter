package com.atlassian.uwc.splitters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.james.mime4j.message.Body;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Header;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.MessageWriter;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.SingleBody;
import org.apache.log4j.Logger;

import biz.artemis.util.FileUtils;

import com.atlassian.uwc.ui.Page;

/**
 * divides a file with mime style delimiters into a list of pages.
 * Developed for use with tikiwiki.
 * https://studio.plugins.atlassian.com/browse/UWC-494
 * @author Tom Eicher
 * @author Laura Kolker
 */
public class MultiPartMimeSplitter implements PageSplitter {

	Properties properties;
	Logger log = Logger.getLogger(this.getClass());
	public List<Page> split(File file) {
		// FIXME this shall of course only be applied for TikiWiki import with history!
		// break up mime multipart into several "Pages".
		// (somehow, javax.mail could not parse any of the files. using apache james mime4j.)
		// http://james.apache.org/mime4j/index.html
		List<Page> pagelist = new LinkedList<Page>();
		try {
			log.info("working on '"+file.getAbsolutePath()+"'"); // XXX remove
			Message msg = new Message(new FileInputStream(file));
			Body body = msg.getBody();
			if (body instanceof Multipart) { // so this is a page with multiple versions
				// which we will now transform into non-multipart individual pages, as Tiki would produce them
				// for single-versioned pages.
				Multipart multipart = (Multipart) body;
				for (int mmparts=0; mmparts < multipart.getCount(); mmparts++) {
					BodyPart mmpart = multipart.getBodyParts().get(mmparts);
					// two problems with the header:
					// 1) the MetadataTitle expects the "Date" header although not really needed, so we clone the header and modify it
					Header partHeader = new Header(msg.getHeader()); //copy, so we may remove stuff
					partHeader.removeFields("Content-Type"); //would be duplicate...
					// 2) the MessageWriter.writeEntity posts a blank line at the end, which we cannot use, so write to memory first.
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					MessageWriter.DEFAULT.writeHeader(partHeader, out);
					String headerString = out.toString().trim()+"\n"; // trim removes terminating newlines, then we put ONE
					out.close();
					out = new ByteArrayOutputStream();
					MessageWriter.DEFAULT.writeEntity(mmpart, out);
					String content = headerString + out.toString();
					out.close();
					//add it to the list
					pagelist.add(createPage(file, content));
				}
			} else if (body instanceof SingleBody) {
				log.info(file+" is single version - continue as is.");
				pagelist.add(createPage(file));
			} else {
				log.error("no Body... SHOULD NOT HAPPEN - PLEASE INVESTIGATE");
			}
		} catch (IOException e) {
			log.error("failed MIME parsing of '"+file.getAbsolutePath()+"'", e);
		}
		return pagelist;
	}

	private Page createPage(File file) {
		try {
			String contents = FileUtils.readTextFile(file);
			return createPage(file, contents);
		} catch (IOException e) {
			log.error("Could not read file: " + file.getAbsolutePath(), e);
			return new Page(file); //doing our best to return something, but there will likely be additional failures in the engine
		}
	}

	//We need to do all these things, as per doc from PageSplitter interface
	protected Page createPage(File file, String content) {
		Page page = new Page(file); //often converters will expect file
		page.setPath(getPath(file));//often converters will expect path has been set
		page.setOriginalText(content);//we need to set the original text here instead of using ConverterEngine.getFileContents
		page.setUnchangedSource(content);//we need to set this here instead of using ConverterEngine.getFileContents
		return page;
	}

	private String getPath(File file) { //derived from ConverterEngine.setupPages
		String pagePath = file.getPath();
        //Strip the file name from the path.
        int fileNameStart = pagePath.lastIndexOf(File.separator);
		if (fileNameStart >= 0) {
		    pagePath = pagePath.substring(0, fileNameStart);
		} else {
		    pagePath = "";
		}
		return pagePath;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
