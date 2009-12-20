package com.atlassian.uwc.exporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.exporters.mindtouch.MindtouchPage;
import com.atlassian.uwc.filters.NoSvnFilter;

public class MindtouchExporterTest extends TestCase {

	private static final String EX_MULT_FILES = "<files count=\"3\" href=\"http://192.168.2.247/@api/deki/pages/40" +
							"/files\"><file id=\"3\" href=\"http://192.168.2.247/@api/deki/files/3/" +
							"info\"><filename>abc.txt</filename><description>text file</description" +
							"><contents type=\"text/plain\" size=\"11\" href=\"http://192.168.2.247" +
							"/@api/deki/files/3/=abc.txt\" /><date.created>2009-09-30T20:20:11Z</da" +
							"te.created><user.createdby id=\"1\" href=\"http://192.168.2.247/@api/d" +
							"eki/users/1\"><nick>laura.kolker</nick><username>laura.kolker</usernam" +
							"e><email>laura.kolker@gmail.com</email><hash.email>9ed3310b0d84b37fe65" +
							"8dce82b713c08</hash.email><uri.gravatar>http://www.gravatar.com/avatar" +
							"/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar></user.createdby><revi" +
							"sions count=\"1\" totalcount=\"1\" href=\"http://192.168.2.247/@api/de" +
							"ki/files/3/revisions\" /><page.parent id=\"40\" href=\"http://192.168." +
							"2.247/@api/deki/pages/40?redirects=0\"><uri.ui>http://192.168.2.247/Sa" +
							"ndbox/Test_Attachments</uri.ui><title>Test Attachments</title><path>Sa" +
							"ndbox/Test_Attachments</path><namespace>main</namespace></page.parent>" +
							"<properties count=\"1\" href=\"http://192.168.2.247/@api/deki/files/3/" +
							"properties\"><property name=\"urn:deki.mindtouch.com#description\" hre" +
							"f=\"http://192.168.2.247/@api/deki/files/3/properties/urn%253adeki.min" +
							"dtouch.com%2523description/info\" etag=\"6.r1_ts2009-09-30T20:20:11Z\"" +
							"><contents type=\"text/plain; charset=utf-8\" size=\"9\" href=\"http:" +
							"//192.168.2.247/@api/deki/files/3/properties/urn%253adeki.mindtouch.c" +
							"om%2523description\">text file</contents><date.modified>2009-09-30T20" +
							":20:11Z</date.modified><user.modified id=\"1\" href=\"http://192.168." +
							"2.247/@api/deki/users/1\"><nick>laura.kolker</nick><username>laura.ko" +
							"lker</username><email>laura.kolker@gmail.com</email><hash.email>9ed33" +
							"10b0d84b37fe658dce82b713c08</hash.email><uri.gravatar>http://www.grav" +
							"atar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar></user" +
							".modified></property></properties></file><file id=\"1\" href=\"http:/" +
							"/192.168.2.247/@api/deki/files/1/info\"><filename>cow.jpg</filename><" +
							"description>Cow</description><contents type=\"image/jpeg\" size=\"230" +
							"96\" width=\"450\" height=\"319\" href=\"http://192.168.2.247/@api/de" +
							"ki/files/1/=cow.jpg\" /><contents.preview rel=\"thumb\" type=\"image/" +
							"jpeg\" maxwidth=\"160\" maxheight=\"160\" href=\"http://192.168.2.247" +
							"/@api/deki/files/1/=cow.jpg?size=thumb\" /><contents.preview rel=\"we" +
							"bview\" type=\"image/jpeg\" maxwidth=\"550\" maxheight=\"550\" href=\"" +
							"http://192.168.2.247/@api/deki/files/1/=cow.jpg?size=webview\" /><dat" +
							"e.created>2009-09-30T20:18:46Z</date.created><user.createdby id=\"1\" " +
							"href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker</n" +
							"ick><username>laura.kolker</username><email>laura.kolker@gmail.com</em" +
							"ail><hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.grav" +
							"atar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</" +
							"uri.gravatar></user.createdby><revisions count=\"1\" totalcount=\"1\" " +
							"href=\"http://192.168.2.247/@api/deki/files/1/revisions\" /><page.pare" +
							"nt id=\"40\" href=\"http://192.168.2.247/@api/deki/pages/40?redirects=" +
							"0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Attachments</uri.ui><tit" +
							"le>Test Attachments</title><path>Sandbox/Test_Attachments</path><names" +
							"pace>main</namespace></page.parent><properties count=\"1\" href=\"http" +
							"://192.168.2.247/@api/deki/files/1/properties\"><property name=\"urn:d" +
							"eki.mindtouch.com#description\" href=\"http://192.168.2.247/@api/deki/" +
							"files/1/properties/urn%253adeki.mindtouch.com%2523description/info\" e" +
							"tag=\"2.r1_ts2009-09-30T20:18:47Z\"><contents type=\"text/plain; chars" +
							"et=utf-8\" size=\"3\" href=\"http://192.168.2.247/@api/deki/files/1/pr" +
							"operties/urn%253adeki.mindtouch.com%2523description\">Cow</contents><d" +
							"ate.modified>2009-09-30T20:18:47Z</date.modified><user.modified id=\"1" +
							"\" href=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker<" +
							"/nick><username>laura.kolker</username><email>laura.kolker@gmail.com</" +
							"email><hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gr" +
							"avatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08" +
							"</uri.gravatar></user.modified></property></properties></file><file id" +
							"=\"2\" href=\"http://192.168.2.247/@api/deki/files/2/info\"><filename>" +
							"hobbespounce.gif</filename><description>gif</description><contents typ" +
							"e=\"image/gif\" size=\"6536\" width=\"92\" height=\"100\" href=\"http:" +
							"//192.168.2.247/@api/deki/files/2/=hobbespounce.gif\" /><contents.prev" +
							"iew rel=\"thumb\" type=\"image/gif\" maxwidth=\"160\" maxheight=\"160" +
							"\" href=\"http://192.168.2.247/@api/deki/files/2/=hobbespounce.gif?si" +
							"ze=thumb\" /><contents.preview rel=\"webview\" type=\"image/gif\" max" +
							"width=\"550\" maxheight=\"550\" href=\"http://192.168.2.247/@api/deki" +
							"/files/2/=hobbespounce.gif?size=webview\" /><date.created>2009-09-30T" +
							"20:18:47Z</date.created><user.createdby id=\"1\" href=\"http://192.16" +
							"8.2.247/@api/deki/users/1\"><nick>laura.kolker</nick><username>laura." +
							"kolker</username><email>laura.kolker@gmail.com</email><hash.email>9ed" +
							"3310b0d84b37fe658dce82b713c08</hash.email><uri.gravatar>http://www.gr" +
							"avatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar></us" +
							"er.createdby><revisions count=\"1\" totalcount=\"1\" href=\"http://19" +
							"2.168.2.247/@api/deki/files/2/revisions\" /><page.parent id=\"40\" hr" +
							"ef=\"http://192.168.2.247/@api/deki/pages/40?redirects=0\"><uri.ui>ht" +
							"tp://192.168.2.247/Sandbox/Test_Attachments</uri.ui><title>Test Attac" +
							"hments</title><path>Sandbox/Test_Attachments</path><namespace>main</n" +
							"amespace></page.parent><properties count=\"1\" href=\"http://192.168." +
							"2.247/@api/deki/files/2/properties\"><property name=\"urn:deki.mindto" +
							"uch.com#description\" href=\"http://192.168.2.247/@api/deki/files/2/p" +
							"roperties/urn%253adeki.mindtouch.com%2523description/info\" etag=\"4." +
							"r1_ts2009-09-30T20:18:48Z\"><contents type=\"text/plain; charset=utf-" +
							"8\" size=\"3\" href=\"http://192.168.2.247/@api/deki/files/2/properti" +
							"es/urn%253adeki.mindtouch.com%2523description\">gif</contents><date.m" +
							"odified>2009-09-30T20:18:48Z</date.modified><user.modified id=\"1\" h" +
							"ref=\"http://192.168.2.247/@api/deki/users/1\"><nick>laura.kolker</ni" +
							"ck><username>laura.kolker</username><email>laura.kolker@gmail.com</em" +
							"ail><hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gra" +
							"vatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08" +
							"</uri.gravatar></user.modified></property></properties></file></files>";
	private static final String TESTDIR = "sampleData/mindtouch/junit_resources/";
	MindtouchExporter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Properties props = null;
	protected void setUp() throws Exception {
		tester = new MindtouchExporter();
		PropertyConfigurator.configure("log4j.properties");
		props = new Properties();
		props.load(new FileInputStream(TESTDIR + "exporter.mindtouch.properties"));
		tester.setProperties(props);
		tester.startRunning();
	}
	
	public void testCreateApiUrl() {
		String input, expected, actual;
		input = props.getProperty(MindtouchExporter.PROPKEY_URLBASE, null);
		assertNotNull(input);
		expected = "http://" + input + "/@api/deki/";
		actual = tester.createApiUrl(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetPages() {
		Vector<MindtouchPage> actual = tester.getPages();
		assertNotNull(actual);
		assertTrue(actual.size() > 0);
		
		MindtouchPage root = actual.get(0);
		assertNotNull(root);
		assertNotNull(root.title);
		assertEquals("MindTouch", root.title);
		assertNotNull(root.id);
		Vector<MindtouchPage> lvl1 = root.getSubpages();
		assertNotNull(lvl1);
		assertFalse(lvl1.isEmpty());
		assertTrue(lvl1.size() > 0);
		
		MindtouchPage sandbox = lvl1.get(2);
		assertNotNull(sandbox);
		assertNotNull(sandbox.title);
		assertEquals("Sandbox", sandbox.title);
		assertNotNull(sandbox.id);
		Vector<MindtouchPage> lvl2 = sandbox.getSubpages();
		assertNotNull(lvl2);
		assertFalse(lvl2.isEmpty());
		assertTrue(lvl2.size() > 6);
		
		int i = 0;
		for (MindtouchPage page : lvl2) {
			assertNotNull(page);
			assertNotNull(page.id);
			assertNotNull(page.title);
			String expTitle = "";
			switch(i) {
			case 0:
				expTitle = "Test Attachments"; break;
			case 1:
				expTitle = "Test Comments"; break;
			case 2:
				expTitle = "Test Headers"; break;
			case 3:
				expTitle = "Test Permissions"; break;
			case 4: 
				expTitle = "Test Syntax"; break;
			case 5: 
				expTitle = "Test Tags"; break;
			case 6:
				expTitle = "Test Titles"; break;
			default: continue;
			}
			assertEquals(expTitle, page.title);
			i++;
		}
		
	}

	public void testGetContent() {
		Vector<MindtouchPage> pages = tester.getPages();
		assertNotNull(pages);
		Vector<MindtouchPage> actual = tester.getContent(pages);
		assertNotNull(actual);
	
		MindtouchPage root = actual.get(0);
		assertNotNull(root);
		Vector<MindtouchPage> lvl1 = root.getSubpages();
		assertNotNull(lvl1);
		
		MindtouchPage sandbox = lvl1.get(2);
		assertNotNull(sandbox);
		assertNotNull(sandbox.content);
		assertEquals("<content type=\"text/html\" title=\"Sandbox\"><body>\n" + 
				"<p>Testing 123</p></body><body target=\"toc\"><em>No headers</em></body></content>", sandbox.content);
		Vector<MindtouchPage> lvl2 = sandbox.getSubpages();
		assertNotNull(lvl2);
		
		int i = 0;
		for (MindtouchPage page : lvl2) {
			assertNotNull(page);
			assertNotNull(page.content);
			String exp = "";
			switch(i) {
			case 0:
				exp = "<content type=\"text/html\" title=\"Test Attachments\"><body>\n" + 
						"<p>We\'re going to have some attachments content.</p>\n" + 
						"<p>We need an image that\'s inline, a link to an attachment, and an attached file " +
						"with no reference in the page.</p>\n" + 
						"<p>Inline:</p>\n" + 
						"<p><img alt=\"cow.jpg\" class=\"internal default\" src=\"http://192.168.2.247/@api" +
						"/deki/files/1/=cow.jpg?parent=Test Attachments\" /></p>\n" + 
						"<p>Link:</p>\n" + 
						"<p><a rel=\"internal\" href=\"http://192.168.2.247/@api/deki/files/3/=abc.txt?parent=Test Attachments\" " +
						"class=\"iconitext-16 ext-txt \">abc.txt</a></p>\n" + 
						"</body><body target=\"toc\"><em>No headers</em></body></content>"; 
				break;
			case 1:
				exp = "<content type=\"text/html\" title=\"Test Comments\"><body>\n" + 
						"<p>This page will have lots of comments, which we\'ll need to keep in order." +
						"</p></body><body target=\"toc\"><em>No headers</em></body></content>"; 
				break;
			case 2:
				exp = "<content type=\"text/html\" title=\"Test Headers\"><body>\n" + 
						"<p>Headers were a little annoying to control.</p>\n" + 
						"<div id=\"section_1\"><span id=\"Header_1\" /><h2 class=\"editable\">Header 1</h2>\n" + 
						"<p>Some text</p>\n" + 
						"<div id=\"section_2\"><span id=\"Header_2\" /><h3 class=\"editable\">Header 2</h3>\n" + 
						"<p>Tralala</p>\n" + 
						"<div id=\"section_3\"><span id=\"Header_3\" /><h4 class=\"editable\">Header 3</h4>\n" + 
						"<p>Foo bar</p>\n" + 
						"<div id=\"section_4\"><span id=\"Header_4\" /><h5 class=\"editable\">Header 4</h5>\n" + 
						"<p>Testing</p>\n" + 
						"<div id=\"section_5\"><span id=\"Header_5\" /><h6 class=\"editable\">Header 5</h6>\n" + 
						"<p>Lorem Ipsum</p>\n" + 
						"<p>Ê</p>\n" + 
						"<p>What about Fonts?</p>\n" + 
						"<p><span style=\"font-family: Courier New;\">Testing Courier</span></p>\n" + 
						"<p><span style=\"font-family: Times New Roman;\">Times</span></p>\n" + 
						"<p><span style=\"font-family: Verdana;\">Verdana</span></p>\n" + 
						"<p>Ê</p>\n" + 
						"<p>and Font size?</p>\n" + 
						"<p><span style=\"font-size: xx-small;\">xxsmall</span></p>\n" + 
						"<p><span style=\"font-size: x-small;\">xsmall</span></p>\n" + 
						"<p><span style=\"font-size: small;\">small</span></p>\n" + 
						"<p><span style=\"font-size: medium;\">medium</span></p>\n" + 
						"<p><span style=\"font-size: large;\">large</span></p>\n" + 
						"<p><span style=\"font-size: x-large;\">xlarge</span></p>\n" + 
						"<p><span style=\"font-size: xx-large;\">xxlarge</span></p>\n" + 
						"<p>Ê</p>\n" + 
						"<p>What happens when we mix and match different stuff</p>\n" + 
						"<p style=\"margin-left: 40px;\"><code><strong>bold indent code</strong></code></p>\n" + 
						"<p><span style=\"font-family: Courier New;\"><span style=\"font-size: x-large;\">Courier xlarge</span></span></p>\n" + 
						"<br />\n" + 
						"<ul> <li>  </li>\n" + 
						"</ul>\n" + 
						"</div></div></div></div></div></body><body target=\"toc\"><ol style=\"list-style-type:none; " +
						"margin-left:0px; padding-left:0px;\"><li><span>1.</span> <a href=\"#Header_1\" rel=\"internal\">" +
						"Header 1</a><ol style=\"list-style-type:none; margin-left:0px; padding-left:15px;\"><li>" +
						"<span>1.1.</span> <a href=\"#Header_2\" rel=\"internal\">Header 2</a>" +
						"<ol style=\"list-style-type:none; margin-left:0px; padding-left:15px;\">" +
						"<li><span>1.1.1.</span> <a href=\"#Header_3\" rel=\"internal\">Header 3</a>" +
						"<ol style=\"list-style-type:none; margin-left:0px; padding-left:15px;\"><li>" +
						"<span>1.1.1.1.</span> <a href=\"#Header_4\" rel=\"internal\">Header 4</a>" +
						"<ol style=\"list-style-type:none; margin-left:0px; padding-left:15px;\"><li>" +
						"<span>1.1.1.1.1.</span> <a href=\"#Header_5\" rel=\"internal\">Header 5</a></li></ol>" +
						"</li></ol></li></ol></li></ol></li></ol></body></content>"; 
				break;
			case 3:
				exp = "<content type=\"text/html\" title=\"Test Permissions\"><body>\n" + 
						"<p>This will be a restricted page, so we can test authentication, " +
						"and export thoroughness." +
						"</p></body><body target=\"toc\"><em>No headers</em></body></content>"; 
				break;
			case 4: 
				i++;
				continue; //ignore this one Dekiscript makes it complicated
			case 5: 
				exp = "<content type=\"text/html\" title=\"Test Tags\"><body>\n" + 
						"<p>This page will have tags associated with it that will need to be " +
						"turned into labels</p></body><body target=\"toc\"><em>No headers</em>" +
						"</body></content>"; 
				break;
			case 6:
				exp = "<content type=\"text/html\" title=\"Test Titles\"><body>\n" + 
						"<p>Titles seem problematic.</p>\n" + 
						"<p>Firstly, what happens if we have two titles?</p>\n" + 
						"<div id=\"section_1\"><span id=\"Title_.23_2\" /><h1 class=\"editable\">" +
						"Title # 2</h1>\n" + 
						"<p>I have no idea. Probably it\'s the first title that matters?</p>" +
						"</div></body><body target=\"toc\"><ol style=\"list-style-type:none; " +
						"margin-left:0px; padding-left:0px;\"><li><span>1.</span> " +
						"<a href=\"#Title_.23_2\" rel=\"internal\">Title # 2</a></li></ol>" +
						"</body></content>"; 
				break;
			default: continue;
			}
			assertEquals(exp, page.content);
			i++;
		}
	}

	public void testGetTags() {
		Vector<MindtouchPage> pages = tester.getPages();
		assertNotNull(pages);
		Vector<MindtouchPage> actual = tester.getTags(pages);
		assertNotNull(actual);
	
		MindtouchPage root = actual.get(0);
		assertNotNull(root);
		Vector<MindtouchPage> lvl1 = root.getSubpages();
		assertNotNull(lvl1);
		
		MindtouchPage sandbox = lvl1.get(0);
		assertNotNull(sandbox);
		assertNotNull(sandbox.tags);
		assertTrue(sandbox.tags.contains("count=\"0\""));
		Vector<MindtouchPage> lvl2 = sandbox.getSubpages();
		assertNotNull(lvl2);
		
		int i = 0;
		for (MindtouchPage page : lvl2) {
			assertNotNull(page);
			switch(i) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4: 
			case 6:
				assertNotNull(page.tags);
				assertTrue(page.tags.contains("count=\"0\""));
				break;
			case 5: //Test Tags
				assertNotNull(page.tags);
				assertEquals("<tags count=\"4\" " +
							"href=\"http://192.168.2.247/@api/deki/pages/43/tags\">" +
						"<tag value=\"test\" id=\"3\" " +
							"href=\"http://192.168.2.247/@api/deki/site/tags/3\">" +
							"<type>text</type>" +
							"<uri>http://192.168.2.247/Special:Tags?tag=test</uri>" +
							"<title>test</title>" +
						"</tag>" +
						"<tag value=\"tag with ws\" id=\"4\" " +
							"href=\"http://192.168.2.247/@api/deki/site/tags/4\">" +
							"<type>text</type>" +
							"<uri>http://192.168.2.247/Special:Tags?tag=tag+with+ws</uri>" +
							"<title>tag with ws</title>" +
						"</tag>" +
						"<tag value=\"tag,punctuation;\" " +
							"id=\"5\" href=\"http://192.168.2.247/@api/deki/site/tags/5\">" +
							"<type>text</type>" +
							"<uri>http://192.168.2.247/Special:Tags?tag=tag,punctuation;</uri>" +
							"<title>tag,punctuation;</title>" +
						"</tag>" +
						"<tag value=\"CAPS\" id=\"6\" " +
							"href=\"http://192.168.2.247/@api/deki/site/tags/6\">" +
							"<type>text</type>" +
							"<uri>http://192.168.2.247/Special:Tags?tag=CAPS</uri>" +
							"<title>CAPS</title>" +
						"</tag>" +
						"</tags>", 
						page.tags);
				break;
			}
			i++;
		}
	}

	public void testGetComments() {
		Vector<MindtouchPage> pages = tester.getPages();
		assertNotNull(pages);
		Vector<MindtouchPage> actual = tester.getComments(pages);
		assertNotNull(actual);

		MindtouchPage root = actual.get(0);
		assertNotNull(root);
		Vector<MindtouchPage> lvl1 = root.getSubpages();
		assertNotNull(lvl1);

		MindtouchPage sandbox = lvl1.get(0);
		assertNotNull(sandbox);
		assertNotNull(sandbox.comments);
		assertTrue(sandbox.comments.contains("count=\"0\""));
		Vector<MindtouchPage> lvl2 = sandbox.getSubpages();
		assertNotNull(lvl2);

		int i = 0;
		for (MindtouchPage page : lvl2) {
			assertNotNull(page);
			switch(i) {
			case 0:
			case 2:
			case 3:
			case 5: 
			case 4: 
			case 6:
				assertNotNull(page.comments);
				assertTrue(page.comments.contains("count=\"0\""));
				break;
			case 1: //Test Comments
				assertNotNull(page.comments);
				assertEquals("<comments sort=\"asc\" count=\"3\" totalcount=\"3\" " +
							"href=\"http://192.168.2.247/@api/deki/pages/42/comments\">" +
							"<comment id=\"1\" href=\"http://192.168.2.247/@api/deki/pages/42/" +
							"comments/1\"><user.createdby id=\"1\" href=\"http://192.168.2.247" +
							"/@api/deki/users/1\"><nick>laura.kolker</nick><username>laura.kolker" +
							"</username><email>laura.kolker@gmail.com</email><hash.email>" +
							"9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gravatar>" +
							"http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08" +
							"</uri.gravatar></user.createdby><date.posted>2009-10-06T04:30:25Z" +
							"</date.posted><title></title><number>1</number><content " +
							"type=\"text/plain; charset=utf-8\" href=\"http://192.168.2.247/@api/" +
							"deki/pages/42/comments/1/content\">" +
						"Comment 1</content></comment>" +
							"<comment id=\"2\" href=\"http://192.168.2.247/@api/deki/pages/42/" +
							"comments/2\"><user.createdby id=\"1\" href=\"http://192.168.2.247/" +
							"@api/deki/users/1\"><nick>laura.kolker</nick><username>laura.kolker" +
							"</username><email>laura.kolker@gmail.com</email><hash.email>" +
							"9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gravatar>" +
							"http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08" +
							"</uri.gravatar></user.createdby><date.posted>2009-10-06T04:30:32Z" +
							"</date.posted><title></title><number>2</number>" +
							"<content type=\"text/plain; charset=utf-8\" " +
							"href=\"http://192.168.2.247/@api/deki/pages/42/comments/2/content\">" +
						"Foo Bar</content></comment><comment id=\"3\" " +
							"href=\"http://192.168.2.247/@api/deki/pages/42/comments/3\">" +
							"<user.createdby id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\">" +
							"<nick>laura.kolker</nick><username>laura.kolker</username><email>" +
							"laura.kolker@gmail.com</email><hash.email>9ed3310b0d84b37fe658dce82b713c08" +
							"</hash.email><uri.gravatar>http://www.gravatar.com/avatar/" +
							"9ed3310b0d84b37fe658dce82b713c08</uri.gravatar></user.createdby>" +
							"<date.posted>2009-10-06T16:24:31Z</date.posted><title></title>" +
							"<number>3</number><content type=\"text/plain; charset=utf-8\" " +
							"href=\"http://192.168.2.247/@api/deki/pages/42/comments/3/content\">" +
						"Abcdef</content></comment></comments>", 
						page.comments);
				break;
			}
			i++;
		}
	}

	public void testGetAttachments() {
		Vector<MindtouchPage> pages = tester.getPages();
		assertNotNull(pages);
		Vector<MindtouchPage> actual = tester.getAttachments(pages);
		assertNotNull(actual);

		MindtouchPage root = actual.get(0);
		assertNotNull(root);
		Vector<MindtouchPage> lvl1 = root.getSubpages();
		assertNotNull(lvl1);

		MindtouchPage sandbox = lvl1.get(0);
		assertNotNull(sandbox);
		assertNotNull(sandbox.attachments);
		assertTrue(sandbox.attachments.contains("count=\"0\""));
		Vector<MindtouchPage> lvl2 = sandbox.getSubpages();
		assertNotNull(lvl2);

		int i = 0;
		for (MindtouchPage page : lvl2) {
			assertNotNull(page);
			switch(i) {
			case 1: 
			case 2:
			case 3:
			case 5: 
			case 4: 
			case 6:
				assertNotNull(page.attachments);
				assertTrue(page.attachments.contains("count=\"0\""));
				break;
			case 0: //Test Attachments
				assertNotNull(page.attachments);
				assertEquals(EX_MULT_FILES, 
						page.attachments);
				break;
			}
			i++;
		}
	}

	public void testCleanOutputDir() {
		File dir = new File("tmp/exported_mindtouch_pages");
		assertFalse(dir.exists());
		
		tester.cleanOutputDir(this.props.getProperty(MindtouchExporter.PROPKEY_OUTPUTDIR));
		
		assertTrue(dir.exists());
		
		tester.deleteDir(dir);
	}
	
	public void testOutputPageData() {
		File out = new File("tmp/exported_mindtouch_pages");
		assertFalse(out.exists());
		
		Vector<MindtouchPage> pages = new Vector<MindtouchPage>();
		MindtouchPage page = new MindtouchPage("39", "Sandbox", 
				"<content type=\"text/html\" title=\"Sandbox\"><body>\n" + 
					"<p>Testing 123</p></body><body target=\"toc\"><em>No headers</em></body></content>",
				"<tags count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/tags\" />",
				"<comments count=\"0\" totalcount=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/comments\" />",
				"<files count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/files\" />");
		MindtouchPage child = new MindtouchPage("43", "Test Tags", 
				"<content type=\"text/html\" title=\"Test Tags\"><body>\n" + 
					"<p>This page will have tags associated with it that will need to be turned into labels</p></body><body target=\"toc\"><em>No headers</em></body></content>",
				"<tags count=\"1\" href=\"http://192.168.2.247/@api/deki/pages/43/tags\">" +
					"<tag value=\"test\" id=\"3\" href=\"http://192.168.2.247/@api/deki/site/tags/3\">" +
					"<type>text</type><uri>http://192.168.2.247/Special:Tags?tag=test</uri><title>test</title>" +
					"</tag>" +
					"</tags>",
				"<comments count=\"0\" totalcount=\"0\" href=\"http://192.168.2.247/@api/deki/pages/43/comments\" />",
				"<files count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/43/files\" />");
		MindtouchPage attchild = new MindtouchPage("40", "Test Attachments", 
				"<content type=\"text/html\" title=\"Test Attachments\"><body>\n" + 
				"<p>We\'re going to have some attachments content.</p>\n" + 
				"<p>We need an image that\'s inline, a link to an attachment, and an attached file with no reference in the page.</p>\n" + 
				"<p>Inline:</p>\n" + 
				"<p><img alt=\"cow.jpg\" class=\"internal default\" src=\"http://192.168.2.247/@api/deki/files/1/=cow.jpg\" /></p>\n" + 
				"<p>Link:</p>\n" + 
				"<p><a rel=\"internal\" href=\"http://192.168.2.247/@api/deki/files/3/=abc.txt\" class=\"iconitext-16 ext-txt \">abc.txt</a></p>\n" + 
				"</body><body target=\"toc\"><em>No headers</em></body></content>",
			"<tags count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/40/tags\" />",
			"<comments count=\"0\" totalcount=\"0\" href=\"http://192.168.2.247/@api/deki/pages/40/comments\" />",
			"<files count=\"1\" href=\"http://192.168.2.247/@api/deki/pages/40/files\">" +
				"<file id=\"3\" href=\"http://192.168.2.247/@api/deki/files/3/info\"><filename>abc.txt</filename><description>text file" +
				"</description><contents type=\"text/plain\" size=\"11\" " +
				"href=\"http://192.168.2.247/@api/deki/files/3/=abc.txt\" /><date.created>2009-09-30T20:20:11Z" +
				"</date.created><user.createdby id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\">" +
				"<nick>laura.kolker</nick><username>laura.kolker</username><email>laura.kolker@gmail.com</email>" +
				"<hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email>" +
				"<uri.gravatar>http://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar>" +
				"</user.createdby><revisions count=\"1\" totalcount=\"1\"" +
				" href=\"http://192.168.2.247/@api/deki/files/3/revisions\" />" +
				"<page.parent id=\"40\" href=\"http://192.168.2.247/@api/deki/pages/40?redirects=0\">" +
				"<uri.ui>http://192.168.2.247/Sandbox/Test_Attachments</uri.ui><title>Test Attachments</title>" +
				"<path>Sandbox/Test_Attachments</path><namespace>main</namespace>" +
				"</page.parent><properties count=\"1\" href=\"http://192.168.2.247/@api/deki/files/3/properties\">" +
				"<property name=\"urn:deki.mindtouch.com#description\" href=\"http://192.168.2.247/@api/deki/files" +
				"/3/properties/urn%253adeki.mindtouch.com%2523description/info\" " +
				"etag=\"6.r1_ts2009-09-30T20:20:11Z\"><contents type=\"text/plain; charset=utf-8\" size=\"9\"" +
				" href=\"http://192.168.2.247/@api/deki/files/3/properties/urn%253adeki.mindtouch.com%" +
				"2523description\">text file</contents><date.modified>2009-09-30T20:20:11Z</date.modified>" +
				"<user.modified id=\"1\" href=\"http://192.168.2.247/@api/deki/users/1\">" +
				"<nick>laura.kolker</nick><username>laura.kolker</username><email>laura.kolker@gmail.com</email>" +
				"<hash.email>9ed3310b0d84b37fe658dce82b713c08</hash.email><uri.gravatar>http" +
				"://www.gravatar.com/avatar/9ed3310b0d84b37fe658dce82b713c08</uri.gravatar>" +
				"</user.modified></property></properties></file></files>");
		page.getSubpages().add(child);
		page.getSubpages().add(attchild);
		pages.add(page);
		
		try {
			tester.outputPageData(pages);
			assertTrue(out.exists());
			File[] outfiles = out.listFiles(new NoSvnFilter());
			assertNotNull(outfiles);
			assertEquals(2, outfiles.length);
			int i = 0;
			File[] children = null;
			for (File outfile : outfiles) {
				assertNotNull(outfile);
				switch(i++) {
				case 0:
					assertEquals("39_Sandbox.xml", outfile.getName());
					assertTrue(outfile.isFile());
					break;
				case 1:
					assertEquals("39_Sandbox_subpages", outfile.getName());
					assertTrue(outfile.isDirectory());
					children = outfile.listFiles(new NoSvnFilter());
					break;
				}
			}
			//subpages
			assertNotNull(children);
			assertEquals(3, children.length);
			for (File outchild : children) {
				assertNotNull(outchild);
				String name = outchild.getName();
				assertTrue(name, "43_TestTags.xml".equals(name) ||
							"40_TestAttachments.xml".equals(name) ||
							"40_TestAttachments_attachments".equals(name));
				if ("40_TestAttachments_attachments".equals(name)) {
					assertTrue(outchild.isDirectory());
					children = outchild.listFiles(new NoSvnFilter());
				}
				else assertTrue(outchild.isFile());
			}
			
			//attachments
			assertNotNull(children);
			assertEquals(1, children.length);
			File attachment = children[0];
			assertNotNull(attachment);
			assertEquals("abc.txt", attachment.getName());
		} finally {
			tester.deleteDir(out);
		}
		
		
	}

	

	public void testGetPagelistXml() {
		String input, expected, actual;
		expected = "<pages>" + 
				"<page id=\"21\" href=\"http://192.168.2.247/@api/deki/pages/21?redirects=0\"><uri.ui>http://192.168.2.247/</uri.ui><title>MindTouch</title><path></path><namespace>main</namespace><subpages>" + 
				"<page id=\"55\" href=\"http://192.168.2.247/@api/deki/pages/55?redirects=0\"><uri.ui>http://192.168.2.247/Mindtouch_Root_Page_Child</uri.ui><title>Mindtouch Root Page Child</title><path>Mindtouch_Root_Page_Child</path><namespace>main</namespace><subpages /></page>" +
				"<page id=\"51\" href=\"http://192.168.2.247/@api/deki/pages/51?redirects=0\"><uri.ui>http://192.168.2.247/NS%3aNew_Namespace</uri.ui><title>NS:New Namespace</title><path>NS:New_Namespace</path><namespace>main</namespace><subpages /></page>" +
				"<page id=\"39\" href=\"http://192.168.2.247/@api/deki/pages/39?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox</uri.ui><title>Sandbox</title><path>Sandbox</path><namespace>main</namespace><subpages>" + 
				"<page id=\"40\" href=\"http://192.168.2.247/@api/deki/pages/40?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Attachments</uri.ui><title>Test Attachments</title><path>Sandbox/Test_Attachments</path><namespace>main</namespace><subpages /></page>" + 
				"<page id=\"42\" href=\"http://192.168.2.247/@api/deki/pages/42?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Comments</uri.ui><title>Test Comments</title><path>Sandbox/Test_Comments</path><namespace>main</namespace><subpages /></page>" + 
				"<page id=\"46\" href=\"http://192.168.2.247/@api/deki/pages/46?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Headers</uri.ui><title>Test Headers</title><path>Sandbox/Test_Headers</path><namespace>main</namespace><subpages /></page>" +
				"<page id=\"41\" href=\"http://192.168.2.247/@api/deki/pages/41?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Permissions</uri.ui><title>Test Permissions</title><path>Sandbox/Test_Permissions</path><namespace>main</namespace><subpages /></page>" +
				"<page id=\"44\" href=\"http://192.168.2.247/@api/deki/pages/44?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Syntax</uri.ui><title>Test Syntax</title><path>Sandbox/Test_Syntax</path><namespace>main</namespace><subpages>" +
				"<page id=\"57\" href=\"http://192.168.2.247/@api/deki/pages/57?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Syntax/Invalid_Html</uri.ui><title>Invalid Html</title><path>Sandbox/Test_Syntax/Invalid_Html</path><namespace>main</namespace><subpages /></page>" +
				"<page id=\"56\" href=\"http://192.168.2.247/@api/deki/pages/56?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Syntax/Tables_and_Bold</uri.ui><title>Tables and Bold</title><path>Sandbox/Test_Syntax/Tables_and_Bold</path><namespace>main</namespace><subpages /></page>" +
				"<page id=\"58\" href=\"http://192.168.2.247/@api/deki/pages/58?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Syntax/Test_Dekiscript</uri.ui><title>Test Dekiscript</title><path>Sandbox/Test_Syntax/Test_Dekiscript</path><namespace>main</namespace><subpages /></page></subpages></page>" + 
				"<page id=\"43\" href=\"http://192.168.2.247/@api/deki/pages/43?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Tags</uri.ui><title>Test Tags</title><path>Sandbox/Test_Tags</path><namespace>main</namespace><subpages /></page>" + 
				"<page id=\"45\" href=\"http://192.168.2.247/@api/deki/pages/45?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Titles</uri.ui><title>Test Titles</title><path>Sandbox/Test_Titles</path><namespace>main</namespace><subpages /></page>" +
				"<page id=\"49\" href=\"http://192.168.2.247/@api/deki/pages/49?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_XYZ%3b_Bad_Chars</uri.ui><title>Test XYZ; Bad Chars</title><path>Sandbox/Test_XYZ;_Bad_Chars</path><namespace>main</namespace><subpages /></page>" +
				"<page id=\"53\" href=\"http://192.168.2.247/@api/deki/pages/53?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Z_Links</uri.ui><title>Test Z Links</title><path>Sandbox/Test_Z_Links</path><namespace>main</namespace><subpages /></page>" +
				"<page id=\"54\" href=\"http://192.168.2.247/@api/deki/pages/54?redirects=0\"><uri.ui>http://192.168.2.247/Sandbox/Test_Z_underscores</uri.ui><title>Test Z_underscores</title><path>Sandbox/Test_Z_underscores</path><namespace>main</namespace><subpages /></page>" +
				"</subpages></page></subpages></page>" +
				"</pages>" + 
				"";
		actual = tester.getPagelistXml();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testParsePageXml() {
		String input;
		input = "<pages>" + 
				"<page id=\"10\" href=\"abc\"><uri.ui>foo</uri.ui>" +
					"<title>Root Title</title>" +
					"<path></path><namespace>main</namespace>" +
					"<subpages>" + 
						"<page id=\"21\" href=\"foo\"><uri.ui>bar</uri.ui>" +
							"<title>Abc</title><path>Sandbox/Test_Attachments</path><namespace>main</namespace><subpages /></page>" + 
						"<page id=\"32\" href=\"bar\"><uri.ui>foo</uri.ui>" +
							"<title>DEF</title><path>Sandbox/Test_Comments</path><namespace>main</namespace><subpages /></page>" + 
					"</subpages>" +
				"</page>" +
				"</pages>" + 
				"";
		Vector<MindtouchPage> pages = tester.parsePageXml(input);
		assertNotNull(pages);
		assertEquals(1, pages.size());
		MindtouchPage root = pages.get(0);
		assertNotNull(root);
		assertEquals("10", root.id);
		assertEquals("Root Title", root.title);
		assertNotNull(root.getSubpages());
		assertEquals(2, root.getSubpages().size());
		Vector<MindtouchPage> subpages = root.getSubpages();
		assertNotNull(subpages);
		MindtouchPage page1 = subpages.get(0);
		assertNotNull(page1);
		assertEquals("21", page1.id);
		assertEquals("Abc", page1.title);
		assertTrue(page1.getSubpages().isEmpty());
		MindtouchPage page2 = subpages.get(1);
		assertNotNull(page2);
		assertEquals("32", page2.id);
		assertEquals("DEF", page2.title);
		assertTrue(page2.getSubpages().isEmpty());
	}

	
	public void testHandleError() throws FileNotFoundException, IOException { //these failures should provide useful errors to the console
		//handlerError is called in 3 methods:
		// * getFile 404
		Vector<String> ids = new Vector<String>();
		Vector<String> names = new Vector<String>();
		ids.add("1000"); // there should be no file with id 1000 (not found)
		names.add("bad");
		File attdir = tester.cleanOutputDir(this.props.getProperty(MindtouchExporter.PROPKEY_OUTPUTDIR));
		try {
			tester.outputAttachmentData(ids, names, attdir);
			assertEquals(404, tester.getError());
		} finally {
			tester.deleteDir(attdir);
		}
		// * getPagelistXml 401 (Unauthorized)
		this.props.setProperty("pass", "notvalid");
		String pagelistXml = null;
		try {
			pagelistXml = tester.getPagelistXml();
			fail();
		} catch (Exception e) {}
		assertNull(pagelistXml);
		assertEquals(401, tester.getError());
		//reset props
		props.load(new FileInputStream(TESTDIR + "exporter.mindtouch.properties"));
		
		// * getPagePart 404 (not found)
		String input, actual;
		input = "1000"; //there should be nothing with this id on the server
		actual = tester.getContent(input);
		assertNull(actual);
		assertEquals(404, tester.getError());
		
		// Permission error: 403
		this.props.setProperty("user", "notmod");
		this.props.setProperty("pass", "notmod");
		input = "41"; //we don't have access with anonymous
		actual = tester.getContent(input);
		assertNull(actual);
		assertEquals(403, tester.getError());
		//reset props
		props.load(new FileInputStream(TESTDIR + "exporter.mindtouch.properties"));

		// Bad request 400
		actual = tester.getPagePart("39", "contents", "section=abc");
		assertNull(actual);
		assertEquals(400, tester.getError());
	}
	
	public void testGetContent_id() {
		String input, expected, actual;
		input = "39";
		expected = "<content type=\"text/html\" title=\"Sandbox\"><body>\n" + 
				"<p>Testing 123</p></body><body target=\"toc\"><em>No headers</em></body></content>" +
				"";
		actual = tester.getContent(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetAttachmentParents() {
		String input, expected, actual;
		input = "<p><img alt=\"cow.jpg\" class=\"internal default\" " +
				"src=\"http://192.168.2.247/@api" +
				"/deki/files/1/=cow.jpg\" /></p>\n" + 
				"<p><a rel=\"internal\" " +
				"href=\"http://192.168.2.247/@api/deki/files/3/=abc.txt\" " +
				"class=\"iconitext-16 ext-txt \">abc.txt</a></p>\n"; 
		expected = "<p><img alt=\"cow.jpg\" class=\"internal default\" " +
				"src=\"http://192.168.2.247/@api" +
				"/deki/files/1/=cow.jpg?parent=Test Attachments\" /></p>\n" + 
				"<p><a rel=\"internal\" " +
				"href=\"http://192.168.2.247/@api/deki/files/3/=abc.txt?parent=Test Attachments\" " +
				"class=\"iconitext-16 ext-txt \">abc.txt</a></p>\n";
		actual = tester.getAttachmentParents(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetTags_id() {
		String input, expected, actual;
		input = "43"; //Test Tags
		expected = "<tags count=\"4\" " +
				"href=\"http://192.168.2.247/@api/deki/pages/43/tags\">" +
				"<tag value=\"test\" id=\"3\" " +
					"href=\"http://192.168.2.247/@api/deki/site/tags/3\">" +
					"<type>text</type>" +
					"<uri>http://192.168.2.247/Special:Tags?tag=test</uri>" +
					"<title>test</title>" +
				"</tag>" +
				"<tag value=\"tag with ws\" id=\"4\" " +
					"href=\"http://192.168.2.247/@api/deki/site/tags/4\">" +
					"<type>text</type>" +
					"<uri>http://192.168.2.247/Special:Tags?tag=tag+with+ws</uri>" +
					"<title>tag with ws</title>" +
				"</tag>" +
				"<tag value=\"tag,punctuation;\" " +
					"id=\"5\" href=\"http://192.168.2.247/@api/deki/site/tags/5\">" +
					"<type>text</type>" +
					"<uri>http://192.168.2.247/Special:Tags?tag=tag,punctuation;</uri>" +
					"<title>tag,punctuation;</title>" +
				"</tag>" +
				"<tag value=\"CAPS\" id=\"6\" " +
					"href=\"http://192.168.2.247/@api/deki/site/tags/6\">" +
					"<type>text</type>" +
					"<uri>http://192.168.2.247/Special:Tags?tag=CAPS</uri>" +
					"<title>CAPS</title>" +
				"</tag>" +
				"</tags>";
		actual = tester.getTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}


	public void testGetFilename() {
		MindtouchPage page = new MindtouchPage("39", "Sandbox", 
				"<content type=\"text/html\" title=\"Sandbox\"><body>\n" + 
					"<p>Testing 123</p></body><body target=\"toc\"><em>No headers</em></body></content>",
				"<tags count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/tags\" />",
				"<comments count=\"0\" totalcount=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/comments\" />",
				"<files count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/files\" />");
		String input, expected, actual;
		expected = "39_Sandbox.xml";
		actual = tester.getFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		page.id = "20";
		expected = "20_Sandbox.xml";
		actual = tester.getFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		page.title = "Testing whitespace";
		expected = "20_Testingwhitespace.xml";
		actual = tester.getFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		page.title = "Testing;badchars";
		expected = "20_Testingbadchars.xml";
		actual = tester.getFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testGetFilesystemTitle() {
		String input, expected, actual;
		input = "test";
		expected = "test";
		actual = tester.getFilesystemTitle(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "a b";
		expected = "ab";
		actual = tester.getFilesystemTitle(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	
		input = "a;!@#$%^*&:{}b";
		actual = tester.getFilesystemTitle(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "a_b"; //underscore is a word char \w
		expected = input;
		actual = tester.getFilesystemTitle(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	

	public void testGetFileContent() {
		String content = "<content type=\"text/html\" title=\"Sandbox\"><body>\n" + 
							"<p>Testing 123</p></body><body target=\"toc\"><em>No headers</em></body></content>";
		String tags = "<tags count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/tags\" />";
		String comments = "<comments count=\"0\" totalcount=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/comments\" />";
		String files = "<files count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/files\" />";
		MindtouchPage page = new MindtouchPage("39", "Sandbox", 
				content,
				tags,
				comments,
				files);
		String input, expected, actual;
		expected = "<pagedata>" + content + tags + comments + files + "</pagedata>";
		actual = tester.getFileContent(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCreateFile() {
		String filename = "testing123.txt";
		File parent = tester.cleanOutputDir(this.props.getProperty(MindtouchExporter.PROPKEY_OUTPUTDIR));
		try {
			File actual = tester.createFile(filename, parent);
			assertNotNull(actual);
			File parentFile = actual.getParentFile();
			assertEquals(parent.getAbsolutePath(), parentFile.getAbsolutePath());
			assertTrue(actual.getAbsolutePath().endsWith(File.separator + filename));
		} finally {
			tester.deleteDir(parent);
		}
	}

	public void testWrite() {
		File parent = tester.cleanOutputDir(this.props.getProperty(MindtouchExporter.PROPKEY_OUTPUTDIR));
		try {
			File file = new File(parent.getAbsolutePath() + File.separator + "test.txt");
			String content = "abcdef";
			tester.write(file , content);

			assertTrue(file.exists());
			String read = "";
			String line;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while ((line = reader.readLine()) != null) {
					read += line;
				}
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			assertEquals(content, read);
		} finally {
			tester.deleteDir(parent);
		}
	}

	public void testGetAttFilename() {
		MindtouchPage page = new MindtouchPage("39", "Sandbox", 
				"<content type=\"text/html\" title=\"Sandbox\"><body>\n" + 
					"<p>Testing 123</p></body><body target=\"toc\"><em>No headers</em></body></content>",
				"<tags count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/tags\" />",
				"<comments count=\"0\" totalcount=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/comments\" />",
				"<files count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/files\" />");
		String input, expected, actual;
		expected = "39_Sandbox_attachments";
		actual = tester.getAttFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		page.id = "20";
		expected = "20_Sandbox_attachments";
		actual = tester.getAttFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		page.title = "Testing whitespace";
		expected = "20_Testingwhitespace_attachments";
		actual = tester.getAttFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		page.title = "Testing;badchars";
		expected = "20_Testingbadchars_attachments";
		actual = tester.getAttFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testOutputAttachmentData() {
		MindtouchPage page = new MindtouchPage("40", "Test Attachments", 
				"<content type=\"text/html\" title=\"Test Attachments\"><body>\n" + 
				"<p>We\'re going to have some attachments content.</p>\n" + 
				"<p>We need an image that\'s inline, a link to an attachment, and an attached file with no reference in the page.</p>\n" + 
				"<p>Inline:</p>\n" + 
				"<p><img alt=\"cow.jpg\" class=\"internal default\" src=\"http://192.168.2.247/@api/deki/files/1/=cow.jpg\" /></p>\n" + 
				"<p>Link:</p>\n" + 
				"<p><a rel=\"internal\" href=\"http://192.168.2.247/@api/deki/files/3/=abc.txt\" class=\"iconitext-16 ext-txt \">abc.txt</a></p>\n" + 
				"</body><body target=\"toc\"><em>No headers</em></body></content>",
			"<tags count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/40/tags\" />",
			"<comments count=\"0\" totalcount=\"0\" href=\"http://192.168.2.247/@api/deki/pages/40/comments\" />",
			EX_MULT_FILES);
		
		File attdir = tester.cleanOutputDir(this.props.getProperty(MindtouchExporter.PROPKEY_OUTPUTDIR));
		try {
			tester.outputAttachmentData(page, attdir);
			File[] files = attdir.listFiles(new NoSvnFilter());
			assertNotNull(files);
			assertEquals(3, files.length);
			int i = 0;
			for (File file : files) {
				assertNotNull(file);
				assertTrue(file.length() > 0);
				assertTrue("cow.jpg".equals(file.getName()) ||
						"abc.txt".equals(file.getName()) ||
						"hobbespounce.gif".equals(file.getName()));
			}
		} finally {
			tester.deleteDir(attdir);
		}
	}
	public void testGetSubFilename() {
		MindtouchPage page = new MindtouchPage("39", "Sandbox", 
				"<content type=\"text/html\" title=\"Sandbox\"><body>\n" + 
					"<p>Testing 123</p></body><body target=\"toc\"><em>No headers</em></body></content>",
				"<tags count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/tags\" />",
				"<comments count=\"0\" totalcount=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/comments\" />",
				"<files count=\"0\" href=\"http://192.168.2.247/@api/deki/pages/39/files\" />");
		String input, expected, actual;
		expected = "39_Sandbox_subpages";
		actual = tester.getSubFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		page.id = "20";
		expected = "20_Sandbox_subpages";
		actual = tester.getSubFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		page.title = "Testing whitespace";
		expected = "20_Testingwhitespace_subpages";
		actual = tester.getSubFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		page.title = "Testing;badchars";
		expected = "20_Testingbadchars_subpages";
		actual = tester.getSubFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCreateDir() {
		File parent = tester.cleanOutputDir(this.props.getProperty(MindtouchExporter.PROPKEY_OUTPUTDIR));
		
		try {
			String newdir = "testing123";
			File actual = tester.createDir(newdir, parent);
			assertNotNull(actual);
			assertTrue(actual.exists());
			assertTrue(actual.isDirectory());
		}
		finally {
			tester.deleteDir(parent);
		}
	}


	public void testHasAttachments() {
		MindtouchPage pagetest = new MindtouchPage();
		assertFalse(pagetest.hasAttachments());
		pagetest.attachments = "<files count=\"0\" ...";
		assertFalse(pagetest.hasAttachments());
		pagetest.attachments = "<files count=\"3\" ...";
		assertTrue(pagetest.hasAttachments());
	}


	/* XXX Basic httpclient + Mindtouch rest api tests */
	public void testAuthenticate() throws HttpException, IOException {
		String baseurl = props.getProperty(MindtouchExporter.PROPKEY_URLBASE, null);
		String user = props.getProperty(MindtouchExporter.PROPKEY_USER, null);
		String pass = props.getProperty(MindtouchExporter.PROPKEY_PASS, null);
		int port = Integer.parseInt(props.getProperty(MindtouchExporter.PROPKEY_URLPORT, "80"));
		assertNotNull(baseurl);
		assertNotNull(user);
		assertNotNull(pass);

		HttpClient client = new HttpClient();
		client.getState().setCredentials(new AuthScope(baseurl, port),
				new UsernamePasswordCredentials(user, pass));

		String apiurl = tester.createApiUrl(baseurl);
		String url = apiurl + "users/authenticate";
		GetMethod get = new GetMethod(url);
		get.setDoAuthentication(true);

		try {
			int result = client.executeMethod(get);
			assertEquals(200, result);
		} finally {
			get.releaseConnection();
		}
	}

	public void testExportPage() throws HttpException, IOException {
		String baseurl = props.getProperty(MindtouchExporter.PROPKEY_URLBASE, null);
		assertNotNull(baseurl);
		String apiurl = tester.createApiUrl(baseurl);
		String pageid = "=Sandbox";
		String url = apiurl + "pages/" + pageid + "/contents";
		GetMethod method = new GetMethod(url);
		HttpClient client = new HttpClient();
		try {
			int result = client.executeMethod(method);
			assertEquals(200, result);
		} finally {
			method.releaseConnection();
		}

		pageid = "41"; //won't work without authentication;
		url = apiurl + "pages/" + pageid + "/contents";
		method = new GetMethod(url);
		try {
			int result = client.executeMethod(method);
			assertEquals(401, result);
		} finally {
			method.releaseConnection();
		}
	}

	public void testExportPageWithAuth() throws HttpException, IOException {
		String baseurl = props.getProperty(MindtouchExporter.PROPKEY_URLBASE, null);
		String user = props.getProperty(MindtouchExporter.PROPKEY_USER, null);
		String pass = props.getProperty(MindtouchExporter.PROPKEY_PASS, null);
		int port = Integer.parseInt(props.getProperty(MindtouchExporter.PROPKEY_URLPORT, "80"));
		assertNotNull(baseurl);
		assertNotNull(user);
		assertNotNull(pass);
		String apiurl = tester.createApiUrl(baseurl);
		String pageid = "41";
		String url = apiurl + "pages/" + pageid + "/contents";
		GetMethod method = new GetMethod(url);
		HttpClient client = new HttpClient();
		client.getState().setCredentials(new AuthScope(baseurl, port),
				new UsernamePasswordCredentials(user, pass));
		method.setDoAuthentication(true);
		try {
			int result = client.executeMethod(method);
			assertEquals(200, result);
		} finally {
			method.releaseConnection();
		}
	}

	public void testIgnoreMindtouch() throws FileNotFoundException, IOException {
		props = new Properties();
		props.load(new FileInputStream(TESTDIR + "exporter.mindtouch-ignore.properties"));
		tester.setProperties(props);
		
		Vector<MindtouchPage> actual = tester.getPages();
		assertNotNull(actual);
		assertFalse(actual.isEmpty());
		assertTrue(actual.size() > 0);
		
		MindtouchPage sandbox = actual.get(2);
		assertNotNull(sandbox);
		assertNotNull(sandbox.title);
		assertEquals("Sandbox", sandbox.title);
		assertNotNull(sandbox.id);
		Vector<MindtouchPage> lvl2 = sandbox.getSubpages();
		assertNotNull(lvl2);
		assertFalse(lvl2.isEmpty());
		assertTrue(lvl2.size() > 6);
		
		int i = 0;
		for (MindtouchPage page : lvl2) {
			assertNotNull(page);
			assertNotNull(page.id);
			assertNotNull(page.title);
			String expTitle = "";
			switch(i) {
			case 0:
				expTitle = "Test Attachments"; break;
			case 1:
				expTitle = "Test Comments"; break;
			case 2:
				expTitle = "Test Headers"; break;
			case 3:
				expTitle = "Test Permissions"; break;
			case 4: 
				expTitle = "Test Syntax"; break;
			case 5: 
				expTitle = "Test Tags"; break;
			case 6:
				expTitle = "Test Titles"; break;
			default: continue;
			}
			assertEquals(expTitle, page.title);
			i++;
		}
	}
	
}
