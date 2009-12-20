package com.atlassian.uwc.ui;

import java.util.Vector;

import com.atlassian.uwc.ui.listeners.FeedbackHandler;

/**
 * stores data about errors that occurred during the conversion
 */
public class ConverterErrors implements FeedbackHandler {
	
	Vector<ConverterError> errors = new Vector<ConverterError>();
	
    /**
     * adds an error
     * @param type the type of error
     * @param note a note about the error
     * @param isFeedbackWindowMessage if true should be displayed in the feedback window
     */
    public void addError(Feedback type, String note, boolean isFeedbackWindowMessage) {
    	ConverterError error = new ConverterError(type, note, isFeedbackWindowMessage);
    	this.errors.add(error);
    }
    
    /**
     * @return the current set of errors
     */
    public Vector getErrors() {
    	return this.errors;
    }
    
    /**
     * @return a string representing all the error messages, or null if no errors exist
     */
    public String getAllErrorMessages() {
    	if (!hasErrors()) return null;
    	
    	String allMessages = "";
    	for (ConverterError error : this.errors) {
			allMessages += error.toString();
		}
    	return allMessages;
    }
    
    /**
     * @return string representing all the error messages that should be 
     * displayed in the feedback window, or null
     * if no such errors exist
     */
    public String getFeedbackWindowErrorMessages() {
    	if (!hasErrors()) return null;
    	
    	String feedbackWindowMessages = "";
    	
    	for (ConverterError error : this.errors) {
			feedbackWindowMessages += error.getFeedbackWindowMessage();
		}
    	return feedbackWindowMessages;
    }
    
    /**
     * @return true if errors have been added
     */
    public boolean hasErrors() {
    	return errors.size() > 0;
    }

    /**
     * removes all errors
     */
    public void clear() {
    	this.errors.removeAllElements();
    }
    
    /**
     * represents one error
     */
    class ConverterError {
    	/**
    	 * The type of error
    	 */
    	Feedback type = Feedback.NONE;
    	/**
    	 * A message about the error
    	 */
    	String note;
    	/**
    	 * if true, this error should be displayed on the feedback window
    	 */
    	boolean isFeedbackWindowMessage = false;

    	public ConverterError(Feedback type, String note, boolean isFeedbackWndowMessage) {
    		this.type = type;
    		this.note = note;
    		this.isFeedbackWindowMessage = isFeedbackWndowMessage;
    	}
    	
    	/**
    	 * @return a String representation of the error
    	 * @see java.lang.Object#toString()
    	 */
    	public String toString() {
    		if (type == Feedback.NONE && note == null) 
    			return "";
    		return type + " " + note + "\n";
    	}
    	
    	/**
    	 * @return a string representation of the error 
    	 * or "" (the empty string), if this error should not
    	 * be displayed on the feedback window 
    	 */
    	public String getFeedbackWindowMessage() {
    		if (!isFeedbackWindowMessage) return "";
    		return toString();
    	}
    	
    }
}
