package com.atlassian.uwc.exporters;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * contains some basic methods that SQL style exporters can take advantage of
 *
 */
public abstract class SQLExporter implements Exporter {

	//log4j
	Logger log = Logger.getLogger(this.getClass());

	//default cancel handling - the UI uses this to pass a cancel signal. 
	//To use: set the running param to true when you start the export, and false when you end it
	//And check the running parameter at the major milestones in the export methods, to allow the
	//cancel signal to do something useful
	boolean running = false;
	public void cancel() {
		String message = "Exporter - Sending Cancel Signal";
    	log.debug(message);
    	this.running = false;
	}
	
	/* Convenience database methods and objects */

	/**
	 * database connection.
	 */
	protected Connection con;

	/**
	 * connects to database
	 * @param driver class used to connect to the database
	 * @param url database url
	 * @param name name of database
	 * @param login database login
	 * @param pass pass that goes with database login
	 * @throws ClassNotFoundException if could not load driver class
	 * @throws SQLException if could not connect to database
	 * @return connection object
	 */
	protected Connection connectToDB(String driver, String url, String name, String login, String pass) throws ClassNotFoundException, SQLException {
		try {
			//load driver
			Class.forName(driver);
			//connect to db
			con = DriverManager.getConnection(url + "/" + name, login, pass);
		} catch (ClassNotFoundException e) {
			log.fatal("Could not load JDBC driver: " + driver);
			throw e;
		} catch (SQLException e) {
			log.fatal("Could not connect to database: " + name + ". Check database settings: url, name, user, and pass.");
			throw e;
		}
		return con;
	}

	/**
	 * closes the currently opened database.
	 */
	protected void closeDB() {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				log.error("Error while closing JDBC connection");
				e.printStackTrace();
			}
		}
	}

	/**
	 * runs an sql query.
	 * @param sql the sql string to be run
	 * @param isUpdate true if is an update, delete, or such type query. false, if just select
	 * @return ResultSet object with results from select query, or null. (Notice, return will
	 * always be null if isUpdate is true)
	 * @throws SQLException if an error happens while executing the sql command
	 */
	protected ResultSet sql(String sql, boolean isUpdate) throws SQLException {
		Statement sqlStatement = null;
		String message = "";
		ResultSet result = null;
		try {
			message = "Creating statement: "  + sql; 
			sqlStatement = con.createStatement();
			log.debug(message);
			message = "Executing statement: " + sql; 
			if (isUpdate) {
				sqlStatement.executeUpdate(sql);
			}
			else {
				result = sqlStatement.executeQuery(sql);
			}
			log.debug(message);
			SQLWarning warn = sqlStatement.getWarnings();
			while (warn != null) {
				log.warn(warn.getErrorCode() + "\n" + 
						warn.getMessage() + "\n" + 
						warn.getSQLState());
				warn = warn.getNextWarning();
			}
		} catch (SQLException e) {
			log.error("Error while: " + message);
			throw e;
		}
		return result;
	}

	/**
	 * Runs an sql SELECT query.
	 * @param sql String, sql SELECT query
	 * @return ResultSet of select query results
	 * @throws SQLException if an error occurs while executing the sql command
	 */
	protected ResultSet sql(String sql) throws SQLException {
		return sql(sql, false);
	}

	/* Convenience File IO methods */
	
	/**
	 * writes the text to the path with the encoding
	 */
	protected void writeFile(String path, String text, String encoding) {
	    try {
	    	FileOutputStream fw = new FileOutputStream(path);
	    	OutputStreamWriter outstream = new OutputStreamWriter(fw, encoding);
	        BufferedWriter out = new BufferedWriter(outstream);
	        out.write(text);
	        out.close();
	    } catch (IOException e) {
	    	log.error("Problem writing to file: " + path);
	    	e.printStackTrace();
	    }
	}
}
