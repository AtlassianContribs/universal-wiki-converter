package com.atlassian.uwc.converters.twiki;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.Perl5Compiler;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * This class prepares TWiki attachments and images for
 * sending to Confluence, but does not actually convert the page
 * text.
 *
 * The attachments aren't sent to Confluence till the next step in the
 * process when the user clicks 'yes - send'
 */
public class TWikiPrepareAttachmentFilesConverter extends BaseConverter {

    Logger log = Logger.getLogger("TWikiPrepareAttachmentFilesConverter");
    static Perl5Compiler compiler = new Perl5Compiler();

    public void convert(Page page) {

        // scan the page and create a list of attachments
        addAttachmentsToPage(page);
    }

    protected void replaceAttachmentLinks(Page page) {
        //To change body of created methods use File | Settings | File Templates.
    }

    /**
     * This method looks at the page's file path....from that plus the
     * attachment dir. specificied in settings (which should point at the
     * TWiki 'pub' dir) the method can determine all the attachments that should be associated with this page.
     * This method then just uploads all of the attachments in this dir.
     *
     * At somepoint we might need to consider figuring out which attachments
     * are actually referenced by the page.....but maybe we'll never care.
     * For now this is a relatively simple and elegant solution
     *
     * @param page
     * @return
     */
    protected void addAttachmentsToPage(Page page) {
        // get TWiki 'web' for this page which is kind of like a space,
        // the pages are organized into directories
        String pagePath = page.getFile().getPath();
        // if seperator is back '\' escape it for the regex String split()
        String regex = File.separator.equals("\\") ? "\\\\" : File.separator;
        String tokens[] = pagePath.split(regex);
        String attachmentWebDirName = tokens[tokens.length - 2];
        String pageDirName = tokens[tokens.length - 1];
        // chop off the end .txt
        int fileNameExtLoc = pageDirName.indexOf(".");
        if (fileNameExtLoc<0) {
            log.error("file name didn't have dot . -> "+ pageDirName);
        }
        pageDirName = pageDirName.substring(0,fileNameExtLoc);
        //
        String attachmentHighLevelDir = this.getAttachmentDirectory();
        // upload all the attachments in that dir
        String attachmentPageDirPath = attachmentHighLevelDir+
                File.separator+
                attachmentWebDirName+
                File.separator+
                pageDirName;
        File attachmentPageDir = new File(attachmentPageDirPath);
        File files[] = attachmentPageDir.listFiles();
        if (files==null) {
            log.info("no attachment files found in directory: "+attachmentPageDirPath);
            return;
        }
        // filter files down
        for (File file : files) {
            if (file.getPath().endsWith(",v")) continue;
            if (!file.exists() || file.isDirectory()) continue;
            page.addAttachment(file);
        }
    }
}
