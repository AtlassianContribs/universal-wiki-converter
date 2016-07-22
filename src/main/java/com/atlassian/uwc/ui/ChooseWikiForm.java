package com.atlassian.uwc.ui;

import com.atlassian.uwc.util.CopyFile;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: brendan
 * Date: Jun 6, 2006
 * Time: 12:15:06 PM
 * To change this template use File | Settings | File Templates.
 * @deprecated Used with gui v2 (UWCForm2)
 */
public class ChooseWikiForm {
    protected JPanel chooseWikiPanel;
    protected JButton cancelButton;
    protected JButton nextButton;
    protected JList wikiJList;
    protected JLabel topLabel;
    public JDialog chooseWikiDialogue;
    protected DefaultListModel converterListModel;
    Logger log = Logger.getLogger("ChooseWikiForm");
    private JPanel buttonPanel;

    public void init() {
        chooseWikiDialogue = new JDialog(UWCForm2.getInstance().mainFrame);
        chooseWikiDialogue.setSize(450,300);
        chooseWikiDialogue.add(chooseWikiPanel);
        chooseWikiDialogue.setTitle("Conversion settings");

        populateList();

        // this button basically copies one of the wiki converter
        // property files like twiki.convert.properties into
        // 'converter.properties' which is what the application
        // works with. Kinda cheap but evolved that way and works
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                UWCForm2 uwc = UWCForm2.getInstance();
                uwc.nextTab();
            }
        });

//        cancelButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                chooseWikiDialogue.setVisible(false);
//            }
//        });


    }

    /**
     * copies the selected converter file such as
     * twiki.convert.properties into
     * 'converter.properties' which is what the application
     * works with. Kinda cheap but evolved that way and works
     */
    public void copySelectedConvertFile() {
        UWCForm2 uwc = UWCForm2.getInstance();
        String converterFileName = (String) wikiJList.getSelectedValue();
        File origin = new File("conf"+File.separator+"converter."+converterFileName+".properties");
        File dest = new File("conf"+File.separator+"converter.properties");
        try {
            CopyFile.copyFile(origin, dest);
        } catch (IOException e1) {
            log.error("file copy failed: "+origin.getPath()+"  to  "+dest.getPath());
            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        chooseWikiDialogue.setVisible(false);
        uwc.populateAllConverterList();
        uwc.addAllConvertersToEngineSelected();


    }

//    public void displayThankyouMessageOneTime() {
//        // check if message has been displayed
//        // if so then return
//        File markerFile = new File(".uwc-run");
//        if (markerFile.exists()) {
//            return;
//        }
//        try {
//            RandomAccessFile raf = new RandomAccessFile(".uwc-run", "rw");
//            raf.write(1);
//            raf.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            // if there was a problem writing the file then just return
//            return;
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            // if there was a problem writing the file then just return
//            return;
//        }
//        BareBonesBrowserLaunch.openURL("http://www.artemissoftware.biz/site/display/uwc/Universal+Wiki+Converter");
//
//    }

    /**
     * populate the list with converter files
     */
    public void populateList() {
        File confDir = new File("conf");
        FilenameFilter filter = new UWCConverterPropFileFilter();
        File[] files = confDir.listFiles(filter);
        converterListModel = new DefaultListModel();
        for (File file : files) {
            String fileName = file.getName();
            StringTokenizer st = new StringTokenizer(fileName, ".");
            st.nextToken();
            fileName = st.nextToken();
            converterListModel.addElement(fileName);
        }

        wikiJList.setModel(converterListModel);
        // @todo - this should be made to remember the setting
        wikiJList.setSelectedIndex(6);
    }

    public class UWCConverterPropFileFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if (name.equalsIgnoreCase("converter.properties")) return false;
            if (name.endsWith(".properties") &&
                    name.startsWith("converter")) {
                return true;
            } else {
                return false;
            }
        }
    }
}
