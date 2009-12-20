package com.atlassian.uwc.exporters;

import java.sql.SQLException;
import java.util.Map;

/**
 * Classes which export other wikis into the proper format
 * for the UWC to convert will implement this class
 */
public interface Exporter {

    /**
     * The UWC will invoke this method when the exporter is called
     * @param propertiesMap
     * @throws SQLException if a database connection fails
     * @throws ClassNotFoundException if a database driver cannot be instantiated
     */
    public void export(Map propertiesMap) throws ClassNotFoundException, SQLException;

    /**
     * signals Exporter to cancel the export
     */
    public void cancel();
    
}
