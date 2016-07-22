package com.atlassian.uwc.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import com.atlassian.uwc.exporters.Exporter;
import com.atlassian.uwc.ui.FeedbackWindow;
import com.atlassian.uwc.ui.State;
import com.atlassian.uwc.ui.SwingWorker;
import com.atlassian.uwc.ui.UWCGuiModel;
import com.atlassian.uwc.ui.UWCUserSettings;
import com.atlassian.uwc.util.PropertyFileManager;

/**
 * object that listens for and initiates an export
 */
public class ExportWikiListener extends ExportHandler implements ActionListener, FeedbackHandler, FeedbackCanceller {
	Logger log = Logger.getLogger(this.getClass());
	/**
	 * represents feedback that might be communicated to the user
	 */
	Feedback feedback = Feedback.NONE;
	/**
	 * window that provides the user with feedback
	 */
	private FeedbackWindow feedbackWindow;
	/**
	 * represents the internal state used to represent feedback that will be communicated to the user
	 */
	private State state;
	/**
	 * thread used to run the export
	 */
	private Worker exportThread;
	/**
	 * object that handles the wiki specific details of the export
	 */
	private Exporter exporter;
	
	/**
	 * @param wikitypes the component that will be set to the desired wiki on export
	 * @param model object representing the UWC model. Important for getting settings
	 * @param dir the directory where export.xxx.properties files exist
	 * @param feedbackWindow window where the user feedback will be displayed. 
	 */
	public ExportWikiListener(JComboBox wikitypes, UWCGuiModel model, String dir, FeedbackWindow feedbackWindow) {
		this.wikitypes = wikitypes;
		this.model = model;
		this.dir = dir;
		this.feedbackWindow = feedbackWindow;
	}

	/**
	 * launches the feedback window, and starts the export 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		//launch feedback window if required
		boolean feedbackOn = Boolean.parseBoolean(
				this.model.getSetting(UWCUserSettings.Setting.FEEDBACK_OPTION));
		if (feedbackOn) {
			this.feedbackWindow.launch();
		}
		this.feedbackWindow.updateFeedback(getInitialFeedback());
		
		//set up cancel handling in the feedback window
		this.feedbackWindow.setCurrent(this);
		this.feedbackWindow.cancelOn();
		
		//export
		this.exportThread = attemptExport();
	}

	/**
	 * @return starts the export as a seperate thread
	 */
	private Worker attemptExport() {
		Worker worker = new Worker(this);
		worker.start();
		return worker;
	}

	
	/**
	 * sends the last feedback to the feedback window
	 */
	void displayFinalFeedback() {
		displayFeedback(this.feedback);
	}
	
	/**
	 * sends the given feedback to the feedback window
	 * @param feedback
	 */
	void displayFeedback(Feedback feedback) {
		String description = getFeedbackDescription(feedback);
		getState().updateNote(description);
	}

	/**
	 * @param feedback
	 * @return a String representing the given feedback
	 */
	String getFeedbackDescription(Feedback feedback) {
		String description = "";
		description += "\nExport Status... ";
		if (feedback != Feedback.OK) {
			description += feedback.toString() + "\n";
			description += "Problem exporting wiki: ";
			switch(feedback) {
			case DB_FAILURE:
				description += "\nProblem logging into database. "
					+ "Check that database is available, and "
					+ "that exporter settings are correct: " 
					+ this.getExporterPropsPath(this.getCurrentWikitype());
				break;
			case NO_EXPORTER_FILE:
				description += "Could not find exporter settings at location: "
					+ this.getExporterPropsPath(this.getCurrentWikitype());
				break;
			default:
				description += feedback.toString();
			}
		} else {
			description += "SUCCESS!";
		}
		
		return description + "\n";
	}

	/**
	 * @return sends some initializing feedback to the feedback window
	 */
	private String getInitialFeedback() {
		String description = "";
		description += 
			"\n***************\n\n" +
				"Exporting Wiki...\n";
		return description;
	}

	/**
	 * sets up the feedback window, validates the chosen wiki type,
	 * loads the export properties, and then runs the export.
	 * heavily influenced by ChooseExporterForm.runExporter
	 */
	protected void exportWiki() {
		//setup feedback window - we're going to use max = 100 for the progress bar
		State state = getState();
		
		//validate wiki type
		state.updateNote("Validating Wiki Type.");
		state.updateProgress(10);
		String wikitype = this.getCurrentWikitype();
		String exporterPropPath = getExporterPropsPath(wikitype);
		File exporterConfFile = new File(exporterPropPath);
		if (!exporterConfFile.exists()) {
			String message = "Could not find export properties file: " + exporterPropPath;
			handleLastError(message, Feedback.NO_EXPORTER_FILE);
			return;
		}
		
		//load export properties
		state.updateNote("Loading Export Properties");
		state.updateProgress(10);
		Map exporterProperties = null;
		try {
			exporterProperties = PropertyFileManager.loadPropertiesFile(exporterConfFile.getPath());
		} catch (IOException e) {
			String message = "Could not load exporter properties file: " + exporterPropPath;
			handleLastError(message, Feedback.BAD_SETTINGS_FILE);
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			return;
		}
		assert exporterProperties != null;
		
		//load export class
		state.updateNote("Loading Export Class");
		state.updateProgress(10);
		String exporterClassName = (String) exporterProperties.get("exporter.class");
		Class exporterClass = null;
		try {
			exporterClass = Class.forName(exporterClassName);
		} catch (ClassNotFoundException e) {
			String message = "The exporter class was not found: "+exporterClassName;
			handleLastError(message, Feedback.BAD_EXPORTER_CLASS);
			return;
		}
		
		//exporter
		state.updateNote("Exporting... ");
		state.updateProgress(10);
		feedback = Feedback.OK;
		try {
			this.exporter = (Exporter) exporterClass.newInstance();

			this.exporter.export(exporterProperties);
		} catch (InstantiationException e) {
			feedback = Feedback.BAD_EXPORTER_CLASS;
			handleLastError("", feedback);
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			exportcleanup();
			return;
		} catch (IllegalAccessException e) {
			feedback = Feedback.BAD_EXPORTER_CLASS;
			handleLastError("", feedback);
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			exportcleanup();
			return;
		} catch (ClassNotFoundException e) {
			feedback = Feedback.DB_DRIVER_FAILURE;
			handleLastError("", feedback);
			e.printStackTrace();
			exportcleanup();
			return;
		} catch (SQLException e) {
			feedback = Feedback.DB_FAILURE;
			handleLastError("", feedback);
			e.printStackTrace();
			exportcleanup();
			return;
		} catch (Exception e) {
			feedback = Feedback.BAD_SETTING;
			handleLastError("", feedback);
			e.printStackTrace();
			exportcleanup();
			return;
		}
		state.updateProgress(state.getMax());
		
		exportcleanup();
	}

	/**
	 * @return creates or gets the State object representing the feedback for this export
	 */
	private State getState() {
		if (this.state == null ) {
			State state = new State("Initializing Exporter", 0, 100);
			this.state = state;
			this.feedbackWindow.setState(state);
		}
		return this.state;
	}

	/**
	 * cleans up the feedback window and the export objects
	 */
	private void exportcleanup() {
		this.feedbackWindow.end();
		this.feedbackWindow.cancelOff();
		this.feedbackWindow.setCurrent(null);
		this.exportThread = null;
		this.exporter = null;
	}

	/**
	 * updates the feedback state with the given message and feedback, 
	 * and ends the progress bar.
	 * @param message
	 * @param feedback
	 */
	private void handleLastError(String message, Feedback feedback) {
		this.feedback = feedback;
		log.error(message);
		this.state.updateNote(message);
		this.state.updateProgress(this.state.getMax());
	}

	/**
	 * @param wikitype
	 * @return the filepath for the given wikitype's properties file
	 */
	private String getExporterPropsPath(String wikitype) {
		String exporterPropPath = 
			this.dir +
			File.separator +
			"exporter." + 
			wikitype + 
			".properties";
		return exporterPropPath;
	}

	/**
	 * @return the current feedback
	 */
	protected Feedback getFeedback() {
		return feedback;
	}

	/**
	 * cancel the current export
	 * @see com.atlassian.uwc.ui.listeners.FeedbackCanceller#cancel()
	 */
	public void cancel() {
		log.info("Begin Cancelling Export");
		if (this.exportThread != null) {
			this.exportThread.cancel();
			handleLastError("Export Cancelled. ", Feedback.CANCELLED);
			exportcleanup();
		}
	}
	
	/**
	 * thread that handles the export
	 */
	public class Worker extends SwingWorker {

		private ExportWikiListener listener;
		public Worker(ExportWikiListener listener) {
			this.listener = listener;
		}
		@Override
		public Object construct() {
			try {
				if (listener.isExportable()) {
					exportWiki();
				}
				else {
					feedback = Feedback.NO_EXPORTER_FILE;
					String feedbackDescription = getFeedbackDescription(feedback);
					log.error("Cannot export this wikitype: " + listener.getCurrentWikitype() +
							feedbackDescription);
				}
			} finally {
				//give back final feedback
				displayFinalFeedback();
			}
			return null;
		}
		
		public void cancel() {
			if (exporter != null) {
				exporter.cancel();
			}
		}
	}
}
