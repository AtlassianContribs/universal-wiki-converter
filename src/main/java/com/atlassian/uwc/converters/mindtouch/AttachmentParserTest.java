package com.atlassian.uwc.converters.mindtouch;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlConverter;
import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.util.PropertyFileManager;

public class AttachmentParserTest extends TestCase {
	XmlConverter tester = null;

	Logger log = Logger.getLogger(this.getClass());

	DefaultXmlEvents events = null;

	protected void setUp() throws Exception {
		tester = new XmlConverter();
		PropertyConfigurator.configure("log4j.properties");
		events = new DefaultXmlEvents();
		events.clearAll();
		events.addEvent("files", "com.atlassian.uwc.converters.mindtouch.AttachmentParser");
	}

	public void testConvert_Attachments() {
		String input = "<content>" + "<files count=\"3\" href=\"http://192.168.2.247/@api/deki/pages/40/files\">" +
				"<file id=\"3\" href=\"http://192.168.2.247/@api/deki/files/3/info\"><filename>abc.txt</filename" +
				"><description>text file</description><contents type=\"text/plain\" size=\"11\" href=\"http://19" +
				"2.168.2.247/@api/deki/files/3/=abc.txt\" /><date.created>2009-09-30T20:20:11Z</date.created><us" +
				"er.createdby id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker</nick>" +
				"<username>laura.kolker</username><email>laura.kolker@gmail.com</email><hash.email>9ed3310b0d84b" +
				"37fe658dce82b713c08</hash.email><uri.gravatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe6" +
				"58dce82b713c08</uri.gravatar></user.createdby><revisions count=\"1\" totalcount=\"1\" href=\"ht" +
				"tp://192.168.2.247/@api/deki/files/3/revisions\" /><page.parent id=\"40\" href=\"http://192.168" +
				".2.247/@api/deki/pages/40?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Attachments</" +
				"uri.ui><title>Test Attachments</title><path>Sandbox/Test_Attachments</path><namespace>main</nam" +
				"espace></page.parent><properties count=\"1\" href=\"http://192.168.2.247/@api/deki/files/3/prop" +
				"erties\"><property name=\"urn:deki.mindtouch.com#description\" href=\"http://192.168.2.247/@api" +
				"/deki/files/3/properties/urn%253adeki.mindtouch.com%2523description/info\" etag=\"6.r1_ts2009-0" +
				"9-30T20:20:11Z\"><contents type=\"text/plain; charset=utf-8\" size=\"9\" href=\"http://192.168." +
				"2.247/@api/deki/files/3/properties/urn%253adeki.mindtouch.com%2523description\">text file</cont" +
				"ents><date.modified>2009-09-30T20:20:11Z</date.modified><user.modified id=\"1\" href=\"http://1" +
				"92.168.2.247/@api/deki/users/1\"><nick>laura.kolker</nick><username>laura.kolker</username><ema" +
				"il>laura.kolker@gmail.com</email><hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email><uri." +
				"gravatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar></user.m" +
				"odified></property></properties></file><file id=\"1\" href=\"http://192.168.2.247/@api/deki/fil" +
				"es/1/info\"><filename>cow.jpg</filename><description>Cow</description><contents type=\"image/jp" +
				"eg\" size=\"23096\" width=\"450\" height=\"319\" href=\"http://192.168.2.247/@api/deki/files/1/" +
				"=cow.jpg\" /><contents.preview rel=\"thumb\" type=\"image/jpeg\" maxwidth=\"160\" maxheight=\"1" +
				"60\" href=\"http://192.168.2.247/@api/deki/files/1/=cow.jpg?size=thumb\" /><contents.preview re" +
				"l=\"webview\" type=\"image/jpeg\" maxwidth=\"550\" maxheight=\"550\" href=\"http://192.168.2.24" +
				"7/@api/deki/files/1/=cow.jpg?size=webview\" /><date.created>2009-09-30T20:18:46Z</date.created>" +
				"<user.createdby id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker</ni" +
				"ck><username>laura.kolker</username><email>laura.kolker@gmail.com</email><hash.email>9ed3310b0d" +
				"84b37fe658dce82b713c08</hash.email><uri.gravatar>http://www.gravatar.com/avatar/9ed3310b0d84b37" +
				"fe658dce82b713c08</uri.gravatar></user.createdby><revisions count=\"1\" totalcount=\"1\" href=" +
				"\"http://192.168.2.247/@api/deki/files/1/revisions\" /><page.parent id=\"40\" href=\"http://192" +
				".168.2.247/@api/deki/pages/40?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Attachmen" +
				"ts</uri.ui><title>Test Attachments</title><path>Sandbox/Test_Attachments</path><namespace>main<" +
				"/namespace></page.parent><properties count=\"1\" href=\"http://192.168.2.247/@api/deki/files/1/" +
				"properties\"><property name=\"urn:deki.mindtouch.com#description\" href=\"http://192.168.2.247/" +
				"@api/deki/files/1/properties/urn%253adeki.mindtouch.com%2523description/info\" etag=\"2.r1_ts20" +
				"09-09-30T20:18:47Z\"><contents type=\"text/plain; charset=utf-8\" size=\"3\" href=\"http://192." +
				"168.2.247/@api/deki/files/1/properties/urn%253adeki.mindtouch.com%2523description\">Cow</conten" +
				"ts><date.modified>2009-09-30T20:18:47Z</date.modified><user.modified id=\"1\" href=\"http://192" +
				".168.2.247/@api/deki/users/1\"><nick>laura.kolker</nick><username>laura.kolker</username><email" +
				">laura.kolker@gmail.com</email><hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gr" +
				"avatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar></user.mod" +
				"ified></property></properties></file><file id=\"2\" href=\"http://192.168.2.247/@api/deki/files" +
				"/2/info\"><filename>hobbespounce.gif</filename><description>gif</description><contents type=\"i" +
				"mage/gif\" size=\"6536\" width=\"92\" height=\"100\" href=\"http://192.168.2.247/@api/deki/file" +
				"s/2/=hobbespounce.gif\" /><contents.preview rel=\"thumb\" type=\"image/gif\" maxwidth=\"160\" m" +
				"axheight=\"160\" href=\"http://192.168.2.247/@api/deki/files/2/=hobbespounce.gif?size=thumb\" /" +
				"><contents.preview rel=\"webview\" type=\"image/gif\" maxwidth=\"550\" maxheight=\"550\" href=" +
				"\"http://192.168.2.247/@api/deki/files/2/=hobbespounce.gif?size=webview\" /><date.created>2009-" +
				"09-30T20:18:47Z</date.created><user.createdby id=\"1\" href=\"http://192.168.2.247/@api/deki/us" +
				"ers/1\"><nick>laura.kolker</nick><username>laura.kolker</username><email>laura.kolker@gmail.com" +
				"</email><hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gravatar>http://www.grava" +
				"tar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar></user.createdby><revisions count" +
				"=\"1\" totalcount=\"1\" href=\"http://192.168.2.247/@api/deki/files/2/revisions\" /><page.paren" +
				"t id=\"40\" href=\"http://192.168.2.247/@api/deki/pages/40?redirects=0\"><uri.ui>http://192.168" +
				".2.247/Sandbox/Test_Attachments</uri.ui><title>Test Attachments</title><path>Sandbox/Test_Attac" +
				"hments</path><namespace>main</namespace></page.parent><properties count=\"1\" href=\"http://192" +
				".168.2.247/@api/deki/files/2/properties\"><property name=\"urn:deki.mindtouch.com#description\"" +
				" href=\"http://192.168.2.247/@api/deki/files/2/properties/urn%253adeki.mindtouch.com%2523descri" +
				"ption/info\" etag=\"4.r1_ts2009-09-30T20:18:48Z\"><contents type=\"text/plain; charset=utf-8\" " +
				"size=\"3\" href=\"http://192.168.2.247/@api/deki/files/2/properties/urn%253adeki.mindtouch.com%" +
				"2523description\">gif</contents><date.modified>2009-09-30T20:18:48Z</date.modified><user.modifi" +
				"ed id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker</nick><username" +
				">laura.kolker</username><email>laura.kolker@gmail.com</email><hash.email>9ed3310b0d84b37fe658d" +
				"ce82b713c08</hash.email><uri.gravatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82" +
				"b713c08</uri.gravatar></user.modified></property></properties></file></files>" + "</content>";
		
		File infile = new File("sampleData/mindtouch/40_SampleMindtouch_InputAttachments.xml");
		assertTrue(infile.exists());
		File attdir = new File("sampleData/mindtouch/40_SampleMindtouch_InputAttachments_attachments");
		assertTrue(attdir.exists());
		
		Page page = new Page(infile);
		page.setOriginalText(input);
		tester.convert(page);
		Set<File> actual = page.getAttachments();
		assertNotNull(actual);
		assertEquals(3, actual.size());
		for (File file : actual) {
			assertNotNull(file);
			assertNotNull(file.getName());
			assertTrue(file.getName().equals("abc.txt") ||
					file.getName().equals("cow.jpg") ||
					file.getName().equals("hobbespounce.gif"));
		}
		
	}
}
