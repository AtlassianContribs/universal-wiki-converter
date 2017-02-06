package com.atlassian.uwc.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import biz.artemis.confluence.xmlrpcwrapper.ConfluenceServerSettings;

import com.atlassian.uwc.exporters.Exporter;
import com.atlassian.uwc.ui.listeners.ConvertListener;
import com.atlassian.uwc.ui.listeners.TestSettingsListener;
import com.atlassian.uwc.ui.listeners.FeedbackHandler.Feedback;
import com.atlassian.uwc.util.PropertyFileManager;

public class UWCCommandLineInterface {

	private static final String BASE_USAGE = "Usage: [-v][-h][-c][-e][-t] settings converter pages";
	private static Logger log = Logger.getLogger(UWCCommandLineInterface.class);
	public static void main(String args[])
	{
		PropertyConfigurator.configure("log4j.properties");

		//check that we have at least one arg
		if (args.length < 1) usage();
		
		if (requestVersion(args)) {
			version();
			return;
		}
		else if (requestHelp(args)) {
			extendedHelp();
			return;
		}
		
		//two args for -t or -e
		if (args.length < 2) usage();
		if (requestConnectionTest(args)) {
			testConnection(args[1]);
			return;
		}
		else if (requestExport(args)) {
			export(args[1]);
			return;
		}
		
		//check that we have min three for default (-c)
		if (requestConversion(args)) { //specified -c explictly
			if (args.length < 3) usage(); 
			else if (args.length > 3) convert(args[1], args[2], args[3]); //using the pagesdir from the commandline
			else convert(args[1], args[2], null); //using the pages key from the settings 
			return;
		}
		log.debug("Using default operation -c");
		//else we didn't specify -c, so we're using the default = conversion
		if (args.length < 3) convert(args[0], args[1], null); //using pages key from settings
		else convert(args[0], args[1], args[2]); //using pagesdir from commandline
		
	}

	private static void export(String exporterPropPath) {
		//get exporter properties
		File exporterConfFile = new File(exporterPropPath);
		if (!exporterConfFile.exists()) {
			log.error("Could not find export properties file: " + exporterPropPath);
			System.exit(1);
		}
		Map exporterProperties = null;
		try {
			exporterProperties = PropertyFileManager.loadPropertiesFile(exporterConfFile.getPath());
		} catch (IOException e) {
			e.printStackTrace();  
			log.error("Could not load exporter properties file: " + exporterPropPath);
			System.exit(1);
		}
		
		//create exporter class that will run the export
		String exporterClassName = (String) exporterProperties.get("exporter.class");
		Class exporterClass = null;
		try {
			exporterClass = Class.forName(exporterClassName);
		} catch (ClassNotFoundException e) {
			log.error("The exporter class was not found: "+exporterClassName);
			System.exit(1);
		}
		
		//do the export 
		try {
			Exporter exporter = (Exporter) exporterClass.newInstance();
			exporter.export(exporterProperties);
		} catch (Exception e) {
			log.error("Problem while exporting");
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void testConnection(String settingsPath) {
		UWCUserSettings settings = new UWCUserSettings(settingsPath);
		
		ConfluenceServerSettings confSettings = new ConfluenceServerSettings();
    	confSettings.login = settings.getLogin();
    	confSettings.password = settings.getPassword();
    	confSettings.url = settings.getUrl(); 
    	confSettings.spaceKey = settings.getSpace();
    	confSettings.truststore = settings.getTruststore();
    	confSettings.trustpass = settings.getTrustpass();
    	confSettings.trustallcerts = settings.getTrustall();

    	try {
    		Feedback testConnectionFeedback = TestSettingsListener.getConnectionFeedback(confSettings);
    		if (testConnectionFeedback != Feedback.OK) {
    			String message = TestSettingsListener.getConnectionFeedbackMessage(confSettings);
    			log.error(message);
    			log.info("Test Connection: FAILED");
    		}
    		else {
    			log.info(TestSettingsListener.SUCCESS_MESSAGE_LONG);
    			log.info("Test Connection: SUCCESS");
    		}
    	} catch (Exception e) {
    		String message = TestSettingsListener.getConnectionFeedbackMessage(confSettings);
			log.error(message);	
    		log.info("Test Connection: FAILED");
    	}
	}

	private static void convert(String settingPropertiesFile, String convertPropertiesFile, String pagesDir) {
		ConverterEngine engine=new ConverterEngine();
		try
		{
			//settings
			UWCUserSettings settings = new UWCUserSettings(settingPropertiesFile);
			engine.getState(settings);
			
			//converter properties
			File props = new File(convertPropertiesFile);
			if (!props.exists()) {
				String message = "No property file at that location: " + convertPropertiesFile;
				log.error(message);
				throw new IllegalArgumentException(message);
			}
			
			TreeMap<String,String> converters = null; 
	        converters = PropertyFileManager.loadPropertiesFile(convertPropertiesFile);
	        
	        if (converters == null)
	        	throw new IllegalArgumentException(); //unlikely, as the error handling above should be sufficient

	        Vector<String> converterStrings = new Vector<String>(converters.keySet().size());
			for (String converter : converters.keySet()) {
				String value = converters.get(converter);
				String converterString = converter + "=" + value;
				converterStrings.add(converterString);
			}

			//get the pages
			List<File> files = new Vector<File>();
			if (pagesDir == null) { //use the pages key from settings
				log.debug("Using pages property from: " + settingPropertiesFile);
				UWCGuiModel model = new UWCGuiModel(settings);
				Vector<String> pageNames = model.getPageNames();
				for (String path : pageNames) files.add(new File(path));
			}
			else {
				log.debug("Using pages command line argument: " + pagesDir);
				files.add(new File(pagesDir));
			}
			
			engine.convert(files, converterStrings, settings);
			displayFinalFeedback(engine.getErrors());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(1);
		}
	}

	private static void displayFinalFeedback(ConverterErrors errors) {
		if (errors.hasErrors()) {
			log.info("\nConversion Status... FAILURE. See uwc.log for details.\n" + errors.getAllErrorMessages());
		} else {
			log.info(ConvertListener.getFeedbackDescription(Feedback.OK, ""));
		}
		
	}

	private static boolean requestConnectionTest(String[] args) {
		return "-t".equals(args[0]);
	}

	private static boolean requestVersion(String[] args) {
		return "-v".equals(args[0]);
	}
	
	private static boolean requestHelp(String[] args) {
		return "-h".equals(args[0]);
	}
	private static boolean requestExport(String[] args) {
		return "-e".equals(args[0]);
	}
	private static boolean requestConversion(String[] args) {
		return "-c".equals(args[0]);
	}

	private static void version() {
		System.out.println(UWCForm3.APP_NAME + " " + UWCForm3.VERSION_NUMBER);
	}
	
	private static void extendedHelp() {
		System.out.println(BASE_USAGE);
		System.out.println("Runs the UWC as a command line operation\n" + 
				"OPTIONS:\n" + 
				"-c settings converter pages\n" +
				"\tconverts pages with the given converter and settings.\n" +
				"\tsettings=conf/confluenceSettings.properties or a file containing similar settings. It must set the connections settings: url, space, login, password. Optionally, set pages with :: delimited absoluate paths.\n" +
				"\tconverter=conf/converter.xxx.properties, the file with converters to be run\n" +
				"\tpages=(optional) dir to your pages. If you don't use this setting, provide pages in the settings file.\n" + 
				"-e exporter\n\texports using the given exporter.\n\texporter=conf/exporter.xxx.properties. You must edit your exporter properties file before it will work.\n" + 
				"-h\toutput extended help\n" + 
				"-t settings\n\ttests the connection settings in the given settings file, and outputs your connection status.\n\tsettings-=conf/confluenceSettings.properties or a file containing similar settings.\n" + 
				"-v\toutput the UWC version");
	}
	
	private static void usage() {
		System.out.println(BASE_USAGE);
		System.exit(1);
	}


}
