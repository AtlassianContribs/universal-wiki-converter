package com.atlassian.uwc.prep;

import java.io.*;

import java.util.logging.Logger;

/**
 * This class prepares a MoinMoin wiki for the UWC by copying the files storing
 * the most recent page contents to a location and name which works more easily with
 * the UWC.
 * <p/>
 * Basically a page named 'FrontPage' is stored in a directory wiki/data/pages/FrontPage
 * in that dir is a file called 'current' containing the file name of the current version
 * which is another file called 00000005 or something like that.
 * <p/>
 * This class just copies 00000005 up to the pages dir as FrontPage.uwc
 */
public class MoinMoinPreparation {
	private static Logger log = Logger.getLogger(MoinMoinPreparation.class.getName());

    private static final String SEPARATOR = "/";
    private static final String CURRENT = "current";
    private static final String REVISIONS = "revisions";
    private static final String BADCONTENT = "BadContent";

    /** File name extension for the page copies */
    public static final String EXTENSION = ".uwc";

    /**
     * main
     *
     * @param args
     */
    public static void main(String[] args) {
        MoinMoinPreparation moinmoinPrep = new MoinMoinPreparation();
        if (args.length != 2) {
            System.out.println("usage: java MoinMoinPreparation <pages dir> <destination dir>");
            System.out.println("This copies all of the most recent page versions up to the <destination dir>. ");
            System.out.println("example: java MoinMoinPreparation C:\\MoinMoinServer\\wiki\\data\\pages c:\\prepared_pages");
            System.out.println("example: java MoinMoinPreparation /var/moinmoin/wiki/data/pages /prepared_pages");
            System.exit(0);
        }

        String pagesDirStr = args[0];
        File pagesDir = new File(pagesDirStr);

        if (pagesDir.isFile() || !pagesDir.exists()) {
        	System.out.println("Pages directory: \"" + pagesDirStr + "\" must be a valid directory.");
        	return;
        }

        String destinationDirStr = args[1];
        File destinationDir = new File(destinationDirStr);

        if (destinationDir.isFile()) {
        	System.out.println("Destination: \"" + destinationDirStr + "\" must be a directory, not a file.");
        	return;
        }
        if (!destinationDir.exists()) {
        	if (!destinationDir.mkdirs()) {
        		System.out.println("Impossible to create destination: \"" + destinationDirStr + "\".");
            	return;
        	}
        }

        String[] pages = moinmoinPrep.getPageDirList(pagesDirStr);
        String current = null;

        for (int i = 0; i < pages.length; i++) {
            if (!pages[i].startsWith(BADCONTENT)) {
            	current = moinmoinPrep.getCurrentRevision(pagesDirStr + SEPARATOR + pages[i]);
            	if (current != null) {
            		try {
            			moinmoinPrep.copyFile(
            					new File(pagesDirStr + SEPARATOR + pages[i] + SEPARATOR + REVISIONS + SEPARATOR + current),
            					new File(destinationDirStr + SEPARATOR + pages[i] + EXTENSION));
            		} catch (FileNotFoundException e) {
            			log.info("Page \"" + pages[i] + "\" has been deleted and will be ignored.");
            		} catch (Exception e) {
            			e.printStackTrace();
            		}
            	}
            }
        }
    }

    /**
     * getPageDirList
     *
     * @param pagesDirStr
     * @return list
     */
    private String[] getPageDirList(String pagesDirStr) {
        File pagesDir = new File(pagesDirStr);
        return pagesDir.list(new PageDirFileFilter());
    }

    /**
     * PageDirFileFilter
     */
    public static class PageDirFileFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return new File(dir, name).isDirectory() && !name.endsWith("MoinEditorBackup");
        }
    }

    /**
     * getCurrentRevision
     *
     * @param pagePath
     * @return current revision filename
     */
    private String getCurrentRevision(String pagePath) {

        String current = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(pagePath + SEPARATOR + CURRENT));
            current = br.readLine();
        } catch (FileNotFoundException e) {
        	log.info("Page \"" + pagePath + "\" has been deleted and will be ignored.");
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return current;
    }

    /**
     * copyFile
     *
     * @param in
     * @param out
     * @throws Exception
     */
    private void copyFile(File in, File out) throws Exception {
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        byte[] buf = new byte[1024];
        int i = 0;
        while ((i = fis.read(buf)) != -1) {
            fos.write(buf, 0, i);
        }
        fis.close();
        fos.close();
    }
}
