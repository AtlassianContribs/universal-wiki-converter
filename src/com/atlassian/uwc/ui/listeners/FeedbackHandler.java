package com.atlassian.uwc.ui.listeners;

/**
 * Centralizes info about result/error/feedback messages.
 * Used by any operation the UWC
 * does that might need to give the user feedback.
 */
public interface FeedbackHandler {

	/**
	 * different types of results and errors that can occur
	 * during a UWC operation (conversion, test connections, export, etc) 
	 */
	public enum Feedback {
		NONE,				//nothing's happened yet. No feedback to give
		OK,					//if the action (export, convert) was successful
		NO_EXPORTER_FILE, 	//this wikitype has no matching exporter
		NO_CONVERTER_FILE,	//the converter properties file could not be found 
		BAD_SETTINGS_FILE,	//problem with loading or saving to user settings
		DB_DRIVER_FAILURE, 	//if a given db driver fails
		DB_FAILURE,			//if a given db command fails (connection, sql command, etc.)  
		BAD_EXPORTER_CLASS, //export class cannot be instantiated (is abstract, is primitive, has no available constructor, is not accessible, etc.)
		BAD_CONVERTER_CLASS,//converter class cannot be instantiated
		BAD_FILE,			//some file could not be opened
		BAD_PROPERTY,		//a property from a properties file is malformed
		BAD_SETTING, 		//general problem with a setting
		BAD_LOGIN,			//wrong login
		BAD_PASSWORD,		//wrong password
		BAD_URL,			//wrong confluence url
		BAD_SPACE, 			//wrong confluence space
		USER_NOT_PERMITTED,	//indicates given login does not have create permissions for given address and space
		UNSUPPORTED_FEATURE,//indicates that known unsupported feature was requested and cannot be provided
		BAD_OUTPUT_DIR, 	//bad directory for saving output pages
		REMOTE_API_ERROR,	//problem communicating with the confluence remote api
		API_FORBIDDEN,		//the api returned a 403 error. This probably means the Remote API is off.
		UNEXPECTED_ERROR,	//unexpected error was noticed
		CONVERTER_ERROR,	//an individual converter caused an error 
		UNKNOWN, 			//error type is unknown
		CANCELLED, 			//activity (conversion or export) was cancelled
		NAMESPACE_COLLISION,//namespace collision (like page title case sensitivity) detected
	}
}
