package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.TestCase;

public class PanelConverterTest extends TestCase {

	PanelConverter tester;
	protected void setUp() throws Exception {
		super.setUp();
		tester = new PanelConverter();
	}

	public void testConvertPanel() {
		String input = "^\n" +
				"Just a panel?\n" +
				"^";
		String expected = "{panel}\n" +
				"Just a panel?\n" +
				"{panel}";
		String actual = tester.convertPanel(input);
		assertEquals(expected, actual);
	}

	public void testMoreLines() {
		String input = "Before\n" +
				"^\n" +
				"Just a panel?\n" +
				"^\n" +
				"After";
		String expected = "Before\n" +
				"{panel}\n" +
				"Just a panel?\n" +
				"{panel}\n" +
				"After";
		String actual = tester.convertPanel(input);
		assertEquals(expected, actual);		
	}
	
	public void testManyPanels() {
		String input = "Before\n" +
				"^\n" +
				"Just a panel?\n" +
				"^\n" +
				"After\n" +
				"^\n" +
				"Another Panel\n" +
				"^\n";
		String expected = "Before\n" +
				"{panel}\n" +
				"Just a panel?\n" +
				"{panel}\n" +
				"After\n" +
				"{panel}\n" +
				"Another Panel\n" +
				"{panel}\n";
		String actual = tester.convertPanel(input);
		assertEquals(expected, actual);
	}
	
	public void testManyPanelLines() {
		String input = "Before\n" +
				"^\n" +
				"Just a panel?\n" +
				"Another Line\n" +
				"And yet another\n" +
				"^\n";
		String expected = "Before\n" +
				"{panel}\n" +
				"Just a panel?\n" +
				"Another Line\n" +
				"And yet another\n" +
				"{panel}\n";
		String actual = tester.convertPanel(input);
		assertEquals(expected, actual);
	}
	
	public void testManyCarets() {
		String input = "Before\n" +
				"^\n" +
				"Just a panel?\n" +
				"^\n" +
				"^\n";
		String expected = "Before\n" +
				"{panel}\n" +
				"Just a panel?\n" +
				"{panel}\n" +
				"^\n"; 
		String actual = tester.convertPanel(input);
		assertEquals(expected, actual);
	}
	
	public void testJustImages() {
		String input = "{img src=&quot;img/wiki_up/hobbespounce.gif&quot; width=&quot;200&quot;}\n" +
		"!Wiki.png!\n" +
		"{img src=show_image.php?id=1 }\n" +
		"\n" +
		"Importance of quotes\n" +
		"{img src=img/wiki_up/hobbespounce.gif}\n" +
		"!Wiki.png!\n" +
		"\n" +
		"Importance of space\n" +
		"!hobbespounce.gif!\n" +
		"!hobbespounce.gif!\n" +
		"!Wiki.png!\n" ;
		String expected = input;
		
		String actual = tester.convertPanel(input);
		assertEquals(expected, actual);
	}
}
