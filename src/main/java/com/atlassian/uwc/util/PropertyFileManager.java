package com.atlassian.uwc.util;

import org.apache.log4j.Logger;

import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.Iterator;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The default Java Properties class almost works but it changes back
 * slashes both when reading and writing.
 *
 * This class simply reads a properties file into a TreeMap and writes
 * a TreeMap to a properties file. Comments are not currently persisted.
 */
public class PropertyFileManager {
    private static final String SEPERATOR = "=";
    static Logger log = Logger.getLogger("PropertyFileManager");

    /**
     * Read in a standard properties file to a TreeMap
     * @param fileLoc
     * @return a treeMap which matches the properties file
     * @throws IOException
     */
    public static TreeMap<String,String> loadPropertiesFile(String fileLoc) throws IOException {
        TreeMap<String,String> map = new TreeMap();
        File inputFile = new File(fileLoc);
        if (!inputFile.exists()) {
            log.error("Property file not found: "+fileLoc);
            return null;
        }
        RandomAccessFile ram = new RandomAccessFile(inputFile,"r");
        String line = null;
        while ((line=ram.readLine())!=null) {
            if (line.startsWith("#")) continue;
            int seperatorLoc = line.indexOf(SEPERATOR);
            if (seperatorLoc<=0) continue;
            String key = line.substring(0,seperatorLoc);
            String value = line.substring(seperatorLoc+1);
            map.put(key,value);
        }
        ram.close();
        return map;
    }

    /**
     * Wrtie a TreeMap to a properties file. If the file exists I think it will
     * overwrite it.
     * @param properties
     * @param fileLoc
     * @throws IOException
     */
    public static void storePropertiesFile(Map<String,String> properties, String fileLoc) throws IOException {
        File inputFile = new File(fileLoc);
        inputFile.delete();
        RandomAccessFile ram = new RandomAccessFile(inputFile,"rw");
        Set<String> keys = properties.keySet();
        for (String key: keys) {
            String value = properties.get(key);
            ram.writeBytes(key+SEPERATOR+value+"\n");
        }
        ram.close();
    }
}
