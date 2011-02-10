package com.atlassian.uwc.ui;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * A class representing a wiki page that is being converted to Confluence.
 * This holds the meta data needed to convert a Page.
 *
 * User: Rex (Rolf Staflin)
 * Date: 2006-94-05
 * Time: 15:40:25
 */
public class Page implements Comparable {
    /**
     * File the page represents
     */
    private File file;
    /**
     * text that the page has before each conversion
     */
    private String originalText;
    /**
	  * original source text. This should never be changed once
	  * it is set. Note: This is different from originalText in that
	  * originalText will be updated to reflect the most current
	  * text at the beginning of each syntax conversion.
     */
    private String unchangedSource;
    /**
     * text that the page has after the conversion
     */
    private String convertedText;
    /**
     * page name. Will be used by Confluence when the page is created in Confluence.
     */
    private String name;
    /**
     * filepath to the page
     */
    private String path;
    /**
     * set of attachments associated with this page
     */
    private Set<File> attachments;
	/**
	 * page version, used by the UWC Page History Framework. 
	 * @see http://confluence.atlassian.com/display/CONFEXT/UWC+Page+History+Framework
	 */
	private int version = 1; //the default is 1
	/**
	 * set of labels associated with this page
	 */
	private Set<String> labels;
	/**
     * list of page comments associated with this page
     * NOTE: At this time, we're only passing in the comment contents, 
     * as all other comment parameters either (a) can't be set with 
     * the remote api or (b) will not need to be assocated seperately (page id) 
     */
    private Vector<String> comments; //It's a vector instead of a set to preserver order
    /**
     * username of author who updated this page. If null, the administrator running the UWC will be used
     */
    private String author;
    /**
     * timestamp when the author updated this page.
     */
    private Date timestamp;
    
    /**
     * map providing information about which version is the latest for a given title.
     * There should only be data in this object if pages have been sorted by history in the ConverterEngine
     */
    private static HashMap<String, Integer> latestVersions = new HashMap<String, Integer>();
    
    /**
     * Optionally associate the page with a spacekey. The ConverterEngine will use this instead of the spacekey setting.
     */
    private String spacekey;
    /**
     * keep needed space data here so the ConverterEngine can create the space, if necessary
     * The key is the spacekey. The value is an array with name and description data in the 0th and 1st indexes,
     * respectively. The name and description data will only be used if the spacekey is not already in use, and 
     * therefore a space needs to be created.
     */
    private static HashMap<String,String[]> spaces = new HashMap<String, String[]>(); //spacekey -> [name, description]
    private boolean isBlog = false;
    private boolean isPersonalSpace = false;
    private String personalSpaceUsername = null;
    
    
    /**
     * Basic constructor. Creates a page with an empty path.
     * @param file The file to be converted.
     */
    public Page(File file) {
        init(file, "");
    }

    /**
     * Basic constructor.
     * @param file The file to be converted.
     * @param path A path used to construct a page hierarchy for
     *             some wikis.
     */
    public Page(File file, String path) {
        init(file, path);
    }

    /**
     * initializes the page 
     * @param file
     * @param path
     */
    private void init(File file, String path) {
        this.file = file;
        attachments = new HashSet<File>();
        labels = new HashSet<String>();
        comments = new Vector<String>();
        setPath(path);
    }

	/**
	 * used when sorting pages. 
	 * Takes into account page name and page version 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		Page b = (Page) o;
		
 		int versionA = this.version;
		int versionB = b.getVersion();
		
		String nameA = this.name;
		String nameB = b.getName();
		
		//make sure there's a default value so we don't get NPE
		if (nameA == null) nameA = "";
		if (nameB == null) nameB = "";
		
		//order by name - if name is the same, in order by version
		int compareValue = (nameA.compareTo(nameB));
		if (compareValue == 0) 
			compareValue = versionA - versionB;
		
		return compareValue;
	}

    /**
     * Adds a single attachment to the page. If the attachment was already
     * added (e.g., an image that appears several times on the page) it is not added again.
     * @param newAttachment The file to be attached to the page. It must not be null.
     */
    public void addAttachment(File newAttachment) {
        assert newAttachment != null;
        attachments.add(newAttachment);
    }

	/* Getters and Setters */

    public File getFile() {
        return file;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getUnchangedSource() {
        return unchangedSource;
    }
    
	 /**
	  * sets the unchangedSource field. Cannot be set more than once.
	  * @throws IllegalStateException if the source has already been set.
	  */
    public void setUnchangedSource(String unchangedSource) {
		  if (this.unchangedSource != null)
			  throw new IllegalStateException("Source Text has already been set. It may not be changed further.");
        this.unchangedSource = unchangedSource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the relative path of the page. This path is constructed by ConverterEngine.recurse().
     * Top-level (root) pages have an empty path, while other pages have paths with the elements
     * separated by <code>File.separator</code>, e.g. "/path/to/the/page.txt" on unix systems and
     * "\path\to\the\page.txt" on Windows systems.
     *
     * @return The path of the page.
     */
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getConvertedText() {
        return convertedText;
    }

    public void setConvertedText(String convertedText) {
        this.convertedText = convertedText;
    }

    public Set<File> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<File> attachments) {
        assert attachments != null;
        this.attachments = attachments;
    }

    public void setVersion(int version) {
    	//save latest version data
    	if (getLatestVersion(getName()) <= version) {
    		latestVersions.put(getName(), version);
    	}
		this.version = version;
	}

    public int getVersion() {
		return this.version;
	}
    
    /**
     * @return map of title -> latest version data.
     */
    public static HashMap<String, Integer> getLatestVersions() {
    	return latestVersions;
    }
    
    /**
     * Latest version data is noted when setVersion is called based on the name the object has
     * when setVersion is called.
     * @param name page name this version is associated with
     * @return latest version for title or 1 if no version set yet
     */
    public static int getLatestVersion(String name) {
    	Integer latest = getLatestVersions().get(name);
    	if (latest == null) return 1;
    	return latest;
    }

	public Set<String> getLabels() {
		return labels;
	}

	public void setLabels(Set<String> labels) {
		this.labels = labels;
	}
	
	/**
	 * @return set of labels as a comma delimited list. null if
	 * labels is null or empty
	 */
	public String getLabelsAsString() {
		if (this.labels == null) return null;
		if (this.labels.isEmpty()) return null;
		boolean first = true;
		String labelString = "";
		for (String label : this.labels) {
			if (first) first = false;
			else labelString += ", ";
			labelString += label;
		}
		return labelString;
	}
	
	/**
	 * adds the given label to the set of labels associated with this page,
	 * removes disallowed confluence label chars,
	 * and toLowerCases the label to: (a) avoid remote api errors and
	 * (b) better represent what the results of uploading a given label to Confluence will be. 
	 * If you don't want the labels to be processed, use the setLabels method instead.
	 * @param label
	 */
	public void addLabel(String label) {
		label = label.trim();
		label = label.replaceAll("[_ !#&()*,.:;<>?@\\[\\]\\^]", ""); //remove disallowed confluence tag chars
		label = label.toLowerCase();
		
		this.labels.add(label);
	}

	public Vector<String> getComments() {
		return comments;
	}

	public void setComments(Vector <String> comments) {
		this.comments = comments;
	}
	
	public void addComment(String comment) {
		this.comments.add(comment);
	}

	public boolean hasComments() {
		return !this.comments.isEmpty();
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	/* Space methods */
	public String getSpacekey() {
		return spacekey;
	}
	public void setSpacekey(String key) {
		this.spacekey = key;
	}
	
	/**
	 * sets the spacekey for this page, and also assigns name and description data
	 * to a map so that if the space needs to be created, we have that information.
	 * @param key
	 * @param name
	 * @param desc
	 */
	public void setSpace(String key, String name, String desc) {
		setSpacekey(key);
		String[] newdata = {name, desc};
		this.spaces.put(key, newdata);
	}
	/**
	 * gets the space data that's been saved for a particular spacekey, or null if no data exists for that key
	 * @param key
	 * @return array. 0th index is the name of the space, 1st index is the description
	 */
	public String[] getSpaceData(String key) {
		if (spaces.containsKey(key)) return spaces.get(key);
		return null;
	}
	/**
	 * true if the spaces data map has an entry for the given key
	 * @param key
	 * @return
	 */
	public boolean hasSpace(String key) {
		return spaces.containsKey(key);
	}
	
	public boolean isBlog() {
		return isBlog;
	}
	
	public void setIsBlog(boolean isBlog) {
		this.isBlog = isBlog;
	}

	public boolean isPersonalSpace() {
		return isPersonalSpace;
	}
	
	public void setIsPersonalSpace(boolean isPersonalSpace) {
		this.isPersonalSpace = isPersonalSpace;
	}
	
	public String getPersonalSpaceUsername() {
		return this.personalSpaceUsername;
	}
	
	public void setPersonalSpaceUsername(String username) {
		this.personalSpaceUsername = username;
	}
}
