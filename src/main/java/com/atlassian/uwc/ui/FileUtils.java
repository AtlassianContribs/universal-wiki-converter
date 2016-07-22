package com.atlassian.uwc.ui;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

/**
 * A couple of random file functions that are used by the engine.
 * User: Rex (Rolf Staflin)
 * Date: 2006-apr-05
 * Time: 16:39:31
 */
public class FileUtils {

    static final Logger log = Logger.getLogger(FileUtils.class);
    /**
     * Make sure the output directory exists
     */
    public static void createOutputDirIfNeeded() {
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
    }

    /**
     * Reads a text file into a String object line by line, converting line breaks to the local format.
     *
     * @param inputFile The name of and path to the file
     * @param charset The Charset of the file
     * @return a String with the file contents.
     * @throws java.io.IOException
     */
    public static String readTextFile(File inputFile, Charset charset) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        InputStreamReader isr = new InputStreamReader(fis, charset);
        BufferedReader reader = new BufferedReader(isr);

        StringBuffer contents = new StringBuffer();
        String line;
        String separator = System.getProperty("line.separator");
        while (( line = reader.readLine()) != null){
          contents.append(line).append(separator);
        }
        fis.close();
        isr.close();
        return contents.toString();
    }
    
    /**
     * Reads a text file into a String object line by line, converting line breaks to the local format.
     *
     * @todo The character set is hard coded to UTF-8, but it should be configurable by the user.
     * The best thing would be to fill a combo box with all available character sets, obtained from
     * Charset.availableCharsets().
     *
     * @param inputFile The name of and path to the file
     * @return a String with the file contents.
     * @throws java.io.IOException
     */
    public static String readTextFile(File inputFile) throws IOException {
    	Charset charset = Charset.forName("UTF-8");
    	return readTextFile(inputFile, charset);
    }
    
    public static String readGzipFile(File file) throws IOException {
    	FileInputStream fis = new FileInputStream(file);
    	GZIPInputStream gis = new GZIPInputStream(fis);
        InputStreamReader isr = new InputStreamReader(gis);
        BufferedReader reader = new BufferedReader(isr);

        StringBuffer contents = new StringBuffer();
        String line;
        String separator = System.getProperty("line.separator");
        while (( line = reader.readLine()) != null){
          contents.append(line).append(separator);
        }
        fis.close();
        isr.close();
        return contents.toString();
    }

    /**
     * Creates or truncates a file and then writes a string to it.
     *
     * Note that errors are not reported from this method.
     *
     * @param text The text to be written
     * @param filePath The file name or path
     */
    public static void writeFile(String text, String filePath) {
        BufferedWriter writer = null;
        try {
            //writer = new BufferedWriter(new FileWriter(filePath));
        	writer =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath),"UTF8"));
            writer.write(text);
        } catch (IOException e) {
            String message = "Error writing to file " + filePath +
            	"\n" +
            	"Note: Output file cannot be written to disk. Check permissions.";
			log.error(message, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                    // Do nothing
                }
            }
        }
    }
    
    /**
     * Writes bytes to the given filePath
     * @param bytes
     * @param filePath
     * @return true, if write was successful.
     * if false, one of several problems occurred.
     * Possibly: couldn't find file, couldn'y write to file, or
     * couldn't close file. 
     * Check logs for error messages.
     */
    public static boolean writeFile(byte[] bytes, String filePath) {
    	FileOutputStream out = null;
    	log.debug("Preparing to write to file: " + filePath);
    	try {
			out = new FileOutputStream(filePath);
		} catch (FileNotFoundException e) {
			log.error("Could not create outputstream for file: " + filePath);
			e.printStackTrace();
			return false;
		}
		try {
			out.write(bytes);
		} catch (IOException e) {
			log.error("Problem writing bytes to " + filePath);
			e.printStackTrace();
			return false;
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				log.error("Problem closing file: " + filePath);
				e.printStackTrace();
				return false;
			}
		}
		log.debug("Wrote bytes to file successfully.");
		return true;
    }

    /**
     * Returns the contents of the file in a byte array. A byte array is
     * what we can pass to XMLRPC-Confluence to upload an attachment
     * (copied from O'Reilly)
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
	
	/**
	 * recursively deletes a directory.
	 * If the passed file is a file, it will simply delete it.
	 * It the passed file is a directory, it will delete it and all it's contents.
	 * If the passed file does not exist, it will be ignored.
	 * @param dir
	 */
	public static void deleteDir(File dir) {
		if (!dir.exists()) return; //if the directory isn't existing, then ignore it
		if (!dir.isDirectory()) {
			dir.delete(); //delete a file
			return;
		}
		//look through each file and delete them 
		File[] files = dir.listFiles();
		for (File file : files) {
			deleteDir(file);
		}
		dir.delete(); //delete the empty directory
	}
}
