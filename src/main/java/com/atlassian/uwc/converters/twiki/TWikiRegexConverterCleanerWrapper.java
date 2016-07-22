package com.atlassian.uwc.converters.twiki;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.ui.Page;

import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * This class is a wrapper around the existing, but currently defunct
 * 'wiki importer' project which has a set of Java classes containing
 * Java regular expressions. Those classes were known as 'Cleaner' classes
 * <p/>
 * Basically you've got subclasses of the Converter class which get
 * executed by the UWC framework.
 * <p/>
 * Here we're dealing with 'Cleaner' classes wrapped in Converter classes. A bit confusing
 * but at least there is documentation which you're now reading :)
 *
 * @todo - put hyperlink to that proj. here
 * <p/>
 * For now these classes are being copied directly (just with a package
 * name change).
 */
public class TWikiRegexConverterCleanerWrapper extends BaseConverter {
    Logger log = Logger.getLogger("TWikiRegexConverterCleanerWrapper");

    // since the classes we're instantiating don't have
    // instance variables that change lets try just using
    // a simple cache
    public static HashMap<String, ContentCleaner> cleanerObjects = new HashMap();

    public void convert(Page page) {
        // get the class Cleaner name and instantiate it dynamically
        String className = getValue();
        ContentCleaner cc = null;
        if (cleanerObjects.containsKey(className)) {
            cc = cleanerObjects.get(className);
        }
        try {
//            Class newCleanerClass = Class.forName(className);
//            cc = (ContentCleaner) newCleanerClass.newInstance();
            cc = getCleanerClassFromCache(className);
            cleanerObjects.put(className, cc);
        } catch (ClassNotFoundException e) {
            log.error("class not found: " + className);
            log.error(e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            log.error("instantion problem for className: " + className);
            log.error(e.getMessage());
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        String converted = cc.clean(page.getOriginalText());
        page.setConvertedText(converted);
    }

    static HashMap cleanerCacheMap = new HashMap();
    static HashMap converterCacheMap = new HashMap();

    /**
     * at long last making some performance enhancements
     * here we are creating an object cache for the 'cleaners' which should help a bit
     *
     * @param cleaner TWiki cleaner class
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private static ContentCleaner getCleanerClassFromCache(String cleanerName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ContentCleaner twikiCleaner = (ContentCleaner) cleanerCacheMap.get(cleanerName);
        if (twikiCleaner == null) {
            Class c = Class.forName(cleanerName);
            twikiCleaner = (ContentCleaner) c.newInstance();
            cleanerCacheMap.put(cleanerName, twikiCleaner);
        }
        return twikiCleaner;
    }

    /**
     * here we are keeping a cache of converter objects which are wrapping the cleaners
     * and only instantiating when needed
     *
     * @param value
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static Converter getTWikiRegexConverterCleanerWrapper(String value) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Converter converter = (Converter) converterCacheMap.get(value);
        if (converter == null) {
            converter = new TWikiRegexConverterCleanerWrapper();
            converterCacheMap.put(value, converter);
        } else {
            return converter;
        }
        return converter;
    }
}