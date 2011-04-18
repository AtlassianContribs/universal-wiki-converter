package com.atlassian.uwc.converters.tikiwiki;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * handles page naming by using the page's metadata to get the pagename
 */
public class MetadataTitle extends BaseConverter {

    Logger log = Logger.getLogger(this.getClass());
    public void convert(Page page) {
        log.info("Getting Title from Metadata - start");
        String input = page.getOriginalText();
        String name = getNameFromMetadata(input);
        if (name != null)
            page.setName(name);

        //getting version, author and timestamp data
        Integer version = getVersionFromMetadata(input);
        if (version != null)
            page.setVersion(version.intValue());
        String author = getAuthorFromMetadata(input);
        if (author != null)
            page.setAuthor(author);
        Date timestamp = getTimestampFromMetadata(input);
        if (timestamp!=null) {
            page.setTimestamp(timestamp);
        }
        log.info("Getting Title from Metadata - complete: '"+name+"'/'"+author+"'/"+version+"/"+timestamp); //XXX REMOVE
    }

    String pagenameData = "pagename=(.*)";
    Pattern pagenamePattern = Pattern.compile(pagenameData);
    String versionData = "version=(.*)";
    Pattern versionPattern = Pattern.compile(versionData);
    String authorData = "author=(.*)";
    Pattern authorPattern = Pattern.compile(authorData);
    String timestampData = "lastmodified=(.*)";
    Pattern timestampPattern = Pattern.compile(timestampData);
    String descriptionData = "description=(.*)";
    Pattern descriptionPattern = Pattern.compile(descriptionData);

    /**
     * gets the title from the input's metadata
     * @param input tikiwiki export file contents. should include title metadata.
     * @return title or null, if could not figure it out
     */
    protected String getNameFromMetadata(String input) {
        Matcher metadataFinder = getMatcher(input);
        String pagename = null;
        if (metadataFinder.lookingAt()) {
            String metadata = metadataFinder.group(1);
            pagename = getPagename(metadata);
            pagename = decodeEntities(pagename);
        }
        return pagename;
    }

    protected Integer getVersionFromMetadata(String input) {
        Matcher metadataFinder = getMatcher(input);
        String version = null;
        if (metadataFinder.lookingAt()) {
            String metadata = metadataFinder.group(1);
            version = getVersion(metadata);
            version = decodeEntities(version);
        }
        try {
            return Integer.valueOf(version);
        } catch (Exception e) {
            log.error("bad version '"+version+"'");
            return null;
        }
    }

    protected String getAuthorFromMetadata(String input) {
        Matcher metadataFinder = getMatcher(input);
        String author = null;
        if (metadataFinder.lookingAt()) {
            String metadata = metadataFinder.group(1);
            author = getAuthor(metadata);
            author = decodeEntities(author);
        }
        if (author!=null && author.equals("admin"))
            author="ahumbert";
        else if (author!=null && author.equals("bkgrah"))
            author="ahumbert";
        else if (author!=null && author.equals("bkgret"))
            author="teicher";
        else //default: system
            author="confluence";
        return author;
    }

    protected Date getTimestampFromMetadata(String input) {
        Matcher metadataFinder = getMatcher(input);
        String timestamp = null;
        if (metadataFinder.lookingAt()) {
            String metadata = metadataFinder.group(1);
            timestamp = getTimestamp(metadata);
        }
        try {
            Long unixEpoch = Long.valueOf(timestamp);
            return new Date(unixEpoch.longValue() * 1000L);
        } catch (Exception e) {
            log.error("bad timestamp '"+timestamp+"'");
            return null;
        }
    }

    /**
     * Finds the pagename amongst the metadata
     * @param metadata
     * @return pagename or null, if no relevant pagename metadata
     */
    protected String getPagename(String metadata) {
        String pagename = null;
        if (metadata == null) return null;
        Matcher pagenameFinder = pagenamePattern.matcher(metadata);
        if (pagenameFinder.find()) {
            pagename = pagenameFinder.group(1);
            if (pagename.endsWith(";"))
                pagename = pagename.substring(0, pagename.length()-1);
        }
        return pagename;
    }

    protected String getVersion(String metadata) {
        String version = null;
        if (metadata == null) return null;
        Matcher versionFinder = versionPattern.matcher(metadata);
        if (versionFinder.find()) {
            version = versionFinder.group(1);
            if (version.endsWith(";"))
                version = version.substring(0, version.length()-1);
        }
        return version;
    }

    protected String getAuthor(String metadata) {
        String author = null;
        if (metadata == null) return null;
        Matcher authorFinder = authorPattern.matcher(metadata);
        if (authorFinder.find()) {
            author = authorFinder.group(1);
            if (author.endsWith(";"))
                author = author.substring(0, author.length()-1);
        }
        return author;
    }

    protected String getTimestamp(String metadata) {
        String timestamp = null;
        if (metadata == null) return null;
        Matcher timestampFinder = timestampPattern.matcher(metadata);
        if (timestampFinder.find()) {
            timestamp = timestampFinder.group(1);
            if (timestamp.endsWith(";"))
                timestamp = timestamp.substring(0, timestamp.length()-1);
        }
        return timestamp;
    }

    protected String decodeEntities(String pagename) {
        if (pagename == null) return null;
        String decoded = null;
        String encoding = "utf-8";
        try {
            decoded = URIUtil.decode(pagename, encoding);
        } catch (URIException e) {
            log.error("Problem decoding pagename with encoding: " + encoding);
            e.printStackTrace();
        }
        if (decoded != null) return decoded;
        return pagename;
    }

    /**
     * @param input
     * @return gets a matcher from the MetadataCleaner.allmetaPattern, and
     * the given input
     */
    private Matcher getMatcher(String input) {
        MetadataCleaner meta = new MetadataCleaner();
        return meta.allmetaPattern.matcher(input);
    }

}
