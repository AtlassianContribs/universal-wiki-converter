package com.atlassian.uwc.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import biz.artemis.confluence.xmlrpcwrapper.ConfluenceServerSettings;
import biz.artemis.confluence.xmlrpcwrapper.RemoteWikiBroker;
import biz.artemis.confluence.xmlrpcwrapper.SpaceForXmlRpc;

import com.atlassian.uwc.ui.FeedbackWindow;
import com.atlassian.uwc.ui.State;
import com.atlassian.uwc.ui.UWCGuiModel;
import com.atlassian.uwc.ui.UWCUserSettings;
import com.atlassian.uwc.ui.UWCUserSettings.Setting;
import com.atlassian.uwc.ui.xmlrpcwrapperOld.RemoteWikiBrokerOld;

/**
 * tests the user's settings when an event is triggered
 */
public class TestSettingsListener implements ActionListener, FeedbackHandler {

	private static final int SPACEKEY_FIELD_INDEX = 3;
	private static final String ERROR_MESSAGE_BADSPACE = "Either the space does not exist, or the user has no access to that space.\n";
	private static final String SUCCESS_MESSAGE = "SUCCESS.\n";
	public static final String SUCCESS_MESSAGE_LONG = "UWC connected successfully with Confluence.";
	private static final String ERROR_MESSAGE = "Problem with User setting: ";
	private static final String ERROR_PREFIX = "FAILURE:\n";
	private static final String NEW_TEST_INTRO = "Testing Connection Settings... ";
	private static final String NEW_TEST_DELIM = "**********************\n\n";
	private Feedback feedback;
	private Logger log = Logger.getLogger(this.getClass());
	UWCUserSettings settings;
	private UWCGuiModel model;
	private FeedbackWindow feedbackWindow;
	private Vector<JTextField> testables;
	
	/**
	 * @param testables vector of textfields that will contain data to be tested
	 * @param model 
	 * @param feedbackWindow
	 */
	public TestSettingsListener(Vector<JTextField> testables, UWCGuiModel model, FeedbackWindow feedbackWindow) {
		this.testables = testables;
		this.model = model;
		settings = new UWCUserSettings();
		this.feedbackWindow = feedbackWindow;
		getSettings(model);
	}

	/**
	 * populates this object's settings with the login, pass, space, and url from the model
	 * @param model
	 */
	private void getSettings(UWCGuiModel model) {
		settings.setLogin(model.getSetting(UWCUserSettings.Setting.LOGIN));
		settings.setPassword(model.getSetting(UWCUserSettings.Setting.PASSWORD));
		settings.setSpace(model.getSetting(UWCUserSettings.Setting.SPACE));
		settings.setUrl(model.getSetting(UWCUserSettings.Setting.URL));
	}
	
	/**
	 * launches the feedback window,
	 * gets all the settings,
	 * and tests them using the XML-RPC interface,
	 * displays the results in the feedback window
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		// pop up dialog window with connection status, and setup progressbar
		this.feedbackWindow.launch();
		this.feedbackWindow.clearProgressBar();
		State state = new State("", 0, 2);
		this.feedbackWindow.setState(state);
		
		//make sure we've saved everything we're trying to use
		state.updateProgress(); //step 1
		for (JTextField testable : this.testables) {
			ActionListener[] actionListeners = testable.getActionListeners();
			for (ActionListener listener : actionListeners) {
				if (listener instanceof SaveListener) {
					SaveListener savelistener = (SaveListener) listener;
					Setting setting = savelistener.getSetting();
					String value = testable.getText();
					this.model.saveSetting(setting, value);
				}
			}
		}
		
		boolean autoDetection = !this.testables.get(SPACEKEY_FIELD_INDEX).isEnabled();
		
		//test the setting
		ConfluenceServerSettings confSettingsTest = getSettings();
		String text = testConnectionSetting(confSettingsTest, autoDetection);
		state.updateProgress(); //step 2
		this.feedbackWindow.updateFeedback(text);
		
		RemoteWikiBrokerOld.getInstance().needNewLogin();
	}

	/**
	 * tests the given settings with the XML-RPC interface,
	 * and returns the result as a String
	 * @param settings
	 * @return test results
	 */
	public String testConnectionSetting(ConfluenceServerSettings settings) {
		return testConnectionSetting(settings, false);
	}
	/**
	 * tests the given settings with the XML-RPC interface,
	 * and returns the result as a String
	 * @param settings
	 * @param autoDetection if false, spaces will be autodetected
	 * @return test results
	 */
	public String testConnectionSetting(ConfluenceServerSettings settings, boolean autoDetection) {
		RemoteWikiBroker rwb = RemoteWikiBroker.getInstance();
	
		String connection = rwb.checkConnectivity(settings);
		if (autoDetection) {
			return getFeedbackMessage(getFeedback(connection, null, autoDetection), null, this.log, autoDetection);
		}
		
		String permissions = null;
		SpaceForXmlRpc space = null;
		try {
			permissions = rwb.getUserPermissionsForUser(settings);
			space = rwb.getSpace(settings, settings.spaceKey);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) { 
			//ignore this. We'll handle it in the feedback part.
		}
	
		this.feedback = getFeedback(connection, space);
		return getFeedbackMessage(this.feedback, permissions, this.log);
	}
	
	/**
	 * @param settings
	 * @return Feedback for connection test with given settings. Possibilities include:
	 * Feedback.OK;
	 * Feedback.BAD_LOGIN;
	 * Feedback.BAD_PASSWORD;
	 * Feedback.BAD_URL;
	 * Feedback.BAD_SPACE;
	 * Feedback.BAD_SETTING;
	 * Note: This means, permissions problems will still be reported as OK!
	 * To test permissions call the getConnectionFeedbackMessage(settings) method. Resulting
	 */
	public static Feedback getConnectionFeedback(ConfluenceServerSettings settings) {
		return getConnectionFeedback(settings, false);
	}
	
	/**
	 * @param settings
	 * @param autoDetection, if true - check that the space setting is valid. Otherwise ignore
	 * space setting.
	 * @return Feedback for connection test with given settings. Possibilities include:
	 * Feedback.OK;
	 * Feedback.BAD_LOGIN;
	 * Feedback.BAD_PASSWORD;
	 * Feedback.BAD_URL;
	 * Feedback.BAD_SPACE;
	 * Feedback.BAD_SETTING;
	 * Note: This means, permissions problems will still be reported as OK!
	 * To test permissions call the getConnectionFeedbackMessage(settings) method. Resulting
	 */
	public static Feedback getConnectionFeedback(ConfluenceServerSettings settings, boolean autoDetection) {
		RemoteWikiBroker rwb = RemoteWikiBroker.getInstance();
		
		String connection = rwb.checkConnectivity(settings);
		if (autoDetection) {
			return getFeedback(connection, null, null, autoDetection);
		}
		String permissions = null;
		SpaceForXmlRpc space = null;
		try {
			permissions = rwb.getUserPermissionsForUser(settings);
			space = rwb.getSpace(settings, settings.spaceKey);		
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) { 
			//ignore this. We'll handle it in the feedback part.
		}
	
		return getFeedback(connection, permissions, space);
	}
	
	/**
	 * tests the given settings using the XML-RPC interface,
	 * and returns the results as a String 
	 * Note: This is a static varient of testConnectioSetting.
	 * The main difference is no log messages.
	 * @param settings
	 * @return test results
	 */
	public static String getConnectionFeedbackMessage(ConfluenceServerSettings settings) {
		return getConnectionFeedbackMessage(settings, false);
	}
	/**
	 * tests the given settings using the XML-RPC interface,
	 * and returns the results as a String 
	 * Note: This is a static varient of testConnectioSetting.
	 * The main difference is no log messages.
	 * @param settings
	 * @param autoDetection false if spaces will be autodetected
	 * @return test results
	 */
	public static String getConnectionFeedbackMessage(ConfluenceServerSettings settings, boolean autoDetection) {
		RemoteWikiBroker rwb = RemoteWikiBroker.getInstance();
		
		String connection = rwb.checkConnectivity(settings);
		if (autoDetection) {
			return getFeedbackMessage(getFeedback(connection, null, autoDetection), null, null, autoDetection);
		}
		String permissions = null;
		SpaceForXmlRpc space = null;
		try {
			permissions = rwb.getUserPermissionsForUser(settings);
			space = rwb.getSpace(settings, settings.spaceKey);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlRpcException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) { 
			//ignore this. We'll handle it in the feedback part.
		}
		
		Feedback feedback = getFeedback(connection, space);
		
		return getFeedbackMessage(feedback, permissions, null);
	}

	/**
	 * @param feedback reflects feedback from connecting to confluence with xml-rpc
	 * @param permissions reflects state of permissions
	 * @param log if null, no log messages will be generated
	 * @return message reflecting the given feedback and permissions info
	 */
	private static String getFeedbackMessage(Feedback feedback, String permissions, Logger log) {
		return getFeedbackMessage(feedback, permissions, log, false);
	}
	/**
	 * @param feedback reflects feedback from connecting to confluence with xml-rpc
	 * @param permissions reflects state of permissions
	 * @param log if null, no log messages will be generated
	 * @param autoDetection, false if spaces will be autodetected
	 * @return message reflecting the given feedback and permissions info
	 */
	private static String getFeedbackMessage(Feedback feedback, String permissions, Logger log, boolean autoDetection) {
		if (log != null) log.debug("feedback = " + feedback);
		if (log != null) log.debug("permissions = " + permissions);
		String text = NEW_TEST_DELIM;
		text += NEW_TEST_INTRO;
		//feedback is not ok
		if (feedback != Feedback.OK) {
			if (feedback == Feedback.BAD_SPACE){
				text += ERROR_PREFIX +
				ERROR_MESSAGE + Feedback.BAD_SPACE + " or " + 
				Feedback.USER_NOT_PERMITTED + "\n" +
				ERROR_MESSAGE_BADSPACE;
			}
			else if (feedback == Feedback.BAD_SETTING) {
				text += ERROR_PREFIX +
				ERROR_MESSAGE + Feedback.BAD_SETTING + "\nIf you're attempting to " +
						"connect to an SSL protected URL, make sure you've set your " +
						"truststore and password, or set trustall to true.\n" +
						"See Help -> Online Doc -> SSL Support"; 
			}
			else if (feedback == Feedback.API_FORBIDDEN) {
				text += ERROR_PREFIX +
						"The API returned a 403 (Forbidden) error.\n" +
						"Make sure the Remote API is turned on.";
			}
			else {
				text += ERROR_PREFIX +
				ERROR_MESSAGE + feedback + "\n";
				if (log != null) log.error(text);
			} 
		}
		else { //feedback is ok, but there might still be a permissions issue
			if (!autoDetection && !permissions.contains("modify")) { //permissions do not list the word "modify"
				feedback = Feedback.USER_NOT_PERMITTED;
				text += ERROR_PREFIX +
						ERROR_MESSAGE + feedback + "\n"; 
			}
			else {
				text += SUCCESS_MESSAGE;
			}
		}
		return text;
	}

	/**
	 * @return gets the settings from the associated model
	 */
	private ConfluenceServerSettings getSettings() {
		getSettings(this.model);
		ConfluenceServerSettings confSettingsTest = new ConfluenceServerSettings();
		confSettingsTest.login = this.settings.getLogin();
		confSettingsTest.password = this.settings.getPassword();
		confSettingsTest.spaceKey = this.settings.getSpace();
		confSettingsTest.url = this.settings.getUrl(); 
		confSettingsTest.truststore = this.settings.getTruststore();
		confSettingsTest.trustpass = this.settings.getTrustpass();
		confSettingsTest.trustallcerts = this.settings.getTrustall();
		return confSettingsTest;
	}

	/**
	 * figures out feedback for the given connectivity message.
	 * Feedback is kept in the field: feedback
	 * @param message representing string returned by checkConnectivity
	 * @param space representing space returned by getSpace, can be null
	 * @return true, if the feedback is OK; false, if an error occurred
	 */
	private static Feedback getFeedback(String message, SpaceForXmlRpc space) {
		return getFeedback(message, space, false);
	}
	/**
	 * figures out feedback for the given connectivity message.
	 * Feedback is kept in the field: feedback
	 * @param message representing string returned by checkConnectivity
	 * @param space representing space returned by getSpace, can be null
	 * @param autoDetection, false if spaces will be autodetected
	 * @return true, if the feedback is OK; false, if an error occurred
	 */
	private static Feedback getFeedback(String message, SpaceForXmlRpc space, boolean autoDetection) {
			
		if (autoDetection) space = new SpaceForXmlRpc();//make artificial space if autodetecting
		Feedback feedback = Feedback.NONE;
		if (message == RemoteWikiBroker.USER_MESSAGE_CONNECTIVTY_SUCCESS && space != null) {
			feedback = Feedback.OK;
		} else if (message == RemoteWikiBroker.USER_ERROR_WRONG_USERNAME) {
			feedback = Feedback.BAD_LOGIN;
		} else if (message == RemoteWikiBroker.USER_ERROR_WRONG_PASSWORD) {
			feedback = Feedback.BAD_PASSWORD;
		} else if (message == RemoteWikiBroker.USER_ERROR_CANNOT_REACH_SERVER) {
			feedback = Feedback.BAD_URL;
		} else if (message == RemoteWikiBroker.USER_MESSAGE_FORBIDDEN) {
			feedback = Feedback.API_FORBIDDEN;
		} else if (message.startsWith(RemoteWikiBroker.BAD_TRUSTSTORE)) {
			feedback = Feedback.BAD_SETTING;
		} else if (space == null) {
			feedback = Feedback.BAD_SPACE;
		} else {
			feedback = Feedback.BAD_SETTING;
		}
		return feedback;
	}
	
	/**
	 * @param connectionMessage
	 * @param permissionsMessage
	 * @param space
	 * @return the feedback for the given connection , permission, and space results.
	 * will return USER_NOT_PERMITTED for permission problems and non-existant spaces.
	 */
	private static Feedback getFeedback(String connectionMessage, String permissionsMessage, SpaceForXmlRpc space) {
		return getFeedback(connectionMessage, permissionsMessage, space, false);
	}
	/**
	 * @param connectionMessage
	 * @param permissionsMessage
	 * @param space
	 * @param autoDetection, false if spaces will be autodetected
	 * @return the feedback for the given connection , permission, and space results.
	 * will return USER_NOT_PERMITTED for permission problems and non-existant spaces.
	 */
	private static Feedback getFeedback(String connectionMessage, String permissionsMessage, SpaceForXmlRpc space, boolean autoDetection) {
		Feedback feedback = getFeedback(connectionMessage, space, autoDetection);
		String feedbackMessage = getFeedbackMessage(feedback, permissionsMessage, null, autoDetection);
		if (feedback == Feedback.OK && !feedbackMessage.endsWith(SUCCESS_MESSAGE)) {
			return Feedback.USER_NOT_PERMITTED;
		}
		return feedback;
	}
	
	/**
	 * @return the current feedback
	 */
	public Feedback getFeedback() {
		return feedback;
	}
	
}
