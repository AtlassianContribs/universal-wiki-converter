package com.atlassian.uwc.exporters;

import java.io.File;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.atlassian.uwc.exporters.liferay.Params;
import com.atlassian.uwc.exporters.liferay.Util;
import com.atlassian.uwc.exporters.liferay.digester.Indigestion;
import com.atlassian.uwc.exporters.liferay.digester.LrWikiTree;
import com.atlassian.uwc.exporters.liferay.digester.Manifest;
import com.atlassian.uwc.exporters.liferay.digester.Page;
import com.atlassian.uwc.exporters.liferay.digester.Portlet;
import com.atlassian.uwc.exporters.liferay.digester.PressCancelException;
import com.atlassian.uwc.exporters.liferay.digester.WikiData;
import com.atlassian.uwc.exporters.liferay.digester.WikiPage;
import com.atlassian.uwc.ui.FileUtils;

/**
 * Liferay:
 * Only Creole and HTML format pages are handled.
 * Images attached to a Liferay page must be linked to in the page text i.e. [[filename.zip]].
 * Takes the latest version only, no history.

Simplified LAR structure. This ignores some unneeded directories and files.

    manifest.xml - Export timestamp, path to portlet-data.xml (Same dir as portlet.xml)
    groups
        comments - Comments data
        portlets
            113518
                portlet-data.xml - Primary index for all nodes, pages and attachments.
            bin - Attachment files
            nodes
                1113469.xml - Wiki last change date?
            pages - Page data (all versions, see portlet-data.xml to find current page versions)
        comments.xml - Links comment and page data



Confluence:
The HTML Macro must be enabled if you have HTML pages to upload.

 * @author SJC 
 *
 */
public class LiferayExporter implements Exporter {
	private static Logger log = Logger.getLogger(LiferayExporter.class);
	static final String VERSION = "1.1";
	static final String OPT_HELP = "help";
	static final String OPT_LARDIR = "lar";
	static final String OPT_OUTDIR = "out";
	static final String OPT_CON_START = "start";
	static final String OPT_ORIGINAL = "original";
	static final String OPT_NOHTML = "nohtml";
	static final String OPT_CON_ORPHANS = "orphans";
	private boolean running = false;
	private Map<String, String> properties;

	@Override
	public void cancel() {
		log.info("Export Cancelled.");
		this.running = false;
	}

	@Override
	public void export(Map properties) {
		this.running = true;
		setProperties(properties);

		try {
			Params params = new Params();
			
			// TODO get other properties and set them in the params
			// for instance opt.addOption(OPT_CON_ORPHANS, true, "Page name for orphan pages");
			
			params.setLarDirectory(getProp("src"));
			params.setOutputDir(new File(getProp("out")));

			convert(params);

		} catch (PressCancelException e) {
			// do nothing here as user has decided to abort
			log.debug("Received PressCancelException");
		} catch (Exception e) {
			log.error("Problem while exporting. Exiting.", e);
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}

		if (this.running) {
			log.info("Export Complete.");
		}

		this.running = false;
	}

	void checkCancel() {
		if (!this.running) {
			throw new PressCancelException();
		}
	}

	public void setProperties(Map<String, String> props) {
		this.properties = props;
	}

	public String getProp(String key) {
		return getProperties().get(key);
	}

	public Map<String, String> getProperties() {
		if (this.properties == null){
			this.properties = new HashMap<String, String>();
		}
		
		return this.properties;
	}

	public static void main(String[] args) {
		CommandLine cl = null;
		HelpFormatter hf = new HelpFormatter();
		Options opt = new Options();

		try {
			Util.initLog4j();

			opt.addOption(OPT_HELP, false, "Print help for this application.");
			opt.addOption(OPT_LARDIR, true, "Unzipped Lifieray .lar file directory");
			opt.addOption(OPT_OUTDIR, true, "Output directory");
			opt.addOption(OPT_CON_START, true, "Confluence Start page");
			opt.addOption(OPT_ORIGINAL, false, "Save original files from .lar");
			opt.addOption(OPT_NOHTML, false, "Uploads HTML formatted pages as {code}");
			opt.addOption(OPT_CON_ORPHANS, true, "Page name for orphan pages");

			BasicParser parser = new BasicParser();
			cl = parser.parse(opt, args);

			if (cl.hasOption(OPT_HELP)) {
				printUsage(hf, opt);
			} else {

				if (cl.hasOption(OPT_LARDIR) && cl.hasOption(OPT_OUTDIR)) {
					LiferayExporter wtx = new LiferayExporter();
					Params params = new Params();

					// the unzipped .lar file directory
					params.setLarDirectory(cl.getOptionValue(OPT_LARDIR));

					params.setOutputDir(new File(cl.getOptionValue(OPT_OUTDIR)));
					
					// confluenceStartPage defaults to Home, this opt can overide this
					String confluenceStartPage = cl.getOptionValue(OPT_CON_START);
					if (confluenceStartPage != null) {
						params.setConfluenceStartPage(confluenceStartPage);
					}
					
					// confluence page off root to hold orphans.
					String confluenceOrphanPage = cl.getOptionValue(OPT_CON_ORPHANS);
					if (confluenceOrphanPage != null) {
						params.setOrphanPages(confluenceOrphanPage);
					}					

					// copies the original pages into the destination. No conversion.
					params.setOriginal(cl.hasOption(OPT_ORIGINAL));

					// copies HTML pages as code
					params.setNoHtml(cl.hasOption(OPT_NOHTML));

					wtx.convert(params);
					log.info("Completed Liferay to Confluence conversion");
				} else {
					printUsage(hf, opt);
				}

				System.exit(0);
			}

		} catch (Exception e) {
			log.error("Main", e);
		}
	}

	private static void printUsage(HelpFormatter hf, Options opt) {
		hf.printHelp("WikiTransfer " + VERSION, "Use to pre process a Liferay Wiki export .lar file. "
				+ "Only looks at the latest version - no history is transfered.", opt,
				"\nE.G. WikiTransfer -lar C:\\WikiIn -out C:\\WikiOut\n", true);

		System.exit(1);
	}

	public void convert(Params params) throws IOException, SAXException {
		
		// make the output directory if it doesn't exist, if it exists delete it.
		createExportDir(params.getOutputDir());					
		
		LrWikiTree lrwt = new LrWikiTree();
		log.info("Input directory (unzipped .lar): " + params.getLarDirectory());
		readPagesInLarFile(params.getLarDirectory(), lrwt);

		checkCancel();		
		lrwt.linkChildren(false);

		WikiPage frontPage = lrwt.findPage(LrWikiTree.FRONTPAGE);
		frontPage.setOutDir(params.getOutputDir());
		log.info("Output directory: " + frontPage.getOutDir());

		lrwt.recursePages(frontPage);
		
		checkCancel();		
		lrwt.reformatAttachments();
		lrwt.saveAttachments(params.getLarDirectory(),
				params.getOutputDir() + File.separator + params.getAttachmentDir());

		checkCancel();
		lrwt.reformatHtmlPages(params.isNoHtml());

		ArrayList<WikiPage> oList = lrwt.findOrphans();
		checkCancel();

		if (!oList.isEmpty()) {
			// remove orphans from the linked list and save them in an Uncategorizd page.
			log.info("Placing " + oList.size() + " orphan pages in " + params.getOrphanPage() + " directory");
			lrwt.unlinkChildren(oList);

			String unCategorized = frontPage.getOutDir() + File.separator + frontPage.getPath() + File.separator
					+ params.getConfluenceStartPage() + File.separator + params.getOrphanPage();

			// create a page in the home directory that will hold all the orphans
			lrwt.createUncategorizedChildPage(unCategorized, params.getOrphanPage());

			lrwt.saveOrphanList(oList, unCategorized, params.isOriginal());
			log.info("---End of orphan list---\r\n");
		}

		// Set Confluence home page by changing the page title. This title gets
		// written as the filename directory on disk.
		frontPage.set__title(params.getConfluenceStartPage());
		checkCancel();

		lrwt.saveRecursive(frontPage, params.isOriginal());
	}

	/**
	 * The lar structure is defined by file paths inside XML files. This function parses the successive XML files and
	 * finds the page content and attachments. The page history is discarded, we only want the current data.
	 * 
	 * @param larDirectory
	 * @param lrwt
	 *            this is filled with Java objects created from the files on disk.
	 * @throws IOException
	 * @throws SAXException
	 */
	private void readPagesInLarFile(String larDirectory, LrWikiTree lrwt) throws IOException, SAXException {
		Indigestion xmlDigest = new Indigestion();
		Manifest mf = xmlDigest.readManifestXml(larDirectory);

		String path = mf.getPortlet().getPath();
		File file = new File(larDirectory, path);
		Portlet portlet = xmlDigest.readPortletXml(file);

		path = portlet.getPortlet().getPath();
		file = new File(larDirectory, path);

		WikiData wd = xmlDigest.readPortletDataXml(file);
		ArrayList<Page> pageList = wd.getPages().getPageList();

		for (Page page : pageList) {
			path = page.getPath();
			file = new File(larDirectory, path);
			WikiPage wp = xmlDigest.readWikiPageXml(file, page.getAttachments());
			lrwt.addPage(wp);
		}
	}

	private void createExportDir(File export) {
		if (export.exists()) {
			log.debug("Deleting existing export dir: " + export.getAbsolutePath());
			FileUtils.deleteDir(export);
		}
		
		if (!export.mkdirs()) {
			log.error("Could not create export dir: " + export.getAbsolutePath());
		}		
	}
	
}
