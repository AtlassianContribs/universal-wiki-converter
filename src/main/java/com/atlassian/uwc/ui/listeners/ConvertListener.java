package com.atlassian.uwc.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.ConverterErrors;
import com.atlassian.uwc.ui.FeedbackWindow;
import com.atlassian.uwc.ui.SwingWorker;
import com.atlassian.uwc.ui.UWCGuiModel;
import com.atlassian.uwc.ui.UWCUserSettings;

/**
 * starts the conversion
 */
public class ConvertListener implements ActionListener, FeedbackHandler, FeedbackCanceller {

	private static final String GENERAL_FAILURE_MESSAGE = "ENCOUNTERED ERRORS - See uwc.log for more details";
	UWCGuiModel model;
	Feedback feedback = Feedback.NONE;
	private JComboBox wikitypes;
	private String dir;
	private FeedbackWindow feedbackWindow;
	Logger log = Logger.getLogger(this.getClass());
	private Worker converter;
	
	/**
	 * creates the listener
	 * @param wikitypes ui containing data about the chosen type
	 * @param model model containing data needed to run the conversion
	 * @param dir directory where the properties files live
	 * @param feedbackWindow window which can be used to display feedback to the user
	 */
	public ConvertListener(JComboBox wikitypes, UWCGuiModel model, String dir, FeedbackWindow feedbackWindow) {
		this.wikitypes = wikitypes;
		this.model = model;
		this.dir = dir;
		this.feedbackWindow = feedbackWindow;
	}

	/**
	 * prepares the feedback window and runs the conversion
	 */
	public void actionPerformed(ActionEvent arg0) {
		final String propsPath = getPropsPath();
		feedback = Feedback.NONE;
		displayInitialFeedback();
		if (!validProps(propsPath)) {
			this.feedbackWindow.launch();
			displayFinalFeedback(propsPath, Feedback.NO_CONVERTER_FILE);
			return;
		} else if (!readAccess(propsPath)) {
			this.feedbackWindow.launch();
			displayFinalFeedback(propsPath, Feedback.BAD_SETTINGS_FILE);
			return;
		}
		
		try {
			//set up feedback window with this listener so feedback status can be kept uptodate
			registerFeedbackWindow();
			
			//set up cancel handling in the feedback window
			feedbackWindow.setCurrent(this);
			feedbackWindow.cancelOn();
			
			//convert
			this.converter = convertWithFeedback(propsPath);

		} catch (IOException e) {
			String message = "Problem with converter properties file: " + propsPath;
			feedback = Feedback.BAD_SETTINGS_FILE;
			log.error(message);
		} catch (IllegalArgumentException e) {
			String message = "Could not find converter properties file: " + propsPath;
			feedback = Feedback.NO_CONVERTER_FILE;
			log.error(message);
		}
	}

	/**
	 * @param propsPath path to a (properties) file
	 * @return true if the given propsPath represents an existing file
	 */
	private boolean validProps(String propsPath) {
		File file = new File(propsPath);
		return file.exists();
	}
	/**
	 * @param propsPath path to a (properties) file
	 * @return true if the given propsPath represents a readable file
	 */
	private boolean readAccess(String propsPath) {
		File file = new File(propsPath);
		return file.canRead();
	}

	/**
	 * registers the feedback window with the model.
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	private void registerFeedbackWindow() throws IOException, IllegalArgumentException {
		model.registerFeedbackWindow(this.feedbackWindow);
		this.feedbackWindow.clearProgressBar(); //reset the progressbar, but leave the text messages alone
	}

	/**
	 * displays the conversion feedback in the feedback window
	 * @param propsPath path to the directory containing the properties files
	 */
	void displayFinalFeedback(String propsPath) {
		Feedback feedback = model.getConverterFeedback();
		if (feedback == null)
			feedback = this.feedback;
		displayFinalFeedback(propsPath, feedback);
	}
	/**
	 * displays the given feedback in the feedback window
	 * @param propsPath path to the dir containing the props files
	 * @param feedback the feedback to display
	 */
	void displayFinalFeedback(String propsPath, Feedback feedback) {

		//get any errors
		ConverterErrors engineErrors = this.model.getErrors();
		boolean hadConverterErrors = this.model.getHadConverterErrors();
		
		//get current feedback info
		String description = getFeedbackDescription(feedback, propsPath);
		
		//but if there are errors, display those, and replace 
		//final feedback with generic error message.
		if (engineErrors.hasErrors() || hadConverterErrors) {
			this.displayErrorMessages(engineErrors);
			if (feedback == Feedback.OK) {
				feedback = Feedback.UNEXPECTED_ERROR;
				description = "\n" + GENERAL_FAILURE_MESSAGE + "\n";
			}
		}
		
		if (feedback != Feedback.OK)
			log.error(getFeedbackDescription(feedback, getPropsPath()));
		else
			log.debug(getFeedbackDescription(feedback, getPropsPath()));
		
		//update the window
		this.feedbackWindow.updateFeedback(description); 	//do the updaate
		this.feedbackWindow.end();							//move feedback window caret to the end
		this.feedbackWindow.cancelOff();					//disable the cancel button
		
		//clean up some objects
		this.feedbackWindow.setCurrent(null);							
		this.feedback = null;
		this.converter = null;
	}

	/**
	 * updates the feedback window with the given error messages
	 * @param errors error messages to be displayed
	 */
	private void displayErrorMessages(ConverterErrors errors) {
		log.debug("Displaying Error Messages: ");
		String feedbackMessages = errors.getFeedbackWindowErrorMessages();
		if (feedbackMessages == null) feedbackMessages = "";
		else feedbackMessages = "\n" + feedbackMessages;
		this.feedbackWindow.updateFeedback(feedbackMessages);
	}

	/**
	 * Start the conversion as another thread.
	 * @param propsPath dir to the props files
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	private Worker convertWithFeedback(final String propsPath) throws IllegalArgumentException, IOException{
		Worker worker = new Worker(propsPath);
		worker.start();
		return worker;
	}

	/**
	 * checks that the feedback window is on, and if so,
	 * launches the feedback window.
	 */
	private void displayInitialFeedback() {
		boolean feedbackOn = Boolean.parseBoolean(
				this.model.getSetting(UWCUserSettings.Setting.FEEDBACK_OPTION));
		if (feedbackOn) {
			this.feedbackWindow.launch();
		}
		String initialWikiMessage = 
			"***************\n\n" +
			"Converting Wiki: " + 
			this.model.getSetting(
					UWCUserSettings.Setting.WIKITYPE
			);
		this.feedbackWindow.updateFeedback(
				initialWikiMessage);
	}

	/**
	 * @param feedback 
	 * @param propsPath dir where properties files are located
	 * @return explanation of given feedback
	 */
	public static String getFeedbackDescription(Feedback feedback, String propsPath) {
		String description = "";
		description += "\nConversion Status... ";
		switch(feedback) {
		case OK:
			description += "SUCCESS\n";
			break;
		case BAD_SETTINGS_FILE:
			description += "FAILURE\n" +
			feedback.toString() + "\n" + 
			"Cannot read properties file:\n" +
			propsPath + "\n";
			break;
		case NO_CONVERTER_FILE:
			description += "FAILURE\n" +
				feedback.toString() + "\n" +
				"No properties file at location:\n" +
				propsPath + "\n";
			break;
		default:
			description += feedback.toString() + "\n";
		}

		return description;
	}

	/**
	 * @return the path to the property file representing the currently chosen wiki type
	 */
	private String getPropsPath() {
		String type = (String) this.wikitypes.getSelectedItem();
		String path = this.dir + "/" + "converter." + type + ".properties";
		return path;
	}

	/**
	 * @return the current feedback
	 */
	public Feedback getFeedback() {
		return this.feedback;
	}

	/** 
	 * cancels the conversion
	 * @see com.atlassian.uwc.ui.listeners.FeedbackCanceller#cancel()
	 */
	public void cancel() {
		log.info("Cancelling Conversion");
		if (this.converter != null)
			this.converter.cancel();
	}
	
	/**
	 * thread that runs the converter
	 */
	public class Worker extends SwingWorker {

		private String propsPath;
		public Worker(String propsPath) {
			this.propsPath = propsPath;
		}
		@Override
		public Object construct() {
			String message = "Problem with converter properties file: " + propsPath;
			try {
				model.convert(propsPath);
			} catch (IOException e) {
				log.error(message);
				feedback = Feedback.NO_CONVERTER_FILE;
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				log.error(message);
				feedback = Feedback.NO_CONVERTER_FILE;
				e.printStackTrace();
			} catch (Exception e) {
				log.error("Unexpected problem while converting: " + e.getMessage());
				feedback = Feedback.UNEXPECTED_ERROR;
				e.printStackTrace();
			} finally {
				displayFinalFeedback(propsPath);
			}
			return null;
		}
		public void cancel() {
			model.cancelConvert();
		}
	}
}
