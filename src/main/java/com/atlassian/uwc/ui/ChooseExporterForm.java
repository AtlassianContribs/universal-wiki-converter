package com.atlassian.uwc.ui;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import java.util.StringTokenizer;
import java.util.Map;

import com.atlassian.uwc.util.PropertyFileManager;
import com.atlassian.uwc.exporters.Exporter;

/**
 * Screen which allows the user to select the exporter if needed.
 * @deprecated Used by GUI v2 (UWCForm2), this isn't used anymore
 */
public class ChooseExporterForm {
    protected JPanel chooseExporterPanel;
    protected JButton cancelButton;
    protected JButton runExporterButton;
    protected JList exporterJList;
    protected JLabel topLabel;
    public JDialog chooseExporteriDialogue;
    protected DefaultListModel exporterListModel;
    Logger log = Logger.getLogger("ChooseExporterForm");
    private JPanel buttonPanel;

    public void init() {
        chooseExporteriDialogue = new JDialog(UWCForm2.getInstance().mainFrame);
        chooseExporteriDialogue.setSize(450,300);
        chooseExporteriDialogue.add(chooseExporterPanel);
        chooseExporteriDialogue.setTitle("Conversion settings");

        populateList();

        // this button basically copies one of the wiki converter
        // property files like twiki.convert.properties into
        // 'converter.properties' which is what the application
        // works with. Kinda cheap but evolved that way and works
        runExporterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runExporter();
            }
        });

//        cancelButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                chooseWikiDialogue.setVisible(false);
//            }
//        });


    }

    private void runExporter() {
        if (exporterJList.getMinSelectionIndex() < 0) {
            JOptionPane.showInternalMessageDialog(UWCForm2.getInstance().mainPanel, "Please choose a wiki type to export.");
            return;
        }
        String exporterFileName = (String) exporterJList.getSelectedValue();
//        File origin = new File("conf"+File.separator+"converter."+converterFileName+".properties");
        File exporterConfFile = new File("conf"+File.separator+"exporter."+exporterFileName+".properties");
        Map exporterProperties = null;
        try {
            exporterProperties = PropertyFileManager.loadPropertiesFile(exporterConfFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        assert exporterProperties != null;
        String exporterClassName = (String) exporterProperties.get("exporter.class");
        Class exporterClass = null;
        try {
            exporterClass = Class.forName(exporterClassName);
        } catch (ClassNotFoundException e) {
            log.error("The exporter class was not found: "+exporterClassName);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            // @todo - put up a dialog saying export is in progress, then take down
            Exporter exporter = (Exporter) exporterClass.newInstance();

            exporter.export(exporterProperties);
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
        	e.printStackTrace(); 
        }
    }

//    /**
//     * copies the selected converter file such as
//     * twiki.convert.properties into
//     * 'converter.properties' which is what the application
//     * works with. Kinda cheap but evolved that way and works
//     */
//    public void copySelectedConvertFile() {
//        UWCForm2 uwc = UWCForm2.getInstance();
//        String converterFileName = (String) exporterJList.getSelectedValue();
//        File origin = new File("conf"+File.separator+"converter."+converterFileName+".properties");
//        File dest = new File("conf"+File.separator+"converter.properties");
//        try {
//            CopyFile.copyFile(origin, dest);
//        } catch (IOException e1) {
//            log.error("file copy failed: "+origin.getPath()+"  to  "+dest.getPath());
//            e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//        chooseExporteriDialogue.setVisible(false);
//        uwc.populateAllConverterList();
//        uwc.addAllConvertersToEngineSelected();
//
//    }

    /**
     * populate the list with converter files
     */
    public void populateList() {
        File confDir = new File("conf");
        FilenameFilter filter = new ChooseExporterForm.UWCExporterPropFileFilter();
        File[] files = confDir.listFiles(filter);
        exporterListModel = new DefaultListModel();
        for (File file : files) {
            String fileName = file.getName();
            StringTokenizer st = new StringTokenizer(fileName, ".");
            st.nextToken();
            fileName = st.nextToken();
            exporterListModel.addElement(fileName);
        }

        exporterJList.setModel(exporterListModel);
//        exporterJList.setSelectedIndex(3);
    }

    public class UWCExporterPropFileFilter implements FilenameFilter {

        public boolean accept(File dir, String name) {
            if (name.equalsIgnoreCase("converter.properties")) return false;
            if (name.endsWith(".properties") &&
                    name.startsWith("exporter")) {
                return true;
            } else {
                return false;
            }
        }
    }
}
