package com.atlassian.uwc.converters;

import java.util.Properties;

import com.atlassian.uwc.ui.ConverterErrors;
import com.atlassian.uwc.ui.Page;

/**
 * Implement this interface to create a Converter. Converters are identified
 * in the GUI, instantiated and the 'convert' method is called for each page when
 * the user clicks on 'Send to Confluence'
 *
 * Note: This interface is implemented by the class <code>BaseConverter</code>.
 * New converters are probably better off extending <code>BaseConverter</code>
 * than implementing <code>Converter</code>.
 */
public interface Converter {

	/**
     * Converts the text in a page. The converter should set
     * the convertedText field in the page to the result of the
     * conversion.
     * 
     * @param page The page to be converted, with originalText set.
     */
    public void convert(Page page); 
    public void setKey(String key);
    public String getKey();
    public void setValue(String value);
    public String getValue();
	public void setAttachmentDirectory(String attachmentDirectory);
	public ConverterErrors getErrors();
	/**
     * @return miscellaneous properties map
     */
	public Properties getProperties();
	/**
     * sets miscellaneous properties map provided by converter properties via ConverterEngine
     * @param properties
     */
	public void setProperties(Properties properties);
}
