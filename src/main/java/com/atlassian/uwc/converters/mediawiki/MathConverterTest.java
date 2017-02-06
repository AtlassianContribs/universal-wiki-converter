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
		input = "<math>\\label{test} g(x,y)\\,</math>\n" +
				"\n" + 
				"Inline math: <math>f(x) = \\int_0^1 e^{-t} g(t) \\, dt.</math>\n" +
				"\n" +
				"<math>A_{B} = 100% - 200% + C_{D}</math>\n" + 
				"\n" + 
				"";
		expected = "{mathblock:anchor=test}\n" +
				"\\begin{eqnarray}\n" +
				"g(x,y)\\,\n" +
				"\\end{eqnarray}\n" + 
				"{mathblock}\n" +
				"\n" + 
				"Inline math: {mathinline}f(x) = \\int_0^1 e^{-t} g(t) \\, dt.{mathinline}\n" +
				"\n" +
				"{mathblock}\n" +
				"\\begin{eqnarray}\n" +
				"A_{B} = 100\\% - 200\\% + C_{D}\n" +
				"\\end{eqnarray}\n" + 
				"{mathblock}\n" +
				"\n";
		actual = tester.convertMath(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
