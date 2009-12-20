package com.atlassian.uwc.ui;

import biz.artemis.confluence.xmlrpcwrapper.ConfluenceServerSettings;
import biz.artemis.confluence.xmlrpcwrapper.RemoteWikiBroker;
import com.atlassian.uwc.ui.xmlrpcwrapperOld.RemoteWikiBrokerOld;
import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Properties;

/**
 * Drives the 'Confluence Settings Form'. This uses IntelliJ's GUI forms layout tool to
 * actually paint the window.
 * <p/>
 * These settings are persisted to a properties file. They are also accessed directly
 * throughout the application.
 */
public class ConfluenceSettingsForm {
    protected JDialog confluenceSettingsDialogue;
    protected JPanel confluenceSettingsPanel;
    protected JTextField spaceNameTextField;
    protected JButton saveButton;
    protected JButton cancelButton;
    protected JTextField urlTextField;
    protected JTextField loginTextField;
    protected JPasswordField passwordTextField;
    private JTextField patternTextField;
    private JTextField attachmentDirectoryField;
    private JButton attachmentDirectoryButton;
    protected JButton testSettingsButton;
    protected String currentPageChooserDir;

    Properties confluenceSettings = new Properties();
    Logger log = Logger.getLogger("ConfluenceSettingsForm");
    public final static String CONFLUENCE_SETTINGS_FILE_LOC = "conf" + File.separator + "confluenceSettings.properties";
    public JDialog wikiChooserDialogue;

    /**
     * Add action listeners and functionality to the buttons
     */
    protected void createActionListeners() {
        testSettingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringBuilder userMessage = new StringBuilder();
                serializeOutConfluenceSettings();
                // test connection and pop up dialog window with connection status
                RemoteWikiBroker rwb = RemoteWikiBroker.getInstance();
                ConfluenceServerSettings confSettingsTest = new ConfluenceServerSettings();
                confSettingsTest.login = getLogin();
                confSettingsTest.password = getPassword();
                confSettingsTest.spaceKey = getSpaceName();
                confSettingsTest.url = getUrl();
                String message = rwb.checkConnectivity(confSettingsTest);
                if (message == RemoteWikiBroker.USER_MESSAGE_CONNECTIVTY_SUCCESS) {
                    // connectivity check was successfull so test space access
                    try {
						userMessage.append(rwb.getUserPermissionsForUser(confSettingsTest));
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                } else {
                    // connectivty failed or was limited
                    userMessage.append(message);
                }
                JOptionPane.showInternalMessageDialog(UWCForm2.getInstance().mainPanel, userMessage.toString());
                RemoteWikiBrokerOld.getInstance().needNewLogin();
            }

        });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                serializeOutConfluenceSettings();
                confluenceSettingsDialogue.setVisible(false);
                // notify the RemoteWikiBrokerOld that it will need to
                // generate new a login token
                RemoteWikiBrokerOld.getInstance().needNewLogin();
            }

        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // set all the text fields back to the current property settings
                setUrl(confluenceSettings.getProperty("url"));
                setLogin(confluenceSettings.getProperty("login"));
                setPassword(confluenceSettings.getProperty("password"));
                setSpaceName(confluenceSettings.getProperty("space"));
                setPattern(confluenceSettings.getProperty("pattern"));
                setAttachmentDirectory(confluenceSettings.getProperty("attachments"));
                confluenceSettingsDialogue.setVisible(false);
            }
        });
        attachmentDirectoryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = chooser.showOpenDialog(confluenceSettingsDialogue);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    setAttachmentDirectory(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
    }


    public String getLogin() {
        return loginTextField.getText();
    }

    public void setLogin(String login) {
        this.loginTextField.setText(login);
    }

    public String getPassword() {
        return new String(passwordTextField.getPassword());
    }

    public void setPassword(String password) {
        this.passwordTextField.setText(password);
    }


    public String getSpaceName() {
        return spaceNameTextField.getText();
    }

    public void setSpaceName(String spaceName) {
        this.spaceNameTextField.setText(spaceName);
    }

    public String getUrl() {
        return urlTextField.getText();
    }

    public void setUrl(String urlTextField) {
        this.urlTextField.setText(urlTextField);
    }

    public String getPattern() {
        return patternTextField.getText();
    }

    public void setPattern(String pattern) {
        this.patternTextField.setText(pattern);
    }

    /**
     * @deprecated Use BaseConverter.getAttachmentDirectory instead
     */
    public String getAttachmentDirectory() {
        return attachmentDirectoryField.getText();
    }

    public void setAttachmentDirectory(String directory) {
        this.attachmentDirectoryField.setText(directory);
    }

    public String getCurrentPageChooserDir() {
        return currentPageChooserDir;
    }

    public void setCurrentPageChooserDir(String currentPageChooserDir) {
        this.currentPageChooserDir = currentPageChooserDir;
    }

    protected void serializeOutConfluenceSettings() {
        confluenceSettings.put("currentPageChooserDir", getCurrentPageChooserDir());
        confluenceSettings.put("url", getUrl());
        confluenceSettings.put("login", getLogin());
        confluenceSettings.put("password", getPassword());
        confluenceSettings.put("space", getSpaceName());
        confluenceSettings.put("pattern", getPattern());
        confluenceSettings.put("attachments", getAttachmentDirectory());

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(CONFLUENCE_SETTINGS_FILE_LOC);
            confluenceSettings.store(fos, null);
        } catch (IOException e) {
            log.error(e);
        }
        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * read in the Confluence export settings from a properties file and populate the form
     */
    protected void populateConfluenceSettings() {
        createActionListeners();
        // if confluenceSettings.properties file exists read in settings
        // otherwise insert default values
        File confSettings = new File(ConfluenceSettingsForm.CONFLUENCE_SETTINGS_FILE_LOC);
        if (confSettings.exists()) {
            // load properties file
            FileInputStream fis;
            try {
                fis = new FileInputStream(ConfluenceSettingsForm.CONFLUENCE_SETTINGS_FILE_LOC);
                confluenceSettings.load(fis);
                fis.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // otherwise insert some default values
            confluenceSettings.put("url", "localhost");
            confluenceSettings.put("login", "admin");
            confluenceSettings.put("password", "");
            confluenceSettings.put("space", "My Space");
            confluenceSettings.put("pattern", "");
            confluenceSettings.put("attachments", "");
        }

        // populate settings form
        setUrl(confluenceSettings.getProperty("url"));
        setLogin(confluenceSettings.getProperty("login"));
        setPassword(confluenceSettings.getProperty("password"));
        setSpaceName(confluenceSettings.getProperty("space"));
        setPattern(confluenceSettings.getProperty("pattern"));
        setAttachmentDirectory(confluenceSettings.getProperty("attachments"));
        setCurrentPageChooserDir(confluenceSettings.getProperty("currentPageChooserDir"));

        confluenceSettingsDialogue = new JDialog(UWCForm2.getInstance().mainFrame);
        confluenceSettingsDialogue.setSize(450, 300);
        confluenceSettingsDialogue.add(confluenceSettingsPanel);
        confluenceSettingsDialogue.setTitle("Conversion settings");

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here 
    }
}
