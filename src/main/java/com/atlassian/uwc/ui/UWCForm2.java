package com.atlassian.uwc.ui;

import com.atlassian.uwc.ui.organizer.OrganizerMainFrame2;
import com.atlassian.uwc.util.BareBonesBrowserLaunch;
import com.atlassian.uwc.util.PropertyFileManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.theme.SubstanceOrangeTheme;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.util.*;
import static java.util.Collections.sort;

/**
 * Created by IntelliJ IDEA.
 * User: brendan
 * Date: Jun 9, 2006
 * Time: 11:34:34 AM
 * To change this template use File | Settings | File Templates.
 * 
 * @deprecated
 */
public class UWCForm2 {
    static UWCForm2 instance;
    protected JTabbedPane tabbedPane1;
    protected JPanel chooseWikiPanel;
    protected JPanel chooseExporterPanel;
    protected JPanel confluenceSettingsPanel;
    protected JPanel selectConvertersPanel;
    protected JPanel choosePagesPanel;
    protected JPanel sendPagesPanel;
    //    protected JPanel organizePanel;
    protected JPanel testRegExToolPanel;
    protected JPanel helpPanel;
    protected JPanel mainPanel;
    protected JButton sendToConfluenceButton;
    private JPanel choosePagesToConvertPanel;
    private JPanel regExToolPanel;
    private JButton nextButton;
    private JPanel nextButtonPanel;
    private JPanel organizerPanel;
    private JButton launchWikiOrganizerButton;
    private JButton launchJavaRegexTestToolButton;


    protected void init() {

        // create button listeners
        createActionListeners();

        //populateAllConverterList();
        confluenceSettingsForm.populateConfluenceSettings();
        chooseWikiForm.init();
        chooseExporterForm.init();

        // set up the engineSelectedConverterList with a converterListModel so elements can be added and removed
        engineSelectedConvertersListModel = new DefaultListModel();
        engineSelectedConvertersJList.setModel(engineSelectedConvertersListModel);
        currentChooserPageDir = getCurrentChooserPageDir();
//        super.init();

        ///////////////////////////////////////////
        // add external screens to tabbed pane panels
        // choose wiki panel
        chooseWikiPanel.setLayout(new BorderLayout());
        chooseWikiPanel.add(chooseWikiForm.chooseWikiPanel, BorderLayout.CENTER);

        chooseExporterPanel.setLayout(new BorderLayout());
        chooseExporterPanel.add(chooseExporterForm.chooseExporterPanel, BorderLayout.CENTER);

        // confluenceSettingsPanel
        confluenceSettingsPanel.setLayout(new BorderLayout());
        confluenceSettingsPanel.add(confluenceSettingsForm.confluenceSettingsPanel, BorderLayout.CENTER);

        // regExTool Panel
        RegExTestTool regExTool = new RegExTestTool(this);
//        regExToolPanel.setLayout(new BorderLayout());
//        regExToolPanel.add(regExTool.regExTestToolPanel, BorderLayout.CENTER);

//        organizerPanel.setLayout(new BorderLayout());
//        JTree organizerTree = OrganizerTree.getOrganizerTree().getJTree();
//        organizerPanel.add(organizerTree, BorderLayout.CENTER);
    }


    /**
     * sends the user to the next tab in the interface
     * <p/>
     * this has some pretty lame wizard handling code, but
     * it's simple enough
     *
     * @return false if not going to next tab
     */
    public boolean nextTab() {
        int tabIndex = tabbedPane1.getSelectedIndex();
        if (tabIndex == 0) {
            // choose wiki
            if (chooseWikiForm.wikiJList.getMinSelectionIndex() < 0) {
                JOptionPane.showInternalMessageDialog(this.mainPanel, "Please choose a wiki type");
                return false;
            }
            chooseWikiForm.copySelectedConvertFile();

        } else if (tabIndex == 1) {
        } else if (tabIndex == 2) {
            // select converters
            int listSize = engineSelectedConverterList.size();
            if (listSize == 0) {
                JOptionPane.showInternalMessageDialog(this.mainPanel, "Please select at least one converter");
                return false;
            }
        } else if (tabIndex == 3) {
            // select pages to convert
            int listSize = pageList.size();
            if (listSize == 0) {
                JOptionPane.showInternalMessageDialog(this.mainPanel, "Please select at least one page to convert.");
                return false;
            }
        } else if (tabIndex == 1) {
        } else if (tabIndex == 1) {
        } else if (tabIndex == 1) {
        } else if (tabIndex == 1) {
        } else if (tabIndex == 1) {
        } else if (tabIndex == 1) {
        }


        tabbedPane1.setSelectedIndex(tabIndex + 1);
        return true;
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        initializeLogging();

        // todo - add splash screen here?        

        SubstanceLookAndFeel.setCurrentTheme(new SubstanceOrangeTheme());
        SubstanceLookAndFeel slf = new SubstanceLookAndFeel();
        UIManager.setLookAndFeel(slf);

        UWCForm2 form = UWCForm2.getInstance();
        form.init();
        form.mainFrame.setContentPane(form.mainPanel);

        form.mainFrame.pack();
        form.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        form.mainFrame.setVisible(true);

        converterListFrame = new JFrame("Converter List");
        converterListFrame.setContentPane(form.converterListForm.converterListPanel);
        converterListFrame.pack();
        converterListFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//
//        regExTestToolFrame = new JFrame("Regular Expression Test Tool");
//        regExTestToolFrame.setContentPane(form.regExTestToolForm.regExTestToolPanel);
//        regExTestToolFrame.pack();
//        regExTestToolFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        hierarchyToolFrame = new JFrame("Hierarchy Tool");
//        hierarchyToolFrame.setContentPane(form.hierarchyToolForm.hierarchyToolPanel);
        hierarchyToolFrame.pack();
        hierarchyToolFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private static void initializeLogging() {
        PropertyConfigurator.configure("log4j.properties");
    }

    /**
     * singleton getter for this class
     *
     * @return
     */
    public static UWCForm2 getInstance() {
        if (instance == null) {
            instance = new UWCForm2();
        }
        return instance;
    }

    ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////
    protected JButton includeConverterButton;
    protected JButton chooseWikiButton;
    protected JButton removePagesButton;
    protected JButton excludeConverterButton;
    protected JList engineSelectedConvertersJList;
    protected JList pageFileJList;
    protected JButton confluenceExportSettingsButton;
    protected JButton organizeWiki;
    protected JButton testRegExFrame;
    protected JButton helpButton;
    protected JButton attachmentsButton;
    protected JScrollPane pageFileListScrollPane;
    protected JScrollPane convertersScrollPane;
    protected DefaultListModel engineSelectedConvertersListModel;

    Properties converterProperties = null;
    protected ArrayList<File> pageList = new ArrayList<File>();
    protected ArrayList<String> engineSelectedConverterList = new ArrayList<String>();
    Logger log = Logger.getLogger("UWCForm2");
    protected ConverterListForm converterListForm = new ConverterListForm(this);
    protected RegExTestTool regExTestToolForm = new RegExTestTool(this);
//    protected HierarchyTool hierarchyToolForm = new HierarchyTool(this);

    TreeMap<String, String> allConverters = new TreeMap<String, String>();
    HashMap activeConverters = new HashMap();
    static JFrame converterListFrame = null;
    static JFrame regExTestToolFrame = null;
    public static JFrame hierarchyToolFrame = null;

    public final static String converterPropFileLoc = "conf" + File.separator + "converter.properties";

    protected ConverterEngine_v2 converterEngine = new ConverterEngine_v2();
    public ConfluenceSettingsForm confluenceSettingsForm = new ConfluenceSettingsForm();
    protected ChooseWikiForm chooseWikiForm = new ChooseWikiForm();
    protected ChooseExporterForm chooseExporterForm = new ChooseExporterForm();

    protected JFrame mainFrame = new JFrame("Universal Wiki Converter");
    private File currentChooserPageDir = null;

    public ConfluenceSettingsForm getConfluenceSettingsForm() {
        return confluenceSettingsForm;
    }

    public ConverterEngine_v2 getConverterEngine() {
        return converterEngine;
    }


    /**
     * gets the directory which the 'page chooser' JFileChooser should
     * be set to when it is popped open. This is very nice to know
     * because otherwise it always defaults to your home dir. which
     * can be annoying to say the least.
     *
     * @return
     */
    private File getCurrentChooserPageDir() {
        String currentPageChooseDirStr = confluenceSettingsForm.getCurrentPageChooserDir();
        if (currentPageChooseDirStr == null) return null;
        return new File(currentPageChooseDirStr);
    }


    /**
     * This method populates the list of all converters presented on the 'ConvertersListForm'
     */
    protected void populateAllConverterList() {
        readInConverterPropFile();
        // local pointer to the list converterListModel
        DefaultListModel converterListModel = converterListForm.converterListModel;
        converterListForm.converterList.setModel(converterListModel);
        converterListForm.converterList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        converterListModel.removeAllElements();
        // @todo - sorting for a List....FNI
        ArrayList<String> tempList = new ArrayList<String>(allConverters.keySet());
        sort(tempList);

        Iterator<String> keys = tempList.iterator();

        while (keys.hasNext()) {
            String key = keys.next();
            String value = allConverters.get(key);
            //tempConverters.add(key+"="+value);
            converterListModel.addElement(key + "=" + value);
        }
        //converterListForm.engineSelectedConverterList = new JList(tempConverters.toArray());
        converterListForm.converterListScrollPane.getViewport().setView(converterListForm.converterList);
        converterListForm.converterList.revalidate();
        converterListForm.converterListScrollPane.revalidate();
    }

    protected void readInConverterPropFile() {
        // clear out list
        allConverters.clear();
        try {
            allConverters = PropertyFileManager.loadPropertiesFile(converterPropFileLoc);
        } catch (IOException e) {
            log.error("IO Exception " + e);
        }

        // Make sure allConverters isn't null (occurs if the property file is missing).
        if (allConverters == null) {
            allConverters = new TreeMap<String, String>();
        }
    }

    /**
     * set up the action listeners for all the form elements. I can't believe IDEA doens't
     * generate this stuff for you.
     */
    protected void createActionListeners() {
        this.mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                displayThankyouMessageOneTime();
            }
        });


        includePagesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addWikiPages(mainPanel);
            }

        });

        includeConverterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                readInConverterPropFile();
                populateAllConverterList();
                converterListFrame.setVisible(true);
            }
        });
        removePagesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removePagesFromList();
            }
        });
        excludeConverterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // excludeConvertersButton button pressed
                removeConvertersFromEngineList();
            }
        });

        launchJavaRegexTestToolButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        regexdemo.AppRegexDemo.main(null);
                    }
                });
            }
        });
        launchWikiOrganizerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showInternalMessageDialog(mainPanel, "The wiki organizer is only in the early\\n stages of development and is not usable.");
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new OrganizerMainFrame2().setVisible(true);
                    }
                });
            }
        });
        sendToConfluenceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final SwingWorker worker = new SwingWorker() {
                    public Object construct() {
                        converterEngine.processPages(UWCForm2.this);
                        return null;
                    }
                };
                worker.start();  //required for SwingWorker 3
            }
        });
//        confluenceExportSettingsButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                confluenceSettingsForm.confluenceSettingsDialogue.setVisible(true);
//            }
//        });
//		organizeWiki.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//            	hierarchyToolFrame.setVisible(true);
//            }
//        });
//        testRegExFrame.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                regExTestToolFrame.setVisible(true);
//            }
//        });
        helpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BareBonesBrowserLaunch.openURL("http://confluence.atlassian.com/display/CONFEXT/Universal+Wiki+Converter");
            }
        });
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextTab();
            }
        });
    }

    /**
     * Add selected pages to the page file list
     *
     * @param parent
     */
    protected void addWikiPages(JComponent parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (currentChooserPageDir != null) chooser.setCurrentDirectory(currentChooserPageDir);
        chooser.setFileSystemView(FileSystemView.getFileSystemView());
        chooser.updateUI();
        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            for (File file : files) {
                pageList.add(file);
            }
            pageFileJList = new JList(pageList.toArray());
            pageFileListScrollPane.getViewport().setView(pageFileJList);
            pageFileListScrollPane.getViewport().setViewPosition(new Point(500, 0));
        }
        // persist the last directory choosen.....this was driving me crazy before.
        currentChooserPageDir = chooser.getCurrentDirectory();
        confluenceSettingsForm.setCurrentPageChooserDir(currentChooserPageDir.getPath());
        confluenceSettingsForm.serializeOutConfluenceSettings();
    }

    /**
     * remove pages button removes the selected pages from the list
     */
    protected void removePagesFromList() {
        Object[] files = pageFileJList.getSelectedValues();
        for (Object file : files) {
            pageList.remove(file);
        }
        pageFileJList = new JList(pageList.toArray());
        pageFileListScrollPane.getViewport().setView(pageFileJList);
        pageFileListScrollPane.getViewport().setViewPosition(new Point(500, 0));
    }

    protected JButton includePagesButton;

    /**
     * send updated converter list to the converter.properties file
     */
    protected void serializeOutConverterPropFile() {
        try {
            PropertyFileManager.storePropertiesFile(allConverters, converterPropFileLoc);
        } catch (IOException e) {
            log.error("could not write file: " + converterPropFileLoc);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void updateConverterListModel() {
        engineSelectedConvertersListModel.removeAllElements();
        for (String s : engineSelectedConverterList) {
            engineSelectedConvertersListModel.addElement(s);
        }
        engineSelectedConvertersJList.revalidate();
        convertersScrollPane.revalidate();

    }

    protected void removeConvertersFromEngineList() {
        Object toRemove[] = engineSelectedConvertersJList.getSelectedValues();
        engineSelectedConverterList.removeAll(engineSelectedConverterList);
        for (Object o : toRemove) {
            // @todo - should be able to more easily moving data between lists and list models - FNI
            engineSelectedConvertersListModel.removeElement(o);
        }
        // sooo messy but we need to synch the List back up with the converterListModel
        int size = engineSelectedConvertersListModel.getSize();
        for (int i = 0; i < size; i++) {
            engineSelectedConverterList.add(engineSelectedConvertersListModel.get(i).toString());
        }

        engineSelectedConvertersJList.revalidate();
        convertersScrollPane.revalidate();
    }

    /**
     * launch the choose wiki list
     */
    protected void launchChooseWiki() {
    }

    /**
     * Add all available converters for this wiki conversion
     * to the list of selected converters
     */
    protected void addAllConvertersToEngineSelected() {
        this.engineSelectedConverterList.clear();
        Set<String> converterSet = allConverters.keySet();
        for (Iterator<String> iterator = converterSet.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            String value = allConverters.get(key);
            this.engineSelectedConverterList.add(key + "=" + value);
            this.updateConverterListModel();
            this.converterListFrame.setVisible(false);

        }
    }

    /**
     * public getter for mainPanel
     *
     * @return
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void displayThankyouMessageOneTime() {
        // check if message has been displayed
        // if so then return
        File markerFile = new File(".uwc-run");
        if (markerFile.exists()) {
            return;
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(".uwc-run", "rw");
            raf.write(1);
            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            // if there was a problem writing the file then just return
            return;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            // if there was a problem writing the file then just return
            return;
        }
        BareBonesBrowserLaunch.openURL("http://www.artemissoftware.biz/site/display/uwc/Universal+Wiki+Converter");

    }
}
