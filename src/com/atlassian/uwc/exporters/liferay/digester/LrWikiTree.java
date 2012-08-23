package com.atlassian.uwc.exporters.liferay.digester;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

/**
 * Liferay (LR) wiki page processor.
 * 
 * @author Developer
 * 
 */
public class LrWikiTree {
	public static final String FRONTPAGE = "FrontPage";

	private ArrayList<WikiPage> pageList = new ArrayList<WikiPage>();
	private static Logger _log = Logger.getLogger(LrWikiTree.class);

	/**
	 * Adds pages to the internal page list. We only keep the current page and discard historical pages.
	 * 
	 * @param page
	 */
	public void addPage(WikiPage page) {
		// if we have an existing page check the version and discard ancient pages
		WikiPage wp = findPage(page.get__title());

		if (wp != null) {
			Double version = wp.getVersion();

			// only keep latest version of the page
			if (page.getVersion() > version) {
				pageList.remove(wp);
				pageList.add(page);
			}
		} else {
			// no previous version so add the page
			// _log.debug("Adding Page: " + page.get__title());
			pageList.add(page);
		}
	}

	/**
	 * Finds pages that don't have parents and are not named Frontpage.
	 * 
	 * @return
	 */
	public ArrayList<WikiPage> findOrphans() {
		ArrayList<WikiPage> retval = new ArrayList<WikiPage>();

		for (WikiPage page : pageList) {
			String parentTitle = page.get__parentTitle();

			if (parentTitle.isEmpty() && !page.get__title().equals(FRONTPAGE)) {
				retval.add(page);
			} else if (!page.get__redirectTitle().isEmpty()) {
				retval.add(page);
			}
		}

		return retval;
	}

	/**
	 * Creates a page in the home directory that will hold all the orphans. Also creates the directory structure on disk
	 * for the pages.
	 * 
	 * @param outputDir
	 * @throws IOException
	 */
	public void createUncategorizedChildPage(String outputDir, String orphanPage) throws IOException {
		String uncategorizedPage = "== Overview ==\r\n\r\nPages that don't have parents are placed here after transfering the Wiki into Confluence.";

		File dir = new File(outputDir);
		dir.mkdirs();

		File unCat = new File(outputDir + File.separator + orphanPage);

		write(unCat, uncategorizedPage);
	}

	/**
	 * Copies the page content of the files in the list to the output directory.
	 * 
	 * @param list
	 * @param outputDir
	 * @throws IOException
	 */
	public void saveOrphanList(ArrayList<WikiPage> list, String outputDir, boolean original) throws IOException {

		for (WikiPage page : list) {
			page.setDepth(0); // page is an orphan so reset it's place to the root in the hierarchy
			recursePages(page);

			page.setPath("");
			page.setOutDir(new File(outputDir));

			saveOrphanRecursive(page, original);
		}
	}

	/**
	 * Writes the content of the orphaned pages onto the Uncategorized directory. Child pages are written maintaining
	 * the hierarchy in the file system.
	 * 
	 * @param frontPage
	 * @param copyOriginal
	 *            true to copy the original xml file to the destination
	 * @throws IOException
	 */
	public void saveOrphanRecursive(WikiPage frontPage, boolean copyOriginal) throws IOException {
		String padding = "";
		for (int i = 0; i < frontPage.getDepth(); i++) {
			padding += "    ";
		}

		_log.info(padding + frontPage.get__title());

		File dir = new File(frontPage.getOutDir() + File.separator + frontPage.getPath());
		dir.mkdirs();

		// _log.info("++" + dir.getCanonicalPath());

		// _log.debug("Create Dir: " + dir);
		if (copyOriginal) {
			copyFile(frontPage.getFile(), new File(dir, frontPage.get__title() + ".xml"));
		} else {
			File filePage = new File(dir, frontPage.get__title());
			write(filePage, frontPage.get__content());
		}

		if (!frontPage.getAttachments().isEmpty()) {
			_log.info(padding + "  " + frontPage.getAttachments().size() + "-Attachments " + frontPage.getAttachments());
		}

		for (WikiPage page : frontPage.getChildren()) {
			page.setPath(frontPage.getPath());
			page.setOutDir(frontPage.getOutDir());

			saveRecursive(page, copyOriginal);
		}
	}

	/**
	 * Removes the parent child relationship for the given pages.
	 * 
	 * @param pages
	 */
	public void unlinkChildren(ArrayList<WikiPage> pages) {
		for (WikiPage page : pages) {
			_log.debug("Removing page '" + page.get__title() + "'");
			if (!pageList.remove(page)) {
				_log.error("Can't remove: " + page.get__title());
			}

			// We also need to remove the child from the parent child relationship
			WikiPage parent = findPage(page.get__parentTitle());
			if (parent != null) {
				if (!parent.removeChild(page)) {
					_log.error("Can't remove child from parent: " + page.get__parentTitle());
				}
			}
		}
	}

	public WikiPage findPage(String title) {
		WikiPage retval = null;

		for (WikiPage page : pageList) {
			if (page.get__title().equals(title)) {
				retval = page;
				break;
			}
		}

		return retval;
	}

	/**
	 * Creates a parent child relationship with the internal pages.
	 * 
	 * @param sort
	 */
	public void linkChildren(boolean sort) {

		if (sort) {
			Collections.sort(pageList);
		}

		for (WikiPage page : pageList) {
			String parentTitle = page.get__parentTitle();

			if (!parentTitle.isEmpty()) {
				WikiPage parent = findPage(parentTitle);

				if (parent != null) {
					parent.addChildPage(page);

				} else {
					_log.debug("!!!Error finding Parent!!! " + parentTitle);
				}
			}
		}
	}

	/**
	 * copies all the attachments for all the pages into the given directory.
	 * 
	 * @param larDirectory
	 * @param destDirName
	 * @throws IOException
	 */
	public void saveAttachments(String larDirectory, String destDirName) throws IOException {
		File destDir = new File(destDirName);
		destDir.mkdirs();
		_log.info("Copying attachments to: " + destDirName);

		for (WikiPage page : pageList) {
			ArrayList<Attachment> files = page.getAttachments();

			for (Attachment attachment : files) {
				String name = attachment.getBinPath();

				copyFile(new File(larDirectory, name), new File(destDir, attachment.getName()));
			}
		}
	}

	/**
	 * Attachments in the LR export are indistinguishable from links. The function looks for [[xxx]], where xxx is an
	 * attachment and changes the download link to [^xxx]. This makes the UWC
	 * com.atlassian.uwc.hierarchies.FilepathHierarchy class upload the attachment.
	 * 
	 * Image conversion {{image.jpg}} -> !image.jpg! is also handled here for the same reason as above.
	 * 
	 */
	public void reformatAttachments() {

		for (WikiPage page : pageList) {
			ArrayList<Attachment> files = page.getAttachments();

			for (Attachment attachment : files) {
				String name = attachment.getName();
				String content = page.get__content();

				if (content.indexOf(name) > 0) {
					String replace = convertAttachLink(content, name);

					replace = convertImageLink(replace, name);

					page.set__content(replace);
				}
			}
		}
	}

	/**
	 * [[link]] converts to [^link] and [[link|link name]] converts to [^link]
	 * 
	 * Linking to an attachment in LR stopped working after the SP2 upgrade but we still use this mechanism to mark
	 * attachments for inclusion in Confluence.
	 * 
	 * @param content
	 * @param name
	 * @return
	 */
	public String convertAttachLink(String content, String name) {
		String retval = content;

		// [[attach] -> [^attach]
		retval = content.replaceAll("\\[\\[" + name + "\\]\\]", "[^" + name + "]");

		// [[attach|attach name]] -> [^attach]
		// // "\\[\\[" + // opening left brackets
		// // name is captured by replaceAll
		// // "\\|" + // until a pipe
		// // "[^\\]|]+" + // anything but a right bracket or | until
		// // "\\]\\]"; // right brackets
		retval = retval.replaceAll("\\[\\[" + name + "\\|[^\\]|]*]]", "[^" + name + "]");

		return retval;
	}

	/**
	 * {{image.jpg}} is display image in LR - in Confluence its !image.jpg! We do this here as we can find out if it's
	 * an image by looking at the attachments.
	 * 
	 * @param content
	 * @param name
	 * @return
	 */
	public String convertImageLink(String content, String name) {
		return content.replaceFirst("\\{\\{" + name + "\\}\\}", "!" + name + "!");
	}

	/**
	 * Html formatted pages need to be surrounded with {html}
	 * 
	 * @param noHtml
	 *            if true encloses the html fragment inside a code block this disables HTML rendering in Confluence
	 */
	public void reformatHtmlPages(boolean noHtml) {

		for (WikiPage page : pageList) {
			String format = page.get__format();
			if (format.equalsIgnoreCase("html")) {
				if (noHtml) {
					_log.debug("Disabling html formatting for page: " + page.get__title());
					String content = page.get__content();
					page.set__content("{code} " + content + "{code}");
				} else {
					_log.debug("Adding html formatting for page: " + page.get__title());
					String content = page.get__content();
					page.set__content("{html} " + content + "{html}");
				}
			} else if (format.equalsIgnoreCase("creole")) {
				// This is the stuff we are expecting
			} else {
				_log.warn("Unsupported format detected: " + format);
			}
		}
	}

	/**
	 * Sets the depth value for the pages in the hierarchy.
	 * 
	 * @param frontPage
	 */
	public void recursePages(WikiPage frontPage) {

		for (WikiPage page : frontPage.getChildren()) {

			// if(page.get__title().contains("Admin")){
			// _log.debug("!break " );
			// }

			page.setDepth(frontPage.getDepth() + 1);
			recursePages(page);
		}
	}

	/**
	 * Prints out the current tree structure.
	 * 
	 * @param frontPage
	 */
	public void showRecursive(WikiPage frontPage) {
		String padding = "";
		for (int i = 0; i < frontPage.getDepth(); i++) {
			padding += "    ";
		}

		_log.info(padding + frontPage.get__title());

		if (!frontPage.getAttachments().isEmpty()) {
			_log.info(padding + "  " + frontPage.getAttachments().size() + "-Attachments " + frontPage.getAttachments());
		}

		for (WikiPage page : frontPage.getChildren()) {
			showRecursive(page);
		}
	}

	/**
	 * Copy the current pagelist original files into the given directory.
	 * 
	 * @param dir
	 * @throws IOException
	 */
	public void copyTo(String dir) throws IOException {
		File destDir = new File(dir);
		destDir.mkdir();
		_log.info("Copying " + pageList.size() + " current pages to: " + dir);

		for (WikiPage page : pageList) {
			_log.info("XML Path: " + page.getFile().getAbsolutePath());
			copyFile(page.getFile(), new File(destDir, page.get__title() + ".xml"));
		}
	}

	/**
	 * Copy the source file to the destination overwriting the file if it exists.
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		copyFile(sourceFile, destFile, true);
	}

	/**
	 * Copy the source file to the destination.
	 * 
	 * @param sourceFile
	 * @param destFile
	 * @param overwrite
	 *            overwrite the file if true.
	 * @throws IOException
	 */
	public static void copyFile(File sourceFile, File destFile, boolean overwrite) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		} else {
			if (!overwrite) {
				throw new IOException("Destination file exists! " + destFile);
			}

			_log.trace("Destination file exists: " + destFile);
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	/**
	 * Writes the content of the pages of the Wiki onto the directory, maintaining the hierarchy structure in the file
	 * system.
	 * 
	 * @param frontPage
	 * @param copyOriginal
	 *            true to copy the original xml file to the destination
	 * @throws IOException
	 */
	public void saveRecursive(WikiPage frontPage, boolean copyOriginal) throws IOException {
		String padding = "";
		for (int i = 0; i < frontPage.getDepth(); i++) {
			padding += "    ";
		}

		_log.info(padding + frontPage.get__title());

		File dir = new File(frontPage.getOutDir() + File.separator + frontPage.getPath(), frontPage.get__title());
		dir.mkdirs();

		// _log.debug("Create Dir: " + dir);
		if (copyOriginal) {
			copyFile(frontPage.getFile(), new File(dir, frontPage.get__title() + ".xml"));
		} else {
			File filePage = new File(dir, frontPage.get__title());
			write(filePage, frontPage.get__content());
		}

		if (!frontPage.getAttachments().isEmpty()) {
			_log.info(padding + "  " + frontPage.getAttachments().size() + "-Attachments " + frontPage.getAttachments());
		}

		for (WikiPage page : frontPage.getChildren()) {
			page.setPath(frontPage.getPath() + File.separator + frontPage.get__title());
			page.setOutDir(frontPage.getOutDir());

			saveRecursive(page, copyOriginal);
		}
	}

	protected void write(File file, String content) {
		try {
			FileOutputStream fw = new FileOutputStream(file);
			OutputStreamWriter outstream = new OutputStreamWriter(fw, "utf-8");
			BufferedWriter out = new BufferedWriter(outstream);
			out.write(content);
			out.close();
		} catch (IOException e) {
			_log.error("Problem writing to file: " + file.getAbsolutePath());
			e.printStackTrace();
		}
	}

	public void showPages() {
		_log.debug("---------Pages-----------");
		for (WikiPage page : pageList) {
			_log.info(page);

			if (!page.showChildren().isEmpty()) {
				_log.info(page.showChildren());
			}
		}
	}

	public void showPagesNoParentTitle() {
		_log.info("---------showPagesNoParentTitle-----------");
		for (WikiPage page : pageList) {

			if (page.get__parentTitle().isEmpty()) {
				_log.debug(page);
			}
		}
	}

}
