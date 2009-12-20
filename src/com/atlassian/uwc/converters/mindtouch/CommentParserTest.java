package com.atlassian.uwc.converters.mindtouch;

import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlConverter;
import com.atlassian.uwc.ui.Page;

public class CommentParserTest extends TestCase {
	XmlConverter tester = null;

	Logger log = Logger.getLogger(this.getClass());

	DefaultXmlEvents events = null;

	protected void setUp() throws Exception {
		tester = new XmlConverter();
		PropertyConfigurator.configure("log4j.properties");
		events = new DefaultXmlEvents();
		events.clearAll();
		events.addEvent("comment", "com.atlassian.uwc.converters.mindtouch.CommentParser");
		events.addEvent("username", "com.atlassian.uwc.converters.mindtouch.CommentParser");
		events.addEvent("date.posted", "com.atlassian.uwc.converters.mindtouch.CommentParser");
		events.addEvent("content", "com.atlassian.uwc.converters.mindtouch.CommentParser");
		
		//these aren't used by the CommentParser. We include them so that their content is cleaned, but
		//in the conf/converter.mindtouch.properties that will be handled by the CleanParser
		events.addEvent("comments", "com.atlassian.uwc.converters.mindtouch.CommentParser");
		events.addEvent("nick", "com.atlassian.uwc.converters.mindtouch.CommentParser");
		events.addEvent("email", "com.atlassian.uwc.converters.mindtouch.CommentParser");
		events.addEvent("title", "com.atlassian.uwc.converters.mindtouch.CommentParser");
		events.addEvent("hash.email", "com.atlassian.uwc.converters.mindtouch.CommentParser");
		events.addEvent("uri.gravatar", "com.atlassian.uwc.converters.mindtouch.CommentParser");
		events.addEvent("number", "com.atlassian.uwc.converters.mindtouch.CommentParser");
	}

	public void testConvert_Comments() {
		String input = "<content>" + 
				"<comments sort=\"asc\" count=\"3\" totalcount=\"3\" href=\"http://192.168.2.247/@api/deki/pag" +
				"es/42/comments\"><comment id=\"1\" href=\"http://192.168.2.247/@api/deki/pages/42/comments/1" +
				"\"><user.createdby id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolk" +
				"er</nick><username>laura.kolker</username><email>laura.kolker@gmail.com</email><hash.email>9" +
				"ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gravatar>http://www.gravatar.com/avatar/9ed" +
				"3310b0d84b37fe658dce82b713c08</uri.gravatar></user.createdby><date.posted>2009-10-06T04:30:2" +
				"5Z</date.posted><title></title><number>1</number><content type=\"text/plain; charset=utf-8\"" +
				" href=\"http://192.168.2.247/@api/deki/pages/42/comments/1/content\">Comment 1</content></co" +
				"mment><comment id=\"2\" href=\"http://192.168.2.247/@api/deki/pages/42/comments/2\"><user.cr" +
				"eatedby id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker</nick><u" +
				"sername>laura.kolker</username><email>laura.kolker@gmail.com</email><hash.email>9ed3310b0d84" +
				"b37fe658dce82b713c08</hash.email><uri.gravatar>http://www.gravatar.com/avatar/9ed3310b0d84b3" +
				"7fe658dce82b713c08</uri.gravatar></user.createdby><date.posted>2009-10-06T04:30:32Z</date.po" +
				"sted><title></title><number>2</number><content type=\"text/plain; charset=utf-8\" href=\"htt" +
				"p://192.168.2.247/@api/deki/pages/42/comments/2/content\">Foo Bar</content></comment><commen" +
				"t id=\"3\" href=\"http://192.168.2.247/@api/deki/pages/42/comments/3\"><user.createdby id=\"" +
				"1\" href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker</nick><username>laura" +
				".kolker</username><email>laura.kolker@gmail.com</email><hash.email>9ed3310b0d84b37fe658dce82" +
				"b713c08</hash.email><uri.gravatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b7" +
				"13c08</uri.gravatar></user.createdby><date.posted>2009-10-06T16:24:31Z</date.posted><title><" +
				"/title><number>3</number><content type=\"text/plain; charset=utf-8\" href=\"http://192.168.2" +
				".247/@api/deki/pages/42/comments/3/content\">Abcdef</content></comment></comments>" + 
				"</content>";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		Vector<String> actual = page.getComments();
		assertNotNull(actual);
		assertEquals(3, actual.size());
		int i = 0;
		for (String comment: actual) {
			switch (i++) {
			case 0:
				assertEquals("laura.kolker says:\nComment 1\n~Posted 04:30, 6 Oct 2009~", comment);
				break;
			case 1:
				assertEquals("laura.kolker says:\nFoo Bar\n~Posted 04:30, 6 Oct 2009~", comment);
				break;
			case 2:
				assertEquals("laura.kolker says:\nAbcdef\n~Posted 16:24, 6 Oct 2009~", comment);
				break;
			}
		}
	}

	
	public void testConvert_Cleanextradata() {
		String input = "<pagedata><content type=\"text/html\" title=\"1_SampleMindtouch_InputComments\"><body>\n" + 
				"<p>testing</p></body></content><tags count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/42/tags\" /><comments sort=\"asc\" count=\"3\" totalcount=\"3\" href=\"http://192.168.2.247/@api/deki/pages/42/comments\"><comment id=\"1\" href=\"http://192.168.2.247/@api/deki/pages/42/comments/1\"><user.createdby id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker</nick><username>laura.kolker</username><email>laura.kolker@gmail.com</email><hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gravatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar></user.createdby><date.posted>2009-10-06T04:30:25Z</date.posted><title></title><number>1</number><content type=\"text/plain; charset=utf-8\" href=\"http://192.168.2.247/@api/deki/pages/42/comments/1/content\">Comment 1</content></comment><comment id=\"2\" href=\"http://192.168.2.247/@api/deki/pages/42/comments/2\"><user.createdby id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker</nick><username>laura.kolker</username><email>laura.kolker@gmail.com</email><hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gravatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar></user.createdby><date.posted>2009-10-06T04:30:32Z</date.posted><title></title><number>2</number><content type=\"text/plain; charset=utf-8\" href=\"http://192.168.2.247/@api/deki/pages/42/comments/2/content\">Foo Bar</content></comment><comment id=\"3\" href=\"http://192.168.2.247/@api/deki/pages/42/comments/3\"><user.createdby id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker</nick><username>laura.kolker</username><email>laura.kolker@gmail.com</email><hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gravatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar></user.createdby><date.posted>2009-10-06T16:24:31Z</date.posted><title></title><number>3</number><content type=\"text/plain; charset=utf-8\" href=\"http://192.168.2.247/@api/deki/pages/42/comments/3/content\">Abcdef</content></comment></comments><files count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/42/files\" /></pagedata>\n" + 
				"";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		String expected = "testing";
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
