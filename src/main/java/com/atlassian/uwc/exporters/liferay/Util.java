package com.atlassian.uwc.exporters.liferay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/** General utility methods */
public class Util {

	/** Load properties from resource. */
	public static Properties load(Object context, String fileName) {
		Properties properties = null;
		InputStream inputStream = null;

		String currentDir = new File(".").getAbsolutePath();

		try {
			properties = new Properties();
			inputStream = new FileInputStream(fileName);
			properties.load(inputStream);
			inputStream.close();
		} catch (Exception e) {
			throw new IllegalArgumentException("Properties file: " + currentDir + " " + fileName, e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					/* Ignore */
				}
			}
		}
		return properties;
	}

	public static Reader loadResource(Object context, String fileName) {
		Reader properties = null;
		
		String currentDir = new File(".").getAbsolutePath();

		try {						
			InputStream inputStream	= context.getClass().getResourceAsStream(fileName);						
			properties = new InputStreamReader(inputStream);
			
//			inputStream.close();
		} catch (Exception e) {
			throw new IllegalArgumentException("Properties file: " + currentDir + " " + fileName, e);
		}

		return properties;
	}

	static void printClasspath() {
		System.out.println("Classpath:-");

		ClassLoader cl = ClassLoader.getSystemClassLoader();

		URL[] urls = ((URLClassLoader) cl).getURLs();

		for (URL url : urls) {
			System.out.println(url.getFile());
		}

	}

	/** Load xml file */
	public static Document loadXml(String fileName) {
		Document doc = null;
		InputStream inputStream = null;
		String currentDir = new File(".").getAbsolutePath();

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			inputStream = new FileInputStream(fileName);

			doc = dBuilder.parse(inputStream);
			doc.getDocumentElement().normalize();

			inputStream.close();
		} catch (Exception e) {
			throw new IllegalArgumentException("XML file path: " + currentDir, e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					/* Ignore */
				}
			}
		}

		return doc;
	}

	public static String printDom(Document document) throws TransformerException {

		String str = "";

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		Source source = new DOMSource(document);
		Result output = new StreamResult(byteOut);
		transformer.transform(source, output);
		str = byteOut.toString();

		return str;
	}

	/** Use default log4j config. */
	public static void initLog4j() {
		Logger root = Logger.getRootLogger();
		if (!root.getAllAppenders().hasMoreElements()) {
			BasicConfigurator.configure();
			root.warn("log4j configuration file not found using defaults!!!");
		}
	}

}
