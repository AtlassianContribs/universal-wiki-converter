package com.atlassian.uwc.converters.dokuwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class PrepRowSpansConverterTest extends TestCase {

	PrepRowSpansConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new PrepRowSpansConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testPrep_MultiRow() {
		String input, expected, actual;
		input = "^ 1            ^ 2                ^ 1            ^ 2                ^\n" + 
				"| a | b     | a         | b             |\n" + 
				"| :::                | b      | :::                | d              |\n" + 
				"| :::                | b  | :::                | d |\n" + 
				"| :::                | b | :::                | d |\n" + 
				"^ 1 ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a        | b               | c     | d |\n" + 
				"| :::                |b             | :::                | d        |\n" + 
				"| :::                | b    | :::                | d     |\n" + 
				"| :::                |b     | :::                | d     |\n" + 
				"";
		expected ="^ 1            ^ 2                ^ 1            ^ 2                ^\n" + 
				"| a ::UWCTOKENROWSPANS:4::| b     | a         ::UWCTOKENROWSPANS:4::| b             |\n" + 
				"| :::                | b      | :::                | d              |\n" + 
				"| :::                | b  | :::                | d |\n" + 
				"| :::                | b | :::                | d |\n" + 
				"^ 1 ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a        ::UWCTOKENROWSPANS:4::| b               | c     ::UWCTOKENROWSPANS:4::| d |\n" + 
				"| :::                |b             | :::                | d        |\n" + 
				"| :::                | b    | :::                | d     |\n" + 
				"| :::                |b     | :::                | d     |\n" + 
				"";
		actual = tester.prep(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPrep_MultiRow_b() {
		String input, expected, actual;
		input = "^ 1            ^ 2                ^ 1            ^ 2                ^\n" + 
				"| a | b     | a         | b             |\n" + 
				"| :::                | b      | :::                | d              |\n" + 
				"| :::                | b  | :::                | d |\n" + 
				"| :::                | b | :::                | d |\n" + 
				"^ 1 ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a        | b               | c     | d |\n" + 
				"| :::                |b             | :::                | d        |\n" + 
				"| :::                | b    | :::                | d     |\n" + 
				"| :::                |b     | c                | d     |\n" + 
				"no longer a table";
		expected ="^ 1            ^ 2                ^ 1            ^ 2                ^\n" + 
				"| a ::UWCTOKENROWSPANS:4::| b     | a         ::UWCTOKENROWSPANS:4::| b             |\n" + 
				"| :::                | b      | :::                | d              |\n" + 
				"| :::                | b  | :::                | d |\n" + 
				"| :::                | b | :::                | d |\n" + 
				"^ 1 ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a        ::UWCTOKENROWSPANS:4::| b               | c     ::UWCTOKENROWSPANS:3::| d |\n" + 
				"| :::                |b             | :::                | d        |\n" + 
				"| :::                | b    | :::                | d     |\n" + 
				"| :::                |b     | c                | d     |\n" + 
				"no longer a table";
		actual = tester.prep(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetNumCols() {
		String input;
		int expected, actual;
		input = "^ 1            ^ 2                ^ 1            ^ 2                ^\n" + 
				"| a | b     | a         | b             |\n" + 
				"| :::                | b      | :::                | d              |\n";
		expected = 4;
		actual = tester.getNumCols(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	
	public void testGetNumCols_WithColspan() {
		String input;
		int expected, actual;
		input = "^ h1  ::UWCTOKENCOLSPANS:6::^\n" + 
				"^ h1       ^ h2 ^ h3 ^ h4 ^ h5 ^ h6 ^\n" + 
				"| Trala        | abc.def.ghu.jkl.edu       | D:\\Path\\TO\\Some\\Place_tada\\asd     | D:\\Path\\TO\\Some\\Place_tada\\asd[mmyyyy].txt  | asd.asd.asd.org  | Asdlkj_askjda_BD  |\n" + 
				"| :::           | foo-1234-bar.abc.def.org:0986  | :::                                       | D:\\AppLogFiles\\BindingLogs_[yyyy-mm].txt        | :::                                      | :::             |\n" + 
				"| Lala         | a.b.c.d.e       | D:\\Apps\\INT\\Do\\ComparioXMLSite_es\\Web     | D:\\AppLogFiles\\ComparioXmlSite.xml[mmyyyy].txt  | sqlcompariopreview.internet.conseur.org  | Compario_ES_ES  |\n" + 
				"";
		expected = 6;
		actual = tester.getNumCols(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHandleColumn_0() {
		String input, expected, actual;
		input = "^ 1            ^ 2                ^ 1            ^ 2                ^\n" + 
				"| a | b     | a         | b             |\n" + 
				"| :::                | b      | :::                | d              |\n" + 
				"| :::                | b  | :::                | d |\n" + 
				"| :::                | b | :::                | d |\n" + 
				"^ 1 ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a        | b               | c     | d |\n" + 
				"| :::                |b             | :::                | d        |\n";
		expected = "^ 1            ^ 2                ^ 1            ^ 2                ^\n" + 
				"| a ::UWCTOKENROWSPANS:4::| b     | a         | b             |\n" + 
				"| :::                | b      | :::                | d              |\n" + 
				"| :::                | b  | :::                | d |\n" + 
				"| :::                | b | :::                | d |\n" + 
				"^ 1 ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a        ::UWCTOKENROWSPANS:2::| b               | c     | d |\n" + 
				"| :::                |b             | :::                | d        |\n";
		actual = tester.handleColumn(0, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHandleColumn_2() {
		String input, expected, actual;
		input = "^ 1            ^ 2                ^ 1            ^ 2                ^\n" + 
				"| a | b     | a         | b             |\n" + 
				"| :::                | b      | :::                | d              |\n" + 
				"| :::                | b  | :::                | d |\n" + 
				"| :::                | b | :::                | d |\n" + 
				"^ 1 ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a        | b               | c     | d |\n" + 
				"| :::                |b             | :::                | d        |\n";
		expected = "^ 1            ^ 2                ^ 1            ^ 2                ^\n" + 
				"| a | b     | a         ::UWCTOKENROWSPANS:4::| b             |\n" + 
				"| :::                | b      | :::                | d              |\n" + 
				"| :::                | b  | :::                | d |\n" + 
				"| :::                | b | :::                | d |\n" + 
				"^ 1 ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a        | b               | c     ::UWCTOKENROWSPANS:2::| d |\n" + 
				"| :::                |b             | :::                | d        |\n";
		actual = tester.handleColumn(2, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testExample() {
		String input, expected, actual;
		input = "^ h1  ^ h2                           ^ h3                                       ^ h4                                       ^ h5                                       ^ h6                                                        ^\n" + 
				"^ r1c1   | r1c2  |  r1c3| r1c4            | r1c5 | r1c6 |\n" + 
				"^ :::          | r2c2                       | r2c3| c4  | c5 | c6 |\n" + 
				"^ :::          | c2 | c3|c4| c5|c6  |\n" + 
				"^ :::          | c2 | c3|c4| c5|c6  |\n" + 
				"^ Health page  | c2  ::UWCTOKENCOLSPANS:5::|\n" + 
				"";
		expected = "^ h1  ^ h2                           ^ h3                                       ^ h4                                       ^ h5                                       ^ h6                                                        ^\n" + 
				"^ r1c1   ::UWCTOKENROWSPANS:4::| r1c2  |  r1c3| r1c4            | r1c5 | r1c6 |\n" + 
				"^ :::          | r2c2                       | r2c3| c4  | c5 | c6 |\n" + 
				"^ :::          | c2 | c3|c4| c5|c6  |\n" + 
				"^ :::          | c2 | c3|c4| c5|c6  |\n" + 
				"^ Health page  | c2  ::UWCTOKENCOLSPANS:5::|\n";
		actual = tester.prep(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testExample2() {
		String input, expected, actual;
		input = "^ h1  ::UWCTOKENCOLSPANS:6::^\n" + 
				"^ h1       ^ h2 ^ h3 ^ h4 ^ h5 ^ h6 ^\n" + 
				"| Trala        | abc.def.ghu.jkl.edu       | D:\\Path\\TO\\Some\\Place_tada\\asd     | D:\\Path\\TO\\Some\\Place_tada\\asd[mmyyyy].txt  | asd.asd.asd.org  | Asdlkj_askjda_BD  |\n" + 
				"| :::           | foo-1234-bar.abc.def.org:0986  | :::                                       | D:\\AppLogFiles\\BindingLogs_[yyyy-mm].txt        | :::                                      | :::             |\n" + 
				"| Lala         | a.b.c.d.e       | D:\\Apps\\INT\\Do\\ComparioXMLSite_es\\Web     | D:\\AppLogFiles\\ComparioXmlSite.xml[mmyyyy].txt  | sqlcompariopreview.internet.conseur.org  | Compario_ES_ES  |\n" + 
				"";
		expected = "^ h1  ::UWCTOKENCOLSPANS:6::^\n" + 
				"^ h1       ^ h2 ^ h3 ^ h4 ^ h5 ^ h6 ^\n" + 
				"| Trala        ::UWCTOKENROWSPANS:2::| abc.def.ghu.jkl.edu       | D:\\Path\\TO\\Some\\Place_tada\\asd     ::UWCTOKENROWSPANS:2::| D:\\Path\\TO\\Some\\Place_tada\\asd[mmyyyy].txt  | asd.asd.asd.org  ::UWCTOKENROWSPANS:2::| Asdlkj_askjda_BD  ::UWCTOKENROWSPANS:2::|\n" + 
				"| :::           | foo-1234-bar.abc.def.org:0986  | :::                                       | D:\\AppLogFiles\\BindingLogs_[yyyy-mm].txt        | :::                                      | :::             |\n" + 
				"| Lala         | a.b.c.d.e       | D:\\Apps\\INT\\Do\\ComparioXMLSite_es\\Web     | D:\\AppLogFiles\\ComparioXmlSite.xml[mmyyyy].txt  | sqlcompariopreview.internet.conseur.org  | Compario_ES_ES  |\n" + 
				"";
		actual = tester.prep(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testExample3() {
		String input, expected, actual;
		input = "Given the following:\n" + 
				"\n" + 
				"^ 1    | 2               |\n" + 
				"^ 1   | 2 |\n" + 
				"^ 1 | 2                     |\n" + 
				"\n" + 
				"tralala:\n" + 
				"\n" + 
				"^ 1           ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a | b | c | !!! |\n" + 
				"| a | b | c |::: |\n" + 
				"| a | b | c | end |\n" + 
				"";
		expected = "Given the following:\n" + 
				"\n" + 
				"^ 1    | 2               |\n" + 
				"^ 1   | 2 |\n" + 
				"^ 1 | 2                     |\n" + 
				"\n" + 
				"tralala:\n" + 
				"\n" + 
				"^ 1           ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a | b | c | !!! ::UWCTOKENROWSPANS:2::|\n" + 
				"| a | b | c |::: |\n" + 
				"| a | b | c | end |\n" + 
				"";
		actual = tester.prep(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testTrimTable() {
		String input, expected, actual;
		input = "^ 1    | 2               |\n" + 
				"^ 1   | 2 |\n" + 
				"^ 1 | 2                     |\n" + 
				"\n" + 
				"tralala:\n" + 
				"\n" + 
				"^ 1           ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a | b | c | !!! ::UWCTOKENROWSPANS:2::|\n" + 
				"| a | b | c |::: |\n" + 
				"| a | b | c | end |\n";
		expected = "^ 1           ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a | b | c | !!! ::UWCTOKENROWSPANS:2::|\n" + 
				"| a | b | c |::: |\n" + 
				"| a | b | c | end |\n";
		actual = tester.trimTable(input)[1];
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testNeedToTrim() {
		String input, expected, actual;
		input = "^ 1    | 2               |\n" + 
				"^ 1   | 2 |\n" + 
				"^ 1 | 2                     |\n" + 
				"\n" + 
				"tralala:\n" + 
				"\n" + 
				"^ 1           ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a | b | c | !!!|\n" + 
				"| a | b | c |::: |\n" + 
				"| a | b | c | end |\n";
		
		assertTrue(tester.needToTrim(input));
		
		input = "tralala:\n" + 
				"\n" + 
				"^ 1           ^ 2 ^ 3 ^ 4 ^\n" + 
				"| a | b | c | !!!|\n" + 
				"| a | b | c |::: |\n" + 
				"| a | b | c | end |\n";
		assertFalse(tester.needToTrim(input));
	}
}
