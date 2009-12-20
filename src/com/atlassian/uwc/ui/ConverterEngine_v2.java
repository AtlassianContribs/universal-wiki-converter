package com.atlassian.uwc.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.converters.IllegalLinkNameConverter;
import com.atlassian.uwc.converters.IllegalPageNameConverter;
import com.atlassian.uwc.converters.JavaRegexConverter;
import com.atlassian.uwc.converters.PerlConverter;
import com.atlassian.uwc.converters.twiki.JavaRegexAndTokenizerConverter;
import com.atlassian.uwc.converters.twiki.TWikiRegexConverterCleanerWrapper;
import com.atlassian.uwc.hierarchies.HierarchyBuilder;
import com.atlassian.uwc.hierarchies.HierarchyNode;
import com.atlassian.uwc.ui.xmlrpcwrapperOld.AttachmentForXmlRpcOld;
import com.atlassian.uwc.ui.xmlrpcwrapperOld.PageForXmlRpcOld;
import com.atlassian.uwc.ui.xmlrpcwrapperOld.RemoteWikiBrokerOld;

/**
 * This class drives the conversion process by gathering all the files, gathering
 * the selected converters, then applying all the converters against each file, then
 * sending the converted 'Pages' and attachments to Confluence via XML-RPC (or
 * possibly some other method in the future)
 */
public class ConverterEngine_v2 {
    
	private static final String NONCONVERTERTYPE_PAGEHISTORYPRESERVATION = "page-history-preservation";
	private static final String NONCONVERTERTYPE_HIERARCHYBUILDER = ".hierarchy-builder";
	private static final String CONVERTERTYPE_TWIKICLEANER = ".twiki-cleaner";
	private static final String CONVERTERTYPE_JAVAREGEX = ".java-regex";
	private static final String CONVERTERTYPE_JAVAREGEXTOKEN = ".java-regex-tokenize";
	private static final String CONVERTERTYPE_PERL = ".perl";
	private static final String CONVERTERTYPE_CLASS = ".class";

	private static final String PROP_ATTACHMENT_SIZE_MAX = "attachment.size.max";
	private static String PROP_LOCATION = ConfluenceSettingsForm.CONFLUENCE_SETTINGS_FILE_LOC;
	
	private HashSet<String> attachedFiles;
	
	private String errorMessage;
    Logger log = Logger.getLogger("ConverterEngine");
    // this logger is used to write out totals for the UWC to a seperate file uwc-totals.log
    Logger totalsFileLog = Logger.getLogger("totalsFileLog");

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
    };
    private HierarchyHandler hierarchyHandler = HierarchyHandler.DEFAULT; 
    
    /**
     * The mapping from file name extension to mime type that is used when sending
     * attachments to Confluence.
     */
    private MimetypesFileTypeMap mimeTypes;

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

    /**
     * This default constructor initializes the mime types.
     */
    public ConverterEngine_v2() {
        try {
            mimeTypes = new MimetypesFileTypeMap(new FileInputStream(mimetypeFileLoc));
        } catch (FileNotFoundException e) {
            addError("Couldn't load mime types!", e);
        }
        totalsFileLog.setAdditivity(false);
    }

    /**
     * @param uwcForm
     * @todo this method might end up not being needed.....refactor it out?
     */
    public void processPages(UWCForm2 uwcForm) {
        convert(uwcForm.pageList, uwcForm.engineSelectedConverterList);
        // if single page then pop up in editor pane window

        // write list to Confluence
    }

    /**
     * High level method to drive the conversion of the 'input pages' via
     * the selected 'converterStrings'
     *
     * @param inputPages       - the full path Strings of the files
     * @param converterStrings - the full Strings for the converterStrings which have been selected
     *                         for the engine.
     * @todo - this isn't currently memory efficient. We should probably switch to streaming
     * if people start running out of memory
     */
    public void convert(List<File> inputPages, List<String> converterStrings) {
        ArrayList<Converter> converters = createConverters(converterStrings);
        
        ConfluenceSettingsForm confSettings = UWCForm2.getInstance().getConfluenceSettingsForm();

        // Recurse through directories, adding all files
        List<Page> allPages = createPages(confSettings, inputPages);

        // Convert the file contents
        if (convertPages(allPages, converters)) {
        	// do final required conversions. This step is seperate, due to state saving issues
        	boolean useUI = true; //this is necessary, so we can turn it off for unit tests
        	convertWithRequiredConverters(allPages, useUI);
            // write out dialog with metrics of total time and memory ('don't show this again'

            // Save the converted pages to disk
            // TODO: This is not needed for the converter, and there should really be an option to turn it off.
            savePages(allPages, confSettings);

            // Finally, send the pages to Confluence if the user approves.
            if (okToSend()) {
                if (hierarchyHandler == HierarchyHandler.HIERARCHY_BUILDER && hierarchyBuilder != null) {
                    HierarchyNode root = hierarchyBuilder.buildHierarchy(allPages);
                    writeHierarchy(root);
                } else {
                    writePages(allPages);
                }
            }
        }
    }

    /**
     * Instantiate all the converterStrings
     *
     * @param converterStrings a list of converter strings of the form "key=value"
     * @return a list of converters
     */
    protected ArrayList<Converter> createConverters(List<String> converterStrings) {
        ArrayList<Converter> converters = new ArrayList<Converter>();
        for (String converterStr : converterStrings) {
            Converter converter;
            if (isNonConverterProperty(converterStr)) {
            	handleNonConverterProperty(converterStr);
            	continue;
            } 
        	converter = getConverterFromString(converterStr);
        	if (converter == null) continue;
        	converters.add(converter);
        }
        return converters;
    }
    
    /**
     * converts the list of pages with the given converter
     * @param pages list of pages to be converted
     * @param useUI set this to false if you do not want the associated GUI elements
     * to be updated. This is useful for unit testing.
     */
    protected void convertWithRequiredConverters(List<Page> pages, boolean useUI) {
    	//create pagename converter and convert with it
		String pagenameConvStr = "MyWiki.9999.illegal-names.class=com.atlassian.uwc.converters.IllegalPageNameConverter";
		ArrayList<Converter> converters = createOneConverter(pagenameConvStr);
    	convertPages(pages, converters, useUI, "Checking for illegal pagenames.");
    	
    	//get the state hashtable
    	IllegalPageNameConverter  pagenameConverter = (IllegalPageNameConverter) converters.remove(0);
    	HashSet<String> illegalNames =  pagenameConverter.getIllegalPagenames();
    	
    	//create linkname converter and convert with it
		String illegallinksConvStr = "MyWiki.9999.illegal-links.class=com.atlassian.uwc.converters.IllegalLinkNameConverter";
    	converters = createOneConverter(illegallinksConvStr);
    	IllegalLinkNameConverter linknameConverter = (IllegalLinkNameConverter) converters.get(0);
    	linknameConverter.setIllegalPagenames(illegalNames);
    	convertPages(pages, converters, useUI, "Checking for links to illegal pagenames.");
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
                addError("Converter ignored -- name pattern not recognized: " + key);
                return null;
            }
        } catch (ClassNotFoundException e) {
            addError("Converter ignored -- the Java class " + value + " was not found");
            return null;
        } catch (IllegalAccessException e) {
            addError("Converter ignored -- there was a problem creating a converter object");
            return null;
        } catch (InstantiationException e) {
            addError("Converter ignored -- there was a problem creating the Java class " + value);
            return null;
        } catch (ClassCastException e) {
            addError("Converter ignored -- the Java class " + value +
                    " must implement the " + Converter.class.getName() + " interface!");
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
     */
    protected void handleNonConverterProperty(String converterStr) {
    	int equalLoc = converterStr.indexOf("=");
        String key = converterStr.substring(0, equalLoc);
        String value = converterStr.substring(equalLoc + 1);
        try {
	    	if (key.indexOf(NONCONVERTERTYPE_HIERARCHYBUILDER) >= 0) {
	    		if (isHierarchySwitch(key))
	    			setHierarchyHandler(value);
	    		else {
		            Class c;
						c = Class.forName(value);
		            HierarchyBuilder hierarchy = (HierarchyBuilder) c.newInstance();
		            hierarchyBuilder = hierarchy;
	    		}
	    	}
	    	else if (key.endsWith(NONCONVERTERTYPE_PAGEHISTORYPRESERVATION)) {
	    		handlePageHistoryProperty(key, value);
	    	}
        } catch (ClassNotFoundException e) {
            addError("Property ignored -- the Java class " + value + " was not found");
        } catch (IllegalAccessException e) {
            addError("Property ignored -- there was a problem creating the object: " + value);
        } catch (InstantiationException e) {
            addError("Property ignored -- there was a problem creating the Java class " + value);
        } catch (ClassCastException e) {
            addError("Property ignored -- the Java class " + value +
                    " must implement the " + Converter.class.getName() + " interface!");
        }    
    }
    


    HashMap<String, Converter> converterCacheMap = new HashMap<String, Converter>();
	

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
        Converter converter = (Converter) converterCacheMap.get(key);
        if (converter == null) {
            Class c = Class.forName(key);
            converter = (Converter) c.newInstance();
            converterCacheMap.put(key, converter);
        }
        return converter;
    }

    /**
     * Creates PageForXmlRpcOld objects for all the files in inputPages.
     * If any of the files is a directory, it is scanned recursively for files
     * matching the pattern in the settings object.
     *
     * @param confSettings The settings that control the engine.
     * @param inputPages   A list of files and directories that Pages should be created for.
     * @return A list of PageForXmlRpcOld objects for all files matching the pattern in the settings.
     */
    protected List<Page> createPages(ConfluenceSettingsForm confSettings, List<File> inputPages) {
        List<Page> allPages = new LinkedList<Page>();
        final String pattern = confSettings.getPattern();

        // This file filter accepts directories and file names ending with the pattern.
        // If the pattern is empty, all files are accepted.
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                assert file != null;
                return pattern == null ||
                       "".equals(pattern) ||
                       file.isDirectory() ||
                       file.getName().endsWith(pattern);
            }
        };
        for (File fileOrDir : inputPages) {
            List<Page> pages = recurse(fileOrDir, filter);
            setupPages(fileOrDir, pages);
            allPages.addAll(pages);
        }
        if (isHandlingPageHistories()) {
        	return sortByHistory(allPages);
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
        if (fileOrDir.isFile() && filter.accept(fileOrDir)) {
            result.add(new Page(fileOrDir));
        } else if (fileOrDir.isDirectory()) {
            File[] files = fileOrDir.listFiles(filter);
            for (File file : files) {
                result.addAll(recurse(file, filter));
            }
        }
        return result;
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
            pagePath = pagePath.substring(baselength);
            log.debug("pagePath: '" + pagePath + "'");
            String pageName = getPagename(pagePath);
            //Strip the file name from the path.
            int fileNameStart = pagePath.lastIndexOf(File.separator);
            if (fileNameStart >= 0) {
                pagePath = pagePath.substring(0, fileNameStart);
            } else {
                pagePath = "";
            }

            page.setPath(pagePath);
            page.setName(pageName);
            if (isHandlingPageHistories()) preserveHistory(page, pageName);
            log.debug("setupPages() Path: '" + page.getPath() + "', Name: " + page.getName());
        }
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
    		log.debug("new regex: " + suffixReplaceRegex);
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
    		int version = Integer.parseInt(versionString);
    		log.debug("version = " + version);
    		page.setVersion(version);
    		log.debug("pagename = " + pagename);
    		page.setName(pagename);
    	}
    	return page;
	}


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
     * This is where it all happens :). This method reads the files and runs the
     * converters on the pages.
     *
     * @param pages      The pages to be converted.
     * @param converters The converters to run on the pages
     * @return false if the user cancelled the task. Otherwise true.
     */
    protected boolean convertPages(List<Page> pages, List<Converter> converters) {
    	boolean useUI = true;
		String progressMessage = "Converting page files";
		return convertPages(pages, converters, useUI, progressMessage);
    }
    
    /**
     * @param pages pages to be converted
     * @param converters converters to be run on pages 
     * @param useUI true if the UI should be updated
     * @param progressMessage info provided to user describing progress monitor
     * @return false if the user cancelled the task
     */
    protected boolean convertPages(
    		List<Page> pages, 
    		List<Converter> converters, 
    		boolean useUI, 
    		String progressMessage) {
        boolean result = true;
        if (useUI) JFrame.setDefaultLookAndFeelDecorated(true);

        // Set up a progress monitor and progress count
        ProgressMonitor progressMonitor = null;
        int progress = 0;
        long startTotalConvertTime = 0;
        if (useUI) {
	        progressMonitor = createProgressMonitor(progressMessage, pages.size());
	        startTotalConvertTime = (new Date()).getTime();
        }
        // loop each page(file) through all the converters.
        // We can't use the enhanced for loop here, because we need to
        // remove pages that we can't read.
        for (Iterator<Page> i = pages.iterator(); i.hasNext();) {
            Page page = i.next();
            long startTimeStamp = ((new Date()).getTime());
            if (log.isInfoEnabled()) {
                log.info("-------------------------------------");
                log.info("converting page file: " + page.getName());
            }
            File file = page.getFile();
//            String inputPage = null;
            if (file == null) {
            	if (page.getOriginalText() != null && !"".equals(page.getOriginalText())) {
            		log.warn("This appears to be a unit test. Continue as for Unit Test.");
            	}
            	else {
	                log.warn("No file was set for page " + page.getName() + "! Skipping page.");
	                i.remove();
	                continue;
            	}
            }
            else if (page.getOriginalText() == null){
	            try {
	                String pageContents = FileUtils.readTextFile(file);
	                page.setOriginalText(pageContents);
	            } catch (IOException e) {
	                addError("Could not read file " + file.getAbsolutePath() + "! Skipping file.", e);
	                i.remove();
	                continue;
	            }
            }
            convertPage(converters, page);
            if (log.isInfoEnabled()) {
                long stopTimeStamp = ((new Date()).getTime());
                log.info("                   time to convert " + (stopTimeStamp - startTimeStamp) + "ms");
            }
            if (useUI) {
		        if (progressMonitor.isCanceled()) {
		            result = false;
		            break;
		        }
		        progressMonitor.setProgress(progress++);
		        if (progress % 10 == 0) {
		            progressMonitor.setNote("Converted " + progress + " out of " + pages.size() + " pages");
		        }
            }
        }
        if (useUI) progressMonitor.close();
// deleteMe - start
//        log.info("::: list of attachment paths not found:::");
//        List allFilesNotFound = PmWikiPrepareAttachmentFilesConverter.allFilesNotFound;
//        for (Object filesNotFound : allFilesNotFound) {
//            String s = (String)filesNotFound;
//            log.info("file not found::: "+s);
//        }
// deleteMe - stop
        if (useUI) {
	        long endTotalConvertTime = (new Date()).getTime();
	        long totalTimeToConvert = (endTotalConvertTime - startTotalConvertTime)/1000;        
	        log.info("::: total time to convert files: "+ totalTimeToConvert+ " seconds.");
            totalsFileLog.info(progressMessage+"::: total time to convert files: "+ totalTimeToConvert+ " seconds."+
            "For "+pages.size()+" pages and using "+converters.size()+" converters.");
        }
        return result;
    }

	/**
	 * converts one page with the given converters
	 * @param converters list of converters
	 * @param page page object 
	 */
	protected Page convertPage(List<Converter> converters, Page page) {
		log.debug("pagename = " + page.getName());
		if (page.getConvertedText() == null)
			page.setConvertedText(page.getOriginalText()); //in case empty converter list
		for (Converter converter : converters) {
		    try {
		         log.debug("running converter: "+converter.getKey());
		        converter.convert(page);
		        page.setOriginalText(page.getConvertedText());
		    } catch (Exception e) {
		        addError("Exception thrown by converter " + converter.getKey() +
		                " on page " + page.getName() + ". Continuing with next converter.", e);
		    }
		}
		return page;
	}


    /**
     * Write pages to disk. They are saved to the directory output/output below the
     * current working directory.
     *
     * @param pages The pages to save
     * @param confSettings A settings object. The pattern (file name extension) is appended
     *        to each page name to create the file name.
     */
    private void savePages(List<Page> pages, ConfluenceSettingsForm confSettings) {
        FileUtils.createOutputDirIfNeeded();
//        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
//        String outputDirName = "output" + File.separator + "output-" + sdf.format(new Date());
        String outputDirName = "output" + File.separator + "output";
        File outputDir = new File(outputDirName);
        if (!outputDir.exists() && !outputDir.mkdir()) {
            addError("Directory creation failed for directory " + outputDirName);
        }
        for (Page page : pages) {
            String outputFileLoc = outputDirName + File.separator + page.getName() + confSettings.getPattern();
            FileUtils.writeFile(page.getConvertedText(), outputFileLoc);
        }
    }

    /**
     * Puts up a dialog asking the user if the pages should be sent to Confluence.
     * @return <code>True</code> if the user answers yet, otherwise <code>false</code>.
     */
    private boolean okToSend() {
        // We must use a final object here, since we'll be accessing it from within an inner class (the Runnable),
        // but if we make a <code>final boolean</code>, the inner class can't change the value.
        // Therefore we make a final array containing a single boolean. The inner class can't change the array,
        // but changing <em>elements</em> of a final array is OK.
        final boolean[] result = new boolean[]{false};
        try {
            // We need to use invokeAndWait(), because the converter engine is not run in the
            // event dispatching thread.
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    int choice = JOptionPane.showInternalConfirmDialog(UWCForm2.getInstance().mainPanel,
                            "Do you want to send these pages to Confluence?", "information",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    result[0] = (choice == JOptionPane.YES_OPTION);
                }
            });
        } catch (Exception ignored) {
            // Do nothing -- in case of error, result[0] will be false and we will not continue.
        }
        return result[0];
    }

    /**
     * Writes the pages to Confluence. If the process takes more than three seconds,
     * a progress monitor will be displayed so that the user can see that something is
     * indeed happening.
     *
     * @param pages The pages to output.
     */
    private void writePages(List<Page> pages) {

        ProgressMonitor progressMonitor = createProgressMonitor("Uploading page files", pages.size());
        int progress = 0;
        // at last write the pages to Confluence!
        for (Page page : pages) {
            sendPage(page, null);
            if (progressMonitor.isCanceled()) {
                break; // User pressed cancel -- leave the loop
            }
            progressMonitor.setProgress(progress++);
            progressMonitor.setNote("Uploaded " + progress + " out of " + pages.size() + " pages.");
        }
        //attachedFiles is cleared so that if we do another conversion
        //without closing the UWC, it won't think the attachment has already been
        //attached
        this.attachedFiles = null; 
        progressMonitor.close();
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
     */
    private void writeHierarchy(HierarchyNode root) {

        // First count the number of nodes in the hierarchy so that we can set up the progress bar.
        int numberOfNodes = root.countDescendants();
        ProgressMonitor progressMonitor = createProgressMonitor("Uploading page files", numberOfNodes);
        int progress = 0;
        // at last write the pages to Confluence!
        for (HierarchyNode topLevelPage : root.getChildren()) {
            progress = writeHierarchy(topLevelPage, null, progressMonitor, progress);
            if (progressMonitor.isCanceled()) {
                break; // User pressed cancel -- leave the loop
            }
        }
        progressMonitor.close();
    }

    /**
     * This is the recursive part of <code>writeHierarchy</code>. Don't call this directly!
     * Call writeHierarchy(root) instead.
     *
     * @param node The current node in the hierarchy
     * @param parentId The Confluence "page ID" of the parent page
     * @param progressMonitor A progress monitor (duh) for the users' benefit.
     * @param progress The number of pages that have been converted so far
     *        (used to keep the progress monitor updated)
     * @return The number of pages converted after this node and all its descendants have been added.
     */
    private int writeHierarchy(HierarchyNode node, String parentId, ProgressMonitor progressMonitor, int progress) {
        // First upload the page contained in this node
        Page page = node.getPage();
        if (page == null) {
            // This node is a "placeholder" because there are pages further down the hierarchy but
            // for some reason this node was not included in the conversion. Create an empty page.
            // Note that this page will only be sent to Confluence if there was no page in place before.
            page = new Page(null);
            page.setName(node.getName());
            page.setOriginalText("");
            page.setConvertedText("");
        }
        String myId = sendPage(page, parentId);
        progress++;
        progressMonitor.setProgress(progress++);
        progressMonitor.setNote("Uploaded " + progress + " out of " + progressMonitor.getMaximum() + " pages.");

        // Then recursively upload all the node's descendants
        if (node == null) {
            log.error("Null node!");
        }
        for (HierarchyNode child : node.getChildren()) {
            progress = writeHierarchy(child, myId, progressMonitor, progress);
            if (progressMonitor.isCanceled()) {
                break; // User pressed cancel -- leave the loop
            }
        }
        return progress;
    }


    /**
     * Sends a page and all its attachments to Confluence.
     * If the page did not exist in Confluenc, it is created.
     * If it already existed, it is updated.
     * @param page A filled-in page object.
     * @param parentId The ID of the page's parent page, or
     *        <code>null</code> if the page is a top-level page.
     * @return The ID of the created or updated page in Confluence.
     */
    private String sendPage(Page page, String parentId) {
        // get a handle to the object that encapsulates writing pages
        RemoteWikiBrokerOld rwb = RemoteWikiBrokerOld.getInstance();

        PageForXmlRpcOld xmlrpcPage = new PageForXmlRpcOld();
        xmlrpcPage.setTitle(page.getName());
        xmlrpcPage.setContent(page.getConvertedText());
        if (parentId != null) {
            xmlrpcPage.setParentId(parentId);
        }

        xmlrpcPage = rwb.storeNewOrUpdatePage(xmlrpcPage);
        String id = xmlrpcPage.getId();
        // Send the attachments
        for (File file : page.getAttachments()) {
            AttachmentForXmlRpcOld attachment = new AttachmentForXmlRpcOld();
            if (alreadyAttached(page, file) || tooBig(file) || doesNotExist(file)) 
            	continue;
            attachment.setFileName(file.getName());
            attachment.setFileLocation(file.getAbsolutePath());
            attachment.setContentType(determineContentType(file));
            attachment.setComment("Added by UWC, the Universal Wiki Converter");
            try {
                rwb.storeAttachment(xmlrpcPage.getId(), attachment);
            } catch (IOException e) {
                addError("Couldn't send attachment " +
                        file.getAbsolutePath() + "! Skipping attachment.", e);
            } catch (XmlRpcException e) {
                addError("Couldn't send attachment " +
                        file.getAbsolutePath() + "! Skipping attachment.", e);
            }
        }
        return id;
    }


    /**
     * @param file
     * @return true if the given file does not exist on the filesystem.
     */
    protected boolean doesNotExist(File file) {
		boolean doesNotExist = !file.exists();
		if (doesNotExist)
			log.debug("File " + file.getName() + " does not exist: Skipping");
		return doesNotExist;
	}

	/**
     * @param file
     * @return true is file size is too big
     */
    protected boolean tooBig(File file) {
    	if (!file.exists()) return false;
		int length = (int) file.length();
		Properties properties = loadProperties(PROP_LOCATION);
		String maxString = (String) properties.get(PROP_ATTACHMENT_SIZE_MAX);
		int maxBytes = getAsBytes(maxString);
		if (maxBytes < 0) return false;
		boolean tooBig = length > maxBytes;
		if (tooBig)
			log.debug("File " + file.getName() + " is too big. Skipping.");
		return tooBig;
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
		String attachmentId = pagename + filename;
		if (attachedFiles == null) 
			attachedFiles = new HashSet<String>();
		boolean attached = attachedFiles.contains(attachmentId);
		
		if (!attached) attachedFiles.add(attachmentId);
		else log.debug("Attachment " + filename + " is already attached: Skipping.");
		
		return attached;
	}

	/**
     * Creates and initializes a progress monitor.
     * @param heading A descriptio of the activity being monitored. 
     * @param numberOfPages The maximum to set in the progress monitor.
     * @return The initialized progress monitor
     */
    private ProgressMonitor createProgressMonitor(String heading, int numberOfPages) {
        ProgressMonitor progressMonitor = new ProgressMonitor(
                UWCForm2.getInstance().mainFrame, heading, "", 0, numberOfPages);
        progressMonitor.setProgress(0);
        progressMonitor.setMaximum(numberOfPages);
        return progressMonitor;
    }

    /**
     * This method determines the mime type of a file. It uses the file
     * mime.types in the conf directory to map from the file name extension
     * to a mime type. The mime type file should be read into the
     * mimeTypes field before this method is called.
     *
     * @param file The file object
     * @return the mime type of the file.
     */
    public String determineContentType(File file) {
        if (mimeTypes != null) {
            return mimeTypes.getContentType(file);
        } else {
            // Assume it's an image
            String filename = file.getName();
            int extensionStart = filename.lastIndexOf(".");
            if (extensionStart >= 0) {
                String extension = filename.substring(extensionStart + 1);
                log.info(extension + " --- " + filename);
                return "image/" + extension;
            }
            // Hmm... No extension. Assume it's a text file.
            return "text/plain";
        }
    }

    /**
     * append all of the errors into a single message automatically adding
     * each with a new line
     *
     * @param errorMsg The error message
     */
    public void addError(String errorMsg) {
        log.error(errorMsg);
        errorMessage += errorMsg + "\n";
    }

    /**
     * Append all of the errors into a single message automatically adding
     * a new line to each. This version of the method takes a Throwable as a
     * second argument. The Throwable is output to the log object, but is not
     * placed in the error message.
     *
     * @param errorMsg The error message
     * @param e        An exception.
     */
    public void addError(String errorMsg, Throwable e) {
        log.error(errorMsg, e);
        errorMessage += errorMsg + "\n";
    }

    /**
     * retrieve the error messages and also clear them out
     *
     * @return errors
     */
    public String getErrorMessage() {
        String errorsTemp = errorMessage;
        // when we retrieve the errors we clear them for the next round
        errorMessage = "";
        return errorsTemp;
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
     * non converter property: (hierarchy builder or page history preserver)
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
		List<Page> sortedPages = new ArrayList<Page>();
		Set<Page> sorted = new TreeSet<Page>();
		sorted.addAll(pages); //sort them and get rid of non-unique pages
		sortedPages.addAll(sorted); //turn them back into a list
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
		Matcher hashFinder = hashPattern .matcher(suffix);
		if (hashFinder.find()) {
			this.pageHistorySuffix = suffix;
			return true;
		}
		log.error("Error trying to preserve page history: Suffix '" + suffix + "' does not have a sortable component. Must include '#'.");
		this.pageHistorySuffix = null;
		return false;
	}

	protected HierarchyBuilder getHierarchyBuilder() {
		return hierarchyBuilder;
	}

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
	 * sets the location of the confluenceSettings.properties file.
	 * Handy for unit testing
	 * @param prop_location
	 */
	protected void setPropLocation(String prop_location) {
		PROP_LOCATION = prop_location;
	}
}
