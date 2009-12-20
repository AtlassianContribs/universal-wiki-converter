package com.atlassian.uwc.converters.sharepoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Element;

public class CleanConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	CleanConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new CleanConverter();
	}

	public void testCleanRaw() {
		String input = "5;#vti_parserversion:SR|12.0.0.6219 ContentType:SW|Wiki Page ContentTypeId:SW" +
				"|0x010108008586D6569CB93B489CBCF8B01B47B952 vti_metatags:VR|CollaborationServer " +
				"SharePoint\\\\ Team\\\\ Web\\\\ Site vti_charset:SR|utf-8 vti_author:SR| vti_setuppath:" +
				"SX|DocumentTemplates\\\\wkpstd.aspx vti_cachedzones:VR|Bottom vti_cachedneedsrewrite:BR" +
				"|false WikiField:SW|<div class=ExternalClass14E9F8AB519A470B995C8046769BE7FC><div " +
				"class=ExternalClass03BFEACBDD2F4A61A6DF16469ED01B1D><div " +
				"class=ExternalClass9831F41C1D6B425D9281846617F46F8B>For Link Testing. " +
				"We\'re going to link to this from <a href=\"/my%20test%20wiki/Test%20Page%2042.aspx\">" +
				"Test Page 42</a>. <br></div></div></div> vti_modifiedby:SR|PUBLIC36\\\\brendanpatterson " +
				"vti_cachedhastheme:BR|false \n";
		String expected = "<div class=ExternalClass14E9F8AB519A470B995C8046769BE7FC><div " +
				"class=ExternalClass03BFEACBDD2F4A61A6DF16469ED01B1D><div " +
				"class=ExternalClass9831F41C1D6B425D9281846617F46F8B>For Link Testing. " +
				"We\'re going to link to this from <a href=\"/my%20test%20wiki/Test%20Page%2042.aspx\">" +
				"Test Page 42</a>. <br></div></div></div>";
		String actual = tester.cleanRaw(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanWithPipesInContent() {
		String input = "5;#vti_parserversion:SR|12.0.0.6219 ContentType:SW|Wiki Page ContentTypeId:SW" +
				"|0x010108008586D6569CB93B489CBCF8B01B47B952 vti_metatags:VR|CollaborationServer " +
				"SharePoint\\\\ Team\\\\ Web\\\\ Site vti_charset:SR|utf-8 vti_author:SR| vti_setuppath:" +
				"SX|DocumentTemplates\\\\wkpstd.aspx vti_cachedzones:VR|Bottom vti_cachedneedsrewrite:BR" +
				"|false WikiField:SW|<div class=ExternalClass14E9F8AB519A470B995C8046769BE7FC><div " +
				"class=ExternalClass03BFEACBDD2F4A61A6DF16469ED01B1D><div " +
				"class=ExternalClass9831F41C1D6B425D9281846617F46F8B><p>Testing annoying characters in text</p>" +
				"<p>Here is a pipe | it\'s an important delimiter in the raw sharepoint output.<br>" +
				"\r\n" +
				"</p></div></div></div> vti_modifiedby:SR|PUBLIC36\\\\brendanpatterson " +
				"vti_cachedhastheme:BR|false \n";
		String expected = "<div class=ExternalClass14E9F8AB519A470B995C8046769BE7FC><div " +
				"class=ExternalClass03BFEACBDD2F4A61A6DF16469ED01B1D><div " +
				"class=ExternalClass9831F41C1D6B425D9281846617F46F8B><p>Testing annoying characters in text</p>" +
				"<p>Here is a pipe | it\'s an important delimiter in the raw sharepoint output.<br>" +
				"\r\n" +
				"</p></div></div></div>";
		String actual = tester.cleanRaw(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCleanAtt() {
		String input = "<abc att=bad>";
		String expected = "<abc att=\"bad\">";
		String actual = tester.cleanAttributes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<div class=ExternalClass092801140FA14EFE800C1C8409FE582F><div class=ExternalClass" +
				"734A488FC71F4D5FB6A3D3FA9B4A7009><div class=ExternalClassF6BBA484EE374A5DA714B173" +
				"D61AAEDB><div class=ExternalClass6326FBE3DC0E46818A48A81057351C01><div class=Exte" +
				"rnalClassD6E735C670CE45ADBE6372ED96807CF6><div class=ExternalClassEC1AD7361D6D4FE" +
				"D8EDB2C7A447879FD><div class=ExternalClass4EB764AAC83A4CF38A18128299D552AC>Testin" +
				"g 123<br><br><font size=5>Links<br><font size=2><a title=\"Search Engine Extraord" +
				"inaire\" href=\"http://www.google.com\">Google</a><br><a href=\"#Lorem Ipsum 2\">" +
				"Lorem Ipsum 2 in page</a><br>How are you supposed to link internally? Link UI imp" +
				"lies http link?<br><a href=\"/my%20test%20wiki/Test%20Page%2072.aspx\">Test Page " +
				"72</a><br><a href=\"/my%20test%20wiki/Sharepoint%20Converter%20Links.aspx\">Share" +
				"point Converter Links</a><br><br><font size=7>Lorem Ipsum 1</font><br><br></font>" +
				"</font><p>Lorem ipsum dolor sit amet, consectetuer\\r\\nadipiscing elit. Aliquam " +
				"fermentum vestibulum est. Cras rhoncus.\\r\\nPellentesque habitant morbi tristiqu" +
				"e senectus et netus et malesuada\\r\\nfames ac turpis egestas. Sed quis tortor. D" +
				"onec non ipsum. Mauris\\r\\ncondimentum, odio nec porta tristique, ante neque mal" +
				"esuada massa, in\\r\\ndignissim eros velit at tellus. Donec et risus in ligula el" +
				"eifend\\r\\nconsectetuer. Donec volutpat eleifend augue. Integer gravida sodales" +
				"\\r\\nleo. Nunc vehicula neque ac erat. Vivamus non nisl. Fusce ac magna.\\r\\nSu" +
				"spendisse euismod libero eget mauris.</p>\\r\\n<p>Ut ligula. Maecenas consequat. " +
				"Aliquam placerat. Cum sociis natoque\\r\\npenatibus et magnis dis parturient mont" +
				"es, nascetur ridiculus mus.\\r\\nNulla convallis. Ut quis tortor. Vestibulum a le" +
				"ctus at diam fermentum\\r\\nvehicula. Mauris sed turpis a nisl ultricies facilisi" +
				"s. Fusce ornare,\\r\\nmi vitae hendrerit eleifend, augue erat cursus nunc, a aliq" +
				"uam elit leo\\r\\nsed est. Donec eget sapien sit amet eros vehicula mollis. In\\r" +
				"\\nsollicitudin libero in felis. Phasellus metus sem, pulvinar in, porta\\r\\nnec" +
				", faucibus in, ipsum. Nam a tellus. Aliquam erat volutpat.</p>\\r\\n<p>Sed id vel" +
				"it ut orci feugiat tempus. Pellentesque accumsan augue at\\r\\nlibero elementum v" +
				"estibulum. Maecenas sit amet metus. Etiam molestie\\r\\nmassa sed erat. Aenean ti" +
				"ncidunt. Mauris id eros. Quisque eu ante.\\r\\nFusce eu dolor. Aenean ultricies a" +
				"nte ut diam. Donec iaculis, pede eu\\r\\naliquet lobortis, wisi est dignissim dia" +
				"m, ut fringilla eros magna a\\r\\nmi. Nulla vel lorem. Donec placerat, lectus qui" +
				"s molestie hendrerit,\\r\\nante tortor pharetra risus, ac rutrum arcu odio eu tor" +
				"tor. In dapibus\\r\\nlacus nec ligula. Aenean vel metus. Nunc mattis lorem posuer" +
				"e felis. In\\r\\nvehicula tempus lacus. Phasellus arcu. Nam ut arcu. Duis eget el" +
				"it id\\r\\neros adipiscing dignissim.</p>\\r\\n                            \\r\\n" +
				"\\r\\n                            \\r\\n\\r\\n                                \\r" +
				"\\n                                \\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n    " +
				"\\r\\n    \\r\\n\\r\\n\\r\\n    \\r\\n    \\r\\n    \\r\\n    \\r\\n             " +
				"                           <br><font size=5><font size=2><br>(anchor here)<br><a " +
				"href=\"#\" name=\"Lorem Ipsum 2\"><br></a></font><a href=\"#\" name=\"Lorem Ipsum" +
				" 2\"></font><a href=\"#\" name=\"Lorem Ipsum 2\"><font size=5><font size=2><font " +
				"size=7>Lorem Ipsum 2</font><br></font></font></a><p><a href=\"#\" name=\"Lorem Ip" +
				"sum 2\">Lorem ipsum dolor sit amet, consectetuer\\r\\nadipiscing elit. Aliquam fe" +
				"rmentum vestibulum est. Cras rhoncus.\\r\\nPellentesque habitant morbi tristique " +
				"senectus et netus et malesuada\\r\\nfames ac turpis egestas. Sed quis tortor. Don" +
				"ec non ipsum. Mauris\\r\\ncondimentum, odio nec porta tristique, ante neque males" +
				"uada massa, in\\r\\ndignissim eros velit at tellus. Donec et risus in ligula elei" +
				"fend\\r\\nconsectetuer. Donec volutpat eleifend augue. Integer gravida sodales\\r" +
				"\\nleo. Nunc vehicula neque ac erat. Vivamus non nisl. Fusce ac magna.\\r\\nSuspe" +
				"ndisse euismod libero eget mauris.</a></p>\\r\\n<p><a href=\"#\" name=\"Lorem Ips" +
				"um 2\">Ut ligula. Maecenas consequat. Aliquam placerat. Cum sociis natoque\\r\\np" +
				"enatibus et magnis dis parturient montes, nascetur ridiculus mus.\\r\\nNulla conv" +
				"allis. Ut quis tortor. Vestibulum a lectus at diam fermentum\\r\\nvehicula. Mauri" +
				"s sed turpis a nisl ultricies facilisis. Fusce ornare,\\r\\nmi vitae hendrerit el" +
				"eifend, augue erat cursus nunc, a aliquam elit leo\\r\\nsed est. Donec eget sapie" +
				"n sit amet eros vehicula mollis. In\\r\\nsollicitudin libero in felis. Phasellus " +
				"metus sem, pulvinar in, porta\\r\\nnec, faucibus in, ipsum. Nam a tellus. Aliquam" +
				" erat volutpat.</a></p>\\r\\n<p><a href=\"#\" name=\"Lorem Ipsum 2\">Sed id velit" +
				" ut orci feugiat tempus. Pellentesque accumsan augue at\\r\\nlibero elementum ves" +
				"tibulum. Maecenas sit amet metus. Etiam molestie\\r\\nmassa sed erat. Aenean tinc" +
				"idunt. Mauris id eros. Quisque eu ante.\\r\\nFusce eu dolor. Aenean ultricies ant" +
				"e ut diam. Donec iaculis, pede eu\\r\\naliquet lobortis, wisi est dignissim diam," +
				" ut fringilla eros magna a\\r\\nmi. Nulla vel lorem. Donec placerat, lectus quis " +
				"molestie hendrerit,\\r\\nante tortor pharetra risus, ac rutrum arcu odio eu torto" +
				"r. In dapibus\\r\\nlacus nec ligula. Aenean vel metus. Nunc mattis lorem posuere " +
				"felis. In\\r\\nvehicula tempus lacus. Phasellus arcu. Nam ut arcu. Duis eget elit" +
				" id\\r\\neros adipiscing dignissim.</a></p>\\r\\n<a href=\"#\" name=\"Lorem Ipsum" +
				" 2\">                            \\r\\n\\r\\n                            \\r\\n\\r" +
				"\\n                                \\r\\n                                \\r\\n\\r" +
				"\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n    \\r\\n    \\r\\n\\r\\n\\r\\n    \\r\\n    \\r" +
				"\\n    \\r\\n    \\r\\n                                        <br><font size=7>Lo" +
				"rem Ipsum 3</font><br><br><br></a><p><a href=\"#\" name=\"Lorem Ipsum 2\">Lorem ip" +
				"sum dolor sit amet, consectetuer\\r\\nadipiscing elit. Aliquam fermentum vestibulu" +
				"m est. Cras rhoncus.\\r\\nPellentesque habitant morbi tristique senectus et netus " +
				"et malesuada\\r\\nfames ac turpis egestas. Sed quis tortor. Donec non ipsum. Mauri" +
				"s\\r\\ncondimentum, odio nec porta tristique, ante neque malesuada massa, in\\r\\n" +
				"dignissim eros velit at tellus. Donec et risus in ligula eleifend\\r\\nconsectetue" +
				"r. Donec volutpat eleifend augue. Integer gravida sodales\\r\\nleo. Nunc vehicula " +
				"neque ac erat. Vivamus non nisl. Fusce ac magna.\\r\\nSuspendisse euismod libero e" +
				"get mauris.</a></p>\\r\\n<p><a href=\"#\" name=\"Lorem Ipsum 2\">Ut ligula. Maecen" +
				"as consequat. Aliquam placerat. Cum sociis natoque\\r\\npenatibus et magnis dis pa" +
				"rturient montes, nascetur ridiculus mus.\\r\\nNulla convallis. Ut quis tortor. Ves" +
				"tibulum a lectus at diam fermentum\\r\\nvehicula. Mauris sed turpis a nisl ultrici" +
				"es facilisis. Fusce ornare,\\r\\nmi vitae hendrerit eleifend, augue erat cursus nu" +
				"nc, a aliquam elit leo\\r\\nsed est. Donec eget sapien sit amet eros vehicula moll" +
				"is. In\\r\\nsollicitudin libero in felis. Phasellus metus sem, pulvinar in, porta" +
				"\\r\\nnec, faucibus in, ipsum. Nam a tellus. Aliquam erat volutpat.</a></p>\\r\\n<" +
				"p><a href=\"#\" name=\"Lorem Ipsum 2\">Sed id velit ut orci feugiat tempus. Pellen" +
				"tesque accumsan augue at\\r\\nlibero elementum vestibulum. Maecenas sit amet metus" +
				". Etiam molestie\\r\\nmassa sed erat. Aenean tincidunt. Mauris id eros. Quisque eu" +
				" ante.\\r\\nFusce eu dolor. Aenean ultricies ante ut diam. Donec iaculis, pede eu" +
				"\\r\\naliquet lobortis, wisi est dignissim diam, ut fringilla eros magna a\\r\\nmi" +
				". Nulla vel lorem. Donec placerat, lectus quis molestie hendrerit,\\r\\nante torto" +
				"r pharetra risus, ac rutrum arcu odio eu tortor. In dapibus\\r\\nlacus nec ligula." +
				" Aenean vel metus. Nunc mattis lorem posuere felis. In\\r\\nvehicula tempus lacus." +
				" Phasellus arcu. Nam ut arcu. Duis eget elit id\\r\\neros adipiscing dignissim.</a" +
				"></p><p><a href=\"#\" name=\"Lorem Ipsum 2\"></p><p><a href=\"#\" name=\"Lorem Ips" +
				"um 2\"><br></a></p>\\r\\n<a href=\"#\" name=\"Lorem Ipsum 2\">                    " +
				"        \\r\\n\\r\\n                            \\r\\n\\r\\n                      " +
				"          \\r\\n                                \\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r" +
				"\\n\\r\\n    \\r\\n    \\r\\n\\r\\n\\r\\n    \\r\\n    \\r\\n    \\r\\n    \\r\\n " +
				"                                       </a></div></div></div></div></div>\\r\\n<p>" +
				"\\r\\nTesting Basic Syntax Syntax</p><p><strong>Bold text here</strong></p><p><em>" +
				"Italics here</em></p><p><u>Underline</u></p><p><u><em><strong>All of the above</st" +
				"rong></em></u></p><p>Testing annoying characters in text</p><p>Here is a pipe | it" +
				"\'s an important delimiter in the raw sharepoint output.<br>\\r\\n</p></div></div>" +
				"";
		expected = "<div class=\"ExternalClass092801140FA14EFE800C1C8409FE582F\"><div class=\"ExternalClass" +
				"734A488FC71F4D5FB6A3D3FA9B4A7009\"><div class=\"ExternalClassF6BBA484EE374A5DA714B173" +
				"D61AAEDB\"><div class=\"ExternalClass6326FBE3DC0E46818A48A81057351C01\"><div class=\"Exte" +
				"rnalClassD6E735C670CE45ADBE6372ED96807CF6\"><div class=\"ExternalClassEC1AD7361D6D4FE" +
				"D8EDB2C7A447879FD\"><div class=\"ExternalClass4EB764AAC83A4CF38A18128299D552AC\">Testin" +
				"g 123<br><br><font size=\"5\">Links<br><font size=\"2\"><a title=\"Search Engine Extraord" +
				"inaire\" href=\"http://www.google.com\">Google</a><br><a href=\"#Lorem Ipsum 2\">" +
				"Lorem Ipsum 2 in page</a><br>How are you supposed to link internally? Link UI imp" +
				"lies http link?<br><a href=\"/my%20test%20wiki/Test%20Page%2072.aspx\">Test Page " +
				"72</a><br><a href=\"/my%20test%20wiki/Sharepoint%20Converter%20Links.aspx\">Share" +
				"point Converter Links</a><br><br><font size=\"7\">Lorem Ipsum 1</font><br><br></font>" +
				"</font><p>Lorem ipsum dolor sit amet, consectetuer\\r\\nadipiscing elit. Aliquam " +
				"fermentum vestibulum est. Cras rhoncus.\\r\\nPellentesque habitant morbi tristiqu" +
				"e senectus et netus et malesuada\\r\\nfames ac turpis egestas. Sed quis tortor. D" +
				"onec non ipsum. Mauris\\r\\ncondimentum, odio nec porta tristique, ante neque mal" +
				"esuada massa, in\\r\\ndignissim eros velit at tellus. Donec et risus in ligula el" +
				"eifend\\r\\nconsectetuer. Donec volutpat eleifend augue. Integer gravida sodales" +
				"\\r\\nleo. Nunc vehicula neque ac erat. Vivamus non nisl. Fusce ac magna.\\r\\nSu" +
				"spendisse euismod libero eget mauris.</p>\\r\\n<p>Ut ligula. Maecenas consequat. " +
				"Aliquam placerat. Cum sociis natoque\\r\\npenatibus et magnis dis parturient mont" +
				"es, nascetur ridiculus mus.\\r\\nNulla convallis. Ut quis tortor. Vestibulum a le" +
				"ctus at diam fermentum\\r\\nvehicula. Mauris sed turpis a nisl ultricies facilisi" +
				"s. Fusce ornare,\\r\\nmi vitae hendrerit eleifend, augue erat cursus nunc, a aliq" +
				"uam elit leo\\r\\nsed est. Donec eget sapien sit amet eros vehicula mollis. In\\r" +
				"\\nsollicitudin libero in felis. Phasellus metus sem, pulvinar in, porta\\r\\nnec" +
				", faucibus in, ipsum. Nam a tellus. Aliquam erat volutpat.</p>\\r\\n<p>Sed id vel" +
				"it ut orci feugiat tempus. Pellentesque accumsan augue at\\r\\nlibero elementum v" +
				"estibulum. Maecenas sit amet metus. Etiam molestie\\r\\nmassa sed erat. Aenean ti" +
				"ncidunt. Mauris id eros. Quisque eu ante.\\r\\nFusce eu dolor. Aenean ultricies a" +
				"nte ut diam. Donec iaculis, pede eu\\r\\naliquet lobortis, wisi est dignissim dia" +
				"m, ut fringilla eros magna a\\r\\nmi. Nulla vel lorem. Donec placerat, lectus qui" +
				"s molestie hendrerit,\\r\\nante tortor pharetra risus, ac rutrum arcu odio eu tor" +
				"tor. In dapibus\\r\\nlacus nec ligula. Aenean vel metus. Nunc mattis lorem posuer" +
				"e felis. In\\r\\nvehicula tempus lacus. Phasellus arcu. Nam ut arcu. Duis eget el" +
				"it id\\r\\neros adipiscing dignissim.</p>\\r\\n                            \\r\\n" +
				"\\r\\n                            \\r\\n\\r\\n                                \\r" +
				"\\n                                \\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n    " +
				"\\r\\n    \\r\\n\\r\\n\\r\\n    \\r\\n    \\r\\n    \\r\\n    \\r\\n             " +
				"                           <br><font size=\"5\"><font size=\"2\"><br>(anchor here)<br><a " +
				"href=\"#\" name=\"Lorem Ipsum 2\"><br></a></font><a href=\"#\" name=\"Lorem Ipsum" +
				" 2\"></font><a href=\"#\" name=\"Lorem Ipsum 2\"><font size=\"5\"><font size=\"2\"><font " +
				"size=\"7\">Lorem Ipsum 2</font><br></font></font></a><p><a href=\"#\" name=\"Lorem Ip" +
				"sum 2\">Lorem ipsum dolor sit amet, consectetuer\\r\\nadipiscing elit. Aliquam fe" +
				"rmentum vestibulum est. Cras rhoncus.\\r\\nPellentesque habitant morbi tristique " +
				"senectus et netus et malesuada\\r\\nfames ac turpis egestas. Sed quis tortor. Don" +
				"ec non ipsum. Mauris\\r\\ncondimentum, odio nec porta tristique, ante neque males" +
				"uada massa, in\\r\\ndignissim eros velit at tellus. Donec et risus in ligula elei" +
				"fend\\r\\nconsectetuer. Donec volutpat eleifend augue. Integer gravida sodales\\r" +
				"\\nleo. Nunc vehicula neque ac erat. Vivamus non nisl. Fusce ac magna.\\r\\nSuspe" +
				"ndisse euismod libero eget mauris.</a></p>\\r\\n<p><a href=\"#\" name=\"Lorem Ips" +
				"um 2\">Ut ligula. Maecenas consequat. Aliquam placerat. Cum sociis natoque\\r\\np" +
				"enatibus et magnis dis parturient montes, nascetur ridiculus mus.\\r\\nNulla conv" +
				"allis. Ut quis tortor. Vestibulum a lectus at diam fermentum\\r\\nvehicula. Mauri" +
				"s sed turpis a nisl ultricies facilisis. Fusce ornare,\\r\\nmi vitae hendrerit el" +
				"eifend, augue erat cursus nunc, a aliquam elit leo\\r\\nsed est. Donec eget sapie" +
				"n sit amet eros vehicula mollis. In\\r\\nsollicitudin libero in felis. Phasellus " +
				"metus sem, pulvinar in, porta\\r\\nnec, faucibus in, ipsum. Nam a tellus. Aliquam" +
				" erat volutpat.</a></p>\\r\\n<p><a href=\"#\" name=\"Lorem Ipsum 2\">Sed id velit" +
				" ut orci feugiat tempus. Pellentesque accumsan augue at\\r\\nlibero elementum ves" +
				"tibulum. Maecenas sit amet metus. Etiam molestie\\r\\nmassa sed erat. Aenean tinc" +
				"idunt. Mauris id eros. Quisque eu ante.\\r\\nFusce eu dolor. Aenean ultricies ant" +
				"e ut diam. Donec iaculis, pede eu\\r\\naliquet lobortis, wisi est dignissim diam," +
				" ut fringilla eros magna a\\r\\nmi. Nulla vel lorem. Donec placerat, lectus quis " +
				"molestie hendrerit,\\r\\nante tortor pharetra risus, ac rutrum arcu odio eu torto" +
				"r. In dapibus\\r\\nlacus nec ligula. Aenean vel metus. Nunc mattis lorem posuere " +
				"felis. In\\r\\nvehicula tempus lacus. Phasellus arcu. Nam ut arcu. Duis eget elit" +
				" id\\r\\neros adipiscing dignissim.</a></p>\\r\\n<a href=\"#\" name=\"Lorem Ipsum" +
				" 2\">                            \\r\\n\\r\\n                            \\r\\n\\r" +
				"\\n                                \\r\\n                                \\r\\n\\r" +
				"\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r\\n    \\r\\n    \\r\\n\\r\\n\\r\\n    \\r\\n    \\r" +
				"\\n    \\r\\n    \\r\\n                                        <br><font size=\"7\">Lo" +
				"rem Ipsum 3</font><br><br><br></a><p><a href=\"#\" name=\"Lorem Ipsum 2\">Lorem ip" +
				"sum dolor sit amet, consectetuer\\r\\nadipiscing elit. Aliquam fermentum vestibulu" +
				"m est. Cras rhoncus.\\r\\nPellentesque habitant morbi tristique senectus et netus " +
				"et malesuada\\r\\nfames ac turpis egestas. Sed quis tortor. Donec non ipsum. Mauri" +
				"s\\r\\ncondimentum, odio nec porta tristique, ante neque malesuada massa, in\\r\\n" +
				"dignissim eros velit at tellus. Donec et risus in ligula eleifend\\r\\nconsectetue" +
				"r. Donec volutpat eleifend augue. Integer gravida sodales\\r\\nleo. Nunc vehicula " +
				"neque ac erat. Vivamus non nisl. Fusce ac magna.\\r\\nSuspendisse euismod libero e" +
				"get mauris.</a></p>\\r\\n<p><a href=\"#\" name=\"Lorem Ipsum 2\">Ut ligula. Maecen" +
				"as consequat. Aliquam placerat. Cum sociis natoque\\r\\npenatibus et magnis dis pa" +
				"rturient montes, nascetur ridiculus mus.\\r\\nNulla convallis. Ut quis tortor. Ves" +
				"tibulum a lectus at diam fermentum\\r\\nvehicula. Mauris sed turpis a nisl ultrici" +
				"es facilisis. Fusce ornare,\\r\\nmi vitae hendrerit eleifend, augue erat cursus nu" +
				"nc, a aliquam elit leo\\r\\nsed est. Donec eget sapien sit amet eros vehicula moll" +
				"is. In\\r\\nsollicitudin libero in felis. Phasellus metus sem, pulvinar in, porta" +
				"\\r\\nnec, faucibus in, ipsum. Nam a tellus. Aliquam erat volutpat.</a></p>\\r\\n<" +
				"p><a href=\"#\" name=\"Lorem Ipsum 2\">Sed id velit ut orci feugiat tempus. Pellen" +
				"tesque accumsan augue at\\r\\nlibero elementum vestibulum. Maecenas sit amet metus" +
				". Etiam molestie\\r\\nmassa sed erat. Aenean tincidunt. Mauris id eros. Quisque eu" +
				" ante.\\r\\nFusce eu dolor. Aenean ultricies ante ut diam. Donec iaculis, pede eu" +
				"\\r\\naliquet lobortis, wisi est dignissim diam, ut fringilla eros magna a\\r\\nmi" +
				". Nulla vel lorem. Donec placerat, lectus quis molestie hendrerit,\\r\\nante torto" +
				"r pharetra risus, ac rutrum arcu odio eu tortor. In dapibus\\r\\nlacus nec ligula." +
				" Aenean vel metus. Nunc mattis lorem posuere felis. In\\r\\nvehicula tempus lacus." +
				" Phasellus arcu. Nam ut arcu. Duis eget elit id\\r\\neros adipiscing dignissim.</a" +
				"></p><p><a href=\"#\" name=\"Lorem Ipsum 2\"></p><p><a href=\"#\" name=\"Lorem Ips" +
				"um 2\"><br></a></p>\\r\\n<a href=\"#\" name=\"Lorem Ipsum 2\">                    " +
				"        \\r\\n\\r\\n                            \\r\\n\\r\\n                      " +
				"          \\r\\n                                \\r\\n\\r\\n\\r\\n\\r\\n\\r\\n\\r" +
				"\\n\\r\\n    \\r\\n    \\r\\n\\r\\n\\r\\n    \\r\\n    \\r\\n    \\r\\n    \\r\\n " +
				"                                       </a></div></div></div></div></div>\\r\\n<p>" +
				"\\r\\nTesting Basic Syntax Syntax</p><p><strong>Bold text here</strong></p><p><em>" +
				"Italics here</em></p><p><u>Underline</u></p><p><u><em><strong>All of the above</st" +
				"rong></em></u></p><p>Testing annoying characters in text</p><p>Here is a pipe | it" +
				"\'s an important delimiter in the raw sharepoint output.<br>\\r\\n</p></div></div>" +
				"";
		actual = tester.cleanAttributes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanDiv() {
		String input = "<html><div class=\"ExternalClass14E9F8AB519A470B995C8046769BE7FC\"><div " +
		"class=\"ExternalClass03BFEACBDD2F4A61A6DF16469ED01B1D\"><div " +
		"class=\"ExternalClass9831F41C1D6B425D9281846617F46F8B\">" +
		"<p>Testing annoying characters in text</p>" +
		"<p>Here is a pipe | it\'s an important delimiter in the raw sharepoint output.<br/>" +
		"\r\n" +
		"Testing <b>bold and <i>italics</i></b>" +
		"</p>" +
		"</div></div></div></html>";
		String expected = "<html>" +
		"<p>Testing annoying characters in text</p>" +
		"<p>Here is a pipe | it\'s an important delimiter in the raw " +
		"sharepoint output.<br/>" +
		"\n" + 
		"Testing <b>bold and <i>italics</i></b>" +
		"</p></html>";
		String actual = tester.cleanDiv(input);
		actual = tester.cleanJTidyExtras(actual); 
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCleanBreaks() {
		String input = "<br>";
		String expected = "<br/>";
		String actual = tester.cleanBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<br/>";
		expected = input;
		actual = tester.cleanBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Testing <br> Testing <br> Testing <br/>";
		expected = "Testing <br/> Testing <br/> Testing <br/>";
		actual = tester.cleanBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//break with other attributes
		input = "<br style=\"background-color:rgb(255, 192, 203)\">";
		expected = "<br/>";
		actual = tester.cleanBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testRemoveTagRecursively() {
		String input = 
			"<html>" +
			"<p>" +
				"<p class=\"test\">" +
					"<br/>" +
				"</p>" +
				"<span class=\"test2\">something</span>" +
				"<p/>" +
			"</p>" +
			"</html>";
		String expected = "<html>" +
				"<br/>" +
			"<span class=\"test2\">something</span>" +
			"</html>";
		Element rootElement = tester.getRootElement(input, false);
		String actual = tester.removeTagRecursively(rootElement, "p");
		actual = tester.cleanJTidyExtras(actual); 
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	
	public void testClean() {
		String input = "5;#vti_parserversion:SR|12.0.0.6219 ContentType:SW|Wiki Page ContentTypeId:" +
				"SW|0x010108008586D6569CB93B489CBCF8B01B47B952 vti_metatags:VR|CollaborationServer " +
				"SharePoint\\\\ Team\\\\ Web\\\\ Site vti_charset:SR|utf-8 vti_author:SR| " +
				"vti_setuppath:SX|DocumentTemplates\\\\wkpstd.aspx vti_cachedzones:VR|Bottom " +
				"vti_cachedneedsrewrite:BR|false WikiField:SW|<div class=ExternalClass14E9F8AB519A470B995C804" +
				"6769BE7FC><div class=ExternalClass03BFEACBDD2F4A61A6DF16469ED01B1D><div class=ExternalClass9" +
				"831F41C1D6B425D9281846617F46F8B>For Link Testing. We\'re going to link to this from " +
				"<a href=\"/my%20test%20wiki/Test%20Page%2042.aspx\">Test Page 42</a>. <br></div></div>" +
				"</div> vti_modifiedby:SR|PUBLIC36\\\\brendanpatterson vti_cachedhastheme:BR|false";
		String expected = "<html>" +
				"For Link Testing. We\'re going to link to this from " +
				"<a href=\"/my%20test%20wiki/Test%20Page%2042.aspx\" shape=\"rect\">Test " +
				"Page 42</a>.<br/></html>";
		String actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testCleanBigFile() { 
	String input, expected, actual;	
		//big file
		input = "4;#vti_parserversion:SR|12.0.0.6219 ContentType:SW|Wiki Page ContentTypeId:SW|0x010108008586D6569CB93B489CBCF8B01B47B952 vti_charset:SR|utf-8 vti_metatags:VR|CollaborationServer SharePoint\\\\ Team\\\\ Web\\\\ Site vti_author:SR| vti_setuppath:SX|DocumentTemplates\\\\wkpstd.aspx vti_cachedzones:VR|Bottom vti_cachedneedsrewrite:BR|false WikiField:SW|<div class=ExternalClass13F61637E5854FA485ACD9F1A04C8657>\n" + 
				"<div class=ExternalClass090DE927E8E447E79574456886B6B63A>\n" + 
				"<div class=ExternalClass491B02EFDD94443EAD8FEB8F9959360D>\n" + 
				"<div class=ExternalClassA90FCB28191447798E54AF9630A387C7>\n" + 
				"<div class=ExternalClass092801140FA14EFE800C1C8409FE582F>\n" + 
				"<div class=ExternalClass734A488FC71F4D5FB6A3D3FA9B4A7009>\n" + 
				"<div class=ExternalClassF6BBA484EE374A5DA714B173D61AAEDB>\n" + 
				"<div class=ExternalClass6326FBE3DC0E46818A48A81057351C01>\n" + 
				"<div class=ExternalClassD6E735C670CE45ADBE6372ED96807CF6>\n" + 
				"<div class=ExternalClassEC1AD7361D6D4FED8EDB2C7A447879FD>\n" + 
				"<div class=ExternalClass4EB764AAC83A4CF38A18128299D552AC>\n" + 
				"Testing 123<br>\n" + 
				"<br>\n" + 
				"<font size=5>\n" + 
				"Links<br>\n" + 
				"<font size=2>\n" + 
				"<a href=\"http://www.google.com\" title=\"Search Engine Extraordinaire\">\n" + 
				"Google</a>\n" + 
				"<br>\n" + 
				"<a href=\"#Lorem Ipsum 2\">\n" + 
				"Lorem Ipsum 2 in page</a>\n" + 
				"<br>\n" + 
				"How are you supposed to link internally? Link UI implies http link?<br>\n" + 
				"<a href=\"/my%20test%20wiki/Test%20Page%2072.aspx\">\n" + 
				"Test Page 72</a>\n" + 
				"<br>\n" + 
				"<a href=\"/my%20test%20wiki/Sharepoint%20Converter%20Links.aspx\">\n" + 
				"Sharepoint Converter Links</a>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<font size=7>\n" + 
				"Lorem Ipsum 1</font>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"</font>\n" + 
				"</font>\n" + 
				"<p>\n" + 
				"Lorem ipsum dolor sit amet, consectetuer\r\nadipiscing elit. Aliquam fermentum vestibulum est. Cras rhoncus.\r\nPellentesque habitant morbi tristique senectus et netus et malesuada\r\nfames ac turpis egestas. Sed quis tortor. Donec non ipsum. Mauris\r\ncondimentum, odio nec porta tristique, ante neque malesuada massa, in\r\ndignissim eros velit at tellus. Donec et risus in ligula eleifend\r\nconsectetuer. Donec volutpat eleifend augue. Integer gravida sodales\r\nleo. Nunc vehicula neque ac erat. Vivamus non nisl. Fusce ac magna.\r\nSuspendisse euismod libero eget mauris.</p>\n" + 
				"\r\n<p>\n" + 
				"Ut ligula. Maecenas consequat. Aliquam placerat. Cum sociis natoque\r\npenatibus et magnis dis parturient montes, nascetur ridiculus mus.\r\nNulla convallis. Ut quis tortor. Vestibulum a lectus at diam fermentum\r\nvehicula. Mauris sed turpis a nisl ultricies facilisis. Fusce ornare,\r\nmi vitae hendrerit eleifend, augue erat cursus nunc, a aliquam elit leo\r\nsed est. Donec eget sapien sit amet eros vehicula mollis. In\r\nsollicitudin libero in felis. Phasellus metus sem, pulvinar in, porta\r\nnec, faucibus in, ipsum. Nam a tellus. Aliquam erat volutpat.</p>\n" + 
				"\r\n<p>\n" + 
				"Sed id velit ut orci feugiat tempus. Pellentesque accumsan augue at\r\nlibero elementum vestibulum. Maecenas sit amet metus. Etiam molestie\r\nmassa sed erat. Aenean tincidunt. Mauris id eros. Quisque eu ante.\r\nFusce eu dolor. Aenean ultricies ante ut diam. Donec iaculis, pede eu\r\naliquet lobortis, wisi est dignissim diam, ut fringilla eros magna a\r\nmi. Nulla vel lorem. Donec placerat, lectus quis molestie hendrerit,\r\nante tortor pharetra risus, ac rutrum arcu odio eu tortor. In dapibus\r\nlacus nec ligula. Aenean vel metus. Nunc mattis lorem posuere felis. In\r\nvehicula tempus lacus. Phasellus arcu. Nam ut arcu. Duis eget elit id\r\neros adipiscing dignissim.</p>\n" + 
				"\r\n                            \r\n\r\n                            \r\n\r\n                                \r\n                                \r\n\r\n\r\n\r\n\r\n\r\n\r\n    \r\n    \r\n\r\n\r\n    \r\n    \r\n    \r\n    \r\n                                        <br>\n" + 
				"<font size=5>\n" + 
				"<font size=2>\n" + 
				"<br>\n" + 
				"<a href=\"#\" name=\"Lorem Ipsum 2\">\n" + 
				"(anchor here)<br>\n" + 
				"<br>\n" + 
				"</font>\n" + 
				"</font>\n" + 
				"<font size=5>\n" + 
				"<font size=2>\n" + 
				"<font size=7>\n" + 
				"Lorem Ipsum 2</font>\n" + 
				"<br>\n" + 
				"</font>\n" + 
				"</font>\n" + 
				"<p>\n" + 
				"Lorem ipsum dolor sit amet, consectetuer\r\nadipiscing elit. Aliquam fermentum vestibulum est. Cras rhoncus.\r\nPellentesque habitant morbi tristique senectus et netus et malesuada\r\nfames ac turpis egestas. Sed quis tortor. Donec non ipsum. Mauris\r\ncondimentum, odio nec porta tristique, ante neque malesuada massa, in\r\ndignissim eros velit at tellus. Donec et risus in ligula eleifend\r\nconsectetuer. Donec volutpat eleifend augue. Integer gravida sodales\r\nleo. Nunc vehicula neque ac erat. Vivamus non nisl. Fusce ac magna.\r\nSuspendisse euismod libero eget mauris.</p>\n" + 
				"\r\n<p>\n" + 
				"Ut ligula. Maecenas consequat. Aliquam placerat. Cum sociis natoque\r\npenatibus et magnis dis parturient montes, nascetur ridiculus mus.\r\nNulla convallis. Ut quis tortor. Vestibulum a lectus at diam fermentum\r\nvehicula. Mauris sed turpis a nisl ultricies facilisis. Fusce ornare,\r\nmi vitae hendrerit eleifend, augue erat cursus nunc, a aliquam elit leo\r\nsed est. Donec eget sapien sit amet eros vehicula mollis. In\r\nsollicitudin libero in felis. Phasellus metus sem, pulvinar in, porta\r\nnec, faucibus in, ipsum. Nam a tellus. Aliquam erat volutpat.</p>\n" + 
				"\r\n<p>\n" + 
				"Sed id velit ut orci feugiat tempus. Pellentesque accumsan augue at\r\nlibero elementum vestibulum. Maecenas sit amet metus. Etiam molestie\r\nmassa sed erat. Aenean tincidunt. Mauris id eros. Quisque eu ante.\r\nFusce eu dolor. Aenean ultricies ante ut diam. Donec iaculis, pede eu\r\naliquet lobortis, wisi est dignissim diam, ut fringilla eros magna a\r\nmi. Nulla vel lorem. Donec placerat, lectus quis molestie hendrerit,\r\nante tortor pharetra risus, ac rutrum arcu odio eu tortor. In dapibus\r\nlacus nec ligula. Aenean vel metus. Nunc mattis lorem posuere felis. In\r\nvehicula tempus lacus. Phasellus arcu. Nam ut arcu. Duis eget elit id\r\neros adipiscing dignissim.</p>\n" + 
				"\r\n\r\n                            \r\n\r\n                                \r\n                                \r\n\r\n\r\n\r\n\r\n\r\n\r\n    \r\n    \r\n\r\n\r\n    \r\n    \r\n    \r\n    \r\n                                        <br>\n" + 
				"<font size=7>\n" + 
				"Lorem Ipsum 3</font>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<br>\n" + 
				"<p>\n" + 
				"Lorem ipsum dolor sit amet, consectetuer\r\nadipiscing elit. Aliquam fermentum vestibulum est. Cras rhoncus.\r\nPellentesque habitant morbi tristique senectus et netus et malesuada\r\nfames ac turpis egestas. Sed quis tortor. Donec non ipsum. Mauris\r\ncondimentum, odio nec porta tristique, ante neque malesuada massa, in\r\ndignissim eros velit at tellus. Donec et risus in ligula eleifend\r\nconsectetuer. Donec volutpat eleifend augue. Integer gravida sodales\r\nleo. Nunc vehicula neque ac erat. Vivamus non nisl. Fusce ac magna.\r\nSuspendisse euismod libero eget mauris.</p>\n" + 
				"\r\n<p>\n" + 
				"Ut ligula. Maecenas consequat. Aliquam placerat. Cum sociis natoque\r\npenatibus et magnis dis parturient montes, nascetur ridiculus mus.\r\nNulla convallis. Ut quis tortor. Vestibulum a lectus at diam fermentum\r\nvehicula. Mauris sed turpis a nisl ultricies facilisis. Fusce ornare,\r\nmi vitae hendrerit eleifend, augue erat cursus nunc, a aliquam elit leo\r\nsed est. Donec eget sapien sit amet eros vehicula mollis. In\r\nsollicitudin libero in felis. Phasellus metus sem, pulvinar in, porta\r\nnec, faucibus in, ipsum. Nam a tellus. Aliquam erat volutpat.</p>\n" + 
				"\r\n<p>\n" + 
				"Sed id velit ut orci feugiat tempus. Pellentesque accumsan augue at\r\nlibero elementum vestibulum. Maecenas sit amet metus. Etiam molestie\r\nmassa sed erat. Aenean tincidunt. Mauris id eros. Quisque eu ante.\r\nFusce eu dolor. Aenean ultricies ante ut diam. Donec iaculis, pede eu\r\naliquet lobortis, wisi est dignissim diam, ut fringilla eros magna a\r\nmi. Nulla vel lorem. Donec placerat, lectus quis molestie hendrerit,\r\nante tortor pharetra risus, ac rutrum arcu odio eu tortor. In dapibus\r\nlacus nec ligula. Aenean vel metus. Nunc mattis lorem posuere felis. In\r\nvehicula tempus lacus. Phasellus arcu. Nam ut arcu. Duis eget elit id\r\neros adipiscing dignissim.</p>\n" + 
				"<p>\n" + 
				"<p>\n" + 
				"<br>\n" + 
				"</p>\n" + 
				"\r\n\r\n                            \r\n\r\n                                \r\n                                \r\n\r\n\r\n\r\n\r\n\r\n\r\n    \r\n    \r\n\r\n\r\n    \r\n    \r\n    \r\n    \r\n                                        </div>" + 
				"</div>" + 
				"</div>" + 
				"</div>" + 
				"</div>" + 
				"\r\n<p>\n" + 
				"\r\nTesting Basic Syntax Syntax</p>\n" + 
				"<p>\n" + 
				"<strong>\n" + 
				"Bold text here</strong>\n" + 
				"</p>\n" + 
				"<p>\n" + 
				"<em>\n" + 
				"Italics here</em>\n" + 
				"</p>\n" + 
				"<p>\n" + 
				"<u>\n" + 
				"Underline</u>\n" + 
				"</p>\n" + 
				"<p>\n" + 
				"<u>\n" + 
				"<em>\n" + 
				"<strong>\n" + 
				"All of the above</strong>\n" + 
				"</em>\n" + 
				"</u>\n" + 
				"</p>\n" + 
				"<p>\n" + 
				"Testing annoying characters in text</p>\n" + 
				"<p>\n" + 
				"Here is a pipe | it\'s an important delimiter in the raw sharepoint output.<br>\n" + 
				"\r\n</p>\n" + 
				"</div>" + 
				"</div>" + 
				"</div>" + 
				"</div>" + 
				"</div>" + 
				"</div>" + 
				" vti_modifiedby:SR|PUBLIC36\\\\brendanpatterson vti_cachedhastheme:BR|false \n";
		expected = "<html>Testing 123<br/><br/><font size=\"5\">Links<br/><font size=" +
				"\"2\"><a href=\"http://www.google.com\" title=\"Search Engine Extraor" +
				"dinaire\" shape=\"rect\">Google</a><br/><a href=\"#Lorem Ipsum 2\" sh" +
				"ape=\"rect\">Lorem Ipsum 2 in page</a><br/>How are you supposed to li" +
				"nk internally? Link UI implies http link?<br/><a href=\"/my%20test%20w" +
				"iki/Test%20Page%2072.aspx\" shape=\"rect\">Test Page 72</a><br/><a hre" +
				"f=\"/my%20test%20wiki/Sharepoint%20Converter%20Links.aspx\" shape=\"re" +
				"ct\">Sharepoint Converter Links</a><br/><br/><font size=\"7\">Lorem Ip" +
				"sum 1</font><br/><br/></font></font><p>Lorem ipsum dolor sit amet, cons" +
				"ectetuer adipiscing elit. Aliquam fermentum vestibulum est. Cras rhonc" +
				"us. Pellentesque habitant morbi tristique senectus et netus et malesua" +
				"da fames ac turpis egestas. Sed quis tortor. Donec non ipsum. Mauris co" +
				"ndimentum, odio nec porta tristique, ante neque malesuada massa, in di" +
				"gnissim eros velit at tellus. Donec et risus in ligula eleifend consec" +
				"tetuer. Donec volutpat eleifend augue. Integer gravida sodales leo. Nu" +
				"nc vehicula neque ac erat. Vivamus non nisl. Fusce ac magna. Suspendis" +
				"se euismod libero eget mauris.</p><p>Ut ligula. Maecenas consequat. Al" +
				"iquam placerat. Cum sociis natoque penatibus et magnis dis parturient " +
				"montes, nascetur ridiculus mus. Nulla convallis. Ut quis tortor. Vesti" +
				"bulum a lectus at diam fermentum vehicula. Mauris sed turpis a nisl ul" +
				"tricies facilisis. Fusce ornare, mi vitae hendrerit eleifend, augue er" +
				"at cursus nunc, a aliquam elit leo sed est. Donec eget sapien sit amet" +
				" eros vehicula mollis. In sollicitudin libero in felis. Phasellus metu" +
				"s sem, pulvinar in, porta nec, faucibus in, ipsum. Nam a tellus. Aliqu" +
				"am erat volutpat.</p><p>Sed id velit ut orci feugiat tempus. Pellentes" +
				"que accumsan augue at libero elementum vestibulum. Maecenas sit amet m" +
				"etus. Etiam molestie massa sed erat. Aenean tincidunt. Mauris id eros." +
				" Quisque eu ante. Fusce eu dolor. Aenean ultricies ante ut diam. Donec" +
				" iaculis, pede eu aliquet lobortis, wisi est dignissim diam, ut fringi" +
				"lla eros magna a mi. Nulla vel lorem. Donec placerat, lectus quis mole" +
				"stie hendrerit, ante tortor pharetra risus, ac rutrum arcu odio eu tor" +
				"tor. In dapibus lacus nec ligula. Aenean vel metus. Nunc mattis lorem " +
				"posuere felis. In vehicula tempus lacus. Phasellus arcu. Nam ut arcu. " +
				"Duis eget elit id eros adipiscing dignissim.</p><br/><font size=\"5\">" +
				"<font size=\"2\"><br/><a id=\"Lorem Ipsum 2\" href=\"#\" name=\"Lorem " +
				"Ipsum 2\" shape=\"rect\"/>(anchor here)<br/><br/></font></font><font size=\"5\"><font" +
				" size=\"2\"><font size=\"7\">Lorem Ipsum 2</font><br/></font></font>" +
				"<p>Lorem ipsum dolor sit amet, consectetuer adipiscing" +
				" elit. Aliquam fermentum vestibulum est. Cras rhoncus. Pellentesque ha" +
				"bitant morbi tristique senectus et netus et malesuada fames ac turpis " +
				"egestas. Sed quis tortor. Donec non ipsum. Mauris condimentum, odio ne" +
				"c porta tristique, ante neque malesuada massa, in dignissim eros velit" +
				" at tellus. Donec et risus in ligula eleifend consectetuer. Donec volu" +
				"tpat eleifend augue. Integer gravida sodales leo. Nunc vehicula neque " +
				"ac erat. Vivamus non nisl. Fusce ac magna. Suspendisse euismod libero " +
				"eget mauris.</p><p>Ut ligula. Maecenas consequat. Aliquam placerat. Cu" +
				"m sociis natoque penatibus et magnis dis parturient montes, nascetur r" +
				"idiculus mus. Nulla convallis. Ut quis tortor. Vestibulum a lectus at " +
				"diam fermentum vehicula. Mauris sed turpis a nisl ultricies facilisis." +
				" Fusce ornare, mi vitae hendrerit eleifend, augue erat cursus nunc, a " +
				"aliquam elit leo sed est. Donec eget sapien sit amet eros vehicula mol" +
				"lis. In sollicitudin libero in felis. Phasellus metus sem, pulvinar in" +
				", porta nec, faucibus in, ipsum. Nam a tellus. Aliquam erat volutpat.<" +
				"/p><p>Sed id velit ut orci feugiat tempus. Pellentesque accumsan augue" +
				" at libero elementum vestibulum. Maecenas sit amet metus. Etiam molest" +
				"ie massa sed erat. Aenean tincidunt. Mauris id eros. Quisque eu ante. " +
				"Fusce eu dolor. Aenean ultricies ante ut diam. Donec iaculis, pede eu " +
				"aliquet lobortis, wisi est dignissim diam, ut fringilla eros magna a m" +
				"i. Nulla vel lorem. Donec placerat, lectus quis molestie hendrerit, an" +
				"te tortor pharetra risus, ac rutrum arcu odio eu tortor. In dapibus la" +
				"cus nec ligula. Aenean vel metus. Nunc mattis lorem posuere felis. In " +
				"vehicula tempus lacus. Phasellus arcu. Nam ut arcu. Duis eget elit id " +
				"eros adipiscing dignissim.</p><br/><font size=\"7\">Lorem Ipsum 3</fon" +
				"t><br/><br/><br/><p>Lorem ipsum dolor sit amet, consectetuer adipiscin" +
				"g elit. Aliquam fermentum vestibulum est. Cras rhoncus. Pellentesque h" +
				"abitant morbi tristique senectus et netus et malesuada fames ac turpis" +
				" egestas. Sed quis tortor. Donec non ipsum. Mauris condimentum, odio n" +
				"ec porta tristique, ante neque malesuada massa, in dignissim eros veli" +
				"t at tellus. Donec et risus in ligula eleifend consectetuer. Donec vol" +
				"utpat eleifend augue. Integer gravida sodales leo. Nunc vehicula neque" +
				" ac erat. Vivamus non nisl. Fusce ac magna. Suspendisse euismod libero" +
				" eget mauris.</p><p>Ut ligula. Maecenas consequat. Aliquam placerat. C" +
				"um sociis natoque penatibus et magnis dis parturient montes, nascetur " +
				"ridiculus mus. Nulla convallis. Ut quis tortor. Vestibulum a lectus at" +
				" diam fermentum vehicula. Mauris sed turpis a nisl ultricies facilisis" +
				". Fusce ornare, mi vitae hendrerit eleifend, augue erat cursus nunc, a" +
				" aliquam elit leo sed est. Donec eget sapien sit amet eros vehicula mo" +
				"llis. In sollicitudin libero in felis. Phasellus metus sem, pulvinar i" +
				"n, porta nec, faucibus in, ipsum. Nam a tellus. Aliquam erat volutpat." +
				"</p><p>Sed id velit ut orci feugiat tempus. Pellentesque accumsan augu" +
				"e at libero elementum vestibulum. Maecenas sit amet metus. Etiam moles" +
				"tie massa sed erat. Aenean tincidunt. Mauris id eros. Quisque eu ante." +
				" Fusce eu dolor. Aenean ultricies ante ut diam. Donec iaculis, pede eu" +
				" aliquet lobortis, wisi est dignissim diam, ut fringilla eros magna a " +
				"mi. Nulla vel lorem. Donec placerat, lectus quis molestie hendrerit, a" +
				"nte tortor pharetra risus, ac rutrum arcu odio eu tortor. In dapibus l" +
				"acus nec ligula. Aenean vel metus. Nunc mattis lorem posuere felis. In" +
				" vehicula tempus lacus. Phasellus arcu. Nam ut arcu. Duis eget elit id" +
				" eros adipiscing dignissim.</p><p><br/></p><p>Testing Basic Syntax Syn" +
				"tax</p><p><strong>Bold text here</strong></p><p><em>Italics here</em><" +
				"/p><p><u>Underline</u></p><p><u><em><strong>All of the above</strong><" +
				"/em></u></p><p>Testing annoying characters in text</p><p>Here is a pip" +
				"e | it\'s an important delimiter in the raw sharepoint output.<br/></p" +
				"></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "4;#vti_parserversion:SR|12.0.0.6219 ContentType:SW|Wiki Page ContentTypeId:SW|0x01010800099E6FC4BD66DF47B01DB6E7E4EB0380 vti_metatags:VR|CollaborationServer SharePoint\\\\ Team\\\\ Web\\\\ Site vti_charset:SR|utf-8 vti_author:SR| vti_setuppath:SX|DocumentTemplates\\\\wkpstd.aspx vti_cachedzones:VR|Bottom vti_cachedneedsrewrite:BR|false WikiField:SW|<div class=ExternalClass4E22CA4B541748E8A8422B9B398F47ED><div class=ExternalClass61FD28F250B2449482FDE7C110E0F0C6><strong><font size=4>Unorderedlist</font></strong><br><ul><li>abc</li><li>def</li><ul><li>ghi</li><li>hij</li></ul><li>klm</li><ul><li>nop</li><ul><li>qrs</li></ul><li>tuv</li></ul></ul><br><strong><font size=4>Orderedlist</font></strong><br>\\r\\n<ol><li>abc</li><li>def</li><ol><li>ghi</li><li>hij</li></ol><li>klm</li><ol><li>nop</li><ol><li>qrs</li></ol><li>tuv</li></ol></ol>\\r\\n<strong><font size=4>Both list</font></strong><br>\\r\\n<ul><li>abc</li><li>def</li><ol><li>ghi</li></ol><ol><li>hij</li></ol></ul><ol><li>klm</li><ol><li>nop</li></ol><ol><ol><li>qrs</li></ol></ol></ol><ul><ul><li>tuv</li></ul></ul><font size=4><strong><br></strong></font><strong><font size=4>Other Syntax</font></strong><br><ul><li>abc</li><li>def</li><ul><li><strong>ghi</strong></li><li><em>hij</em></li></ul><li>klm</li><ul><li><font color=\"#ff0000\">nop</font></li><ul><li><u>qrs</u></li></ul><li>tuv</li></ul></ul><strong><font size=4>With Font Size</font></strong><br><ul><li><font size=4>abc</font></li><li><font size=4>def</font></li><ul><li><font size=4>ghi</font></li><li><font size=4>hij</font></li></ul><li><font size=4>klm</font></li><ul><li><font size=4>nop</font></li><ul><li><font size=4>qrs</font></li></ul><li><font size=4>tuv</font></li></ul></ul></div></div> vti_modifiedby:SR|PUBLIC36\\\\laura.kolker vti_cachedhastheme:BR|false";
		expected = "<html>" +
				"<strong><font size=\"4\">Unorderedlist</font>" +
				"</strong><br/><ul><li>abc</li><li>def</li><li/>" +
				"<li style=\"list-style: none\"><ul><li>ghi</li><li>hij</li></ul>" +
				"</li><li>klm</li><li/><li style=\"list-style: none\"><ul><li>nop</li>" +
				"<li/><li style=\"list-style: none\"><ul><li>qrs</li></ul></li>" +
				"<li>tuv</li></ul></li></ul><br/><strong><font size=\"4\">Orderedlist</font></strong><br/><ol><li>abc</li><li>def</li><li/><li style=\"list-style: none\"><ol><li>ghi</li><li>hij</li></ol></li><li>klm</li><li/><li style=\"list-style: none\"><ol><li>nop</li><li/><li style=\"list-style: none\"><ol><li>qrs</li></ol></li><li>tuv</li></ol></li></ol><strong><font size=\"4\">Both list</font></strong><br/><ul><li>abc</li><li>def</li><li/><li style=\"list-style: none\"><ol><li>ghi</li></ol><ol><li>hij</li></ol></li></ul><ol><li>klm</li><li/><li style=\"list-style: none\"><ol><li>nop</li></ol><ol><li/><li style=\"list-style: none\"><ol><li>qrs</li></ol></li></ol></li></ol><ul><li/><li style=\"list-style: none\"><ul><li>tuv</li></ul></li></ul><font size=\"4\"><strong><br/></strong></font><strong><font size=\"4\">Other Syntax</font></strong><br/><ul><li>abc</li><li>def</li><li/><li style=\"list-style: none\"><ul><li><strong>ghi</strong></li><li><em>hij</em></li></ul></li><li>klm</li><li/><li style=\"list-style: none\"><ul><li><font color=\"#ff0000\">nop</font></li><li/><li style=\"list-style: none\"><ul><li><u>qrs</u></li></ul></li><li>tuv</li></ul></li></ul><strong><font size=\"4\">With Font Size</font></strong><br/><ul><li><font size=\"4\">abc</font></li><li><font size=\"4\">def</font></li><li/><li style=\"list-style: none\"><ul><li><font size=\"4\">ghi</font></li><li><font size=\"4\">hij</font></li></ul></li><li><font size=\"4\">klm</font></li><li/><li style=\"list-style: none\"><ul><li><font size=\"4\">nop</font></li><li/><li style=\"list-style: none\"><ul><li><font size=\"4\">qrs</font></li></ul></li><li><font size=\"4\">tuv</font></li></ul></li></ul></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanAnchors() {
		String input = "<a href=\"#\" name=\"Lorem Ipsum 2\">";
		String expected = "<a href=\"#\" name=\"Lorem Ipsum 2\"/>";
		String actual = tester.cleanAnchors(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<a href=\"#\" name=\"Lorem Ipsum 2\">Testing</a>";
		expected = input;
		actual = tester.cleanAnchors(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<a href=\"#\" name=\"Lorem Ipsum 2\"><font size=\"5\">Testing</font></a>";
		expected = input;
		actual = tester.cleanAnchors(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//one bad one good
		input = "<a href=\"#\" name=\"Lorem Ipsum 2\">" + //bad - no closing
				"<font size=\"5\">Testing</font>" +
				"<a href=\"#\" name=\"something\"/>"; //good - has closing /
		expected = "<a href=\"#\" name=\"Lorem Ipsum 2\"/>" + //add closing /
				"<font size=\"5\">Testing</font>" +
				"<a href=\"#\" name=\"something\"/>"; 
		actual = tester.cleanAnchors(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//two bad
		input = "<a href=\"#\" name=\"Lorem Ipsum 2\">" + //bad - no closing
				"<font size=\"5\">Testing</font>" +
				"<a href=\"#\" name=\"something\">"; 
		expected = "<a href=\"#\" name=\"Lorem Ipsum 2\"/>" + //add closing /
				"<font size=\"5\">Testing</font>" +
				"<a href=\"#\" name=\"something\"/>"; 
		actual = tester.cleanAnchors(input);
		assertNotNull(actual);
		assertEquals(expected, actual);


	}
	
	public void testCleanParaTags() {
		String input = "<p><p>"; 
		String expected = "<p/><p>";
		String actual = tester.cleanParaTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<p>\r\n<p>";
		expected = "<p/>\r\n<p>";
		actual = tester.cleanParaTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanWS() {
		String input = "<p>\n\n\n\n\n\n\n\n\n\n</p>";
		String expected = "<p></p>";
		String actual = tester.cleanWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<p>\r\n\r\n\r\n\r\n\r\n\r\n</p>";
		actual = tester.cleanWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<p>\r\n</p>";
		actual = tester.cleanWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<p>\r\n\r\n                            \r\n</p>";
		actual = tester.cleanWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<p>\\r\\n\\r\\n                            \\r\\n</p>";
		actual = tester.cleanWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><p>Testing\n" + 
		"</p></html>";
		expected = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><p>Testing" + 
		"</p></html>";
		actual = tester.cleanWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title/></head><body><div class=\"ExternalClass14E9F8AB519A470B995C8046769BE7FC\">\n" + 
				"<div class=\"ExternalClass03BFEACBDD2F4A61A6DF16469ED01B1D\">\n" + 
				"<div class=\"ExternalClass9831F41C1D6B425D9281846617F46F8B\">For Link\n" + 
				"Testing. We\'re going to link to this from <a href=\"/my%20test%20wiki/Test%20Page%2042.aspx\" shape=\"rect\">Test Page\n" + 
				"42</a>.<br/>\n" + 
				"</div>\n" + 
				"</div>\n" + 
				"</div></body></html>";
		expected = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title/></head><body><div class=\"ExternalClass14E9F8AB519A470B995C8046769BE7FC\">" + 
				"<div class=\"ExternalClass03BFEACBDD2F4A61A6DF16469ED01B1D\">" + 
				"<div class=\"ExternalClass9831F41C1D6B425D9281846617F46F8B\">For Link" +
				" " + 
				"Testing. We\'re going to link to this from <a href=\"/my%20test%20wiki/Test%20Page%2042.aspx\" shape=\"rect\">Test Page" +
				" " + 
				"42</a>.<br/>" + 
				"</div>" + 
				"</div>" + 
				"</div></body></html>";
		actual = tester.cleanWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testWithJTidy() {
		String input = "<div class=\"ms-wikicontent\"><div class=ExternalClass6401589625F24EB681A45DBE6B1A1B3E>\n" + 
				"<h3 class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B><font face=Arial>xxxxxx</font></h3>\n" + 
				"<p class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B><strong><em><font face=Arial>xxxxxx</font></em></strong></p><font size=3>\n" + 
				"<p class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B><span style=\"color:black\"><font face=Arial size=2>xxxxxx<a href=\"xxxxxx\">xxxxxx</a>Êxxxxxx</font></span></p>\n" + 
				"<p class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B><span style=\"color:black\"><font face=Arial size=2>xxxxxx &quot;xxxxxx&quot; xxxxxx </font></span></p>\n" + 
				"<p class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B><font face=Arial></font></font></p>\n" + 
				"<p class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B><font face=Arial><strong><em>xxxxxx<br></em></strong>xxxxxx</font></p>\n" + 
				"<p class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B><font face=Arial><strong><em>xxxxxx<br></em></strong>xxxxxx</font><a href=\"xxxxxx\"><font face=Arial>xxxxxx</font></a></p>\n" + 
				"<h3 class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B><font face=Arial>xxxxxx</font></h3>\n" + 
				"<p class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<div class=ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B>\n" + 
				"<ul type=disc>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</a>Ê</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li></ul></div>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial><em><b><span style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em><span style=\"color:black\"></span></font></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><em><b><span style=\"font-size:8.5pt;font-family:Verdana\"><font face=Arial>xxxxxx</font></span></b></em></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial><em><b><span style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em><span style=\"color:black\"></span></font></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><em><b><span style=\"font-size:8.5pt;font-family:Verdana\"><font face=Arial>xxxxxx</font></span></b></em></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial><em><b><span style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em><span style=\"color:black\"></span></font></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial><em><b><span style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em><span style=\"color:black\"></span></font></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial><em><b><span style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx (<a class=ms-wikilink href=\"xxxxxx\">xxxxxx</a>)</span></b></em><span style=\"color:black\"></span></font></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial><em><b><span style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em><span style=\"color:black\"></span></font></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial><em><b><span style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em><span style=\"color:black\"></span></font></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p><span style=\"color:black\">\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial><em><b><span style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx &amp; xxxxxx</span></b></em></font></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx <a class=ms-missinglink href=\"xxxxxx\" title=\"xxxxxx\">xxxxxx</a>, <a class=ms-missinglink href=\"xxxxxx\" title=\"xxxxxx\">xxxxxx</a>xxxxxx<a href=\"xxxxxx\">xxxxxx</a> (xxxxxx) and <a href=\"xxxxxx\">xxxxxx</a>. xxxxxx <a href=\"xxxxxx\">xxxxxx</a>. </font></span></p></span>\n" + 
				"<h3 class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial>xxxxxx</font></h3>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><em><b><span style=\"font-size:8.5pt;font-family:Verdana\"><font face=Arial>xxxxxx</font></span></b></em></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial><em><b><span style=\"font-size:8.5pt;color:black;font-family:Verdana\">xxxxxx</span></b></em><span style=\"font-size:8.5pt;color:black;font-family:Verdana\"></span></font></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><em><b><span style=\"font-size:8.5pt;color:black;font-family:Verdana\"><font face=Arial>xxxxxx</font></span></b></em></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><em><b><span style=\"font-size:8.5pt;font-family:Verdana\"><font face=Arial>xxxxxx</font></span></b></em></p>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial><font size=2><span style=\"color:black\">xxxxxx</span><span style=\"color:black\">xxxxxx</span><span style=\"color:black\">xxxxxx<a href=\"xxxxxx\">xxxxxx</a>. xxxxxx:</span></font></font></p><font size=3><span style=\"color:black\"><span style=\"color:black\">\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"<div class=ExternalClass392E9BBB57E64482B84B1A425C8AE164 style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\"><font face=Arial><font size=2><span style=\"color:black\">xxxxxx <a class=ms-missinglink href=\"xxxxxx\" title=\"xxxxxx\">xxxxxx</a> xxxxxx</span></font></font></div></li>\n" + 
				"<li>\n" + 
				"<div class=ExternalClass392E9BBB57E64482B84B1A425C8AE164 style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\"><font face=Arial><font size=2><span style=\"color:black\">xxxxxx</span></font></font></div></li>\n" + 
				"<li>\n" + 
				"<div class=ExternalClass392E9BBB57E64482B84B1A425C8AE164 style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\"><font face=Arial><font size=2><span style=\"color:black\">xxxxxx</span></font></font></div></li>\n" + 
				"<li>\n" + 
				"<div class=ExternalClass392E9BBB57E64482B84B1A425C8AE164 style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\"><font face=Arial><font size=2><span style=\"color:black\">xxxxxx</span></font></font></div></li>\n" + 
				"<li>\n" + 
				"<div class=ExternalClass392E9BBB57E64482B84B1A425C8AE164 style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\"><font face=Arial><font size=2><span style=\"color:black\">xxxxxx</span></font></font></div></li>\n" + 
				"<li>\n" + 
				"<div class=ExternalClass392E9BBB57E64482B84B1A425C8AE164 style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\"><font face=Arial><font size=2><span style=\"color:black\">xxxxxx</span></font></font></div></li></ul>\n" + 
				"<p class=ExternalClass392E9BBB57E64482B84B1A425C8AE164><font face=Arial></font></span></span></font><font size=3><span style=\"color:black\"><span style=\"color:black\"><span style=\"color:black\"><span style=\"color:black\"><span style=\"color:black\"></p>\n" + 
				"<div class=ExternalClass392E9BBB57E64482B84B1A425C8AE164>\n" + 
				"<ul><font face=Arial></font></span></span></span></span></span></font></ul></div>\n" + 
				"<p class=ExternalClass338C1C537FBB4A40966BEC3D88CE46B8><font face=Arial></font></p>\n" + 
				"<h3 class=ExternalClass338C1C537FBB4A40966BEC3D88CE46B8><font face=Arial>xxxxxx</font></h3>\n" + 
				"<p class=ExternalClass338C1C537FBB4A40966BEC3D88CE46B8><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p><span style=\"color:black\"></span>\n" + 
				"<p class=ExternalClass338C1C537FBB4A40966BEC3D88CE46B8 align=center><span style=\"color:black;font-family:Verdana\"><img alt=\"xxxxxx\" src=\"xxxxxx\"></span></p>\n" + 
				"<h3 class=ExternalClass338C1C537FBB4A40966BEC3D88CE46B8><font face=Arial>xxxxxx</font></h3>\n" + 
				"<p class=ExternalClass338C1C537FBB4A40966BEC3D88CE46B8><span style=\"color:black;font-family:\'Times New Roman\'\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p class=ExternalClass338C1C537FBB4A40966BEC3D88CE46B8 align=center><span style=\"color:black;font-family:\'Times New Roman\'\"><img alt=\"xxxxxx\" src=\"xxxxxx\"></span></p>\n" + 
				"<h3 class=ExternalClass338C1C537FBB4A40966BEC3D88CE46B8><font face=Arial>xxxxxx</font></h3>\n" + 
				"<p class=ExternalClass338C1C537FBB4A40966BEC3D88CE46B8><i><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></i></p>\n" + 
				"<div class=ExternalClass338C1C537FBB4A40966BEC3D88CE46B8>\n" + 
				"<ul>\n" + 
				"<li><i><span style=\"color:black\"></span></i><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></li></ul></div>\n" + 
				"<p><i><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></i></p>\n" + 
				"<ul>\n" + 
				"<li><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></li></ul>\n" + 
				"<p><i><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></i></p>\n" + 
				"<ul>\n" + 
				"<li><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></li></ul>\n" + 
				"<p><i><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></i></p>\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"<div class=MsoNormal style=\"margin:0in 0in 0pt\"><span style=\"color:black\"><font face=Arial size=2>xxxxxx<span>Ê </span>xxxxxx</font></span></div></li></ul>\n" + 
				"<p><i><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></i></p>\n" + 
				"<ul>\n" + 
				"<li><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></li></ul>\n" + 
				"<p><font face=Arial></font></p>\n" + 
				"<h3><font face=Arial>xxxxxx</font></h3>\n" + 
				"<p><span style=\"color:black\"><font face=Arial size=2>xxxxxx&amp;xxxxxx</font></span></p>\n" + 
				"<p><span style=\"color:black\"><font face=Arial size=2>xxxxxx</font></span></p>\n" + 
				"<p><font face=Arial></font></p>\n" + 
				"<h3><font face=Arial>xxxxxx</font></h3>\n" + 
				"<p><span style=\"color:black\"><font face=Arial size=2><a class=ms-wikilink href=\"xxxxxx\">xxxxxx</a>xxxxxx</font></span></p>\n" + 
				"<ul type=disc>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font></li>\n" + 
				"<li class=MsoNormal style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font face=Arial size=2>xxxxxx</font><a name=\"OLE_LINK1\"></a></li></ul></div><p></p></div>"; 
		
		String expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" + 
				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + 
				"<html>\n" + 
				"<head>\n" + 
				"<title></title>\n" + 
				"</head>\n" + 
				"<body>\n" + 
				"<div class=\"ms-wikicontent\">\n" + 
				"<div class=\"ExternalClass6401589625F24EB681A45DBE6B1A1B3E\">\n" + 
				"<h3 class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></h3>\n" + 
				"\n" + 
				"<p class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\">\n" + 
				"<strong><em><font face=\"Arial\">xxxxxx</font></em></strong></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\"><font\n" + 
				"size=\"3\"><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx<a\n" + 
				"href=\"xxxxxx\">xxxxxx</a>&Acirc;&nbsp;xxxxxx</font></span></font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\" size=\"2\">xxxxxx \"xxxxxx\"\n" + 
				"xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\"><font\n" + 
				"face=\"Arial\"><strong><em>xxxxxx<br />\n" + 
				"</em></strong>xxxxxx</font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\"><font\n" + 
				"face=\"Arial\"><strong><em>xxxxxx<br />\n" + 
				"</em></strong>xxxxxx</font><a href=\"xxxxxx\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></a></p>\n" + 
				"\n" + 
				"<h3 class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></h3>\n" + 
				"\n" + 
				"<p class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<div class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\">\n" + 
				"<ul type=\"disc\">\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx&Acirc;&nbsp;</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"</ul>\n" + 
				"</div>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\"><em><b><span\n" + 
				"style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em></font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\">\n" + 
				"<em><b><span style=\"font-size:8.5pt;font-family:Verdana\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></span></b></em></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\"><em><b><span\n" + 
				"style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em></font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\">\n" + 
				"<em><b><span style=\"font-size:8.5pt;font-family:Verdana\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></span></b></em></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\"><em><b><span\n" + 
				"style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em></font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\"><em><b><span\n" + 
				"style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em></font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\"><em><b><span\n" + 
				"style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx (<a\n" + 
				"class=\"ms-wikilink\"\n" + 
				"href=\"xxxxxx\">xxxxxx</a>)</span></b></em></font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\"><em><b><span\n" + 
				"style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em></font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\"><em><b><span\n" + 
				"style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx</span></b></em></font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\"><em><b><span\n" + 
				"style=\"font-size:8.5pt;font-family:Verdana\">xxxxxx &amp;\n" + 
				"xxxxxx</span></b></em></font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\" size=\"2\">xxxxxx <a\n" + 
				"class=\"ms-missinglink\" href=\"xxxxxx\" title=\"xxxxxx\">xxxxxx</a>, <a\n" + 
				"class=\"ms-missinglink\" href=\"xxxxxx\"\n" + 
				"title=\"xxxxxx\">xxxxxx</a>xxxxxx<a href=\"xxxxxx\">xxxxxx</a> (xxxxxx)\n" + 
				"and <a href=\"xxxxxx\">xxxxxx</a>. xxxxxx <a\n" + 
				"href=\"xxxxxx\">xxxxxx</a>.</font></span></p>\n" + 
				"\n" + 
				"<h3 class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></h3>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\">\n" + 
				"<em><b><span style=\"font-size:8.5pt;font-family:Verdana\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></span></b></em></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\"><em><b><span\n" + 
				"style=\"font-size:8.5pt;color:black;font-family:Verdana\">xxxxxx</span></b></em></font></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\">\n" + 
				"<em><b><span\n" + 
				"style=\"font-size:8.5pt;color:black;font-family:Verdana\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></span></b></em></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\">\n" + 
				"<em><b><span style=\"font-size:8.5pt;font-family:Verdana\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></span></b></em></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"><font\n" + 
				"face=\"Arial\"><font size=\"2\"><span\n" + 
				"style=\"color:black\">xxxxxx</span><span\n" + 
				"style=\"color:black\">xxxxxx</span><span style=\"color:black\">xxxxxx<a\n" + 
				"href=\"xxxxxx\">xxxxxx</a>. xxxxxx:</span></font></font></p>\n" + 
				"\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"<div class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"\n" + 
				"style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\">\n" + 
				"<font size=\"3\"><font face=\"Arial\"><font size=\"2\"><span\n" + 
				"style=\"color:black\">xxxxxx <a class=\"ms-missinglink\" href=\"xxxxxx\"\n" + 
				"title=\"xxxxxx\">xxxxxx</a> xxxxxx</span></font></font></font></div>\n" + 
				"</li>\n" + 
				"\n" + 
				"<li>\n" + 
				"<div class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"\n" + 
				"style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\">\n" + 
				"<font face=\"Arial\"><font size=\"2\"><span\n" + 
				"style=\"color:black\">xxxxxx</span></font></font></div>\n" + 
				"</li>\n" + 
				"\n" + 
				"<li>\n" + 
				"<div class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"\n" + 
				"style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\">\n" + 
				"<font face=\"Arial\"><font size=\"2\"><span\n" + 
				"style=\"color:black\">xxxxxx</span></font></font></div>\n" + 
				"</li>\n" + 
				"\n" + 
				"<li>\n" + 
				"<div class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"\n" + 
				"style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\">\n" + 
				"<font face=\"Arial\"><font size=\"2\"><span\n" + 
				"style=\"color:black\">xxxxxx</span></font></font></div>\n" + 
				"</li>\n" + 
				"\n" + 
				"<li>\n" + 
				"<div class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"\n" + 
				"style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\">\n" + 
				"<font face=\"Arial\"><font size=\"2\"><span\n" + 
				"style=\"color:black\">xxxxxx</span></font></font></div>\n" + 
				"</li>\n" + 
				"\n" + 
				"<li>\n" + 
				"<div class=\"ExternalClass392E9BBB57E64482B84B1A425C8AE164\"\n" + 
				"style=\"margin-left:0.5in;text-indent:-0.25in;tab-stops:list .5in\">\n" + 
				"<font face=\"Arial\"><font size=\"2\"><span\n" + 
				"style=\"color:black\">xxxxxx</span></font></font></div>\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"<h3 class=\"ExternalClass338C1C537FBB4A40966BEC3D88CE46B8\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></h3>\n" + 
				"\n" + 
				"<p class=\"ExternalClass338C1C537FBB4A40966BEC3D88CE46B8\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				" \n" + 
				"\n" + 
				"<p class=\"ExternalClass338C1C537FBB4A40966BEC3D88CE46B8\"\n" + 
				"align=\"center\"><span style=\"color:black;font-family:Verdana\"><img\n" + 
				"alt=\"xxxxxx\" src=\"xxxxxx\" /></span></p>\n" + 
				"\n" + 
				"<h3 class=\"ExternalClass338C1C537FBB4A40966BEC3D88CE46B8\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></h3>\n" + 
				"\n" + 
				"<p class=\"ExternalClass338C1C537FBB4A40966BEC3D88CE46B8\"><span\n" + 
				"style=\"color:black;font-family:\'Times New Roman\'\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p class=\"ExternalClass338C1C537FBB4A40966BEC3D88CE46B8\"\n" + 
				"align=\"center\"><span\n" + 
				"style=\"color:black;font-family:\'Times New Roman\'\"><img alt=\"xxxxxx\"\n" + 
				"src=\"xxxxxx\" /></span></p>\n" + 
				"\n" + 
				"<h3 class=\"ExternalClass338C1C537FBB4A40966BEC3D88CE46B8\"><font\n" + 
				"face=\"Arial\">xxxxxx</font></h3>\n" + 
				"\n" + 
				"<p class=\"ExternalClass338C1C537FBB4A40966BEC3D88CE46B8\"><i><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></i></p>\n" + 
				"\n" + 
				"<div class=\"ExternalClass338C1C537FBB4A40966BEC3D88CE46B8\">\n" + 
				"<ul>\n" + 
				"<li><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></li>\n" + 
				"</ul>\n" + 
				"</div>\n" + 
				"\n" + 
				"<p><i><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></i></p>\n" + 
				"\n" + 
				"<ul>\n" + 
				"<li><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"<p><i><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></i></p>\n" + 
				"\n" + 
				"<ul>\n" + 
				"<li><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"<p><i><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></i></p>\n" + 
				"\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"<div class=\"MsoNormal\" style=\"margin:0in 0in 0pt\"><span\n" + 
				"style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx<span>&Acirc;&nbsp;</span>\n" + 
				"xxxxxx</font></span></div>\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"<p><i><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></i></p>\n" + 
				"\n" + 
				"<ul>\n" + 
				"<li><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"<h3><font face=\"Arial\">xxxxxx</font></h3>\n" + 
				"\n" + 
				"<p><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx&amp;xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<p><span style=\"color:black\"><font face=\"Arial\"\n" + 
				"size=\"2\">xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<h3><font face=\"Arial\">xxxxxx</font></h3>\n" + 
				"\n" + 
				"<p><span style=\"color:black\"><font face=\"Arial\" size=\"2\"><a\n" + 
				"class=\"ms-wikilink\"\n" + 
				"href=\"xxxxxx\">xxxxxx</a>xxxxxx</font></span></p>\n" + 
				"\n" + 
				"<ul type=\"disc\">\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font></li>\n" + 
				"\n" + 
				"<li class=\"MsoNormal\"\n" + 
				"style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\"><font\n" + 
				"face=\"Arial\" size=\"2\">xxxxxx</font><a id=\"OLE_LINK1\"\n" + 
				"name=\"OLE_LINK1\"></a></li>\n" + 
				"</ul>\n" + 
				"</div>\n" + 
				"</div>\n" + 
				"</body>\n" + 
				"</html>\n" + 
				"\n" + 
				"";
		Element expRoot = tester.getRootElement(expected);
		assertNotNull(expRoot);
		
		String actual = tester.cleanWithJTidy(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		actual = tester.clean(actual);
		
		Element root = tester.getRootElement(actual);
		assertNotNull(root);

		input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
		"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
		"<p>Testing</p>";
		expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" + 
				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + 
				"<html>\n" + 
				"<head>\n" + 
				"<title></title>\n" + 
				"</head>\n" + 
				"<body>\n" + 
				"<p>Testing</p>\n" + 
				"</body>\n" + 
				"</html>\n" + 
				"\n";
		actual = tester.cleanWithJTidy(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		actual = tester.clean(actual);
		expected = "<html>" +
		"<p>Testing</p></html>";
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testNotAddingXMLNSAttributes() {
		//no xmlns
		String input = 
			"<p>" +
			"<p att=\"test\">" +
			"<br/>" +
			"</p>" +
			"<span att=\"test2\">something</span>" +
			"<p/>" +
			"</p>";
		String actual = tester.cleanWithJTidy(input);
		assertFalse(actual.contains("xmlns"));
	}
	
	public void testCleanMeta() {
		String input, expected, actual;
		input = "<head>\n" + 
				"<meta name=\"generator\" content=\"HTML Tidy, see www.w3.org\">\n" + 
				"<title></title>\n" + 
				"</head>\n";
		expected = "\n";
		actual = tester.cleanHead(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + 
				"<html>\n" + 
				"<head>\n" + 
				"<meta name=\"generator\" content=\"HTML Tidy, see www.w3.org\">\n" + 
				"<title></title>\n" + 
				"</head>\n" + 
				"<body>\n" + 
				"<div class=\"ms-wikicontent\">\n" + 
				"<h3 class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\" style=\n" + 
				"\"font-family: Arial\">xxxxxx</h3>\n" + 
				"\n";
		expected = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n" +
				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" + 
				"<html>\n\n" +
				"<body>\n" + 
				"<div class=\"ms-wikicontent\">\n" +
				"<h3 class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\" style=\n" + 
				"\"font-family: Arial\">xxxxxx</h3>\n\n";
		actual = tester.cleanHead(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testNoAddingWsToBeginning() {
		String input = "5;#vti_parserversion:SR|12.0.0.6219 ContentType:SW|Wiki Page" +
				" ContentTypeId:SW|0x010108008586D6569CB93B489CBCF8B01B47B952 " +
				"vti_metatags:VR|CollaborationServer SharePoint\\\\ Team\\\\ Web\\\\ " +
				"Site vti_charset:SR|utf-8 vti_author:SR| vti_setuppath:SX|Document" +
				"Templates\\\\wkpstd.aspx vti_cachedzones:VR|Bottom vti_cachedneed" +
				"srewrite:BR|false WikiField:SW|<div class=ExternalClass14E9F8AB51" +
				"9A470B995C8046769BE7FC><div class=ExternalClass03BFEACBDD2F4A61A6" +
				"DF16469ED01B1D><div class=ExternalClass9831F41C1D6B425D9281846617" +
				"F46F8B><div class=ExternalClass482E934BA36E41D190108C5FF6077782>" +
				"Place to test Sharepoint Converter syntax.\n" + 
				"<br>\n" + 
				"Internal Link:\n" + 
				"[[Home]]\n" + 
				"<br>\n" + 
				"Link to an image:\n" + 
				"<a href=\"/Shared%20Documents/10year.jpg\">10year.jpg</a>\n" + 
				"<br>\n" + 
				"Show an image:\n" + 
				"<img src=\"/Shared%20Documents/10year.jpg\" height=200 />\n" + 
				"<br>\n" + 
				"<a href=\"/subsite2/testwiki/TestPage.aspx\">Testing Subsite Link</a>\n" + 
				"</div></div></div></div> vti_modifiedby:SR|PUBLIC36\\\\brendanpatterson vti_cachedhastheme:BR|false\n" + 
				"";
		String actual = tester.clean(input);
		assertTrue("actual starts with: " + actual.substring(0, 15), actual.startsWith("<html>Place"));
		
	}

	
	public void testCleanLists() {
		String input = 
			"<ul>" +
				"<li>abc</li>" +
				"<li>def</li>" +
				"<ol>" +
					"<li>ghi</li>" +
				"</ol>" +
				"<ol>" +
					"<li>hij</li>" +
				"</ol>" +
			"</ul>" +
			"<ol>" +
				"<li>klm</li>" +
				"<ol>" +
					"<li>nop</li>" +
				"</ol>" +
				"<ol>" +
					"<ol>" +
						"<li>qrs</li>" +
					"</ol>" +
				"</ol>" +
			"</ol>" +
			"<ul>" +
				"<ul>" +
					"<li>tuv</li>" +
				"</ul>" +
			"</ul>";
		String expected = 
			"<ul>" +
				"<li>abc</li>" +
				"<li>def</li>" +
				"<li/><ol>" +
					"<li>ghi</li>" +
				"</ol>" +
				"<ol>" +
					"<li>hij</li>" +
				"</ol>" +
			"</ul>" +
			"<ol>" +
				"<li>klm</li>" +
				"<li/><ol>" +
					"<li>nop</li>" +
				"</ol>" +
				"<ol>" +
					"<li/><ol>" +
						"<li>qrs</li>" +
					"</ol>" +
				"</ol>" +
			"</ol>" +
			"<ul>" +
				"<li/><ul>" +
					"<li>tuv</li>" +
				"</ul>" +
			"</ul>";
		String actual = tester.cleanLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	
	public void testCleanOctalWS() {
		String input, expected, actual;
		String path = "./sampleData/sharepoint/citi-sample/sample2.html";
		File testfile = new File(path);
		assertTrue(testfile.exists());
		String filestring = "";
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			while ((line = reader.readLine()) != null) {
				filestring += line;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String string = " ";
		byte[] bytes = null;
		try {
			bytes = string.getBytes("ascii");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			fail();
		}
		byte b = bytes[0];
		Pattern p = Pattern.compile(string);
		Matcher m = p.matcher(filestring);
		if (m.find()) {
			String found = m.group();
			String regex = "[\\s\\p{Graph}]";
			Pattern p1 = Pattern.compile(regex);
			Matcher m1 = p1.matcher(found);
			assertFalse(m1.find());
			regex = "[^\\s\\p{Graph}]";
			String newstring = found.replaceAll(regex, "abc");
			assertEquals("abc", newstring);
		}
		
		expected = " ";
		actual = tester.cleanWS(string);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertDivWithTextContent() {
		String input, expected, actual;
		input = "<html>" +
				"<div>justtext</div>" +
				"</html>";
		expected = "<html>justtext<br/></html>";
		actual = tester.cleanDiv(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html><div/></html>";
		expected = "<html/>";
		actual = tester.cleanDiv(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html><div><p>no br needed</p></div></html>";
		expected = "<html><p>no br needed</p></html>";
		actual = tester.cleanDiv(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testNeedsNL() {
		String input, expected, actual;
		input = "<html>" +
			"<div>justtext</div>" +
			"</html>";
		Element root = tester.getRootElement(input, false);
		Element el = root.element("div");
		List content = el.content();
		assertTrue(tester.needsNL(content));
		
		input = "<html><div/></html>";
		root = el = null;
		root = tester.getRootElement(input, false);
		el = root.element("div");
		content = el.content();
		assertFalse(tester.needsNL(content));
		
		input = "<html><div><p>no br needed</p></div></html>";
		root = el = null;
		root = tester.getRootElement(input, false);
		el = root.element("div");
		content = el.content();
		assertFalse(tester.needsNL(content));
		
		input = "<html>" +
				"<div class=\"ExternalClassEDE3A2A0FAF34C62B1E98D765C5078BF\">" +
				"<strong>How to Log into the ODS Maintenance Tool?</strong>" +
				"</div>" +
				"</html>";
		root = el = null;
		root = tester.getRootElement(input, false);
		el = root.element("div");
		content = el.content();
		assertTrue(tester.needsNL(content));
		
	}
	
	public void testAddNL() {
		String input, expected, actual;
		input = "<html>" +
			"<div>justtext</div>" +
			"</html>";
		Element root = tester.getRootElement(input, false);
		Element el = root.element("div");
		List content = el.content();
		content = tester.addBreak(content);
		expected = "justtext<br/>";
		actual = tester.toString(content);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertWS() {
		String input, expected, actual;
		
		input = "<p>xxxx yyyy</p>";
		expected = "<html><p>xxxx yyyy</p></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "<p>xxxx <span>yyyy</span></p>";
		expected = "<html><p>xxxx <span>yyyy</span></p></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "xxxxxx <span style=\"font-size:12pt;font-family:\'Times New Roman\'\">xxxxxx (<span style=\"font-size:12pt;font-family:\'Times New Roman\'\">xxxxxx</span>)xxxxxx<span>";
		expected = "<html>xxxxxx <span style=\"font-size:12pt;font-family:&apos;Times New Roman&apos;\">xxxxxx (<span style=\"font-size:12pt;font-family:&apos;Times New Roman&apos;\">xxxxxx</span>)xxxxxx</span></html>"; 
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		String weirdchar = " ";
		input = "<p class=MsoNormal style=\"margin:0in 0in 0pt;text-align:justify\"><span style=\"font-size:12pt\"><font face=\"Times New Roman\">abc <span style=\"font-size:12pt;font-family:\'Times New Roman\'\">def (<span style=\"font-size:12pt;font-family:\'Times New Roman\'\">xxxxxx</span>)" + weirdchar + " xxxxxx<span>" + weirdchar + "  </span>xxxxxx</span></font></span></p>\n";
		expected = "<html><p style=\"margin:0in 0in 0pt;text-align:justify\"><span style=\"font-size:12pt\"><font face=\"Times New Roman\">abc <span style=\"font-size:12pt;font-family:&apos;Times New Roman&apos;\">def (<span style=\"font-size:12pt;font-family:&apos;Times New Roman&apos;\">xxxxxx</span>) xxxxxx xxxxxx</span></font></span></p></html>"; 
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<p><span><font>xyzxyz <span style=\"font-size:12pt;font-family:\'Times New Roman\'\">xxxxxx (<span style=\"font-size:12pt;font-family:\'Times New Roman\'\">xxxxxx</span>)" + weirdchar + " xxxxxx<span>" + weirdchar + "  </span>xxxxxx</span></font></span></p>\n";
		expected = "<html>" +
				"<p><span><font>xyzxyz <span style=\"font-size:12pt;font-family:&apos;Times New Roman&apos;\">xxxxxx (<span style=\"font-size:12pt;font-family:&apos;Times New Roman&apos;\">xxxxxx</span>) xxxxxx xxxxxx</span></font></span></p></html>"; 
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<p style=\"margin:0in 0in 0pt;text-align:justify\"><span style=\"font-size:12pt\"><font face=\"Times New Roman\">xyzxyz <span style=\"font-size:12pt;font-family:\'Times New Roman\'\">xxxxxx (<span style=\"font-size:12pt;font-family:\'Times New Roman\'\">xxxxxx</span>)" + weirdchar + " xxxxxx<span>" + weirdchar + "  </span>xxxxxx</span></font></span></p>\n";
		expected = "<html><p style=\"margin:0in 0in 0pt;text-align:justify\"><span style=\"font-size:12pt\"><font face=\"Times New Roman\">xyzxyz <span style=\"font-size:12pt;font-family:&apos;Times New Roman&apos;\">xxxxxx (<span style=\"font-size:12pt;font-family:&apos;Times New Roman&apos;\">xxxxxx</span>) xxxxxx xxxxxx</span></font></span></p></html>"; 
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<p class=MsoNormal style=\"margin:0in 0in 0pt;text-align:justify\"><span style=\"font-size:12pt\"><font face=\"Times New Roman\">xyzxyz <span style=\"font-size:12pt;font-family:\'Times New Roman\'\">xxxxxx (<span style=\"font-size:12pt;font-family:\'Times New Roman\'\">xxxxxx</span>)" + weirdchar + " xxxxxx<span>" + weirdchar + "  </span>xxxxxx</span></font></span></p>\n";
		expected = "<html><p style=\"margin:0in 0in 0pt;text-align:justify\"><span style=\"font-size:12pt\"><font face=\"Times New Roman\">xyzxyz <span style=\"font-size:12pt;font-family:&apos;Times New Roman&apos;\">xxxxxx (<span style=\"font-size:12pt;font-family:&apos;Times New Roman&apos;\">xxxxxx</span>) xxxxxx xxxxxx</span></font></span></p></html>"; 
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<p>xyz" + weirdchar + "xyz</p>";
		expected = "<html><p>xyz xyz</p></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "<ul><li class=MsoNormal style=\"margin:0in 0in 0pt;tab-stops:list .5in\"><span style=\"font-size:12pt\"><font face=\"Times New Roman\">xxxxxx<span>"+weirdchar+"</span>xxxxxx</font></span></li></ul>\n";
		expected = "<html><ul><li style=\"margin:0in 0in 0pt;tab-stops:list .5in\"><span style=\"font-size:12pt\"><font face=\"Times New Roman\">xxxxxx<span> </span>xxxxxx</font></span></li></ul></html>";
	}
	
	public void testCleanEmptySpan() {
		String input, expected, actual;
		input = "<p>a<span> </span>b<span>   </span>c</p>";
		expected = "<html><p>a b c</p></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual); 
	}

	public void testQuotes() {
		String input, expected, actual;
		input = "<TABLE class=ms-main height=\"100%\" cellSpacing=0 cellPadding=0 width=\"100%\" \n" + 
				"border=0>\n" +
				"<tr><td>1</td></tr>" + 
				"</table>";
		expected = "<html>" +
				"<table class=\"ms-main\" height=\"100%\" cellspacing=\"0\" " +
				"cellpadding=\"0\" width=\"100%\" border=\"0\">" +
				"<tr><td rowspan=\"1\" colspan=\"1\">1</td></tr>" + 
				"</table>" +
				"</html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCleanListsWithDollarSigns() {
		String input, expected, actual;
		input = "<html><p><ul><li>$1</li><li>$2</li></ul></p></html>";
		expected = input;
		actual = tester.cleanLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>" +
				"<ul>\n" + 
				"<li>$1</li>\n" + 
				"<li>$2</li>\n" + 
				"</ul>\n" +
				"<br/>\n" +
				"<br/>\n" + 
				"</html>";
		expected = input.replaceAll("\n", "");
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanImagesWithDollarSigns() {
		String input, expected, actual;
		input = "<html><p>" +
				"<img src=\"http://something.com/image.jpg\" " +
				"alt=\"money $$$\"/><br/>" + 
				"</p></html>";
		expected = input;
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanImagesWithBackslashes() {
		String input, expected, actual;
		input = "<html><p>" +
				"<img src=\"http://something.com/image.jpg\" " +
				"alt=\"money \\\\\\\"/><br/>" + 
				"</p></html>";
		expected = input;
		actual = tester.cleanAttributes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testRemoveUnderlinesSurroundingLinks() {
		String input, expected, actual;
		input = "<html><u><a href=\"something\">else</a></u></html>";
		expected = "<html><a href=\"something\" shape=\"rect\">else</a></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "<html>This page has an internal link to a page with a bad char:" +
				"<u><br/><a class=\"ms-wikilink\" " +
				"href=\"/test%20wiki/Page%20With%20Bad%20;Char.aspx\" shape=\"rect\">" +
				"Page With Bad ;Char</a><br/></u></html>"; 
		expected = "<html>This page has an internal link to a page with a bad char:" +
				"<br/><a class=\"ms-wikilink\" " +
				"href=\"/test%20wiki/Page%20With%20Bad%20;Char.aspx\" shape=\"rect\">" +
				"Page With Bad ;Char</a><br/></html>"; 
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html><u><a href=\"something\">else</a></u>a<u>b</u></html>";
		expected = "<html><a href=\"something\" shape=\"rect\">else</a>a<u>b</u></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
 
		input = "<html><u><b><a href=\"something\">else</a></b></u></html>";
		expected = "<html><b><a href=\"something\" shape=\"rect\">else</a></b></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
 
		input = "<html><u>a</u><a href=\"something\">else</a></html>";
		expected = "<html><u>a</u><a href=\"something\" shape=\"rect\">else</a></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html><u><a href=\"something\">else</a> still underlined!</u></html>";
		expected = "<html><u><a href=\"something\" shape=\"rect\">else</a> still underlined!</u></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "<html><u><a href=\"dollar$\">$$</a></u></html>";
		expected = "<html><a href=\"dollar$\" shape=\"rect\">$$</a></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "<html><u><a href=\"back\\\">\\\\</a></u></html>";
		expected = "<html><a href=\"back\\\" shape=\"rect\">\\\\</a></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanNBSP() {
		String input, expected, actual;
		input = "<html>\n" +
				"<STRONG><U><FONT size=2>Testing</FONT></U>" +
				"<FONT face=\"Trebuchet MS\" size=2>&nbsp;</FONT></STRONG>\n" +
				"</html>"; 
		expected = "<html>" +
				"<STRONG><U><FONT size=2>Testing</FONT></U>" +
				"<FONT face=\"Trebuchet MS\" size=2> </FONT></STRONG>" +
				"</html>";
		actual = tester.cleanWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanEmptyFontTags() {
		String input, expected, actual;
		input = "<html>abc <font size=2/> def</html>";
		expected = "<html>abc  def</html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanNoSpaceBetweenClosingTags() {
		String input, expected, actual;
		input = "<html><b><u>abc</u> </b></html>";
		expected = "<html><b><u>abc</u></b></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
		"<STRONG><U><FONT size=2>Testing</FONT></U>" +
		"<FONT face=\"Trebuchet MS\" size=2>&nbsp;</FONT></STRONG>\n" +
		"</html>"; 
		expected = "<html>" +
		"<strong><u><font size=\"2\">Testing</font></u>" +
		"</strong>" +
		"</html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testHandleDivNL() {
		String input, expected, actual;
		input = "<html>" +
				"<DIV class=ExternalClassEDE3A2A0FAF34C62B1E98D765C5078BF><STRONG>How to Log into the ODS Maintenance Tool?</STRONG></DIV>\n" + 
				"<DIV class=ExternalClassEDE3A2A0FAF34C62B1E98D765C5078BF>1. Enter the User Name.</DIV>\n" + 
				"</html>";
		expected = "<html>" +
				"<strong>How to Log into the ODS Maintenance Tool?</strong><br xmlns=\"\"/>" +
				"1. Enter the User Name.<br xmlns=\"\"/>" +
				"</html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
				"<DIV class=ExternalClassEDE3A2A0FAF34C62B1E98D765C5078BF>1. Enter the User Name.</DIV>\n" + 
				"<DIV class=ExternalClassEDE3A2A0FAF34C62B1E98D765C5078BF>2. Enter the Password.</DIV>\n" + 
				"</html>";
		expected = "<html>" +
				"1. Enter the User Name.<br xmlns=\"\"/>" +
				"2. Enter the Password.<br xmlns=\"\"/>" +
				"</html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPreserveInlineWS() {
		String input, expected, actual;
		input = "<html><p>Each status has a <STRONG>severity color</STRONG> associated with it.</p></html>";
		expected = "<html><p>Each status has a <strong>severity color</strong> associated with it.</p></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>" +
			"<div class=\"ExternalClassEDE3A2A0FAF34C62B1E98D765C5078BF\">" +
			"<span style=\"FONT-FAMILY: Arial\"><font size=\"2\">" +
			"<strong>Severity: </strong>" +
			"Each status has a" +
			" " +
			"<strong>severity color</strong>" +
			" " +
			"associated with it. When a status is created dynamically, its color defaults to ~SYELLOW~T. The default color can be updated later to a different color (~SGREEN~T or ~SRED~T) via a GUI front-end." +
			"</font></span></div>\n" + 
			"</html>";
		expected = "<html>" +
			"<span style=\"FONT-FAMILY: Arial\"><font size=\"2\">" +
			"<strong>Severity:</strong>" +
			" " +
			"Each status has a" +
			" " +
			"<strong>severity color</strong>" +
			" " +
			"associated with it. When a status is created dynamically, its color defaults to ~SYELLOW~T. The default color can be updated later to a different color (~SGREEN~T or ~SRED~T) via a GUI front-end." +
			"</font></span><br xmlns=\"\"/>" + 
			"</html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>" +
				"<div>" +
				"Perhaps the support person is able to correct the " +
				"problem manually, then they would click a modify button " +
				"(GUI) on this event, to change the status to a ~Sgreen~T status." +
				"<SPAN>&nbsp; </SPAN>" +
				"They would also add the comment" +
				"</div>" +
				"</html>";
		expected = "<html>" +
				"Perhaps the support person is able to correct the " +
				"problem manually, then they would click a modify button " +
				"(GUI) on this event, to change the status to a ~Sgreen~T status." +
				" " +
				"They would also add the comment<br xmlns=\"\"/>" +
				"</html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testNoAdditionalWS() {
		String input, expected, actual;
		input = "6;#vti_parserversion:SR|12.0.0.6219 ContentType:SW|Wiki Page ContentTypeId:SW|" +
				"0x01010800099E6FC4BD66DF47B01DB6E7E4EB0380 vti_metatags:VR|CollaborationServer " +
				"SharePoint\\\\ Team\\\\ Web\\\\ Site vti_charset:SR|utf-8 vti_author:SR| vti_setuppath" +
				":SX|DocumentTemplates\\\\wkpstd.aspx vti_cachedzones:VR|Bottom vti_cachedneedsrewrite:BR|" +
				"false WikiField:SW|" +
				"<div class=ExternalClass76A70C78FE914AC8B125D1E3DF7754A9>" +
				"<div class=ExternalClassF1984C10BFB34AD4B039E4E3E8CEF7DE>" +
				"<strong>Testing Table<br></strong>" +
				"<br></div></div>" +
				" vti_modifiedby:SR|PUBLIC36\\\\laura.kolker vti_cachedhastheme:BR|false";
		expected = "<html><strong>Testing Table<br/></strong><br/></html>";
		actual = tester.clean(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}