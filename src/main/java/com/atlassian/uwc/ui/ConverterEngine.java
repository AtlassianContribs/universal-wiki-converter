package com.atlassian.uwc.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import biz.artemis.confluence.xmlrpcwrapper.AttachmentForXmlRpc;
import biz.artemis.confluence.xmlrpcwrapper.BlogForXmlRpc;
import biz.artemis.confluence.xmlrpcwrapper.CommentForXmlRpc;
import biz.artemis.confluence.xmlrpcwrapper.ConfluenceServerSettings;
import biz.artemis.confluence.xmlrpcwrapper.PageForXmlRpc;
import biz.artemis.confluence.xmlrpcwrapper.RemoteWikiBroker;
import biz.artemis.confluence.xmlrpcwrapper.SpaceForXmlRpc;
import biz.artemis.confluence.xmlrpcwrapper.SpaceForXmlRpc.SpaceType;

import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.converters.IllegalLinkNameConverter;
import com.atlassian.uwc.converters.IllegalPageNameConverter;
import com.atlassian.uwc.converters.JavaRegexConverter;
import com.atlassian.uwc.converters.PerlConverter;
import com.atlassian.uwc.converters.RequiresEngineConverter;
import com.atlassian.uwc.converters.twiki.JavaRegexAndTokenizerConverter;
import com.atlassian.uwc.converters.twiki.TWikiRegexConverterCleanerWrapper;
import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlEvents;
import com.atlassian.uwc.filters.FilterChain;
import com.atlassian.uwc.hierarchies.DokuwikiHierarchyTest;
import com.atlassian.uwc.hierarchies.HierarchyBuilder;
import com.atlassian.uwc.hierarchies.HierarchyNode;
import com.atlassian.uwc.splitters.PageSplitter;
import com.atlassian.uwc.ui.listeners.FeedbackHandler;
import com.atlassian.uwc.ui.listeners.TestSettingsListener;

/**
 * This class drives the conversion process by gathering all the files, gathering
 * the selected converters, then applying all the converters against each file, then
 * sending the converted 'Pages' and attachments to Confluence via XML-RPC (or
 * possibly some other method in the future)
 */
public class ConverterEngine implements FeedbackHandler {


	/* START CONSTANTS */
	private static final int NUM_REQ_CONVERTERS = 2;
	private static final String REQUIRED_CONVERTER_ILLEGAL_LINKS = "MyWiki.9999.illegal-links.class=com.atlassian.uwc.converters.IllegalLinkNameConverter";
	private static final String REQUIRED_CONVERTER_ILLEGAL_NAMES = "MyWiki.9999.illegal-names.class=com.atlassian.uwc.converters.IllegalPageNameConverter";
	private static final String NONCONVERTERTYPE_PAGEHISTORYPRESERVATION = "page-history-preservation";
	private static final String NONCONVERTERTYPE_HIERARCHYBUILDER = ".hierarchy-builder";
	private static final String NONCONVERTERTYPE_ILLEGALHANDLING = "illegal-handling";
	private static final String NONCONVERTERTYPE_AUTODETECTSPACEKEYS = "autodetect-spacekeys";
	private static final String NONCONVERTERTYPE_MISCPROPERTIES = ".property";
	private static final String NONCONVERTERTYPE_FILTERS = ".filter";
	private static final String NONCONVERTERTYPE_XMLEVENT = ".xmlevent";
	private static final String CONVERTERTYPE_TWIKICLEANER = ".twiki-cleaner";
	private static final String CONVERTERTYPE_JAVAREGEX = ".java-regex";
	private static final String CONVERTERTYPE_JAVAREGEXTOKEN = ".java-regex-tokenize";
	private static final String CONVERTERTYPE_PERL = ".perl";
	private static final String CONVERTERTYPE_CLASS = ".class";

	private static final String XMLEVENT_PROP_ERROR = "Xmlevent Property must follow this format convention: {tag}xmltag{class}com.something.Class";
	private static final String PROP_ATTACHMENT_SIZE_MAX = "attachment.size.max";
	private static final int DEFAULT_NUM_STEPS = 1000;
	private static final String ORPHAN_ATTACHMENTS_PAGE_TITLE="Orphan attachments";
	private static final String DEFAULT_ATTACHMENT_UPLOAD_COMMENT = "Added by UWC, the Universal Wiki Converter";
	public static final String PROPKEY_ENGINE_SAVES_TO_DISK = "engine-saves-to-disk";	
	private static final String PROPKEY_SPACEPERMS = "spaceperms";

	/* START FIELDS */
	public boolean running = false; //Methods check this to see if the conversion needs to be cancelled

	/**
	 * used to disable check for illegal names and links. 
	 * We want to allow users to override this so they can handle it themselves
	 * with converters. 
	 */
	private boolean illegalHandlingEnabled = false; //default = false, as of Confluence 4.2 doesn't appear to be necessary
	private boolean autoDetectSpacekeys = false; //default = false
	private HashSet<String> attachedFiles;//attachmentids
	private HashSet<String> attachedPaths;//attachment file paths

	Logger log = Logger.getLogger(this.getClass());
	// this logger is used to write out totals for the UWC to a seperate file uwc-totals.log
	Logger totalsFileLog = Logger.getLogger("totalsFileLog");
	Logger attachmentLog = Logger.getLogger("attachmentsLog");

	/**
	 * The string that directory separators (e.g., / on Unix and \ on Windows) are replaced
	 * with in page titles.
	 * This is used by DokuWikiLinkConverter too.
	 */
	public static final String CONFLUENCE_SEPARATOR = " -- ";

	protected enum HierarchyHandler {
		DEFAULT, 			//no hierarchy handling
		HIERARCHY_BUILDER,	//hierarchyBuilder handles 
		PAGENAME_HIERARCHIES//hierarchy maintained in pagename
	}
	private HierarchyHandler hierarchyHandler = HierarchyHandler.DEFAULT; 

	/**
	 * The mapping from file name extension to mime type that is used when sending
	 * attachments to Confluence.
	 * NOTE: static so that other files can get access to this easily.
	 */
	private static MimetypesFileTypeMap mimeTypes;

	/**
	 * This is the location of the mime type mapping file. For details on the file format,
	 * refer to the link below.
	 *
	 * @see javax.activation.MimetypesFileTypeMap
	 */
	public final static String mimetypeFileLoc = "conf" + File.separator + "mime.types";

	/**
	 * This field is set if a hierarchy builder "converter" is used. The field controls the
	 * way in which pages are added/updated in Confluence. If hierarchyBuilder is <code>null</code>, all
	 * pages are added as top-level pages in the selected space. Otherwise, the hierarchy builder is
	 * called on to create a page hierarchy, and the engine will insert the pages correspondingly.
	 */
	private HierarchyBuilder hierarchyBuilder = null;
	private UWCUserSettings settings;
	private State state;
	private Properties miscProperties = new Properties(); //instantiate this here - UWC-293
	private Set<String> filterValues;

	/**
	 * the number of properties that are not converters from the properties file. 
	 * When we set up the progress bar, we calculate the max number of steps
	 * we're going to encounter with the number of properties that could be converters.
	 * But we update the progress bar a step only foreach converter property. So, we'll use
	 * this field to update the progress bar the extra amount.
	 */
	private int numNonConverterProperties;
	private Feedback feedback;

	private int newNodes;

	HashMap<String, Converter> converterCacheMap = new HashMap<String, Converter>();
	private long startTotalConvertTime;

	//Error handlers
	private ConverterErrors errors = new ConverterErrors();
	private boolean hadConverterErrors;
	private HashMap<String, String> homepages = new HashMap<String, String>();

	/* START CONSTRUCTORS */

	/**
	 * This default constructor initializes the mime types.
	 */
	public ConverterEngine() {
		try {
			mimeTypes = new MimetypesFileTypeMap(new FileInputStream(mimetypeFileLoc));
		} catch (FileNotFoundException e) {
			String note = "Couldn't load mime types!";
			log.error(note, e);
			this.errors.addError(Feedback.BAD_SETTINGS_FILE, note, false);
		}
		totalsFileLog.setAdditivity(false);
	}

	/* START METHODS */

	/**
	 * converts the files with the converterstrings, and hooks any feedback into the given ui
	 * @param inputPages pages from the filesystem to be converted
	 * @param converterStrings list of converters as strings which will be run on the pages
	 * @param sendToConfluence true if the pages should be uploaded to confluence
	 * @param wikitype The wiki type that's being converted into Confluence, ex: Mediawiki
	 */
	public void convert(List<File> inputPages, List<String> converterStrings, UWCUserSettings settings) {
		//setup
		this.running = true;
		resetFeedback();
		resetErrorHandlers();
		resetHierarchy();

		//settings
		boolean sendToConfluence = Boolean.parseBoolean(settings.getSendToConfluence());
		this.settings = settings;
		if (!this.running) {
			this.feedback = Feedback.CANCELLED;
			return;
		}

		//convert
		convert(inputPages, converterStrings, sendToConfluence, settings.getPattern());

		//cleanup
		if (this.feedback == Feedback.NONE)
			this.feedback = Feedback.OK;
		this.running = false;
	}

	/**
	 * cancels the conversion
	 */
	public void cancel() {
		String message = "Engine - Sending Cancel Signal";
		log.debug(message);
		this.state.updateNote(message);
		this.running = false;
	}

	/**
	 * gets a new State object.
	 * The State object will be used by the converter engine to measure progress.
	 * @param inputPages list of pages 
	 * @param converterStrings list of converters
	 * @param settings settings
	 * @return the state object the engine uses
	 */
	public State getState(List<File> inputPages, List<String> converterStrings, UWCUserSettings settings) {
		//The inputPages and converterString objects do not approximate the number of steps well because
		//inputPages includes directories which are counted as 1 object in the inputPages list, and
		//converterStrings includes non-converter properties. So, for now we'll use a default value to set up
		//the state.
		//    	int steps = 
		//    		getNumberOfSteps(
		//    				inputPages, 
		//    				converterStrings, 
		//    				Boolean.parseBoolean(settings.getSendToConfluence())
		//    				);
		return getState(settings);
	}

	/**
	 * gets a new State object, using a default number of steps.
	 * The State object will be used by the converter engine to measure progress.
	 * @param settings
	 * @return
	 */
	public State getState(UWCUserSettings settings) {
		int steps = DEFAULT_NUM_STEPS;
		String initialMessage = "Converting Wiki\n" +
				"Wikitype = " + settings.getWikitype() + "\n";
		this.state = new State(initialMessage, 0, steps);
		return state;
	}

	/**
	 * gets the number of steps for the given pages and converters lists
	 * @param pages list of objects representing pages. Can be any object: String, Page, etc.
	 * @param converters list of objects representing pages. Can be any object: String, Converter, etc.
	 * @param sendToConfluence true, if the conversion will upload the objects to confluence
	 * @return the number of steps that the engine will measure progress against
	 */
	protected int getNumberOfSteps(List pages, List converters, boolean sendToConfluence) {
		return getNumberOfSteps(pages.size(), converters.size(), sendToConfluence);
	}

	/**
	 * Counts the number of steps needed to do the entire conversion from start to finish.
	 * Here are the steps:
	 * 1. init converters - number of converters
	 * 2. create page objects - number of pages
	 * 3. convert the files - num of converterstrings * num of pages (or num of pages)
	 * 4. convert with required converters - num of pages
	 * 5. save pages - num of pages
	 * 6. upload pages (if send to confluence) num of pages, or 0
	 * @param pages number of pages
	 * @param converters number of converters
	 * @param sendToConfluence true if sending pages to confluence
	 * @return number of steps for performing conversion from start to finish
	 */
	private int getNumberOfSteps(int pages, int converters, boolean sendToConfluence) {
		return getNumberOfSteps(pages, converters, converters, sendToConfluence);
	}

	/**
	 * Counts the number of steps needed to do an entire conversion from start to finish.
	 * Used with progress monitor
	 * @param pages number of pages
	 * @param properties number of all converter file properties (including non-converter properties)
	 * @param converters number of converters
	 * @param sendToConfluence true if sending pages to Confluence
	 * @return number of steps for performing conversion from start to finish
	 */
	private int getNumberOfSteps(int pages, int properties, int converters, boolean sendToConfluence) {
		return getNumberOfSteps(pages, pages, properties, converters, sendToConfluence);
	}

	/**
	 * Counts the number of steps needed to do an entire conversion from start to finish.
	 * Used with progress monitor
	 * @param files number of files or directories chosen by the user. Does not count contents of directories seperately. 
	 * @param pages number of individual pages that will be converted
	 * @param properties number of all converter file properties (including non-converter properties)
	 * @param converters number of converters
	 * @param sendToConfluence true if sending pages to Confluence
	 * @return number of steps for performing conversion from start to finish
	 */
	private int getNumberOfSteps(int files, int pages, int properties, int converters, boolean sendToConfluence) {
		int numReqConverters = isIllegalHandlingEnabled()?NUM_REQ_CONVERTERS:0;
		int steps = 
				properties + 					//1. initialize converters (handles both converter and non-converter properties)
				files + 						//2. create page objects (uses the original list of chosen file objects)
				(converters * pages) +			//3. convert the files (uses the number of page objects)
				(numReqConverters) +			//4. create required converters (2, right now)
				(numReqConverters * pages) + 	//5. convert with required converters (2, right now)
				pages + 						//6. save the files
				(sendToConfluence?pages:0);		//7. upload pages if sendToConfluence
		return steps;
	}

	/**
	 * converts the given pages using the given converterStrings, and sends the pages
	 * to Confluence, if sendToConfluence is true.
	 * @param pages
	 * @param converterStrings
	 * @param sendToConfluence
	 */
	public void convert(List<File> pages, List<String> converterStrings, boolean sendToConfluence) {
		convert(pages, converterStrings, sendToConfluence, "");
	}

	/**
	 * converts the given pages not filtered out with the given filterPattern
	 * using the given converterStrings, and sends the pages to Confluence, 
	 * if sendToConfluence is true
	 * @param pages
	 * @param converterStrings
	 * @param sendToConfluence 
	 * @param filterPattern ignores files with this filter pattern
	 */
	public void convert(List<File> pages, List<String> converterStrings, boolean sendToConfluence, String filterPattern) {
		log.info("Starting conversion.");

		initConversion();

		//create converters
		ArrayList<Converter> converters = createConverters(converterStrings);

		//create page objects - Recurse through directories, adding all files
		FileFilter filter = createFilter(filterPattern);
		List<Page> allPages = createPages(filter, pages);

		//fix progressbar max, which is dependent on the previous two lists
		int steps = getNumberOfSteps(pages.size(), allPages.size(), converterStrings.size(), converters.size(), sendToConfluence);
		this.state.updateMax(steps);


		//convert the files
		if (convertPages(allPages, converters)) {
			//in case converting the pages disqualified some pages, we need to break if there are no pages left
			if (allPages.size() < 1) {
				String message = "All pages submitted were disqualified for various reasons. Could not complete conversion.";
				log.warn(message);
				this.errors.addError(Feedback.CONVERTER_ERROR, message, true);
				this.state.updateMax(this.state.getStep()); //complete progress bar, prematurely
				return;
			}
			//in case converting the pages disqualified some pages, we need to recompute progressbarmax
			steps = getNumberOfSteps(pages.size(), allPages.size(), converterStrings.size(), converters.size(), sendToConfluence);
			if (steps != this.state.getMax()) this.state.updateMax(steps);

			// do final required conversions. This step is seperate, due to state saving issues
			convertWithRequiredConverters(allPages);

			//save pages if engine-saves-to-disk property is true. Useful for debugging.
			//We are making this opt-in because users that don't need it will get a speed boost with fewer disk calls
			if (Boolean.parseBoolean(this.miscProperties.getProperty(PROPKEY_ENGINE_SAVES_TO_DISK, "false")))
				savePages(allPages, filterPattern);
			else log.debug("Engine Saves To Disk setting turned off.");

			//handling page histories and not sorting on create
			if (isHandlingPageHistories() && 
					!(isPageHistorySortOnCreate())) {
				allPages = sortByHistory(allPages);
			}

			if (hierarchyHandler == HierarchyHandler.HIERARCHY_BUILDER && hierarchyBuilder != null) {
				//tell the hierarchy builder about the page histories framework
				//do this here so that we're sure the page histories properties are set
				if (hierarchyBuilder.getProperties() != null) { 
					hierarchyBuilder.getProperties().setProperty("switch."+NONCONVERTERTYPE_PAGEHISTORYPRESERVATION, isHandlingPageHistories()+"");
					if (getPageHistorySuffix() != null)	
						hierarchyBuilder.getProperties().setProperty("suffix."+NONCONVERTERTYPE_PAGEHISTORYPRESERVATION, getPageHistorySuffix());
				}
				//tell the hierarchy some other information
				if (hierarchyBuilder.getProperties() != null) {
					hierarchyBuilder.getProperties().setProperty("spacekey", settings.getSpace());
				}
				//build the hierarchy
				HierarchyNode root = hierarchyBuilder.buildHierarchy(allPages);
				int currenttotal = root.countDescendants()-1; //-1 for the null root;
				log.debug("number of nodes in the hierarchy = " + root.countDescendants());
				//upload pages, if the user approves
				if (sendToConfluence && this.running) { //check here so that hierarchy can impact collisions without upload
					if (Boolean.parseBoolean(this.miscProperties.getProperty("onlyorphans", "false"))) {
						log.debug("Orphan attachments only.");
						noteAttachments(root);
					} 
					else {
						writeHierarchy(root, currenttotal, settings.getSpace());
					}
					handleOrphanAttachments();
				}
				else if (!sendToConfluence){
					log.debug("Send To Confluence setting turned off. --> Not uploading pages.");
				}
			} else { //no hierarchy
				if (sendToConfluence && this.running) {//check here so that hierarchy can impact collisions without upload
					writePages(allPages, settings.getSpace());
					handleOrphanAttachments();
				}
				else if (!sendToConfluence){
					log.debug("Send To Confluence setting turned off. --> Not uploading pages.");
				}
			}

			//check for namespace collisions and emit errors if found
			//(after hierarchy has had a chance to make changes)
			listCollisions(allPages);
			clearAttachedFileList();
		}	
		log.info("Conversion Complete");
	}

	private void noteAttachments(HierarchyNode root) {
		if (root.getPage() != null) {
			log.debug("ORPHANDEBUG node: " + root.getPage().getName());
			for (Attachment attachment : root.getPage().getAllAttachmentData()) {
				alreadyAttached(root.getPage(), attachment.getFile());
				if (root.getPage().getAncestors() != null) {
					for (VersionPage ancestor : root.getPage().getAncestors()) {
						for (Attachment ancAtt : ancestor.getAllAttachmentData()) {
							log.debug("VERSION ORPHANDEBUG: " + ancestor.getName() + ": " + ancAtt.getFile().getName());
							alreadyAttached(ancestor, ancAtt.getFile());
						}
					}
				}
			}
		}
		if (!root.getChildren().isEmpty()) {
			for (HierarchyNode child : root.getChildren()) {
				noteAttachments(child);
			}
		}

	}

	protected boolean isPageHistorySortOnCreate() {
		return Boolean.parseBoolean(this.miscProperties.getProperty("page-history-sortoncreate", "true"));
	}

	/**
	 * handle any cleanup
	 */
	protected void initConversion() {
		this.miscProperties.clear();
	}

	/**
	 * Instantiate all the converterStrings
	 *
	 * @param converterStrings a list of converter strings of the form "key=value"
	 * @return a list of converters
	 */
	public ArrayList<Converter> createConverters(List<String> converterStrings) {
		return createConverters(converterStrings, true);
	}
	public ArrayList<Converter> createConverters(List<String> converterStrings, boolean runningState) {
		String message = "Initializing Converters...";
		if (runningState) this.state.updateNote(message);
		log.info(message);

		new DefaultXmlEvents().clearAll(); 	//everytime this method is called, have a clean slate of events

		ArrayList<Converter> converters = new ArrayList<Converter>(); 
		this.numNonConverterProperties = 0; 
		for (String converterStr : converterStrings) {
			if (runningState) this.state.updateProgress();
			if (runningState && !this.running) {
				this.feedback = Feedback.CANCELLED;
				return null;
			}
			Converter converter;
			if (isNonConverterProperty(converterStr)) {
				this.numNonConverterProperties++;
				handleNonConverterProperty(converterStr);
				continue;
			} 
			converter = getConverterFromString(converterStr);
			if (converter == null) {
				continue;
			}
			converters.add(converter);
		}
		if (runningState) addDefaultMiscProperties();

		return converters;
	}

	/**
	 * coverts pages with required converters
	 * @param pages
	 */
	protected void convertWithRequiredConverters(List<Page> pages) {
		if (isIllegalHandlingEnabled()) {
			//create pagename converter and convert with it
			String pagenameConvStr = REQUIRED_CONVERTER_ILLEGAL_NAMES;
			ArrayList<Converter> converters = createOneConverter(pagenameConvStr);
			convertPages(pages, converters, "Checking for illegal pagenames.");

			//create linkname converter and convert with it
			String illegallinksConvStr = REQUIRED_CONVERTER_ILLEGAL_LINKS;
			converters = createOneConverter(illegallinksConvStr);
			convertPages(pages, converters, "Checking for links to illegal pagenames.");
		} 
	}

	/**
	 * converts the list of pages with the given converter
	 * @param pages list of pages to be converted
	 * @param useUI set this to false if you do not want the associated GUI elements
	 * to be updated. This is useful for unit testing.
	 */
	protected void convertWithRequiredConverters(List<Page> pages, boolean useUI) {
		//XXX used only by the junit tests

		//create pagename converter and convert with it
		String pagenameConvStr = REQUIRED_CONVERTER_ILLEGAL_NAMES;
		ArrayList<Converter> converters = createOneConverter(pagenameConvStr);
		convertPages(pages, converters, "Checking for illegal pagenames.");

		//get the state hashtable
		IllegalPageNameConverter  pagenameConverter = (IllegalPageNameConverter) converters.remove(0);
		HashSet<String> illegalNames =  pagenameConverter.getIllegalPagenames();

		//create linkname converter and convert with it
		String illegallinksConvStr = REQUIRED_CONVERTER_ILLEGAL_LINKS;
		converters = createOneConverter(illegallinksConvStr);
		IllegalLinkNameConverter linknameConverter = (IllegalLinkNameConverter) converters.get(0);
		linknameConverter.setIllegalPagenames(illegalNames);
		convertPages(pages, converters, "Checking for links to illegal pagenames.");
	}

	/**
	 * creates the arraylist of converters when only one converter is needed
	 * @param converterString string representing the converter. Should be in property format. Example:<br/>
	 * key=value
	 * @return arraylist with one converter as its sole item
	 */
	protected ArrayList<Converter> createOneConverter(String converterString) {
		ArrayList<String> converterStrings = new ArrayList<String>();
		converterStrings.add(converterString);
		ArrayList<Converter> converters = createConverters(converterStrings);
		return converters;
	}

	/**
	 * Instantiates a converter from a correctly formatted String.
	 * <p/>
	 * Note: This method is now only called once per converter -- first all converters
	 * are created, then all pages, then all converters are run on all pages.
	 *
	 * @param converterStr A string of the form "name.keyword=parameters". The
	 *  keyword is used to create the correct type of converter, and the parameters
	 *  are then passed to the converter. Finally, the "name.keyword" part is set as
	 *  the key in the converter, mainly for debugging purposes. 
	 * @return converter or null if no converter can be parsed/instantiated
	 */
	public Converter getConverterFromString(String converterStr) {
		Converter converter;
		int equalLoc = converterStr.indexOf("=");
		String key = converterStr.substring(0, equalLoc);
		String value = converterStr.substring(equalLoc + 1);
		try {
			if (key.indexOf(CONVERTERTYPE_CLASS) >= 0) {
				converter = getConverterClassFromCache(value);
			} else if (key.indexOf(CONVERTERTYPE_PERL) >= 0) {
				converter = PerlConverter.getPerlConverter(value);
				converter.setValue(value);
			} else if (key.indexOf(CONVERTERTYPE_JAVAREGEXTOKEN) >= 0) {
				converter = JavaRegexAndTokenizerConverter.getConverter(value);
				converter.setValue(value);
			} else if (key.indexOf(CONVERTERTYPE_JAVAREGEX) >= 0) {
				converter = JavaRegexConverter.getConverter(value);
				converter.setValue(value);
			} else if (key.indexOf(CONVERTERTYPE_TWIKICLEANER) >= 0) {
				//converter = getConverterClassFromCache(value);
				converter = TWikiRegexConverterCleanerWrapper.getTWikiRegexConverterCleanerWrapper(value);
				converter.setValue(value);
			} else { 
				String note = "Converter ignored -- name pattern not recognized: " + key;
				this.errors.addError(Feedback.BAD_PROPERTY, note, true);
				log.error(note);
				return null;
			}
			converter.setProperties(this.miscProperties);
			if (converter instanceof RequiresEngineConverter) {
				((RequiresEngineConverter) converter).setEngine(this);
			}
		} catch (ClassNotFoundException e) {
			this.errors.addError(Feedback.BAD_CONVERTER_CLASS, "Converter ignored -- the Java class " + value + " was not found", true);
			return null;
		} catch (IllegalAccessException e) {
			this.errors.addError(Feedback.BAD_CONVERTER_CLASS, "Converter ignored -- there was a problem creating a converter object", true);
			return null;
		} catch (InstantiationException e) {
			this.errors.addError(Feedback.BAD_CONVERTER_CLASS, "Converter ignored -- there was a problem creating the Java class " + value, true);
			return null;
		} catch (ClassCastException e) {
			this.errors.addError(Feedback.BAD_CONVERTER_CLASS, "Converter ignored -- the Java class " + value +
					" must implement the " + Converter.class.getName() + " interface!", true);
			return null;
		}
		converter.setKey(key);
		return converter;
	}

	/**
	 * handles necessary state changes for expected properties 
	 * that were set in the converter properties file.
	 * expected nonconverter properties include hierarchy builder properties
	 * and page history preservation properties
	 * @param converterStr should be a line from the converter properties file
	 * Example:
	 * MyWiki.0001.someproperty.somepropertytype=setting
	 * <br/>
	 * where somepropertytype is an expected property type:
	 * <br/>
	 * NONCONVERTERTYPE_HIERARCHYBUILDER or NONCONVERTERTYPE_PAGEHISTORYPRESERVATION
	 * or NONCONVERTERTYPE_ILLEGALHANDLING
	 * or NONCONVERTERTYPE_AUTODETECTSPACEKEYS
	 * or NONCONVERTERTYPE_FILTERS
	 * or NONCONVERTERTYPE_MISCPROPERTIES
	 */
	protected void handleNonConverterProperty(String converterStr) {
		int equalLoc = converterStr.indexOf("=");
		String key = converterStr.substring(0, equalLoc);
		String value = converterStr.substring(equalLoc + 1);
		String parent = "";
		try {
			if (key.indexOf(NONCONVERTERTYPE_HIERARCHYBUILDER) >= 0) {
				if (isHierarchySwitch(key))
					setHierarchyHandler(value);
				else {
					parent = HierarchyBuilder.class.getName(); 
					Class c;
					c = Class.forName(value);
					HierarchyBuilder hierarchy = (HierarchyBuilder) c.newInstance();
					hierarchyBuilder = hierarchy;
					this.hierarchyBuilder.setProperties(this.miscProperties);
				}
			}
			else if (key.endsWith(NONCONVERTERTYPE_PAGEHISTORYPRESERVATION)) {
				handlePageHistoryProperty(key, value);
			}
			else if (key.endsWith(NONCONVERTERTYPE_ILLEGALHANDLING)) {
				handleIllegalHandling(key, value);
			}
			else if (key.endsWith(NONCONVERTERTYPE_AUTODETECTSPACEKEYS)) {
				handleAutoDetectSpacekeys(key, value);
			}
			else if (key.endsWith(NONCONVERTERTYPE_MISCPROPERTIES)) {
				handleMiscellaneousProperties(key, value);
			}
			else if (key.endsWith(NONCONVERTERTYPE_FILTERS)) {
				parent = FileFilter.class.getName();
				handleFilters(key, value);
			}
			else if (key.endsWith(NONCONVERTERTYPE_XMLEVENT)) {
				handleXmlEvents(key, value);
			}
		} catch (ClassNotFoundException e) { 
			String message = "Property ignored -- the Java class " + value + " was not found";
			log.error(message);
			this.errors.addError(Feedback.BAD_PROPERTY, message, true);
		} catch (IllegalAccessException e) {
			String message = "Property ignored -- there was a problem creating the Java class: " + value +
					".\n" +
					"Note: A necessary method's permissions were too restrictive. Check the constructor. ";
			log.error(message);
			this.errors.addError(Feedback.BAD_PROPERTY, message, true);
		} catch (InstantiationException e) {
			String message = "Property ignored -- there was a problem creating the Java class " + value +
					".\n" +
					"Note: The class cannot be instantiated as it is abstract or is an interface.";
			log.error(message);
			this.errors.addError(Feedback.BAD_PROPERTY, message, true);
		} catch (ClassCastException e) { 
			String message = "Property ignored -- the Java class " + value +
					" must implement the " + parent + " interface!";
			log.error(message);
			this.errors.addError(Feedback.BAD_PROPERTY, message, true);
		} catch (IllegalArgumentException e) {
			String message = "Property ignored -- property value was not in expected format.";
			log.error(message);
			this.errors.addError(Feedback.BAD_PROPERTY, message, true);
		}
	}

	/**
	 * at long last making some performance enhancements
	 * here we are creating an object cache which should help a bit
	 *
	 * @param key A string representing the converter (actually the part after the
	 *        equals sign of the converter string).
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private Converter getConverterClassFromCache(String key) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		Converter converter = converterCacheMap.get(key);
		if (converter == null) {
			Class c = Class.forName(key);
			converter = (Converter) c.newInstance();
			converterCacheMap.put(key, converter);
		}
		return converter;
	}

	/**
	 * creates file filter.
	 * If we have no filter values, returns null.
	 * If we have at least one filter value, uses the FilterChain class
	 * to create FileFilter that will handle all of the filter requirements.
	 * There are two types of supported filters: Class filters, and endswith filters.
	 * Class filters are fully resolved class names for classes that implement FileFilter.
	 * Endswith filters are text strings that the end of the filename must conform to.
	 * If there are more than one filter invoked, the following will be used to resolve 
	 * which files to accept: Only pages that all class filters accept as long as 
	 * any endswith filter accepts as well will be included. (Class filters are ANDed. 
	 * Endswith filters are ORed.) Example: If you had two endswiths, and a class: ".txt",
	 * ".xml", and NoSvnFilter, then .txt and .xml files that the NoSvnFilter accepts 
	 * will be included.
	 * @return FileFilter or null
	 */
	protected FileFilter createFilter(final String pattern) {
		Set<String> values = getFilterValues();
		if (pattern != null && !"".equals(pattern)) 
			values.add(pattern);

		if (values.isEmpty()) return null;

		FilterChain chain = new FilterChain(values, this.miscProperties);
		return chain.getFilter();
	}

	/**
	 * Creates PageForXmlRpcOld objects for all the files in inputPages.
	 *
	 * @param filter file filter to be used to filter input pages, or null, if no such filter should be used
	 * @param inputPages   A list of files and directories that Pages should be created for.
	 * @return A list of PageForXmlRpcOld objects for all files matching the pattern in the settings.
	 */
	protected List<Page> createPages(FileFilter filter, List<File> inputPages) {
		String message = "Initializing Pages...";
		this.state.updateNote(message);
		log.info(message);

		List<Page> allPages = new LinkedList<Page>();

		for (File fileOrDir : inputPages) {
			this.state.updateProgress();
			List<Page> pages = recurse(fileOrDir, filter);
			setupPages(fileOrDir, pages);
			allPages.addAll(pages);
		}

		log.debug("Number of page inputs (all): " + allPages.size());
		if (isHandlingPageHistories() && isPageHistorySortOnCreate()) {
			List<Page> sorted = sortByHistory(allPages);
			log.debug("Number of page inputs (sorted): " + sorted.size());
			return sorted;
		}
		return allPages;
	}

	/**
	 * Recurses through a directory structure and adds all files in it matching the filter.
	 * Called by createPages.
	 *
	 * @param fileOrDir A directory or file. Must not be <code>null</code>.
	 * @param filter    the filter to use when selecting files
	 * @return A list with PageForXmlRpcOld objects for all the matching files in the directory and its subdirectories
	 */
	private List<Page> recurse(File fileOrDir, FileFilter filter) {
		assert fileOrDir != null;
		List<Page> result = new LinkedList<Page>();
		if (fileOrDir.isFile()) {									//it's a file AND
			if (filter == null || filter.accept(fileOrDir)) {		//there's no filter OR the filter accepts the file
				PageSplitter splitter = getPageSplitter();
				if (splitter == null)
					result.add(new Page(fileOrDir));
				else 
					result.addAll(splitter.split(fileOrDir));
			}
			else 
				log.debug("Filtering out filename: " + fileOrDir.getName());
		} else if (fileOrDir.isDirectory()) {
			File[] files = fileOrDir.listFiles(filter);
			for (File file : files) {
				result.addAll(recurse(file, filter));
			}
		}
		else { //some other problem
			String message = "Could not find file: '" +
					fileOrDir.getAbsolutePath() +
					"'.\n" +
					"Check existence and permissions.";
			log.warn(message);
			this.errors.addError(Feedback.BAD_FILE, message, true);
		}
		return result;
	}

	private PageSplitter getPageSplitter() {
		String classname = this.miscProperties.getProperty("pagesplitter", null);
		if (classname == null) return null;
		Class c;
		try {
			c = Class.forName(classname);
		} catch (ClassNotFoundException e) {
			log.error("Could not find pagesplitter class named: " + classname, e);
			return null;
		}
		try {
			PageSplitter splitter = (PageSplitter) c.newInstance();
			return splitter;
		} catch (InstantiationException e) {
			log.error("Could not instantiate pagesplitter class named: " + classname, e);
		} catch (IllegalAccessException e) {
			log.error("Pagesplitter class can not legally be accessed: " + classname, e);
		}
		return null;
	}

	/**
	 * Set the names of the pages and performs any other setup needed. Called by recurse().
	 * If the user selected a directory and this file is inside it, the base directory's
	 * path is removed and the rest is used as the page name.
	 * <p/>
	 * Any directory separators are replaced with the constant CONFLUENCE_SEPARATOR.
	 *
	 * @param baseDir The directory that the top-level documents are in
	 * @param pages A list of pages to set up
	 */
	protected void setupPages(File baseDir, List<Page> pages) {
		String basepath = baseDir.getParentFile().getPath() + File.separator;
		int baselength = basepath.length();

		for (Page page : pages) {
			String pagePath = page.getFile().getPath();
			String pageName = getPagename(pagePath.substring(baselength));
			log.debug("New page: " + pageName + " -> " + pagePath);
			//Strip the file name from the path.
			String path = getPath(pagePath);
			page.setPath(path);
			page.setName(pageName);
			if (isHandlingPageHistoriesFromFilename()) preserveHistory(page, pageName);
		}
	}

	/**
	 * figures out path var for Page based on complete path to page's file
	 * @param pagePath
	 * @return
	 */
	private String getPath(String pagePath) {
		int fileNameStart = pagePath.lastIndexOf(File.separator);
		if (fileNameStart >= 0) {
			pagePath = pagePath.substring(0, fileNameStart);
		} else {
			pagePath = "";
		}
		return pagePath;
	}

	/**
	 * uses the filename to set the version and name of the given page
	 * so that the history is preserved in the conversion. Note:
	 * uses the pageHistorySuffix which is set by the handlePageHistoryProperty 
	 * method
	 * @param page object that will be changed to reflect pagename and version of given filename 
	 * @param filename should use the pageHistorySuffix to indicate version and pagename:
	 * <br/>
	 * if pageHistorySuffix is -#.txt
	 * <br/>
	 * then filename should be something like: pagename-2.txt
	 * @return Page with changed name and version
	 * Will return passed page with no changes if:
	 * <ul>
	 * <li>suffix is null</li>
	 * <li> suffix has no numerical indicator (#)</li>
	 * </ul>
	 */
	protected Page preserveHistory(Page page, String filename) {
		if (loadOnAncestors()) {
			addAncestors(page);
			if (!page.getAncestors().isEmpty()) {
				page.setVersion(page.getLatestVersion()+1);
				log.debug("Current page version: " + page.getVersion());
			}
			return page;
		}
		return identifyHistoryOnePage(page, filename);
	}

	public Page identifyHistoryOnePage(Page page, String filename) {
		//get suffix
		String suffix = getPageHistorySuffix(); 
		if (suffix == null) {
			log.error("Error attempting to preserve history: Page history suffix is Null.");
			return page;
		}
		//create regex for filename based on the suffix
		Matcher hashFinder = hashPattern.matcher(suffix);
		String suffixReplaceRegex = "";
		if (hashFinder.find()) {
			suffixReplaceRegex = hashFinder.replaceAll("(\\\\d+)");
			suffixReplaceRegex = "(.*)" + suffixReplaceRegex;
		} 
		else {
			log.error("Error attempting to preserve history: Suffix is invalid. Must contain '#'.");
			return page;
		}
		//get the version and name
		Pattern suffixReplacePattern = Pattern.compile(suffixReplaceRegex);
		Matcher suffixReplacer = suffixReplacePattern.matcher(filename);
		if (suffixReplacer.find()) {
			String pagename = suffixReplacer.group(1);
			String versionString = suffixReplacer.group(2);
			page.setName(pagename); //set name before version so latestversion data is properly set in Page
			if (Boolean.parseBoolean(this.miscProperties.getProperty("page-history-sortwithtimestamp", "false"))) 
				page.setTimestamp(new Date(Long.parseLong(versionString)*1000));
			else
				page.setVersion(Integer.parseInt(versionString));
		}
		return page;
	}

	/* Page History Load on Ancestors methods - START */
	private boolean loadOnAncestors() {
		return Boolean.parseBoolean(this.miscProperties.getProperty("page-history-load-as-ancestors", "false"));
	}

	private void addAncestors(Page page) {
		String ancestorDir = this.miscProperties.getProperty("page-history-load-as-ancestors-dir", null);
		if (ancestorDir == null) {
			log.warn("page-history-load-as-ancestors-dir must be set. Cannot add ancestors.");
			return;
		}
		String relPath = getPageRelativePath(page);
		if (!ancestorDir.endsWith(File.separator) && !relPath.startsWith(File.separator)) 
			ancestorDir += File.separator;
		String ancestorPath = ancestorDir + relPath;
		File dir = new File(ancestorPath);
		File[] allFiles = dir.listFiles();
		for (File file : allFiles) {
			String filename = file.getName();
			Page newPage = new VersionPage(file);
			newPage.setParent(page); 
			newPage = identifyHistoryOnePage(newPage, filename);
			if (newPage.getName() == null) continue;
			if (newPage.getName().equalsIgnoreCase(page.getName().replaceFirst("[.][^.]+$", ""))) {
				newPage.setName(page.getName()); //we need them to have the same name for latestversion to work
				log.debug("Found ancestor page: " + newPage.getFile().getPath());
				newPage.setPath(getPath(newPage.getFile().getPath()));
				page.addAncestor((VersionPage) newPage);
			}
		}
		Collections.sort(page.getAncestors());
		if (!page.getAncestors().isEmpty() &&
				Boolean.parseBoolean(this.miscProperties.getProperty("page-history-sortwithtimestamp", "false"))) {
			if (Boolean.parseBoolean(this.miscProperties.getProperty("page-history-load-as-ancestors-lastiscurrent", "false"))) {
				page.getAncestors().remove(page.getAncestors().lastElement());//remove the last ancestor if its the same as current
			}
			for (int i = 1; i < page.getAncestors().size(); i++) {
				VersionPage version = page.getAncestors().get(i);
				version.setVersion(version.getLatestVersion()+1);
			}
			page.setSortWithTimestamp(true); //affects sorting of collections of pages (including hierarchies)
		}
		if (this.miscProperties.containsKey("page-history-maxversions")) { //useful for debugging
			String maxString = this.miscProperties.getProperty("page-history-maxversions", null);
			int max = Integer.parseInt(maxString);
			if (max <= page.getAncestors().size()) {
				log.debug("number of ancestors: " + page.getAncestors().size());
				int actmax = page.getAncestors().size();
				for (int i = actmax-1; i >=max; i--) {
					page.getAncestors().remove(i);
				}
				log.debug("after limiting, number of ancestors: " + page.getAncestors().size());
			}
		}

	}

	protected String getPageRelativePath(Page page) {
		String ignorable = this.miscProperties.getProperty("filepath-hierarchy-ignorable-ancestors", "");
		String full = page.getPath();
		if (full == null) return null;
		return full.replaceAll("\\Q"+ignorable + "\\E", "");
	}
	/* Page History Load on Ancestors methods - END */


	/**
	 * gets the pagename given the pagepath
	 * @param pagePath
	 * @return pagename
	 */
	protected String getPagename(String pagePath) {
		String pageName = "";
		if (hierarchyHandler == HierarchyHandler.DEFAULT ||
				hierarchyHandler == HierarchyHandler.HIERARCHY_BUILDER) {
			pageName = pagePath.substring(pagePath.lastIndexOf(File.separator) + 1);
		} else if (hierarchyHandler == HierarchyHandler.PAGENAME_HIERARCHIES) {
			String quotedSeparator = Pattern.quote(File.separator);
			pageName = pagePath.replaceAll(quotedSeparator, CONFLUENCE_SEPARATOR);
		}
		return pageName;
	}

	/**
	 * converts the given pages with the given converts
	 * @param pages
	 * @param converters
	 * @return true if conversion of all pages succeeded
	 */
	protected boolean convertPages(List pages, List<Converter> converters) {
		return convertPages(pages, converters, "Converting pages...");
	}

	/**
	 * converts the given pages with the given converters
	 * @param pages
	 * @param converters
	 * @param note, message for the progress monitor
	 * @return true if conversion of all pages succeeded
	 */
	protected boolean convertPages(List<Page> pages, List<Converter> converters, String note) {
		boolean result = true;
		this.state.updateNote(note);
		log.info(note);

		this.startTotalConvertTime = (new Date()).getTime();
		//go through each page
		for (Iterator<Page> iter = pages.iterator(); iter.hasNext();) {
			if (!this.running) {
				this.feedback = Feedback.CANCELLED;
				return false;
			}

			Page page = iter.next();

			//some bookkeeping
			long startTimeStamp = conversionBookkeepingNextPage(page);

			//get the file's contents

			if (page.getOriginalText() == null || "".equals(page.getOriginalText())) {
				File file = getFileContents(page);
				if (file == null) {
					iter.remove(); //get rid of this page from the iterator.
					continue;
				}
			} //else we used a PageSplitter to set the original text, so we can go straight to conversion

			//convert the page
			convertPage(converters, page);
			//more bookkeeping
			conversionBookkeepingEndThisPage(startTimeStamp);
			if (!this.running) {
				this.feedback = Feedback.CANCELLED;
				return false;
			}
			if (page.getAncestors() != null && !page.getAncestors().isEmpty()) {
				convertPages(page.getAncestors(), converters);
			}
		}
		//still more bookkeeping
		conversionBookkeepingEndAll(pages, converters);
		return result;

	}

	/**
	 * make some log entries about the time it took to convert a page
	 * @param startTimeStamp
	 * @return
	 */
	private long conversionBookkeepingEndThisPage(long startTimeStamp) {
		long stopTimeStamp = ((new Date()).getTime());
		log.info("                   time to convert " + (stopTimeStamp - startTimeStamp) + "ms");
		return stopTimeStamp;
	}

	/**
	 * make some log entries regarding the length of time it took to do the entire conversion
	 * @param pages
	 * @param converters
	 */
	private void conversionBookkeepingEndAll(List<Page> pages, List<Converter> converters) {
		long endTotalConvertTime = (new Date()).getTime();
		long totalTimeToConvert = (endTotalConvertTime - startTotalConvertTime)/1000;        
		String baseMessage = "::: total time to convert files: "+ totalTimeToConvert+ " seconds.";
		log.info(baseMessage);
		String message = baseMessage +
				"For " +
				pages.size() +
				" pages and using " +
				converters.size() +
				" converters.";
		totalsFileLog.info(message);
	}

	/**
	 * update the progress monitor and write some log entries for this page
	 * @param page
	 * @return
	 */
	private long conversionBookkeepingNextPage(Page page) {
		long startTimeStamp = ((new Date()).getTime());
		log.info("-------------------------------------");
		log.info("converting page file: " + page.getName());
		if (page.getFile() != null && page.getFile().getName() != null)
			log.debug("original file name: " + page.getFile().getName());
		return startTimeStamp;
	}

	/**
	 * get the file of the given page 
	 * @param page file for this page
	 * @return the file, or return null if not possible
	 */
	private File getFileContents(Page page) {
		File file = page.getFile();
		if (file == null) {
			if (page.getOriginalText() != null && !"".equals(page.getOriginalText())) {
				log.warn("This appears to be a unit test. Continue as for Unit Test.");
				String path = page.getPath();
				if (path == null) path = "";
				file = new File(path);
			}
			else {
				log.warn("No file was set for page " + page.getName() + ". Skipping page.");
				return null;
			}
		}
		else if (page.getOriginalText() == null){
			try {
				String pageContents = "";
				if (isGzip() && page instanceof VersionPage) {
					pageContents = getGzipText(file);
				}
				else pageContents = getAsciiText(file);
				page.setOriginalText(pageContents);
			} catch (IOException e) {
				String message = "Could not read file " + file.getAbsolutePath() + ".\n" +
						"Check existence and permissions.";
				log.error(message);
				this.errors.addError(Feedback.BAD_FILE, message, true);
				return null;
			}

			// Save the true source since the original will get modified in convert.
			page.setUnchangedSource(page.getOriginalText());
		}
		return file;
	}

	private boolean isGzip() {
		return Boolean.parseBoolean(this.miscProperties.getProperty("page-history-load-as-ancestors-isgzip", "false"));
	}

	private String getGzipText(File file) throws IOException {
		if (changingEncoding()) {
			log.error("Changing Encoding from Gzip file is not supported yet! Can't change encoding");
		}
		return FileUtils.readGzipFile(file);
	}

	public String getAsciiText(File file) throws IOException,
	UnsupportedEncodingException {
		String pageContents;
		if (changingEncoding()) {
			String encoding = getEncoding();
			byte[] pagebytes = FileUtils.getBytesFromFile(file);
			try {
				pageContents = new String(pagebytes, encoding);
			} catch (UnsupportedEncodingException e) {
				String baseerror = "Could not encode file with encoding: " + encoding + ".";
				log.error(baseerror + " Using utf-8.");
				this.errors.addError(Feedback.BAD_SETTING, baseerror, true);
				pageContents = new String(pagebytes, "utf-8");
			}
		}
		else pageContents = FileUtils.readTextFile(file);
		return pageContents;
	}

	private boolean changingEncoding() {
		if (this.miscProperties != null)
			return this.miscProperties.containsKey("encoding"); 
		return false;
	}

	private String getEncoding() {
		if (this.miscProperties != null)
			return this.miscProperties.getProperty("encoding", "utf-8");
		return "utf-8";
	}


	/**
	 * converts one page with the given converters
	 * @param converters list of converters
	 * @param page page object 
	 */
	protected Page convertPage(List<Converter> converters, Page page) {
		if (page.getConvertedText() == null)
			page.setConvertedText(page.getOriginalText()); //in case empty converter list

		for (Converter converter : converters) {
			try {
				this.state.updateProgress();
				if (this.settings != null) {
					converter.setAttachmentDirectory(this.settings.getAttachmentDirectory());
				}
				else {
					//for backwards compatibility with v2
					ConfluenceSettingsForm confSettings = UWCForm2.getInstance().getConfluenceSettingsForm();
					converter.setAttachmentDirectory(confSettings.getAttachmentDirectory());
				}
				converter.convert(page);
				// Need to reset originalText here because each converted expects
				// to start with the result of previous conversions.
				page.setOriginalText(page.getConvertedText());
			} catch (Exception e) {
				String note = "Exception thrown by converter " + converter.getKey() +
						" on page " + page.getName() + ". Continuing with next converter.";
				log.error(note, e);
				this.errors.addError(Feedback.CONVERTER_ERROR, note, true);
			}
			if (converter.getErrors().hasErrors()) {
				this.hadConverterErrors = true;
				this.state.updateNote(converter.getErrors().getFeedbackWindowErrorMessages());
			}
		}

		return page;
	}

	/**
	 * Write pages to disk. They are saved to the directory output/output below the
	 * current working directory.
	 *
	 * @param pages The pages to save
	 * @param pattern This file name extension is appended
	 *        to each page name to create the file name.
	 */
	private void savePages(List<Page> pages, String pattern) {
		String message = "Saving Pages to Filesystem";
		this.state.updateNote(message);
		log.info(message);

		FileUtils.createOutputDirIfNeeded();

		String outputDirName = UWCGuiModel.getOutputDir();
		log.debug("Output Directory = " + outputDirName);

		File outputDir = new File(outputDirName);
		if (!outputDir.exists() && !outputDir.mkdir()) {
			String dirfailMessage = "Directory creation failed for directory " + outputDirName;
			log.error(Feedback.BAD_OUTPUT_DIR + ": " + dirfailMessage);
			this.errors.addError(Feedback.BAD_OUTPUT_DIR, dirfailMessage, true);
		}

		for (Page page : pages) {
			if (!this.running) {
				this.feedback = Feedback.CANCELLED;
				return;
			}
			this.state.updateProgress();
			String outputFileLoc = outputDirName + File.separator + page.getName() + pattern;
			FileUtils.writeFile(page.getConvertedText(), outputFileLoc);
		}
	}

	protected Vector listCollisions(List<Page> pages) {
		Vector<String> collisions = new Vector<String>();
		//check to see if "off" property is present
		if (this.miscProperties != null && 
				this.miscProperties.containsKey("list-collisions") &&
				!Boolean.parseBoolean(this.miscProperties.getProperty("list-collisions"))) {
			log.debug("Namespace Collisions Feature turned off.");
			return collisions;
		}
		//sort
		Vector<Page> sorted = new Vector<Page>();
		sorted.addAll(pages);
		AsciiVersionComparator version = new AsciiVersionComparator();
		Collections.sort(sorted, version);
		//look for collisions
		Page last = new Page(null);
		last.setName("");
		last.setPath("");
		for (int i = 1; i < sorted.size(); i++) {
			Page page1 = (Page) sorted.get(i-1);
			Page page2 = (Page) sorted.get(i);
			log.debug("Checking for collisions: " + page1.getName() + " and " + page2.getName());
			String collision = "";
			//if each page lower cased is the same
			if (colliding(page1, page2)) {
				if (getCollisionComparisonString(page1).equals(getCollisionComparisonString(last))) { //already have one for this name
					String latestPath = getPagePath(page2);
					String current = collisions.remove(collisions.size()-1);
					collision = current + ", " + latestPath + page2.getName();
				}
				else { //starting here
					collision = getPagePath(page1) + page1.getName() + ", " +
							getPagePath(page2) + page2.getName();
				}
				collisions.add(collision);
				last = page1;
				String error = "Potential namespace collision detected for pages: " + collision;
				this.getErrors().addError(Feedback.NAMESPACE_COLLISION, 
						error, 
						true);
				this.log.error(error);
			}
		}
		return collisions;
	}

	protected boolean colliding(Page page1, Page page2) {
		boolean name = getCollisionComparisonString(page1).equals(getCollisionComparisonString(page2));
		boolean version = page1.getVersion() == page2.getVersion();

		boolean space = false;
		if (page1.getSpacekey() != null) { 
			space = page1.getSpacekey().equals(page2.getSpacekey());
		}
		else if (page2.getSpacekey() != null) {
			space = page2.getSpacekey().equals(page1.getSpacekey());
		}
		else if (page1.getSpacekey() == null && page2.getSpacekey() == null) 
			space = true;

		boolean path = !getPagePath(page1).equals(getPagePath(page2));
		return name
				&& version //and same page history version
				&& space// and same space
				&& path; // but not the same path
	}

	/**
	 * @param page
	 * @return the namespace collision comparison string. Either
	 * the pagename, lower cased, or if we're using the auto detect spacekeys
	 * feature, then the page page + the pagename, all lower cased.
	 */
	protected String getCollisionComparisonString(Page page) {
		if (this.autoDetectSpacekeys)
			return (getPagePath(page) + page.getName()).toLowerCase();
		return page.getName().toLowerCase();
	}

	/**
	 * @param page
	 * @return the page's path, including an ending file seperator if one isn't
	 * already there
	 */
	private String getPagePath(Page page) {
		return (page.getPath().endsWith(File.separator)?
				page.getPath():
					page.getPath()+File.separator);
	}

	/**
	 * Writes the pages to Confluence. If the process takes more than three seconds,
	 * a progress monitor will be displayed so that the user can see that something is
	 * indeed happening.
	 *
	 * @param pages The pages to output.
	 * @param spacekey space to which the pages will be written
	 */
	protected void writePages(List pages, String spacekey) {
		writePages(pages, spacekey, true);
	}
	protected void writePages(List pages, String spacekey, boolean logging) {
		if (logging) {
			String note = "Uploading Pages to Confluence...";
			this.state.updateNote(note);
			log.info(note);
		}

		int numUploaded = 0;
		List<Page> casted = (List<Page>) pages;
		// at last, write the pages to Confluence!
		for (Page page : casted) {
			this.state.updateProgress();
			if (!this.running) {
				this.feedback = Feedback.CANCELLED;
				return;
			}
			if (sendPage(page, null, this.settings) == null) continue;
			numUploaded++;
			if (logging && (numUploaded % 10) == 0) {
				String message = "Uploaded " + numUploaded + 
						" out of " + pages.size() + 
						" page"+ (numUploaded==1?"":"s") +
						".";
				this.state.updateNote(message);
				log.info(message);
			}
		}

		if (logging) {
			String message = "Uploaded " + numUploaded + 
					" out of " + pages.size() + 
					" page"+ (numUploaded==1?"":"s") +
					".";
			this.state.updateNote(message);
			log.info(message);
		}
	}

	protected void clearAttachedFileList() {
		//attachedFiles is cleared so that if we do another conversion
		//without closing the UWC, it won't think the attachment has already been
		//attached
		log.debug("Clearing Attached Filelist for next run.");
		this.attachedFiles = null;
		this.attachedPaths = null;
	}

	Pattern uploadedPattern = Pattern.compile("Attachment Uploaded: (.*)");
	public void handleOrphanAttachments() {
		if (this.settings.getUploadOrphanAttachments().equalsIgnoreCase("true"))
		{
			if (this.miscProperties.containsKey("attachments-uploaded-file")) {
				identifyPreviouslyAttachedPaths(this.miscProperties.getProperty("attachments-uploaded-file"));
			}
			ArrayList <File> orphanAttachments=findOrphanAttachments(this.settings.getAttachmentDirectory());
			uploadOrphanAttachments(orphanAttachments);
		}
	}

	protected void identifyPreviouslyAttachedPaths(String uploadedpath) {
		if (uploadedpath != null && !"".equals(uploadedpath)) {
			File uploadedFile = new File(uploadedpath);
			if (uploadedFile.exists()) {
				try {
					String uploaded = FileUtils.readTextFile(uploadedFile);
					Matcher fileFinder = uploadedPattern.matcher(uploaded);
					while (fileFinder.find()) {
						String alreadyFound = fileFinder.group(1);
						if (this.attachedPaths == null) this.attachedPaths = new HashSet<String>();
						this.attachedPaths.add(alreadyFound);
					}
				} catch (IOException e) {
					log.error("Could not open: " + uploadedpath, e);
				}
			}
		}
	}

	//for unit testing purposes
	protected void addAttachedPath(String path) {
		if (this.attachedPaths == null) this.attachedPaths = new HashSet<String>();
		this.attachedPaths.add(path);
	}

	//for unit testing purposes
	protected void clearAttachedPath() {
		this.attachedPaths = null;
	}


	/******
	 * to find all orphan attachment files
	 * @param dirName
	 * @return
	 */
	protected ArrayList<File> findOrphanAttachments(String dirName)
	{
		ArrayList<File> orphanAttachments=new ArrayList<File>();
		File directory=new File(dirName);
		for (File file : directory.listFiles()) {
			if (file.isDirectory())
				orphanAttachments.addAll(findOrphanAttachments(file.getAbsolutePath()));
			else if (file.isFile())
			{
				if (orphanAlreadyAttached(file))
					continue;  
				log.debug("Found orphan attachment: " + file.getAbsolutePath());
				orphanAttachments.add(file);
			}
		}

		return orphanAttachments;


	}
	/**
	 * to upload the orphan attachment files to wiki.
	 */
	protected void uploadOrphanAttachments(ArrayList<File> orphanAttachments)
	{
		if (orphanAttachments == null || orphanAttachments.size() == 0)
			return;

		Hashtable pageTable = new Hashtable();
		pageTable.put("content", "");
		pageTable.put("title", ORPHAN_ATTACHMENTS_PAGE_TITLE); 

		//create ConfluenceServerSettings object
		ConfluenceServerSettings confSettings = new ConfluenceServerSettings();
		confSettings.login = settings.getLogin();
		confSettings.password = settings.getPassword();
		confSettings.url = settings.getUrl(); 
		confSettings.spaceKey = settings.getSpace();
		confSettings.truststore = settings.getTruststore();
		confSettings.trustpass = settings.getTrustpass();
		confSettings.trustallcerts = settings.getTrustall();

		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();	
		//check for problems with settings 
		checkConfluenceSettings(confSettings);
		//send page
		String pageId = sendPage(broker, pageTable, confSettings);

		int total=orphanAttachments.size();
		int count=0;
		for (File file : orphanAttachments) {
			sendAttachment(file, broker, pageId, confSettings);
			count++;
			if ((count % 10) == 0) {
				String message = "Uploaded " + count + 
						" out of " + total + " orphan attachments.";
				this.state.updateNote(message);
				log.info(message);
			}
		}

		String message = "Uploaded " + total + " orphan attachments.";
		this.state.updateNote(message);
		log.info(message);

	}

	/****
	 * send an attachment file to wiki.
	 * 
	 * @param file
	 * @param broker
	 * @param pageId
	 * @param confSettings
	 * @return the attachment object we sent. used by junit tests
	 */
	protected AttachmentForXmlRpc sendAttachment(File file, RemoteWikiBroker broker, String pageId, ConfluenceServerSettings confSettings) {
		return sendAttachment(new Attachment(file), broker, pageId, confSettings);
	}
	protected AttachmentForXmlRpc sendAttachment(Attachment attachment, RemoteWikiBroker broker, String pageId, ConfluenceServerSettings confSettings)
	{
		File file = attachment.getFile();
		AttachmentForXmlRpc attachmentRpc = new AttachmentForXmlRpc();
		if (tooBig(file) || doesNotExist(file)) 
			return null;
		attachmentRpc.setFileName(attachment.getName()); 
		attachmentRpc.setFileLocation(file.getAbsolutePath());
		attachmentRpc.setContentType(determineContentType(file)); //XXX Note: if the filename is different from the file, the content type determining might be foiled
		attachmentRpc.setComment(attachment.getComment() == null?getAttachmentUploadComment():attachment.getComment());
		String errorMessage = "Couldn't send attachmentRpc " +
				file.getAbsolutePath() + ". Skipping attachmentRpc.";
		if (usingWebdav()) {
			String webdavPath = getWebdavPath();
			sendAttachmentWebdav(broker, pageId, confSettings, attachmentRpc, webdavPath, errorMessage);
		}
		else 
			sendAttachmentRemoteAPI(broker, pageId, confSettings, attachmentRpc, errorMessage);
		attachmentLog.info("Attachment Uploaded: " + file.getAbsolutePath());
		return attachmentRpc;//for junit tests
	}

	private String getAttachmentUploadComment() {
		if (this.miscProperties != null &&
				this.miscProperties.containsKey("attachment-upload-comment")) {
			String comment = this.miscProperties.getProperty("attachment-upload-comment");
			if (comment == null) return DEFAULT_ATTACHMENT_UPLOAD_COMMENT;
			return comment;
		}
		return DEFAULT_ATTACHMENT_UPLOAD_COMMENT;
	}

	protected boolean usingWebdav() {
		if (this.miscProperties != null && 
				this.miscProperties.containsKey("attachments-use-webdav")) {
			return Boolean.parseBoolean(
					this.miscProperties.getProperty("attachments-use-webdav", "false"));
		}
		return false;
	}

	protected String getWebdavPath() {
		if (this.miscProperties != null && 
				this.miscProperties.containsKey("webdav-path")) {
			return this.miscProperties.getProperty("webdav-path", RemoteWikiBroker.WEBDAV_PATH_EARLY);
		}
		return RemoteWikiBroker.WEBDAV_PATH_EARLY;
	}

	private void sendAttachmentWebdav(RemoteWikiBroker broker, String pageId, 
			ConfluenceServerSettings confSettings, 
			AttachmentForXmlRpc attachment, String basepath, 
			String errorMessage) {
		try {
			Map pagesByIdMap = broker.getAllServerPagesMapById(confSettings, confSettings.spaceKey);
			String webdavPath = broker.getWebDAVPagePath(confSettings.url, confSettings.spaceKey, pageId, pagesByIdMap, basepath);
			broker.sendFileViaWebDAV(attachment.getFileLocation(), webdavPath, confSettings.login, confSettings.password);
		} catch (IOException e) {
			log.error(Feedback.BAD_FILE + ": " + errorMessage, e);
			this.errors.addError(Feedback.BAD_FILE, errorMessage, true);
		} catch (XmlRpcException e) {
			log.error(Feedback.REMOTE_API_ERROR + ": " + errorMessage, e);
			this.errors.addError(Feedback.REMOTE_API_ERROR, errorMessage, true);
			if (Pattern.matches(".*?You do not have the permissions.*", e.getMessage())) {
				String noPermissionsError = "User '" + confSettings.login + 
						"' " +
						"does not have permission to attach files to space '" + 
						confSettings.spaceKey +
						"'.";
				log.debug(Feedback.USER_NOT_PERMITTED + ": " + noPermissionsError);
				this.errors.addError(Feedback.USER_NOT_PERMITTED, noPermissionsError, true);
			}
		}

	}

	private void sendAttachmentRemoteAPI(RemoteWikiBroker broker, String pageId, ConfluenceServerSettings confSettings, AttachmentForXmlRpc attachment, String errorMessage) {
		try {
			broker.storeAttachment(confSettings, pageId, attachment);
		} catch (IOException e) {
			log.error(Feedback.BAD_FILE + ": " + errorMessage, e);
			this.errors.addError(Feedback.BAD_FILE, errorMessage, true);
		} catch (XmlRpcException e) {
			log.error(Feedback.REMOTE_API_ERROR + ": " + errorMessage, e);
			this.errors.addError(Feedback.REMOTE_API_ERROR, errorMessage, true);
			if (Pattern.matches(".*?You do not have the permissions.*", e.getMessage())) {
				String noPermissionsError = "User '" + confSettings.login + 
						"' " +
						"does not have permission to attach files to space '" + 
						confSettings.spaceKey +
						"'.";
				log.debug(Feedback.USER_NOT_PERMITTED + ": " + noPermissionsError);
				this.errors.addError(Feedback.USER_NOT_PERMITTED, noPermissionsError, true);
			}
		}
	}
	/**
	 * Writes a hierarchy of pages to Confluence. The empty nodes (those with page=null) will
	 * not be written if there already exists a page in Confluence. If there is no page at the
	 * corresponding place in Confluence, an empty page will be created.
	 *
	 * Like writePages(), this method will show a progress bar if the hierarchy takes more than
	 * a few seconds to send to Confluence.
	 *
	 * @param root The root of the hierarchy. Note: The root node itself will <strong>NOT</strong> be
	 *        added to Confluence. All it's children will be added as top-level pages in the space.
	 * @param maxProgress used by logging and status messages
	 * @param spacekey space to which the pages will be written
	 */
	protected void writeHierarchy(HierarchyNode root, int maxProgress, String spacekey) {
		String message = "Uploading Pages to Confluence...";
		this.state.updateNote(message);
		log.info(message);

		int progressNum = 0;
		this.newNodes = 0; //this has to be a field, because we're already returning something; 
		// at last write the pages to Confluence!
		for (HierarchyNode topLevelPage : root.getChildren()) {
			log.debug("writeHierarchy: toplevelpage = " + topLevelPage.getName());
			log.debug("number of children this toplevelpage has = " + topLevelPage.getChildren().size());
			progressNum = writeHierarchy(topLevelPage, null, progressNum, maxProgress, spacekey);
			if (!this.running) {
				this.feedback = Feedback.CANCELLED;
				return;
			}
		}
	}

	/**
	 * This is the recursive part of <code>writeHierarchy</code>. Don't call this directly!
	 * Call writeHierarchy(root) instead.
	 *
	 * @param node The current node in the hierarchy
	 * @param parentId The Confluence "page ID" of the parent page
	 * @param progress The number of pages that have been converted so far
	 *        (used to keep the progress monitor updated)
	 * @param spacekey space to which the pages will be written
	 * @return The number of pages converted after this node and all its descendants have been added.
	 */
	private int writeHierarchy(
			HierarchyNode node, 
			String parentId, 
			int progress, 
			int maxProgress, 
			String spacekey) {
		if (!this.running) {
			this.feedback = Feedback.CANCELLED;
			return progress;
		}
		// First upload the page contained in this node
		Page page = node.getPage();
		// create missing nodes - like a directory that didn't have a corresponding page
		if (page == null) {
			// This node is a "placeholder" because there are pages further down the hierarchy but
			// for some reason this node was not included in the conversion. Create an empty page.
			// Note that this page will only be sent to Confluence if there was no page in place before.
			page = new Page(null);
			page.setName(node.getName());
			page.setOriginalText("");
			page.setConvertedText("");
			page.setPath(node.getName()); //needed by auto-detect spacekeys feature

			String message = "Page '" + page.getName() + "' does not exist. Creating it now.";
			log.info(message);
			this.state.updateNote(message);

			this.newNodes++;
			this.state.updateMax(this.state.getMax() + 1);
		}

		//upload the page
		String myId = sendPage(page, parentId, this.settings);

		//some bookkeeping
		progress++;
		logProgressMessage(progress, maxProgress);

		// Then recursively upload all the node's descendants
		for (HierarchyNode child : node.getChildren()) {
			progress = writeHierarchy(child, myId, progress, maxProgress, spacekey);
			if (!this.running) {
				this.feedback = Feedback.CANCELLED;
				return progress;
			}
		}

		return progress;
	}

	/**
	 * sends upload progress messages to the feedback window and the log 
	 * @param current current number of uploaded pages
	 * @param max max number of uploaded pages
	 */
	private void logProgressMessage(int current, int max) {
		this.state.updateProgress();
		String message = "Uploaded " + current + " out of " + (max + this.newNodes) + " pages.";

		//more visible note if current is divisible by 10 or last page
		if ((current % 10) == 0 || current == (max + this.newNodes)) { 
			this.state.updateNote(message);
			log.info(message);
		}
		else { //less visible note for everything else
			log.debug(message);
		}
	}

	/**
	 * sends page, using settings from  the given settings
	 * @param page
	 * @param parentId
	 * @return page id
	 * @throws IllegalArgumentException if a confluenceSetting is invalid
	 */
	protected String sendPage(Page page, String parentId, UWCUserSettings settings) {
		//write current page
		//XXX why are we setting these up every page. Most of these are global. 
		//XXX If we set these up earlier in the process, we could do the checkConfluenceSettings call 
		//(currently in the next sendPage) earlier in the process as well
		ConfluenceServerSettings confSettings = getConfluenceServerSettings(settings);

		//check to see if we've assigned a space to the page
		if (page.getSpacekey() != null && !"".equals(page.getSpacekey())) { 
			confSettings.spaceKey = page.getSpacekey();
			String[] spacedata = page.getSpaceData(page.getSpacekey());
			String spacename = (spacedata == null || spacedata.length < 1)?page.getSpacekey():spacedata[0];
			String spacedesc = (spacedata == null || spacedata.length < 2)?"":spacedata[1];
			if (!createSpace(confSettings, spacename, spacedesc, page.isPersonalSpace(), page.getPersonalSpaceUsername())) {
				log.warn("Could not create space '" + confSettings.spaceKey + "' assigned to page '" + page.getName() + "'. " +
						"Using default space from settings.");
				confSettings.spaceKey = settings.getSpace();
			}
		} // check to see if we're automatically detecting spaces based on the file system
		else if (isAutoDetectingSpacekeys()) { 
			confSettings.spaceKey = determineSpaceKey(page);
			if ("".equals(confSettings.spaceKey) || confSettings.spaceKey == null) {
				String error = "Could not find spacekeys. Note: the auto-detect spacekeys" +
						" framework is being used. You must choose directories not individual files" +
						" for conversion.\nCannot upload files to Confluence. Exiting.";
				log.error(error);
				this.errors.addError(Feedback.BAD_SPACE, error, true);
				this.state.updateProgress(this.state.getMax());
				this.running = false;
				return "";
			}
			if (!createSpace(confSettings)) return null;
		} //else otherwise use the default (settings based) spacekey

		return sendPage(page, parentId, confSettings);
	}

	Pattern spacepermPattern = Pattern.compile("[{]groupname[}](.*?)[{]permissions[}](.*)");
	private void updateSpacePermissions(ConfluenceServerSettings confSettings) {
		if (!this.miscProperties.containsKey(PROPKEY_SPACEPERMS)) return;

		String allperms = null;
		String groupname = null;
		Vector<String> perms = new Vector<String>();
		String spaceperms = this.miscProperties.getProperty(PROPKEY_SPACEPERMS);
		boolean addgroup = Boolean.parseBoolean(this.miscProperties.getProperty("spaceperms-addgroup", "true"));
		Matcher permsFinder = spacepermPattern.matcher(spaceperms);
		if (permsFinder.find()) {
			groupname = permsFinder.group(1);
			allperms = permsFinder.group(2); 
			String[] permsArray = allperms.split(",");
			for (String perm : permsArray) {
				perms.add(perm);
			}
		}
		if (groupname != null && !perms.isEmpty()) {
			RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
			try {
				if (addgroup && !broker.hasGroup(confSettings, groupname)) {
					log.info("Adding group: " + groupname);
					broker.addGroup(confSettings, groupname);
				}
				log.debug("Updating permissions...");
				broker.addPermissionsToSpace(confSettings, perms, groupname);
				log.info("Updated permissions for group: " + groupname + " in space " + confSettings.getSpaceKey());
			} catch (Exception e) {
				String message = "Could not update permissions ('"+allperms+"') for groupname: '" + groupname +"'";
				getErrors().addError(Feedback.REMOTE_API_ERROR,
						message,
						true);
				log.error(message,e);
			}
		}
	}

	public ConfluenceServerSettings getConfluenceServerSettings(
			UWCUserSettings settings) {
		ConfluenceServerSettings confSettings = new ConfluenceServerSettings(); 
		confSettings.login = settings.getLogin();
		confSettings.password = settings.getPassword();
		confSettings.url = settings.getUrl(); 
		confSettings.spaceKey = settings.getSpace();
		confSettings.truststore = settings.getTruststore();
		confSettings.trustpass = settings.getTrustpass();
		confSettings.trustallcerts = settings.getTrustall();
		return confSettings;
	}

	/**
	 * creates a space for the spacekey in the given settings, if it
	 * doesn't already exist
	 * @param confSettings
	 * @return false if space could not be created
	 */
	protected boolean createSpace(ConfluenceServerSettings confSettings) {
		String spaceName = confSettings.spaceKey;
		String description = "This space was auto-generated by the UWC.";
		return createSpace(confSettings, spaceName, description, false, null);
	}
	/**
	 * creates a space for the spacekey in the given settings, if it doesn't already exist
	 * @param confSettings 
	 * @param name name of space to be created. will not be used, if space already exists
	 * @param description description of space to be created. will not be used if space already exists
	 * @param isPersonalSpace true if space is personal space
	 * @param personalSpaceUsername should be non-null if isPersonalSpace is true. If this condition is not
	 * maintained, will use spacekey in confSettings instead
	 * @return false if space could not be created
	 */
	protected boolean createSpace(ConfluenceServerSettings confSettings, String name, String description, 
			boolean isPersonalSpace, String personalSpaceUsername) {
		String spacekey = confSettings.spaceKey;
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		SpaceForXmlRpc space = broker.createSpace(spacekey, name, description);
		if (isPersonalSpace) {
			space.setType(SpaceType.PERSONAL);
			space.setUsername(personalSpaceUsername);
		}

		try {
			// Conf 2.x will throw an exception if the space doesn't exist.
			SpaceForXmlRpc space2 = broker.getSpace(confSettings, spacekey); 
			// Conf 3.x will not throw an exception but will return null
			if (space2 == null) { //Confluence 3.x
				String note = "Creating space with spacekey '" + spacekey + "' and name: " + name;
				log.info(note);
				this.state.updateNote(note);
				try {
					broker.addSpace(confSettings, space);
					//at some point in Confluence 4.x, the Home page stopped being set to Home, so let's prestore the homepage id
					Vector newspacepages = broker.getAllServerPageSummaries(confSettings, space.getSpaceKey());
					PageForXmlRpc newhome = (PageForXmlRpc) newspacepages.get(0); //should only be one at this point
					this.homepages.put(space.getSpaceKey(), newhome.getId());
					//check to see if we're setting any permissions
					updateSpacePermissions(confSettings);
				} catch (Exception e) {
					getErrors().addError(Feedback.BAD_LOGIN, 
							"Could not create space: " + spacekey +
							" with login: " + confSettings.login +
							". That login may not have permission to create spaces.", 
							true);
					e.printStackTrace();
					return false;
				}
			}
		} catch (Exception e) { //Confluence 2.x
			try { //exception! So, try adding the space
				String note = "Creating space with spacekey '" + spacekey + "' and name: " + name;
				log.info(note);
				this.state.updateNote(note);
				broker.addSpace(confSettings, space);
			} catch (Exception e1) { //something bad happened.
				getErrors().addError(Feedback.BAD_LOGIN, 
						"Could not create space: " + spacekey +
						" with login: " + confSettings.login +
						". That login may not have permission to create spaces.", 
						true);
				e1.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * determines the correct spacekey for the given page.
	 * Note: used in conjunction with the autoDetectSpacekeys Framework
	 * @param page
	 * @return spacekey
	 */
	protected String determineSpaceKey(Page page) {
		log.debug("determining space key. page = " + page.getName());
		String path = page.getPath();
		log.debug("determining space key. path = " + path);
		String ignore = this.miscProperties.getProperty("auto-detect-ignorable-ancestors", "");
		if (this.miscProperties.containsKey("filepath-hierarchy-ignorable-ancestors") &&
				page.getName().equals(path)) {
			ignore = ""; //turn off the ignore property - this is the top level directory
		}
		if (!"".equals(ignore)) {
			log.debug("Ignoring these ancestors: " + ignore);
			path = page.getFile().getParentFile().getAbsolutePath().
					replaceFirst("^\\Q" + ignore + "\\E", "");
		}
		String validated = validateSpacekey(path);
		log.debug("validated space key = " + validated);
		return validated;
	}

	/**
	 * validates the given spacekey, removing illegal chars,
	 * as necessary 
	 * @param spacekey
	 * @return valid spacekey
	 */
	public static String validateSpacekey(String spacekey) {
		String validated = spacekey.replaceAll("[^A-Za-z0-9]", "");
		return validated;
	}

	/*********
	 * to send a page table which has all the attributes for a page to wiki
	 * @param broker
	 * @param pageTable
	 * @param confSettings
	 * @return
	 */
	protected String sendPage(RemoteWikiBroker broker, Hashtable pageTable, ConfluenceServerSettings confSettings)
	{
		PageForXmlRpc brokerPage = PageForXmlRpc.create(pageTable);
		PageForXmlRpc newPage = null;
		try {
			newPage = broker.storeNewOrUpdatePage(confSettings, confSettings.spaceKey, brokerPage);
			if (newPage == null || newPage.getPageParams() == null) {
				String message = "Unknown problem occured while sending page '" + pageTable.get("title") +
						"'. See atlassian-confluence.log for more details.";
				log.error(message);
				getErrors().addError(Feedback.REMOTE_API_ERROR, message, true);
				return null;
			}
		} catch (Exception e) {
			getErrors().addError(Feedback.REMOTE_API_ERROR, 
					"The Remote API threw an exception when it tried to upload page: \"" +
							pageTable.get("title") +
							"\".", true);
			e.printStackTrace();
			return null;
		}
		log.debug("Page URL: " + newPage.getUrl());

		//move the page if necessary and you can
		log.debug("Identifying parent location for page...");
		String parentid = null;
		if (pageTable.containsKey("parentId"))
			parentid = (String) pageTable.get("parentId");
		else { //We would like to put pages, by default, under the homepage.  
			if (this.homepages.containsKey(confSettings.spaceKey)) {//is there a known Home page in the space?
				parentid = this.homepages.get(confSettings.spaceKey);
				if ("-1".equals(parentid)) parentid = null;// no homepage in this space
			}
			else { //can we find the home page for this space?
				try {
					log.debug("Identifying Homepage for spacekey: " + confSettings.spaceKey);
					SpaceForXmlRpc space = broker.getSpace(confSettings, confSettings.spaceKey);
					parentid = space.getSpaceParams().get("homePage");
					this.homepages.put(confSettings.spaceKey, parentid);
				} catch (Exception e) {
					parentid = null;
					this.homepages.put(confSettings.spaceKey, "-1");
				}
			}

		}
		if (parentid != null) {
			log.debug("Attempting to set parent to: " + parentid);
			try {
				broker.movePage(confSettings, newPage.getId(), parentid, RemoteWikiBroker.Position.APPEND);
			} catch (Exception e) {
				log.error("Could not move page " + pageTable.get("title") + "\n" + e.getMessage() + "\n" +
						e.getStackTrace()); //could be because Confluence is earlier than 2.9
			}
		}

		return newPage.getId();

	}

	/*********
	 * to send a blog table which has all the attributes for a blog to wiki
	 * @param broker
	 * @param pageTable
	 * @param confSettings
	 * @return
	 */
	protected String sendBlog(RemoteWikiBroker broker, Hashtable pageTable, ConfluenceServerSettings confSettings)
	{
		BlogForXmlRpc brokerPage = BlogForXmlRpc.create(pageTable);
		BlogForXmlRpc newPage = null;
		try {
			newPage = broker.storeBlog(confSettings, confSettings.spaceKey, brokerPage);
			if (newPage == null || newPage.getblogParams() == null) {
				String message = "Unknown problem occured while sending page '" + pageTable.get("title") +
						"'. See atlassian-confluence.log for more details.";
				log.error(message);
				getErrors().addError(Feedback.REMOTE_API_ERROR, message, true);
				return null;
			}
		} catch (Exception e) {
			getErrors().addError(Feedback.REMOTE_API_ERROR, 
					"The Remote API threw an exception when it tried to upload page: \"" +
							pageTable.get("title") +
							"\".", true);
			e.printStackTrace();
			return null;
		}	
		log.debug("Page URL: " + newPage.getUrl());
		return newPage.getId();
	}


	protected void checkConfluenceSettings(ConfluenceServerSettings confSettings)
	{
		//check for problems with settings 
		Feedback testConnectionFeedback = TestSettingsListener.getConnectionFeedback(confSettings, isAutoDetectingSpacekeys());
		if (testConnectionFeedback != Feedback.OK) {
			String message = TestSettingsListener.getConnectionFeedbackMessage(confSettings, isAutoDetectingSpacekeys());
			log.error(message);
			this.state.updateNote(message);
			throw new IllegalArgumentException(message);
		}
		//    	log.info(TestSettingsListener.SUCCESS_MESSAGE_LONG); //this is getting called for every page 

	}
	/**
	 * sends page using the given settings
	 * @param page
	 * @param parentId
	 * @param confSettings
	 * @return page id the confluence page id for the page being stored. Used if
	 * page is new.
	 */
	protected String sendPage(Page page, String parentId, ConfluenceServerSettings confSettings) {
		//create wiki broker
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		//update page content to be xhtml
		if (Boolean.parseBoolean(this.miscProperties.getProperty("engine-markuptoxhtml", "true"))) {
			page = pageContentToXhtml(broker, confSettings, page);
		} else {
			log.debug("Engine: markup to xhtml property set to false");
		}
		//create page that broker can use
		Hashtable pageTable = createPageTable(page, parentId);
		//check for problems with settings 
		checkConfluenceSettings(confSettings); //XXX Why are we doing this for every page? 'cause we seem to create the confSettings on a page by page basis?
		//write ancestors, if any, first
		if (page.getAncestors() != null && !page.getAncestors().isEmpty()) {
			pageTable = handleAncestors(page, confSettings, pageTable);
		}
		//send page
		String id = null;
		if (!(page instanceof VersionPage) && page.getFile() != null) {
			log.debug("Original Filepath: " + page.getFile().getAbsolutePath());
		}
		String tmpspacekey = (page.getSpacekey()!=null)?page.getSpacekey():confSettings.spaceKey;
		if (page.isBlog()) {
			log.debug("Attempting to send blog: " + page.getName() + " to space: " + tmpspacekey);
			id = sendBlog(broker, pageTable, confSettings);
		} else { 
			log.debug("Attempting to send page: " + page.getName() + " to space: " + tmpspacekey);
			id = sendPage(broker, pageTable, confSettings);
		}
		if (id == null) return null;
		//send attachments
		sendAttachments(page, broker, id, confSettings);
		//send labels 
		sendLabels(page, broker, id, confSettings);
		//send comments
		sendComments(page, broker, id, confSettings);
		//set author
		log.debug("Page Version: " + page.getVersion());
		sendAuthor(page, broker, id, confSettings);
		//set timestamp
		sendTimestamp(page, broker, id, confSettings);
		//return the page id
		return id;
	}

	private Hashtable handleAncestors(Page page,
			ConfluenceServerSettings confSettings, Hashtable pageTable) {
		enforceAncestorTitleAndKey(page.getAncestors(), page.getName(), page.getSpacekey(), page.isBlog());
		if (page.isBlog()) { //get the blog id to make certain all ancestors and current page are made the same CEO
			Page first = page.getAncestors().remove(0);
			String blogid = sendPage(first, null, confSettings);
			enforceBlogId(page, page.getAncestors(), blogid);
			pageTable.put("id", blogid);
		}
		if (page.getAncestors() != null) log.info("Number of ancestors for page '"+page.getName()+"': " + page.getAncestors().size());
		writePages(page.getAncestors(), settings.getSpace(), false);
		return pageTable;
	}

	private void enforceBlogId(Page page, Vector<VersionPage> pages,
			String blogid) {
		page.setId(blogid);
		for (VersionPage anc : pages) {
			anc.setId(blogid);
		}
	}

	private void enforceAncestorTitleAndKey(Vector<VersionPage> pages,
			String name, String spacekey, boolean isBlog) {
		for (VersionPage page : pages) {
			page.setName(name);
			page.setSpacekey(spacekey);
			page.setIsBlog(isBlog);
		}

	}

	public String markupToXhtml(String markup) {
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		ConfluenceServerSettings confSettings = getConfluenceServerSettings(this.settings);
		try {
			return getContentAsXhtmlFormat(broker, confSettings, markup);
		} catch (Exception e) {
			String errorMessage = "Could not transform wiki content from markup to xhtml.";
			log.error(Feedback.REMOTE_API_ERROR + ": " + errorMessage);
			this.errors.addError(Feedback.REMOTE_API_ERROR, errorMessage, true);
			return null;
		}
	}

	private Page pageContentToXhtml(RemoteWikiBroker broker,
			ConfluenceServerSettings confSettings, Page page) {
		try {
			String xhtml = getContentAsXhtmlFormat(broker, confSettings, page.getConvertedText());
			page.setConvertedText(xhtml);
		} catch (Exception e) {
			String errorMessage = "Could not transform wiki content in page: '"+page.getName()+
					"' from markup to xhtml.";
			log.error(Feedback.REMOTE_API_ERROR + ": " + errorMessage);
			this.errors.addError(Feedback.REMOTE_API_ERROR, errorMessage, true);
		}
		return page;
	}

	/**
	 * creates a parameter table with the given page and parentId.
	 * @param page
	 * @param parentId
	 * @return table with Remote API page parameters
	 */
	private Hashtable createPageTable(Page page, String parentId) {
		Hashtable table = new Hashtable();
		if (page.getConvertedText() == null) page.setConvertedText("");
		table.put("content", page.getConvertedText());
		table.put("title", page.getName()); 
		if (parentId != null && !parentId.equals("null")) table.put("parentId", parentId);
		if (page.getVersion() > 0) table.put("version", page.getVersion() + "");
		if (page.isBlog() && page.getId() != null) table.put("id", page.getId());
		return table;
	}

	/**
	 * checks the given page for valid attachments,
	 * and sends them to Confluence using the rwb and the given pageId string as the 
	 * page to be attached to.
	 * 
	 * @param page given page object that might have attachments
	 * @param broker XML-RPC broker which will communicate with Confluence
	 * @param pageId the page the Confluence will attach the attachment to
	 * @param confSettings the confluence user settings needed by the broker to connect to Confluence 
	 */
	private void sendAttachments(Page page, RemoteWikiBroker broker, String pageId, ConfluenceServerSettings confSettings) {
		log.debug("Examining attachments for page: " + page.getName());
		// Send the attachments
		for (Attachment attachment : page.getAllAttachmentData()) {
			if (alreadyAttached(page, attachment.getFile()))
				continue;
			sendAttachment(attachment, broker, pageId, confSettings);
		}
	}

	protected void sendLabels(Page page, RemoteWikiBroker broker, String pageId, ConfluenceServerSettings confSettings) {
		log.debug("Examining labels for page: " + page.getName());
		//check to see if we're sending labels for this version of the page
		if (badVersionForSendingLabels(page)) return;
		String labels = page.getLabelsAsString();
		log.debug("Sending Labels: " + labels);
		if (labels == null) 
			return;
		try {
			broker.addLabels(confSettings, labels, pageId);
		} catch (Exception e) {
			String errorMessage = "Could not add labels '" + labels + "' to page '" + page.getName() +"'";
			log.error(Feedback.REMOTE_API_ERROR + ": " + errorMessage);
			this.errors.addError(Feedback.REMOTE_API_ERROR, errorMessage, true);
		}
	}

	private boolean badVersionForSendingLabels(Page page) {
		int version = page.getVersion();
		int latest = Page.getLatestVersion(page.getName());
		boolean history = isHandlingPageHistories();
		String allVersionsProp = (String) this.miscProperties.get("page-history-allversionlabels");
		boolean allVersions = (allVersionsProp != null) && Boolean.parseBoolean(allVersionsProp);
		return history && !allVersions && version != latest;
	}

	/**
	 * adds page comments to a page in confluence
	 * @param page given page object that might have comments
	 * @param broker XML-RPC broker which will communicate with Confluence
	 * @param pageId the page id for the given page
	 * @param confSettings the confluence user settings needed by the broker to connect to Confluence
	 */
	protected void sendComments(Page page, RemoteWikiBroker broker, String pageId, ConfluenceServerSettings confSettings) {
		if (page.hasComments()) {
			log.debug("Sending comments for page: " + page.getName());
			try {
				for (Comment comment : page.getAllCommentData()) {
					if (comment == null) {
						log.error("Comment was null! SKIPPING");
						this.errors.addError(Feedback.CONVERTER_ERROR, "Comment should not be null!", true);
						continue; 
					}
					//create page that broker can use
					CommentForXmlRpc brokerComment = new CommentForXmlRpc();
					brokerComment.setPageId(pageId);
					String commentcontent = comment.text;
					if (!comment.isXhtml()) {
						commentcontent = getContentAsXhtmlFormat(broker, confSettings, comment.text);
					}
					brokerComment.setContent(commentcontent);
					//upload comment
					CommentForXmlRpc uploadedComment = broker.addComment(confSettings, brokerComment);
					if (comment.hasCreator()) {
						boolean usersMustExist = false;
						broker.setCreator(confSettings, comment.creator, uploadedComment.getId(), usersMustExist);
						broker.setLastModifier(confSettings, comment.creator, uploadedComment.getId(), usersMustExist);
					}
					if (comment.hasTimestamp()) {
						broker.setCreateDate(confSettings, comment.timestamp, uploadedComment.getId());
						broker.setLastModifiedDate(confSettings, comment.timestamp, uploadedComment.getId());
					}
				}
			} catch (Exception e) {
				String errorMessage = null;
				if (e.getMessage() == null) {
					log.error("Problem with comments!", e);
					return;
				}
				else if (e.getMessage().contains("NotPermittedException")) {
					errorMessage = "User is not permitted to add comments to page: " + page.getName() + "'";
				}
				else if (e.getMessage().contains("does not exist")) {
					errorMessage = "Cannot add comments to the page because it does not exist: " + page.getName();
				}
				else {
					errorMessage = "Could not send comments to page '" + page.getName() +"'";
				}
				log.error(Feedback.REMOTE_API_ERROR + ": " + errorMessage);
				this.errors.addError(Feedback.REMOTE_API_ERROR, errorMessage, true);
			}
		}
		//    	else log.debug("Page has no comments."); //DELETE
	}

	public String getContentAsXhtmlFormat(RemoteWikiBroker broker, ConfluenceServerSettings confSettings, String text) throws XmlRpcException, IOException {
		return broker.convertWikiToStorageFormat(confSettings, text);
	}
	private void sendAuthor(Page page, RemoteWikiBroker broker, String id, ConfluenceServerSettings confSettings) {
		if (page.getAuthor() != null) {
			log.debug("Sending author data.");
			boolean exist = true;
			if (this.miscProperties.containsKey("users-must-exist")) 
				exist = Boolean.parseBoolean((String) this.miscProperties.get("users-must-exist"));
			try {
				if (page.getVersion() == 1) { //only set the creator if its the first version
					broker.setCreator(confSettings, page.getAuthor(), id, exist);
				}
				//set the modifier 
				broker.setLastModifier(confSettings, page.getAuthor(), id, exist);
			} catch (Exception e) {
				String errorMessage = Feedback.REMOTE_API_ERROR + ": Problem setting creator or last modifier data.";
				log.error(errorMessage);
				this.errors.addError(Feedback.REMOTE_API_ERROR, errorMessage, true);
				e.printStackTrace();
			}
		}
	}

	private void sendTimestamp(Page page, RemoteWikiBroker broker, String id, ConfluenceServerSettings confSettings) {
		if (page.getTimestamp() != null) {
			log.debug("Sending timestamp data: " + page.getTimestamp());
			try {
				DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss:SS"); //XXX Settable?
				if (this.miscProperties.getProperty("user-timezone", null) != null) {
					String timezone = this.miscProperties.getProperty("user-timezone", null);
					timezone = timezone.trim();
					dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
				}
				String timestamp = dateFormat.format(page.getTimestamp());

				if (page.getVersion() == 1) { //only set the creator if its the first version
					broker.setCreateDate(confSettings, timestamp, id);
				}
				//set the modifier 
				broker.setLastModifiedDate(confSettings, timestamp, id);
			} catch (Exception e) {
				String errorMessage = Feedback.REMOTE_API_ERROR + ": Problem setting create or last modified date.";
				log.error(errorMessage);
				this.errors.addError(Feedback.REMOTE_API_ERROR, errorMessage, true);
				e.printStackTrace();
			}
		}
	}


	/**
	 * @param file
	 * @return true if the given file does not exist on the filesystem.
	 */
	protected boolean doesNotExist(File file) {
		boolean doesNotExist = !file.exists();
		if (doesNotExist)
			log.warn("File '" + file.getPath() + "' does not exist: Skipping");
		return doesNotExist;
	}

	/**
	 * @param file
	 * @return true if file size is too big
	 */
	protected boolean tooBig(File file) {

		if (!file.exists()) return false;
		int length = (int) file.length();

		String maxString = getMaxAttachmentSizeString();

		int maxBytes = getAsBytes(maxString);
		if (maxBytes < 0) return false;
		boolean tooBig = length > maxBytes;
		if (tooBig)
			log.warn("File " + file.getName() + " is larger than " + maxString + ". Skipping.");
		return tooBig;
	}

	/**
	 * @return the max attachment size as a string
	 */
	private String getMaxAttachmentSizeString() {
		return getMaxAttachmentSizeStringFromModel();
	}

	/**
	 * @return the max attachment size as it's represented in the model
	 */
	private String getMaxAttachmentSizeStringFromModel() {
		return this.settings.getAttachmentSize();
	}

	/**
	 * @param fileLocation location of properties file
	 * @return a Properties object containing confluence settings
	 */
	protected Properties loadProperties(String fileLocation) {
		/* most of this code grabbed from ConfluenceSettingsForm.populateConfluenceSettings*/
		Properties properties = new Properties();
		File confSettings = new File(fileLocation);
		if (confSettings.exists()) {
			// load properties file
			FileInputStream fis;
			try {
				fis = new FileInputStream(fileLocation);
				properties.load(fis);
				fis.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return properties;
	}

	/**
	 * calculates the number of bytes the given maxString represents
	 * @param maxString file size described as a String.
	 * Example: 5B, 5K, 5M, 5G, etc.
	 * @return as Bytes.
	 * Respectively: 5, 5120, 5242880, 5368709120
	 */
	protected int getAsBytes(String maxString) {
		String maxRegex = "^(\\d+)(\\D)";
		if (maxString == null || "".equals(maxString)) 
			return -1;

		int power, num = 0;
		String numString, unitString = null;
		if (Pattern.matches("^\\d+$", maxString)) {
			unitString = "B";
			numString = maxString;
		}
		else {
			numString = maxString.replaceFirst(maxRegex, "$1");
			unitString = maxString.replaceFirst(maxRegex, "$2");
		}
		try {
			num = Integer.parseInt(numString);
		} catch (NumberFormatException e) {
			String message = PROP_ATTACHMENT_SIZE_MAX + " setting is malformed.\n" +
					"Setting must be formatted like so: [number][unit], where unit is\n" +
					"one of the following: B, K, M, G. No max attachment size set.";
			log.error(message);
			return -1;
		}
		unitString = unitString.toUpperCase();
		char unit = unitString.toCharArray()[0]; //first char in that string

		switch (unit) {
		case ('B'): power = 0;break;
		case ('K'): power = 1;break;
		case ('M'): power = 2;break;
		case ('G'): power = 3;break;
		default: return -1;
		}

		int multiplier = (int) Math.pow(1024, power);
		int value = num * multiplier;
		return value;
	}

	/**
	 * @param page
	 * @param file
	 * @return true if a particular page already has a particular
	 * file attached.
	 */
	protected boolean alreadyAttached(Page page, File file) {
		String pagename = page.getName();
		String filename = file.getName();
		boolean isblog = page.isBlog();
		String attachmentId = pagename + filename + isblog;
		if (page.getSpacekey() != null) attachmentId = page.getSpacekey() + attachmentId;
		if (attachedFiles == null) 
			attachedFiles = new HashSet<String>();
		boolean attached = attachedFiles.contains(attachmentId);

		if (!attached) {
			attachedFiles.add(attachmentId);
			if (attachedPaths == null) attachedPaths = new HashSet<String>();
			attachedPaths.add(file.getAbsolutePath()); //used with orphan upload checking
		}
		else log.debug("Attachment " + filename + " is already attached: Skipping.");

		return attached;
	}

	/**
	 * to check if the file has been attached with any pages.
	 * @param fileName
	 * @return
	 */    
//	protected boolean alreadyAttached(String fileName) {
//		if (this.attachedFiles == null || this.attachedFiles.isEmpty())
//			return false;
//		Iterator <String>it=attachedFiles.iterator();
//		while (it.hasNext())
//		{
//			String item=it.next();
//			int index=item.lastIndexOf(fileName);
//			if (index < 0)
//				continue;
//			if (item.length() - index == fileName.length())
//				return true;		
//		}
//		return false;
//
//	}

	protected boolean orphanAlreadyAttached(File file) {
		if (this.attachedPaths == null) return false;
		return (this.attachedPaths.contains(file.getAbsolutePath()));
	}

	/**
	 * This method determines the mime type of a file. It uses the file
	 * conf/mime.types to map from the file name extension
	 * to a mime type. The mime type file should be read into the
	 * mimeTypes field before this method is called.
	 *
	 * @param file The file object
	 * @return the mime type of the file.
	 */
	public static String determineContentType(File file) {
		if (mimeTypes != null) {
			return mimeTypes.getContentType(file);
		} 
		//else assume it's an image
		String filename = file.getName();
		int extensionStart = filename.lastIndexOf(".");
		if (extensionStart >= 0) {
			String extension = filename.substring(extensionStart + 1);
			return "image/" + extension;
		}
		// Hmm... No extension. Assume it's a text file.
		return "text/plain";
	}

	Pattern switchPattern = Pattern.compile("switch");
	Pattern suffixPattern = Pattern.compile("suffix");
	private boolean handlingPageHistories = false;
	private String pageHistorySuffix = null;

	/**
	 * set the page history state to reflect the page history property
	 * and associated value that are passed as arguments
	 * @param key
	 * @param value
	 */
	protected void handlePageHistoryProperty(String key, String value) {
		Matcher switchFinder = switchPattern.matcher(key);
		if (switchFinder.find()) {
			//the default should be false, so it's ok to just parse the string.
			this.handlingPageHistories = Boolean.parseBoolean(value);
			return;
		}
		Matcher suffixFinder = suffixPattern.matcher(key);
		if (suffixFinder.find()) {
			setPageHistorySuffix(value);
			return;
		}
	}

	protected void handleIllegalHandling(String key, String value) {
		boolean enabled = false; //default, confluence 4 doesn't appear to need this
		value = value.trim();
		if ("false".equals(value))
			enabled = false;
		illegalHandlingEnabled = enabled;
	}

	protected void handleAutoDetectSpacekeys(String key, String value) {
		boolean enabled = false; //default
		value = value.trim();
		if ("true".equals(value)) {
			enabled = true;
		}
		autoDetectSpacekeys = enabled;
	}

	Pattern miscPropsPattern = Pattern.compile("" +
			"\\w+\\.\\d+\\.([^.]+)\\.property"
			);
	protected Properties handleMiscellaneousProperties(String key, String value) {
		Matcher miscKeyFinder = miscPropsPattern.matcher(key);
		if (miscKeyFinder.matches()) {
			String misckey = miscKeyFinder.group(1);
			if (this.miscProperties == null)
				this.miscProperties = new Properties();
			this.miscProperties.put(misckey, value);
			log.debug("Miscellaneous Property set: " + misckey + "=" + value);
			return this.miscProperties;
		}
		String error = "Miscellaneous property was detected, " +
				"but key was invalid. Could not instantiate property: " +
				key + "=" + value;
		log.error(error);
		this.errors.addError(Feedback.BAD_PROPERTY, error, true);
		return this.miscProperties;
	}

	private void addDefaultMiscProperties() {
		handleMiscellaneousProperties("Testing.1234.spacekey.property", this.settings.getSpace());
	}

	protected void handleFilters(String key, String value) throws InstantiationException, IllegalAccessException {
		log.debug("filter property = " + value);
		getFilterValues().add(value);
	}

	private Set<String> getFilterValues() {
		if (this.filterValues == null)
			this.filterValues = new HashSet<String>();
		return this.filterValues;
	}

	/**
	 * sets up .xmlevent properties
	 * @param key must end in .xmlevent
	 * @param value must follow this format:
	 * <tt>
	 * {tag}tagname{class}classname
	 * </tt>
	 * where tagname is the xml tag to associate the event with (b, for bold)
	 * and classname is the parser that will manage the events for that tag.
	 * tagname can contain a comma-delimited list of tags. For example:
	 * {tag}h1, h2, h3{class}com.example.HeaderParser
	 */
	private void handleXmlEvents(String key, String value) {
		String tag = getXmlEventTag(value);
		String classname = getXmlEventClassname(value);
		String[] tags = tag.split(",");
		for (String onetag : tags) {
			onetag = onetag.trim();
			addOneXmlEvent(onetag, classname);
		}
	}

	/**
	 * adds one xml event object to the events handler, such that the classname becomes 
	 * an instantiated class that is associated with the given tag.
	 * The events handler can be custom (using the xmlevents misc property), or the default
	 * xml events handler will be used
	 * @param tag
	 * @param classname
	 */
	private void addOneXmlEvent(String tag, String classname) {
		if (this.miscProperties.containsKey("xmlevents")) {
			Class eventsClass;
			String xmleventsclass = this.miscProperties.getProperty("xmlevents");
			try {
				eventsClass = Class.forName(xmleventsclass);
			} catch (ClassNotFoundException e) {
				log.warn("xmlevents property value - " +
						xmleventsclass +
						" - does not exist. Using DefaultXmlEvents.");
				this.miscProperties.remove("xmlevents");
				eventsClass = DefaultXmlEvents.class; //try setting the DefaultXmlEvents
			}
			XmlEvents events = null;
			try {
				events = (XmlEvents) eventsClass.newInstance();
				events.addEvent(tag, classname); //call the custom events class
				return;
			} catch (Exception e) { 
				log.warn("xmlevents property value - " +
						xmleventsclass + " - hasn't implemented XmlEvents. " +
						"Using DefaultXmlEvents.");
				this.miscProperties.remove("xmlevents");
				//continue to DefaultXmlEvents.addEvent below
			}
		}
		new DefaultXmlEvents().addEvent(tag, classname);
	}

	Pattern xmleventClassPattern = Pattern.compile("" +
			"\\{class\\}(.*)");
	protected String getXmlEventClassname(String value) {
		Matcher finder = xmleventClassPattern.matcher(value);
		if (finder.find()) {
			return finder.group(1);
		}
		throw new IllegalArgumentException(XMLEVENT_PROP_ERROR);
	}

	Pattern xmleventTagPattern = Pattern.compile("" +
			"\\{tag\\}([^}]+)\\{class\\}");
	protected String getXmlEventTag(String value) {
		Matcher finder = xmleventTagPattern.matcher(value);
		if (finder.find()) {
			return finder.group(1);
		}
		throw new IllegalArgumentException(XMLEVENT_PROP_ERROR);
	}

	/**
	 * @param key
	 * @return true if the given key is the switch to turn on the 
	 * Hierarchy framework
	 */
	protected boolean isHierarchySwitch(String key) {
		Matcher switchFinder = switchPattern.matcher(key);
		return switchFinder.find();
	}

	/**
	 * determines if the given string represents an allowed 
	 * non converter property: (hierarchy builder, page history preserver,
	 * illegalname handler, autodetect spacekeys)
	 * @param input represents an entire converter/property string. For example:
	 * <br/>
	 * Wiki.0011.somefilename.propertytype=something
	 * @return true if it's an expected/allowed non converter property
	 */
	public boolean isNonConverterProperty(String input) {
		String converterTypes = 
				"(" +
						"(" + 
						NONCONVERTERTYPE_HIERARCHYBUILDER + 
						")" + 
						"|" +
						"(" + 
						NONCONVERTERTYPE_PAGEHISTORYPRESERVATION + 
						")" +
						"|" +
						"(" + 
						NONCONVERTERTYPE_ILLEGALHANDLING + 
						")" +
						"|" +
						"(" + 
						NONCONVERTERTYPE_AUTODETECTSPACEKEYS + 
						")" +
						"|" +
						"(" +
						NONCONVERTERTYPE_FILTERS + 
						")" +
						"|" +
						"(" +
						NONCONVERTERTYPE_MISCPROPERTIES +
						")" +
						"|" +
						"(" +
						NONCONVERTERTYPE_XMLEVENT +
						")" +
						")";
		String converterPattern = "[-\\w\\d.]+?" + converterTypes + "=" + ".*";
		return input.matches(converterPattern);
	}
	/**
	 * @return true if the converter should handle page histories
	 */
	public boolean isHandlingPageHistories() {
		return this.handlingPageHistories;
	}

	/**
	 * @return true if the converter should handle page histories
	 */
	public boolean isHandlingPageHistoriesFromFilename() {
		return this.handlingPageHistories && this.pageHistorySuffix != null;
	}

	/**
	 * @return the current page history suffix
	 */
	public String getPageHistorySuffix() {
		return this.pageHistorySuffix;
	}

	/**
	 * sorts the given list of pages.
	 * Note: sorting will take into account page name 
	 * and page version. Non unique page objects will be culled.
	 * @param pages list of Page objects
	 * @return sorted list
	 */
	protected List<Page> sortByHistory(List<Page> pages) {
		this.state.updateNote("Sorting Pages by Page History");
		log.debug("num of pages: " + pages.size());
		List<Page> sortedPages = new ArrayList<Page>();
		Set<Page> sorted = new TreeSet<Page>();
		sorted.addAll(pages); //sort them and get rid of non-unique pages
		sortedPages.addAll(sorted); //turn them back into a list
		if (log.isDebugEnabled()) {
			for (Page page : sorted) {
				log.debug("Sorted pages: " + page.getFile().getName());
			}
		}
		return sortedPages;
	}

	Pattern hashPattern = Pattern.compile("#+");
	/**
	 * sets the page history suffix, if it's a valid suffix.
	 * If not, sets it to null.
	 * @param suffix candidate suffix, a valid candidate will have 
	 * a numeric component, represented by a '#' (hash) symbol
	 * <br/>
	 * Example: -v#.txt
	 * @return true, if a valid suffix was saved.
	 * false, if the suffix was invalid, and therefore was not saved.
	 */
	protected boolean setPageHistorySuffix(String suffix) {
		//check for suffix goodness
		Matcher hashFinder = hashPattern.matcher(suffix);
		if (hashFinder.find()) {
			this.pageHistorySuffix = suffix;
			return true;
		}
		log.error("Error trying to preserve page history: Suffix '" + suffix + "' " +
				"does not have a sortable component. Must include '#'.");
		this.pageHistorySuffix = null;
		return false;
	}

	/**
	 * @return HierarchyBuilder object. used by tests.
	 */
	protected HierarchyBuilder getHierarchyBuilder() {
		return hierarchyBuilder;
	}

	/**
	 * @return HierarchyHandler object. used by tests.
	 */
	protected HierarchyHandler getHierarchyHandler() {
		return hierarchyHandler;
	}

	/**
	 * sets how the hierarchy framework is to be used. 
	 * @param input "UseBuilder", "UsePagenames", or "Default". 
	 * If input is none of these, no changes occur
	 */
	private void setHierarchyHandler(String input) {
		if (input.matches("UseBuilder")) hierarchyHandler = HierarchyHandler.HIERARCHY_BUILDER;
		else if (input.matches("UsePagenames")) hierarchyHandler = HierarchyHandler.PAGENAME_HIERARCHIES;
		else if (input.matches("Default")) hierarchyHandler = HierarchyHandler.DEFAULT;
	}

	/**
	 * @return the feedback as it currently stands
	 */
	public Feedback getConverterFeedback() {
		return this.feedback;
	}

	/**
	 * resets the feedback state to Feedback.NONE
	 */
	public void resetFeedback() {
		this.feedback = Feedback.NONE;
	}

	/**
	 * clears state relating to the error handling
	 */
	public void resetErrorHandlers() {
		this.errors.clear();
		this.hadConverterErrors = false;
	}

	/**
	 * clears state relating to the hierarchy framework
	 */
	public void resetHierarchy() {
		this.hierarchyBuilder = null;
		this.hierarchyHandler = HierarchyHandler.DEFAULT;
	}

	/**
	 * @return object contains information relating to errors triggered during the conversion
	 */
	public ConverterErrors getErrors() {
		return this.errors;
	}

	/**
	 * @return true if the conversion has generated errors
	 */
	public boolean hadConverterErrors() {
		return this.hadConverterErrors;
	}

	/**
	 * setter
	 * @param running
	 */
	protected void setRunning(boolean running) {
		this.running = running; //used in junit
	}

	/**
	 * setter
	 * @param settings
	 */
	protected void setSettings(UWCUserSettings settings) {
		this.settings = settings; //used in junit
	}


	/**
	 * @return true if the illegal handling (names and links) should occur. 
	 * false if it should be disabled
	 */
	public boolean isIllegalHandlingEnabled() {
		return illegalHandlingEnabled;
	}

	public boolean isAutoDetectingSpacekeys() {
		return autoDetectSpacekeys;
	}

	public class AsciiVersionComparator implements Comparator {
		public int compare(Object a, Object b) {
			Page pa = (Page) a;
			Page pb = (Page) b;
			String sa = pa.getName().toLowerCase();
			String sb = pb.getName().toLowerCase();
			int ascii = sa.compareTo(sb);
			int sav = pa.getVersion();
			int sbv = pb.getVersion();
			int version = sbv - sav;
			return ascii - version;
		}
	}
}
