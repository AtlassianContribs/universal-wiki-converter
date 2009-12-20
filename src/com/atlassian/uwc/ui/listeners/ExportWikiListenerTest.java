package com.atlassian.uwc.ui.listeners;

import java.io.File;
import java.util.Iterator;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.FeedbackWindow;
import com.atlassian.uwc.ui.UWCGuiModel;

public class ExportWikiListenerTest extends TestCase {

	private static final String CONF_DEFAULT = "conf";
	private static final String CONF_LOCAL = "conf-local";
	private static final String TEST_EXPORTSETTINGS_DIR = "/Users/laura/Code/Subversion/universal-wiki-converter/devel/sampleData/mediawiki/testexporter";
	ExportWikiListener tester = null;
	private String dir;
	
	private JComboBox wikitypes;
	private UWCGuiModel model;
	private Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		wikitypes = new JComboBox();
		model = new UWCGuiModel();
		this.dir = CONF_DEFAULT;
		
		//note: if we use a different dir, we need to call these methods again
		setupModel(this.dir);
	}

	public void testComboBoxSetting() {
		//just a reminder that we're using the default directory by ... default.
		String input = "mediawiki";
		wikitypes.setSelectedItem(input);
		assertEquals(input, wikitypes.getSelectedItem());
	}
	
	public void testExportWiki() {
		this.dir = CONF_LOCAL;
		setupModel(this.dir);
		wikitypes.setSelectedItem("mediawiki"); //I'm pretty clear on what the mediawiki exporter should do.
		
		//check that existing exported dir doesn't exist
		String EXPORT_DIR = "/Users/laura/Desktop/exported_mediawiki_pages";
		File exportFile = new File(EXPORT_DIR);
		if (exportFile.exists()) {
			String message = "You need to delete '" + EXPORT_DIR + "' directory, first.";
			log.error(message);
			fail(message);
		}
		
		//test pre-export feedback
		ExportWikiListener.Feedback expected = ExportWikiListener.Feedback.NONE;
		assertEquals(expected, tester.getFeedback());
		
		//XXX Remember we need the database to be on in order for the export to work.
		
		//test exporting
		tester.exportWiki();
		
		//test directory was created. 
		assertTrue (exportFile.exists()); 			//exists
		assertTrue (exportFile.isDirectory());		//is a dir
		
		//test that it has contents
		String[] exportChildren = exportFile.list();
		int numExportChildren = exportChildren.length;
		assertTrue (numExportChildren > 0);	
		
		//look for at least one specific file that should be there
		boolean found = false;
		for (String child : exportChildren) {
			File childFile = new File(EXPORT_DIR + File.separator + child);
			assertTrue(childFile.exists());
			
			//look for the Pages directory
			if (child.equals("Pages")) {
				assertTrue(childFile.exists());
				assertTrue(childFile.isDirectory());
				found = true;
				break;
			}
		}
		assertTrue("No Pages directory.", found);
		
		//what's the result for a succesful export
		expected = ExportWikiListener.Feedback.OK;
		assertEquals(expected, tester.getFeedback());
	}
	
	public void testNoExporterForThatWikitype() {
		//what's the result if the wikitype has no exporter
		
		//still using the default directory, but we have to explicitly set tikiwiki
		//in the model, 'cause we can't select a non-existant ite,
		wikitypes.setModel(new DefaultComboBoxModel(new String []{"tikiwiki", "mediawiki"}));
		wikitypes.setSelectedItem("tikiwiki"); //not exportable at this time
		tester.exportWiki();
		ExportWikiListener.Feedback expected = ExportWikiListener.Feedback.NO_EXPORTER_FILE;
		assertEquals(expected, tester.getFeedback());
		
	}
	public void testDbConnectionRefused() {
		//what's the result if the db connection is refused
		String test = "dbtest";
		setupSettingsTest(test);
		ExportWikiListener.Feedback expected = ExportWikiListener.Feedback.DB_FAILURE;
		assertEquals(expected, tester.getFeedback());
	}


	public void testBadSettings() {
		//what's the result if any of the non-db settings are bad
		String test;
		ExportWikiListener.Feedback expected;
		//bad exporter class
		test = "badclass";
		expected = ExportWikiListener.Feedback.BAD_EXPORTER_CLASS;
		setupSettingsTest(test);
		assertEquals(expected, tester.getFeedback());
		
		//bad db name
		test = "badDbName";
		expected = ExportWikiListener.Feedback.DB_FAILURE;
		setupSettingsTest(test);
		assertEquals(expected, tester.getFeedback());
		
		//bad dbUrl
		test = "badDbUrl";
		expected = ExportWikiListener.Feedback.DB_FAILURE;
		setupSettingsTest(test);
		assertEquals(expected, tester.getFeedback());
		
		//bad driver
		test = "badDriver";
		expected = ExportWikiListener.Feedback.DB_DRIVER_FAILURE;
		setupSettingsTest(test);
		assertEquals(expected, tester.getFeedback());
		
		//bad login
		test = "badLogin";
		expected = ExportWikiListener.Feedback.DB_FAILURE;
		setupSettingsTest(test);
		assertEquals(expected, tester.getFeedback());
		
		//bad password
		test = "badPass";
		expected = ExportWikiListener.Feedback.DB_FAILURE;
		setupSettingsTest(test);
		assertEquals(expected, tester.getFeedback());
		
		//bad prefix
		test = "badPrefix";
		expected = ExportWikiListener.Feedback.DB_FAILURE;
		setupSettingsTest(test);
		assertEquals(expected, tester.getFeedback());

	}
	/* Helper Methods */

	/**
	 * prepares combobox model that will be used by the tester object
	 * @param dir
	 */
	private void setupModel(String dir) {
		wikitypes.setModel(new DefaultComboBoxModel(model.getExportTypes(dir)));
		tester = new ExportWikiListener(wikitypes, model, dir, new FeedbackWindow() );
	}
	

	/**
	 * helper method to test a particular file's bad setting
	 * @param test identifying string for the particular file
	 */
	private void setupSettingsTest(String test) {
		this.dir = TEST_EXPORTSETTINGS_DIR; //this is where our test prop files will live
		//we've changed the dir. the model has to be reset
		setupModel(this.dir);
		wikitypes.setSelectedItem(test); //looks for exporter.dbtest.properties
		
		tester.exportWiki();
	}
	
}
