package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MathConverterTest extends TestCase {

	MathConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new MathConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertMath() {
		String input, expected, actual;
		input = "<math>g(x,y)\\,</math>\n" + 
				"\n" + 
				"<math>f(x) = \\int_0^1 e^{-t} g(t) \\, dt.</math>\n" + 
				"";
		expected = "{latex}\n" +
				"\\begin{eqnarray}\n" + 
				"{\n" + 
				"g(x,y)\\,\n" + 
				"}\n" + 
				"\\end{eqnarray}\n" + 
				"{latex}\n" + 
				"\n" + 
				"{latex}\n" + 
				"\\begin{eqnarray}\n" + 
				"{\n" + 
				"f(x) = \\int_0^1 e^{-t} g(t) \\, dt.\n" + 
				"}\n" + 
				"\\end{eqnarray}\n" + 
				"{latex}\n" + 
				"";
		actual = tester.convertMath(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
