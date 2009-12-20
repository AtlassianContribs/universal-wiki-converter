package com.atlassian.uwc.ui;

import junit.framework.TestCase;

import com.atlassian.uwc.ui.listeners.FeedbackHandler;
import com.atlassian.uwc.ui.listeners.FeedbackHandler.Feedback;

public class ConverterErrorsTest extends TestCase {

	ConverterErrors tester;
	private String note;
	private Feedback type;
	private boolean isFeedbackWindowMessage;
	protected void setUp() throws Exception {
		tester = new ConverterErrors();
		
		note = "This is a test";
		type = FeedbackHandler.Feedback.CONVERTER_ERROR;
		isFeedbackWindowMessage = false;
		
	}

	public void testAddError() {
		assertNotNull(tester.getErrors());
		assertTrue(tester.getErrors().isEmpty());
		
		tester.addError(type, note, isFeedbackWindowMessage);
		
		assertNotNull(tester.getErrors());
		assertFalse(tester.getErrors().isEmpty());
		assertTrue(tester.getErrors().size() == 1);
	}

	public void testGetAllErrorMessages() {
		
		assertNull(tester.getAllErrorMessages());
		
		String note1 = note + "1";
		String note2 = note + "2";
		tester.addError(type, note1, false);
		tester.addError(type, note2, true);
		
		String expected = 
			type + " " + note + "1\n" +
			type + " " + note + "2\n"
			;
		String actual = tester.getAllErrorMessages();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetFeedbackWindowErrorMessages() {
		assertNull(tester.getFeedbackWindowErrorMessages());
		
		String note1 = note + "1";
		String note2 = note + "2";
		tester.addError(type, note1, false);
		tester.addError(type, note2, true);
		
		String expected = 
			type + " " + note + "2\n"
			;
		String actual = tester.getFeedbackWindowErrorMessages();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testHasErrors() {
		assertFalse(tester.hasErrors());
		
		tester.addError(type, note, isFeedbackWindowMessage);
		
		assertTrue(tester.hasErrors());
	}
	
	public void testClear() {
		tester.addError(type, note, isFeedbackWindowMessage);
		assertFalse(tester.getErrors().isEmpty());
		
		tester.clear();
		assertTrue(tester.getErrors().isEmpty());
	}
}
