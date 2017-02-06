/*
 * SwikiFile.java
 *
 * Created on March 5, 2007, 3:07 PM
 *
 * Represents an XML file holding one Swiki page
 */

package com.atlassian.uwc.exporters;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.Character;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author sitongia
 */
public class SwikiFile {
	private static final String FILE_SEP = System.getProperty("file.separator");
	
	private Logger log = Logger.getLogger(this.getClass());
    
    private File input_directory;
    private File output_directory;
    private File attachmentsOutDir;
    private File attachmentsInDir;
    private String name;
    private String text;
    private String number;
    private Element root;
    private StringBuffer buffer;
    private static HashMap<String, String> linksFound = new HashMap<String, String>();
    
    /**
     * Creates a new instance of SwikiFile
     */
    public SwikiFile(File input_directory, File attachmentsInDir, File output_directory, 
    		File attachmentsOutDir, String input_file) {
        
    	String[] fileSplit = input_file.split("\\.");

    	//linksFound.put(fileSplit[0] , input_file);
    	
        log.info("Processing "+input_file);

        this.input_directory = input_directory;        
        this.output_directory = output_directory;
        this.attachmentsOutDir = attachmentsOutDir;
        this.attachmentsInDir = attachmentsInDir;
        
        setNumber(input_file);
        
        File inputFile = new File(input_directory, input_file);
        if (!inputFile.exists()) {
            log.error("Input directory or file is in error.");
            return;
        }

        // Use JDOM to handle the XML
        Document Doc=this.readDocument(inputFile);
        
        // The name of the page is in the <name> element
        root = Doc.getRootElement();
        
        setName();
        setText();
        linksFound.put(fileSplit[0] , name);
        buffer = new StringBuffer();
    }
    
    /**
     * Get the numerical value of the original filename.
     */
    public String getNumber() {
        return number;
    }
    
    /**
     * Set the numerical value of the original filename.
     */
    private void setNumber(String name) {
        String[] parts = name.split("\\.");
        number = parts[0];
    }
    
    /**
     * Get the string value of the new filename.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the string value of the new filename from
     * the XML <name/> element.
     */
    private void setName() {
        // The name content of the page is in the <name> element
        Element nameElement = root.getChild("name");
        name = nameElement.getText();
        // remove any swiki referance in the name
        name = name.replaceAll("[S|s][W|w][I|i][K|k][I|i]", "");
        name = name.replaceAll("/", "-");
        name = name.trim();
    }
    
    /**
     * Get the text data.
     */
    public String getText() {
        return text;
    }
    
    /**
     * Set the string value of the XML <text/> element.
     */
    private void setText() {
        // The text body content of the page is in the <text> element
        Element textElement = root.getChild("text");
        text = textElement.getText();
    }
    
    /**
     * Convert the links in the file to references to the filename.
     *
     * Parse the page, looking for page links, which are of the form
     * *#* where # is the name of the page linked to, in the same directory
     * and is #.xml

     */
    public void convert() {
        
        // TODO: pathological cases to consider:
        // TODO: case of '**'?  Two asterisks that aren't a link
        // TODO: case of '*' at end of text!
        // TODO: case of '*a' at end of text!
        // TODO: case of '*666' at end of text!
        /*
        StringBuffer link = new StringBuffer();
        Character letter, nextletter;
        boolean isDescLink=false;
        
        for (int i = 0; i < text.length(); i++) {
            letter = Character.valueOf(text.charAt(i));

            if (letter.compareTo('*') == 0) {
                // Could be the start of a link
                link.setLength(0);
                for (int j = 1; j < 100; j++) {
                    if (i+j == text.length()) {
                        // Asterisk happens to appear near end of file
                        buffer.append(letter);
                        break;
                    }
                    nextletter = Character.valueOf(text.charAt(i+j));
                    if (Character.isDigit(nextletter)) {
                        // Save letter in buffer for the numerical value of link
                        link.append(nextletter);
                    } else if (nextletter.compareTo('*') == 0 && j > 1) {
                        // End of link
                        //System.out.println("Found link "+link);
 
                        // Instantiate a page representing this XML file in the swiki 
                        if(!linksFound.containsKey(link.toString())){
                        	SwikiFile linked_page = new SwikiFile(input_directory, attachmentsInDir,
                        		output_directory, attachmentsOutDir, link+".xml");
                        
                        	// Convert it
                        	linked_page.convert();
                        	linked_page.save();
                        	// Write the link to this newly converted page
                        	String newname = linked_page.getName();
                        	//TODO: remove spaces from name
                        	buffer.append('[');
                        	buffer.append(newname);
                        	buffer.append(']');
                        
                        	i+=j;
                        	break;
                        }
                        else
                        {
                        	buffer.append('[');
                        	buffer.append(linksFound.get(link.toString()));
                        	buffer.append(']');
                        	i+=j;
                        	break;
                        }
                        
                    } else {
                        // Not a link; write the letter out
                        buffer.append(letter);
                        break;
                    }
                }
            } else {
                // Not a link; write the letter out
                buffer.append(letter);
            }
        }
	*/
    	processFalseLinks();
    	processDirectLink();
    	processDescriptionLink();
    	
    }
    
    /************************************************************************
     * To process link like *1*
     */
    public void processDirectLink()
    {
    	Pattern pattern = null;
		Matcher matcher = null;
		StringBuffer sb = new StringBuffer();
		boolean found=false;		
		
		pattern = Pattern.compile("\\*([\\d]+)\\*");
		matcher = pattern.matcher(buffer.toString());
		while(matcher.find())
		{
			found=true;
			String link = matcher.group();
			String converted = link.replaceFirst("\\*", "");
			converted = converted.replaceAll("\\*$", "");
			if(!linksFound.containsKey(converted.toString())){
             	SwikiFile linked_page = new SwikiFile(input_directory, attachmentsInDir,
             		output_directory, attachmentsOutDir, converted+".xml");
             
             	// Convert it
             	linked_page.convert();
             	linked_page.save();
             	// Write the link to this newly converted page
             	String newname = linked_page.getName();
             	matcher.appendReplacement(sb, '[' + newname + ']');
			}
			else
			{
				matcher.appendReplacement(sb, '[' + linksFound.get(converted.toString()) + ']');
			}
		}
		matcher.appendTail(sb);

		if(found)
			buffer = sb;
		
    }
    
    /***************************************************************
     * to process the link like [ abc>1]
     */
    public void processDescriptionLink()
    {
    	Pattern pattern = null;
		Matcher matcher = null;
		StringBuffer sb = new StringBuffer();
		boolean found=false;		
		
		pattern = Pattern.compile("\\*.*\\>\\d*\\*");
		matcher = pattern.matcher(buffer.toString());
		while(matcher.find())
		{
			found=true;
			String buffer = matcher.group();
			int index=buffer.length();
			int length=buffer.length();
			while (index > 0)
			{
				if (buffer.charAt(index - 1) == '>')
					break;
				index--;
			}
			
			String ss1=buffer.substring(0, index);
			String ss2=buffer.substring(index, length - 1);
			if(!linksFound.containsKey(ss2.toString())){
             	SwikiFile linked_page = new SwikiFile(input_directory, attachmentsInDir,
             		output_directory, attachmentsOutDir, ss2+".xml");
             
             	// Convert it
             	linked_page.convert();
             	linked_page.save();
             	// Write the link to this newly converted page
             	String newname = linked_page.getName();
             	matcher.appendReplacement(sb, '*' + ss1 + newname + '*');
			}
			else
			{
				matcher.appendReplacement(sb, '*' + ss1 + linksFound.get(ss2.toString()) + '*');
			}
		}
		matcher.appendTail(sb);
		if(found)
			buffer = sb;
    }   
    /**
     * Write the file to the output name given by the value of
     * the XML <name/> element.
     */
    public void save() {
        // Output file name is content of <name>
    		
        File output_file = new File(output_directory, getName()+".txt");

        try {
            FileWriter pageFW = new FileWriter(output_file);
            pageFW.write(buffer.toString());
            pageFW.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
       
    }
    /**
     * Swiki Attachments are stored in uploads/#. The # is related to the #.xml page.
     * e.g. Attachments for page 9.xml will be in uploads/9. These will be copied into
     * the exported/attachments directory
     * @author Kelly Meese
     *
     */
    public void copyAttachments()
    {
    	log.info("Starting Copying attachments.");
    	File uploads = attachmentsInDir;

    	if(uploads.exists()){
        
    		for(File srcFileName : uploads.listFiles()) {
    			//System.out.println("Src file: " + srcFileName.getName());
    			if(srcFileName.isFile()) {
    				copyToDir(srcFileName, new File(attachmentsOutDir.getAbsolutePath() + FILE_SEP +
    	    				srcFileName.getName()));

    			}else if(srcFileName.isDirectory()){
    				File uploadDir = srcFileName;
    				for(File fileName : uploadDir.listFiles()){
    					copyToDir(fileName, new File(attachmentsOutDir.getAbsolutePath() + FILE_SEP + 
    							fileName.getName()));
    				}
    					
    			}
    		}

    	}
    	log.info("Copy attachments complete.");
    	
    }
    /**
     * Copies attachments from the source to dest. It uses the attachments in and out dir
     * for finding attachments and copying them for the attachments parser.
     * @param srcFileName
     * @param dstFileName
     */
    private void copyToDir(File srcFileName, File dstFileName){
		try {

			// Create channel on the source
			FileChannel srcChannel = new FileInputStream(srcFileName).getChannel();

			// Create channel on the destination
			FileChannel dstChannel = new FileOutputStream(dstFileName).getChannel();

			// Copy file contents from source to destination
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

			// Close the channels
			srcChannel.close();
			dstChannel.close();
		} catch (IOException e) {
			log.error("Unable to copy file: " + e.getMessage());
		}
    }
    /**
     * Convert the remaining pages that do not have pages linked to them.
     * 
     */
    public void convertRemainingPages(){
    	
    	log.info("Converting remaining pages");
    	for(String fileName : input_directory.list()){
    	
    		if(fileName.endsWith(".xml")){
 
    			if(!linksFound.containsValue(fileName)){
    				SwikiFile linked_page = new SwikiFile(input_directory, attachmentsInDir,
                    		output_directory, attachmentsOutDir, fileName);
    				linked_page.convert();
    				linked_page.save();
    			}
    		}
    		
    		
    	}
    	log.info("Conversion of remaining pages complete.");
    	
    }
    /**
     * Once the exporter has finished clear out the links.
     */
    public void clearLinks()
    {
    	linksFound.clear();
    }
    
    /*****************************************************
	 * To read the xml file and build an XML Document
	 * @param fileName
	 * @return
	 */
	public Document readDocument(File file) {
		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			byte[] buf1 = new byte[1024];
			byte[] buf2 = new byte[0], buf3 = null;
			int size = 0;
			int len;
			while ((len = in.read(buf1)) > 0) {
				buf3 = new byte[size + len];
				System.arraycopy(buf2, 0, buf3, 0, size);
				System.arraycopy(buf1, 0, buf3, size, len);
				size += len;
				buf2 = new byte[size];
				System.arraycopy(buf3, 0, buf2, 0, size);

			}
			in.close();
			//replace the bad bytes with spaces
			for (int i = 0; i < size; i++) {
				if (buf3[i] == 6 || buf3[i] == 16 || buf3[i] > 127 || buf3[i] < 0)
					buf3[i] = 32;
			}
			ByteArrayInputStream bis = new ByteArrayInputStream(buf3);
			doc = builder.build(bis);

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (JDOMException ex) {
			ex.printStackTrace();
		}

		return doc;
	}
	
	/*************************************************************************
	 * to convert the false links ([something]) from swiki to italian style
	 */
	public void processFalseLinks()
	{
		Pattern pattern = null;
		Matcher matcher = null;
		StringBuffer sb = new StringBuffer();
		Boolean found = false;
		pattern = Pattern.compile("\\[([^\\]]*)\\]");
		matcher = pattern.matcher(text);
		while(matcher.find())
		{
			found = true;
			String link = matcher.group();
			String converted = link.replaceFirst("\\[", "\\\\\\\\[");
			converted = converted.replaceAll("\\]", "\\\\\\\\]");
			matcher.appendReplacement(sb, converted);
		}
		matcher.appendTail(sb);

		if(found)
			buffer = sb;
		else
			buffer = new StringBuffer(text);
	}

    
}
