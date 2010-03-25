package com.atlassian.uwc.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import regexdemo.AppRegexDemo;
import regexdemo.FrameRegexDemo;

import com.atlassian.uwc.ui.UWCUserSettings.Setting;
import com.atlassian.uwc.ui.listeners.AddPagesListener;
import com.atlassian.uwc.ui.listeners.ChangeAttachmentCheckboxListener;
import com.atlassian.uwc.ui.listeners.ChangeCheckboxListener;
import com.atlassian.uwc.ui.listeners.CheckboxObserver;
import com.atlassian.uwc.ui.listeners.ConvertListener;
import com.atlassian.uwc.ui.listeners.EnableTextFieldObserver;
import com.atlassian.uwc.ui.listeners.ExportWikiListener;
import com.atlassian.uwc.ui.listeners.GuiDisablingListener;
import com.atlassian.uwc.ui.listeners.HasAllConvertSettingsListener;
import com.atlassian.uwc.ui.listeners.IgnoreAltListener;
import com.atlassian.uwc.ui.listeners.LaunchFeedbackListener;
import com.atlassian.uwc.ui.listeners.PageHandler;
import com.atlassian.uwc.ui.listeners.RemovePagesListener;
import com.atlassian.uwc.ui.listeners.SaveListener;
import com.atlassian.uwc.ui.listeners.SelectFileDropTargetListener;
import com.atlassian.uwc.ui.listeners.TestSettingsListener;
import com.atlassian.uwc.ui.listeners.UrlLauncher;
import com.atlassian.uwc.ui.listeners.WikiIsExportableListener;

/**
 * Improved GUI for the UWC
 */
public class UWCForm3 {

	//CONSTANTS
	protected static final String UWC_DOC_URL = "https://studio.plugins.atlassian.com/wiki/display/UWC/";
	private static final String DEFAULT_DIR_LABEL = "Attachments";
	public static final String UWC_DOC_WEBSITE = UWC_DOC_URL + "Universal+Wiki+Converter";
	private static final String PASSWORD_TOOLTIP = "The password to the Confluence user account being used in the Login field.";
	private static final String SPACE_TOOLTIP = "This is the spacekey of the Confluence space you will be importing files to.";
	private static final String ATTACHMENTS_TOOLTIP = "This is the directory where attachments are kept for your particular wiki. This setting will be wiki dependent, so check the appropriate Help docs for more info.";
	private static final String LOGIN_TOOLTIP = "This is the username of an account on the Confluence server with write privileges to the space where you\'re adding content.";
	private static final String ADDRESS_TOOLTIP = "This is the url to Confluence. Example: 'localhost:8080'";
	private static final String VERSION_INDICATOR = ""; 
	public static final String VERSION_NUMBER = "3.11.0";
	private static final Dimension TABBEDPANE_SIZE = new Dimension(420, 475);
	public static final String APP_NAME = "Universal Wiki Converter";
	private static final int BASIC_RIGHT_MARGIN = 35;
	private static final int MED_WIDTH = 185;
	private static final int MED_HEIGHT = 18;
	private static final Dimension MED_SIZE = new Dimension(MED_WIDTH, MED_HEIGHT);
	private static final int BASE_INSET = 5;
	private static final int LABEL_RIGHT_MARGIN = 5;
	private static final int LABEL_LEFT_MARGIN = 55;
	/**
	 * default directory where properies are located
	 */
	private static final String PROPS_DIR = "conf";  

	//Swing objects
	private JFrame jFrame = null;  
	private JPanel jContentPane = null;
	private JFrame aboutDialog = null;  //  @jve:decl-index=0:visual-constraint="510,12"
	private JPanel aboutContentPane = null;
	private UWCLabel aboutVersionLabel = null;
	private JTabbedPane jTabbedPane = null;
	private JPanel jPanelBasic = null;
	private JPanel jPanelAdvanced = null;
	private UWCLabel jLabelType = null;
	private JComboBox jComboBox_WikiType = null;
	private JButton jButtonExport = null;
	private JPanel jPanelFromLabel = null;
	private UWCGuiLine line1 = null;
	private UWCGuiLine line2 = null;
	private JLabel jLabelDirectory = null;
	protected JTextField jTextFieldAttachments = null;
	private JButton jButtonDirectory = null;
	private JLabel jLabelPages = null;
	private JScrollPane jScrollPanePages = null;
	private JList jListPages = null;
	private JButton jButtonAddPages = null;
	private JButton jButtonRemovePages = null;
	private JPanel jPanelToLabel = null;
	private UWCGuiLine line3 = null;
	private UWCGuiLine line4 = null;
	private UWCLabel jLabelAddress = null;
	private JTextField jTextFieldAddress = null;
	private UWCLabel jLabelLogin = null;
	private JTextField jTextFieldLogin = null;
	private UWCLabel jLabelPass = null;
	private JPasswordField jPasswordField = null;
	private UWCLabel jLabelSpace = null;
	private JTextField jTextFieldSpace = null;
	private JPanel jPanelMain = null;
	private JButton jButtonConvert = null;
	private JButton jButtonClose = null;
	private JCheckBox jCheckBoxSendToConfluence = null;
	private JButton jButtonTestConnection = null;
	private TestSettingsListener testSettingsListener;
	private JButton jButtonLaunchRegexTester = null;
	private UWCLabel jLabelSendToConfluence = null;
	private JPanel jPanel_OptSettings;
	private UWCGuiLine line5;
	private UWCGuiLine line6;
	private JPanel jPanel_Tools;
	private UWCGuiLine line7;
	private UWCGuiLine line8;
	private UWCLabel jLabelTestConnection;
	private UWCLabel jLabelLaunchRegexTester;
	private FrameRegexDemo regexFrame;
	private JCheckBox jCheckBoxFeedbackOption;
	private UWCLabel jLabelFeedbackOption;
	private JButton jButtonLaunchFeedback;
	private UWCLabel jLabelLaunchFeedback;
	private FeedbackWindow feedbackWindow;
	private JCheckBox jCheckBoxAttachmentSize = null;
	private JLabel jLabelAttachmentSize = null;
	private JTextField jTextFieldAttachmentSize = null;

	/* Menu objects */
	
	private JMenuBar jJMenuBar = null;
	private JMenu fileMenu = null;
	private JMenu helpMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenuItem aboutMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JMenu converterMenu = null;
	private JMenu viewMenu = null;
	private JMenu toolsMenu = null;
	private JMenuItem convertMenuItem = null;
	private JMenuItem exportMenuItem = null;
	private JMenuItem addMenuItem = null;
	private JMenuItem removeMenuItem = null;
	private JMenuItem conversionTabMenuItem = null;
	private JMenuItem otherToolsTabMenuItem = null;
	private JCheckBoxMenuItem sendPagesCheckboxMenuItem = null;
	private JCheckBoxMenuItem launchFeedbackCheckboxMenuItem = null;
	private JCheckBoxMenuItem attachmentSizeCheckboxMenuItem = null;
	private JMenuItem testConnectionMenuItem = null;
	private JMenuItem regexTesterMenuItem = null;
	private JMenuItem launchFeedbackMenuItem = null;
	private JMenu whenConvertingMenu = null;
	private JMenu onlineDocMenu = null;
	private JMenuItem uwcMainDoc = null;
	private JMenuItem quickDoc = null;
	private JMenuItem sslDoc = null;
	private JMenuItem guiDoc = null;

	/* Listeners */
	private ChangeCheckboxListener sendPagesCheckboxListener;
	private ChangeCheckboxListener launchFeedbackCheckboxListener;
	private ChangeCheckboxListener attachmentSizeCheckboxListener;
	private ActionListener regexLauncher;  
	private LaunchFeedbackListener launchFeedbackListener;
	private Vector<Integer> availableWikiHelpDocMnemonics;
	private HasAllConvertSettingsListener hasAllConvertSettingsListener;
	private AddPagesListener addPagesListener;
	private RemovePagesListener removePagesListener;
	private ChangeCheckboxListener attachmentSizeMenuItemListener;
	private SelectFileDropTargetListener dragnDropListener;

	/* General Objects*/
	Logger log = Logger.getLogger(this.getClass());  
	private HashMap<String, JMenuItem> wikiHelpMenuItems; 
	/**
	 * contains access to all the necessary state, underlying data, settings
	 */
	UWCGuiModel model = null;  
	/**
	 * overriding properties directory. If this is null, then
	 * PROPS_DIR will be used.
	 */
	private static String commandLineArgDir;

	/* Methods */
	
	/**
	 * autosaves settings, and shuts down the UWC
	 */
	void shutdownUWC() {
//		preShutdownCleanup(); //XXX This should get called by the shutdown hook. See ShutDownController
		System.exit(0);
	}

	/**
	 * handles all the UWC cleanup needed before exiting the program.
	 * called from ShutDownController
	 */
	protected void preShutdownCleanup() {
		log.debug("Shutting Down...");
		save();
	}

	/**
	 * saves all the settings to the file system as properties
	 */
	private void save() {
		Setting unsavedKey = this.model.getUnsaved();
		if (unsavedKey != null) {
			String value = getFormValue(unsavedKey);
			if (SaveListener.valid(unsavedKey, value)) {
				//log it but hide passwords
				String printVal = getPrintablePassword(unsavedKey, value);
				log.debug("Saving last form element: '" + unsavedKey + "' = '" + printVal +"'");
				this.model.saveSetting(unsavedKey, value);
			}
			else {
				log.warn("Unsaved form element: " + unsavedKey + " is invalid. Not saving.");
			}
		}
		this.model.saveAllSettings();
	}

	/**
	 * gets the value allowed to be shown to anyone, based on the given setting and value
	 * @param setting
	 * @param value
	 * @return masks the value, if appropriate. Example, if the setting is
	 * <table>
	 * <tr><td>setting</td><td>value</td><td>returns</td></tr>
	 * <tr><td>PASSWORD</td><td>1234</td><td>*******</td></tr>
	 * <tr><td>SPACE</td><td>key</td><td>key</td></tr>
	 * </table>
	 */
	private String getPrintablePassword(Setting setting, String value) {
		String printVal = value;
		if (setting == Setting.PASSWORD) {
			printVal = "*******";
		}
		return printVal;
	}
	
	/**
	 * gets the value for the given key. only enabled for
	 * URL, LOGIN, PASSWORD, SPACE, and ATTACHMENT_SIZE.
	 * 
	 * @param setting given key
	 * @return the value or null, if this method is not supported with the
	 * given key
	 * @throws IllegalArgumentException if the given setting is null
	 */
	public String getFormValue(Setting setting) {
		if (setting == null) throw new IllegalArgumentException("unsavedKey parameter may not be null");
		switch (setting) {
		case URL:
			return getJTextfieldAddress().getText();
		case LOGIN:
			return getJTextFieldLogin().getText();
		case PASSWORD:
			return SaveListener.getPasswordData(getJPasswordField());
		case SPACE:
			return getJTextFieldSpace().getText();
		case ATTACHMENT_SIZE:
			return getJTextFieldAttachmentSize().getText();
		case ATTACHMENTS:
			return getJTextFieldAttachments().getText();
		default:
			log.warn("Not handling element: " + setting);	
		}
		return null;
	}
	
	/* Swing Object Getters/Creators */

	/**
	 * creates or gets the container which handles the 
	 * tabs.	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.setFont(UWCLabel.getUWCFont());
			//title, icon, component, tip
			jTabbedPane.addTab("Conversion Settings", null, getJPanelBasic(), null);
			jTabbedPane.addTab("Other Tools", null, getJPanelAdvanced(), null);
			//set the current tab from settings
			jTabbedPane.setSelectedIndex(
					Integer.parseInt(
							model.getSetting(
									UWCUserSettings.Setting.CURRENT_TAB)));
			//create listener to save current tab as a setting
			jTabbedPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent event) {
					JTabbedPane pane = (JTabbedPane) event.getSource();
					int index = pane.getSelectedIndex();
					log.debug("Saving Current Tab = " + index);
					model.saveSetting(UWCUserSettings.Setting.CURRENT_TAB, index + "");
				}
			});
		}
		return jTabbedPane;
	}

	/**
	 * creates or gets the panel reflecting
	 * the main UI elements contained in the 
	 * Conversion Settings tab	
	 */
	private JPanel getJPanelBasic() {
		if (jPanelBasic == null) {
			
			jPanelBasic = new JPanel();
			jPanelBasic.setLayout(new GridBagLayout());
			jPanelBasic.setPreferredSize(TABBEDPANE_SIZE);
			
			//convert from wiki label
			GridBagConstraints gridBagConstraints_jPanelFromLabel = getHeaderLabelConstraints(0);
			gridBagConstraints_jPanelFromLabel.weightx = 0.1;
			getJPanelFromLabel();
			
			//type label
			jLabelType = new UWCLabel();
			jLabelType.setText("Type:");
			GridBagConstraints gridBagConstraints_jLabelType = getBaseLabelConstraints(1);

			//wikitypes combobox
			GridBagConstraints gridBagConstraints_jComboBoxWikiType = getBaseConstraints();
			gridBagConstraints_jComboBoxWikiType.gridx = 1;
			gridBagConstraints_jComboBoxWikiType.gridy = 1;
			gridBagConstraints_jComboBoxWikiType.gridwidth = 2;
			gridBagConstraints_jComboBoxWikiType.fill = GridBagConstraints.BOTH;
			
			
			//export button
			GridBagConstraints gridBagConstraints_jButtonExport = getBaseConstraints();
			gridBagConstraints_jButtonExport.gridx = 3;
			gridBagConstraints_jButtonExport.gridy = 1;
			gridBagConstraints_jButtonExport.insets = new Insets(BASE_INSET,BASE_INSET,BASE_INSET, BASIC_RIGHT_MARGIN);
						
			//directory label
			jLabelDirectory = new UWCLabel();
			jLabelDirectory.setText(getDirectoryLabel()+":");
			jLabelDirectory.setToolTipText(ATTACHMENTS_TOOLTIP);

			//browse for dir button
			GridBagConstraints gridBagConstraints_jButtonDirectory = getBaseConstraints();
			gridBagConstraints_jButtonDirectory.gridx = 3;
			gridBagConstraints_jButtonDirectory.gridy = 3;
			gridBagConstraints_jButtonDirectory.insets = new Insets(BASE_INSET,BASE_INSET,BASE_INSET, BASIC_RIGHT_MARGIN);

			//pages label
			jLabelPages = new UWCLabel();
			jLabelPages.setText("Pages:");
			
			//pages list (in a scroll pane)
			GridBagConstraints gridBagConstraints_jScrollPages = getBaseConstraints();
			gridBagConstraints_jScrollPages.gridx = 1;
			gridBagConstraints_jScrollPages.gridy = 4;
			gridBagConstraints_jScrollPages.gridwidth = 3;
			gridBagConstraints_jScrollPages.gridheight = 7;
			gridBagConstraints_jScrollPages.fill = GridBagConstraints.BOTH;
			gridBagConstraints_jScrollPages.weighty = 0.75; 
			gridBagConstraints_jScrollPages.insets = new Insets(BASE_INSET,BASE_INSET,BASE_INSET, BASIC_RIGHT_MARGIN);
			
			//add page button
			GridBagConstraints gridBagConstraints_jButtonAdd = getBaseConstraints();
			gridBagConstraints_jButtonAdd.gridx = 1;
			gridBagConstraints_jButtonAdd.gridy = GridBagConstraints.RELATIVE;
			gridBagConstraints_jButtonAdd.anchor = GridBagConstraints.FIRST_LINE_START;
			
			//remove page button
			GridBagConstraints gridBagConstraints_jButtonRemove = getBaseConstraints();
			gridBagConstraints_jButtonRemove.gridx = 2;
			gridBagConstraints_jButtonRemove.gridy = GridBagConstraints.RELATIVE;
			gridBagConstraints_jButtonRemove.anchor = GridBagConstraints.FIRST_LINE_START;

			//to label
			GridBagConstraints gridBagConstraints_jPanelToLabel = getHeaderLabelConstraints(GridBagConstraints.RELATIVE);

			//add components to jPanelBasic
			jPanelBasic.add(getJPanelFromLabel(), gridBagConstraints_jPanelFromLabel);
			jPanelBasic.add(jLabelType, gridBagConstraints_jLabelType);
			jPanelBasic.add(getJComboBox_WikiType(), gridBagConstraints_jComboBoxWikiType);
			jPanelBasic.add(getJButtonExport(), gridBagConstraints_jButtonExport);
			jPanelBasic.add(jLabelDirectory, getBaseLabelConstraints(3));
			jPanelBasic.add(getJTextFieldAttachments(), getBaseSettingsConstraints(3));
			jPanelBasic.add(getJButtonAttachments(), gridBagConstraints_jButtonDirectory);
			jPanelBasic.add(jLabelPages, getBaseLabelConstraints(4));
			jPanelBasic.add(getJScrollPanePages(), gridBagConstraints_jScrollPages);
			jPanelBasic.add(getJButtonAdd(), gridBagConstraints_jButtonAdd);
			jPanelBasic.add(getJButtonRemovePages(), gridBagConstraints_jButtonRemove);
			jPanelBasic.add(getJPanelToLabel(), gridBagConstraints_jPanelToLabel);
			jPanelBasic.add(getJLabelAddress(), getBaseLabelConstraints(GridBagConstraints.RELATIVE));
			jPanelBasic.add(getJTextfieldAddress(), getToConfluenceTextfieldContraints(13));
			jPanelBasic.add(getJLabelLogin(), getBaseLabelConstraints(14));
			jPanelBasic.add(getJTextFieldLogin(), getToConfluenceTextfieldContraints(14));
			jPanelBasic.add(getJLabelPass(), getBaseLabelConstraints(15));
			jPanelBasic.add(getJPasswordField(), getToConfluenceTextfieldContraints(15));
			jPanelBasic.add(getJLabelSpace(), getBaseLabelConstraints(16));
			jPanelBasic.add(getJTextFieldSpace(), getToConfluenceTextfieldContraints(16));
		}
		return jPanelBasic;
	}

	/**
	 * @return the contents of the label for the directory field. 
	 * Note: this was encapuslated in a field to provide a spot for
	 * a possible new feature: extracting an optional non-converter property
	 * which would provide the wiki specific label, using the default label when 
	 * the property is not supplied
	 */
	private String getDirectoryLabel() {
		//XXX Possibly use a non-converter property to extract a wiki specific label?
		return DEFAULT_DIR_LABEL;
	}

	/**
	 * creates or gets the label for the space field
	 */
	private UWCLabel getJLabelSpace() {
		if (jLabelSpace == null) {
			jLabelSpace = new UWCLabel();
			jLabelSpace.setText("Space Key:");
			jLabelSpace.setToolTipText(SPACE_TOOLTIP);
		}
		return jLabelSpace;
	}

	/**
	 * creates or gets the label for the password field
	 */
	private UWCLabel getJLabelPass() {
		if (jLabelPass == null) {
			jLabelPass = new UWCLabel();
			jLabelPass.setText("Password:");
			jLabelPass.setToolTipText(PASSWORD_TOOLTIP);
		}
		return jLabelPass;
	}

	/**
	 * creates or gets the label for the login field
	 */
	private UWCLabel getJLabelLogin() {
		if (jLabelLogin == null) {
			jLabelLogin = new UWCLabel();
			jLabelLogin.setText("Login:");
			jLabelLogin.setToolTipText(LOGIN_TOOLTIP);
		}
		return jLabelLogin;
	}

	/**
	 * creates or gets the address textfield
	 */
	private JTextField getJTextfieldAddress() {
		if (jTextFieldAddress == null) {
			jTextFieldAddress = new JTextField();
			jTextFieldAddress.setFont(UWCLabel.getUWCFont());
			jTextFieldAddress.setToolTipText(ADDRESS_TOOLTIP);
			
			SaveListener saveListener = new SaveListener(
										this.jTextFieldAddress, 
										this.model, 
										Setting.URL,
										this.feedbackWindow 
								);
			jTextFieldAddress.addActionListener(saveListener);
			jTextFieldAddress.addFocusListener(saveListener);
			jTextFieldAddress.addFocusListener(getHasAllListener());
			jTextFieldAddress.addKeyListener(new IgnoreAltListener(jTextFieldAddress));
			jTextFieldAddress.getDocument().addDocumentListener(getHasAllListener());

			jTextFieldAddress.setText(this.model.getSetting(Setting.URL));
			
		}
		return jTextFieldAddress;
	}

	/**
	 * creates or gets the label for the address field
	 */
	private UWCLabel getJLabelAddress() {
		if (jLabelAddress == null) {
			jLabelAddress = new UWCLabel();
			jLabelAddress.setText("Address:");
			jLabelAddress.setToolTipText(ADDRESS_TOOLTIP);
		}
		return jLabelAddress;
	}

	/**
	 * creates or gets the wikitype dropdown menu
	 */
	private JComboBox getJComboBox_WikiType() {
		if (jComboBox_WikiType == null) {
			jComboBox_WikiType = new JComboBox();
			jComboBox_WikiType.setFont(UWCLabel.getUWCFont());
			jComboBox_WikiType.setPreferredSize(MED_SIZE);
			jComboBox_WikiType.setModel(getWikiTypes());
			
			jComboBox_WikiType.addActionListener(
					new WikiIsExportableListener(
							jComboBox_WikiType,
							getJButtonExport(),
							this.model,
							getPropsDir()
							));
			jComboBox_WikiType.addActionListener(
					new WikiIsExportableListener(
							jComboBox_WikiType,
							getExportMenuItem(),
							this.model,
							getPropsDir()
							));
			jComboBox_WikiType.addActionListener(
					new SaveListener(
							this.jComboBox_WikiType,
							this.model,
							Setting.WIKITYPE,
							this.feedbackWindow
					));
			jComboBox_WikiType.addActionListener(
					new GuiDisablingListener(
							this.jComboBox_WikiType,
							getPropsDir(),
							this.feedbackWindow,
							this
					));
			jComboBox_WikiType.addActionListener(getHasAllListener());
			jComboBox_WikiType.setSelectedItem(this.model.getSetting(Setting.WIKITYPE));
		}
		return jComboBox_WikiType;
	}

	/**
	 * @return the directory where properties are located.
	 * The default value is PROPS_DIR.
	 */
	private String getPropsDir() {
		return (commandLineArgDir == null)?PROPS_DIR:commandLineArgDir;
	}

	/**
	 * @return a model that will provide the available wiki types,
	 * based on the existing converter.xxx.properties files in existence
	 */
	protected ComboBoxModel getWikiTypes() {
		Vector wikitypes = model.getWikiTypesList(getPropsDir());
		ComboBoxModel cbModel = new DefaultComboBoxModel(wikitypes);
		cbModel.setSelectedItem(null);
		return cbModel;
	}

	/**
	 * creates or gets the Export button
	 */
	private JButton getJButtonExport() {
		if (jButtonExport == null) {
			jButtonExport = new JButton();
			jButtonExport.setText("Export");
			jButtonExport.setFont(UWCLabel.getUWCFont());
//			this gets set as a result of an event triggered by the wikitypes combobox
			jButtonExport.setEnabled(false); 
			jButtonExport.addActionListener(new ExportWikiListener(
					jComboBox_WikiType,
					this.model,
					getPropsDir(),
					getFeedbackWindow()));
		}
		return jButtonExport;
	}

	/**
	 * creates or gets the From Confluence header label
	 */
	private JPanel getJPanelFromLabel() {
		if (jPanelFromLabel == null) {
			jPanelFromLabel = createLineLabel(
					jPanelFromLabel, 
					line1, 
					line2, 
					"Convert From Wiki", 50);
		}
		return jPanelFromLabel;
	}

	/**
	 * creates or gets the To Confluence header label
	 */
	private JPanel getJPanelToLabel() {
		if (jPanelToLabel == null) {
			
			jPanelToLabel = createLineLabel(
					jPanelToLabel,
					line3,
					line4,
					"To Confluence", 50);
		}
		return jPanelToLabel;
	}

	/**
	 * creates a panel which visibly represents 
	 * an app wide label with headers and decorative lines
	 * @param panel We pass this in because we want to use fields for these objects,
	 * <i>and</i> we might want them to be different for different usages of this method.
	 * @param line1 see panel
	 * @param line2 see panel
	 * @param text Label's visible text
	 * @param leftMargin left column's left margin
	 * @return
	 */
	private JPanel createLineLabel(
			JPanel panel, 
			UWCGuiLine line1, 
			UWCGuiLine line2, 
			String text, 
			int leftMargin) {
		
		panel = new JPanel();
		
		int width = 107;
		
		line1 = new UWCGuiLine(0, 5, width, 5);
		line2 = new UWCGuiLine(0, 5, width, 5);

		UWCLabel label = new UWCLabel();
		label.setText(text);
		label.setHorizontalAlignment(SwingConstants.CENTER); 
		Dimension size = label.getPreferredSize();
		size.width -= 40;
		label.setPreferredSize(size);

		panel.setLayout(new GridBagLayout());


		/*
		 * NOTE: The layout for these panels is
		 * a delicate balancing act between the
		 * weightx, anchors, and margins.
		 * The anchor for each object is CENTER,
		 * set in the getHeaderConstraintsByColumn method.
		 * 
		 * As far as I can tell, a decent English translation of these 
		 * constraint instructions would go something like:
		 * First: give the left line a left margin of 50px.
		 * Second: Divide the remaining space into 3 chunks, such that:
		 * - label's chunk is half of line2's, and
		 * - line1's chunk is 7/10 of line2's
		 * Last: Center the objects into their particular chunk of space
		 * 
		 * The purpose is to place line1 close to the left edge of label
		 * and line2 close to the right edge of label
		 */

		/* 
		 * line 1 contraints need different insets 
		 * so that the first line is placed correctly.
		 */
		GridBagConstraints line1Constraints = getHeaderConstraintsByColumn(0, 0.7);  
		line1Constraints.insets = new Insets(BASE_INSET, leftMargin, BASE_INSET, BASE_INSET);

		panel.add(line1, line1Constraints);
		panel.add(label, getHeaderConstraintsByColumn(1, 0.5));
		panel.add(line2, getHeaderConstraintsByColumn(2, 1.0));
		
		return panel;
	}

	/**
	 * creates or gets attachment directory component 
	 */
	private JTextField getJTextFieldAttachments() {
		if (jTextFieldAttachments == null) {
			jTextFieldAttachments = new JTextField();
			jTextFieldAttachments.setFont(UWCLabel.getUWCFont());
			jTextFieldAttachments.setToolTipText(ATTACHMENTS_TOOLTIP);
			
			//saving events
			SaveListener saveListener = new SaveListener(
										this.jTextFieldAttachments, 
										this.model, 
										Setting.ATTACHMENTS,
										this.feedbackWindow 
								);
			jTextFieldAttachments.addActionListener(saveListener);
			jTextFieldAttachments.addFocusListener(saveListener);
			jTextFieldAttachments.addKeyListener(new IgnoreAltListener(jTextFieldAttachments));
			//loading setting
			jTextFieldAttachments.setText(this.model.getSetting(Setting.ATTACHMENTS));
		}
		return jTextFieldAttachments;
	}

	/**
	 * creates or gets the browse button for the attachment's textfield 
	 */
	private JButton getJButtonAttachments() {
		if (jButtonDirectory == null) {
			jButtonDirectory = new JButton();
			jButtonDirectory.setText("Browse");
			jButtonDirectory.setFont(UWCLabel.getUWCFont());
			jButtonDirectory.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent event) {
					//code grabbed from http://www.exampledepot.com/egs/javax.swing.filechooser/CreateDlg.html
					JFileChooser dialog = new JFileChooser();
				    dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				    // Show open dialog; this method does not return until the dialog is closed
				    int result = dialog.showOpenDialog(new JFrame());
				    if (result == JFileChooser.APPROVE_OPTION) {
				    	File dirFile = dialog.getSelectedFile();
				    	jTextFieldAttachments.setText(dirFile.getPath());
				    	//save the field
				    	jTextFieldAttachments.getActionListeners()[0].actionPerformed(
				    			new ActionEvent(event.getSource(), event.getID(), SaveListener.DEFAULT_COMMAND));
				    }
				    	
				}
			});
			
		}
		return jButtonDirectory;
	}

	/**
	 * creates or gets the pages panel which displays the chosen pages to be converted
	 */
	private JScrollPane getJScrollPanePages() {
		if (jScrollPanePages == null) {
			jScrollPanePages = new JScrollPane(getJListPages());
			jScrollPanePages.setFont(UWCLabel.getUWCFont());

				
			jScrollPanePages.addMouseListener(getAddPagesListener());
			jScrollPanePages.setDropTarget(new DropTarget(
					jScrollPanePages, 
					DnDConstants.ACTION_COPY, //allowed actions
					getDragnDropListener(), //listener that handles drag-n-drop events
					true //pane is accepting drops
					));
			getJListPages().setDropTarget(new DropTarget(
					jListPages,
					DnDConstants.ACTION_COPY,
					getDragnDropListener(),
					true
			));
		}
		return jScrollPanePages;
	}
	
	private JList getJListPages() {
		if (this.jListPages == null) {
			//pages are from confluenceSetting.properties
			Vector<String> filenames = this.model.getPageNames();
			if (filenames == null || filenames.isEmpty())
				this.jListPages = new JList();
			else
				this.jListPages = new JList(filenames); 
			this.jListPages.setFont(UWCLabel.getUWCFont());
		}
		return this.jListPages;
	}


	private SelectFileDropTargetListener getDragnDropListener() {
		if (this.dragnDropListener == null) {
			dragnDropListener = new SelectFileDropTargetListener(jScrollPanePages, this.model);
			//observers
			dragnDropListener.addObserver(getRemoveButtonEnabledListener());
			dragnDropListener.addObserver(getHasAllListener());
			dragnDropListener.addObserver(new SaveListener(
					getJListPages(),
					this.model,
					Setting.PAGES,
					this.feedbackWindow
			));
		}
		return dragnDropListener;
	}

	/**
	 * creates or gets the add pages button 
	 */
	private JButton getJButtonAdd() {
		if (jButtonAddPages == null) {
			jButtonAddPages = new JButton();
			jButtonAddPages.setText("Add");
			jButtonAddPages.setFont(UWCLabel.getUWCFont());
			AddPagesListener addPagesListener = getAddPagesListener();
			jButtonAddPages.addActionListener(addPagesListener);
			
		}
		return jButtonAddPages;
	}
	
	/**
	 * @return this form's model
	 */
	UWCGuiModel getModel() {
		return this.model;
	}

	/**
	 * creates or gets the remove button  
	 */
	JButton getJButtonRemovePages() {
		if (jButtonRemovePages == null) {
			jButtonRemovePages = new JButton();
			jButtonRemovePages.setText("Remove");
			jButtonRemovePages.setFont(UWCLabel.getUWCFont());
			RemovePagesListener removePagesListener = getRemovePagesListener();
			jButtonRemovePages.addActionListener(removePagesListener);
			
			jButtonRemovePages.setEnabled(!model.getPageFiles().isEmpty());
		}
		return jButtonRemovePages;
	}

	/**
	 * creates or gets the login textfield  
	 */
	private JTextField getJTextFieldLogin() {
		if (jTextFieldLogin == null) {
			jTextFieldLogin = new JTextField();
			jTextFieldLogin.setFont(UWCLabel.getUWCFont());
			jTextFieldLogin.setToolTipText(LOGIN_TOOLTIP);
			
			SaveListener saveListener = new SaveListener(
										this.jTextFieldLogin, 
										this.model, 
										Setting.LOGIN,
										this.feedbackWindow 
								);
			jTextFieldLogin.addActionListener(saveListener);
			jTextFieldLogin.addFocusListener(saveListener);
			jTextFieldLogin.addFocusListener(getHasAllListener());
			jTextFieldLogin.addKeyListener(new IgnoreAltListener(jTextFieldLogin));
			jTextFieldLogin.getDocument().addDocumentListener(getHasAllListener());
			
			jTextFieldLogin.setText(this.model.getSetting(Setting.LOGIN));
		}
		return jTextFieldLogin;
	}

	/**
	 * creates or gets the password field  
	 */
	private JPasswordField getJPasswordField() {
		if (jPasswordField == null) {
			jPasswordField = new JPasswordField();
			jPasswordField.setFont(UWCLabel.getUWCFont());
			jPasswordField.setToolTipText(PASSWORD_TOOLTIP);
			
			SaveListener saveListener = new SaveListener(
										this.jPasswordField, 
										this.model, 
										Setting.PASSWORD,
										this.feedbackWindow 
								);
			jPasswordField.addActionListener(saveListener);
			jPasswordField.addFocusListener(saveListener);
			jPasswordField.addKeyListener(new IgnoreAltListener(jPasswordField));
			jPasswordField.getDocument().addDocumentListener(getHasAllListener());
			
			jPasswordField.setText(this.model.getSetting(Setting.PASSWORD));
		}
		return jPasswordField;
	}

	/**
	 * creates or gets the space textfield  
	 */
	private JTextField getJTextFieldSpace() {
		if (jTextFieldSpace == null) {
			jTextFieldSpace = new JTextField();
			jTextFieldSpace.setFont(UWCLabel.getUWCFont());
			jTextFieldSpace.setToolTipText(SPACE_TOOLTIP);
			
			//saving events
			SaveListener saveListener = new SaveListener(
										this.jTextFieldSpace, 
										this.model, 
										Setting.SPACE,
										this.feedbackWindow 
								);
			jTextFieldSpace.addActionListener(saveListener);
			jTextFieldSpace.addFocusListener(saveListener);
			jTextFieldSpace.addFocusListener(getHasAllListener());
			jTextFieldSpace.addKeyListener(new IgnoreAltListener(jTextFieldSpace));
			jTextFieldSpace.getDocument().addDocumentListener(getHasAllListener());
			
			//loading from settings
			jTextFieldSpace.setText(this.model.getSetting(Setting.SPACE));
		}
		return jTextFieldSpace;
	}

	/**
	 * creates or gets the advanced settings panel.	
	 */
	private JPanel getJPanelAdvanced() {
		if (jPanelAdvanced == null) {
			
			jPanelAdvanced = new JPanel();
			jPanelAdvanced.setLayout(new GridBagLayout());
			jPanelAdvanced.setPreferredSize(TABBEDPANE_SIZE);
			
			GridBagConstraints constraints_OptSettings = getHeaderLabelConstraints(0);
			constraints_OptSettings.weightx = 0.1;
			constraints_OptSettings.gridwidth = 2;
			constraints_OptSettings.insets = new Insets(BASE_INSET, BASE_INSET, BASE_INSET, BASIC_RIGHT_MARGIN);

			GridBagConstraints constraints_Tools = getHeaderLabelConstraints(5);
			constraints_Tools.weightx = 0.1;
			constraints_Tools.gridwidth = 2;
			int moreVerticalSpace = 30;
			constraints_Tools.insets = new Insets(moreVerticalSpace, BASE_INSET, BASE_INSET, BASIC_RIGHT_MARGIN);

			int row = 0;
			jPanelAdvanced.add(getJPanelOptSettings(), constraints_OptSettings);
			row++;
			jPanelAdvanced.add(getJCheckBoxSendToConfluence(), getAdvancedButtonConstraints(row));
			jPanelAdvanced.add(getJLabelSendToConf(), getAdvancedLabelConstraints(row++));
			jPanelAdvanced.add(getJCheckBoxFeedbackOption(), getAdvancedButtonConstraints(row));
			jPanelAdvanced.add(getJLabelFeedbackOption(), getAdvancedLabelConstraints(row++));
			jPanelAdvanced.add(getJCheckBoxAttachmentSize(), getAdvancedButtonConstraints(row));
			jPanelAdvanced.add(getJLabelAttachmentSize(), getAttachSizeLabelConstraints(row++));	
			jPanelAdvanced.add(getJTextFieldAttachmentSize(), getAttachSizeTextFieldConstraints(row++));
			jPanelAdvanced.add(getJPanelTools(), constraints_Tools);
			row++;
			jPanelAdvanced.add(getJButtonTestConnection(), getAdvancedButtonConstraints(row));
			jPanelAdvanced.add(getJLabelTestConnection(), getAdvancedLabelConstraints(row++));
			jPanelAdvanced.add(getJButtonLaunchRegexTester(), getAdvancedButtonConstraints(row));
			jPanelAdvanced.add(getJLabelLaunchRegexTester(), getAdvancedLabelConstraints(row++));
			jPanelAdvanced.add(getJButtonLaunchFeedback(), getAdvancedButtonConstraints(row));
			jPanelAdvanced.add(getJLabelLaunchFeedback(), getAdvancedLabelConstraints(row++));
		}
		return jPanelAdvanced;
	}

	/**
	 * creates or gets the label for the attachment size setting
	 */
	private JLabel getJLabelAttachmentSize() {
		if (jLabelAttachmentSize == null) {
			jLabelAttachmentSize = new UWCLabel();
			String text = 
					"<html>" +
					"Restrict Uploaded Attachment Size." +
					"<br>" +
					"Max File Size:" +
					"</html>";
			jLabelAttachmentSize.setText(text);
			jLabelAttachmentSize.setPreferredSize(new Dimension(200, 40));
		}
		return jLabelAttachmentSize;
	}

	/**
	 * creates or gets the Optional Settings header label
	 */
	private JPanel getJPanelOptSettings() {
		if (jPanel_OptSettings == null) {
			jPanel_OptSettings = createLineLabel(
					jPanel_OptSettings, 
					line5, 
					line6, 
					"Optional Settings", 40);
		}
		return jPanel_OptSettings;
	}

	/**
	 * creates or gets the Developer Tools header label
	 */
	private JPanel getJPanelTools() {
		if (jPanel_Tools == null) {
			jPanel_Tools = createLineLabel(
					jPanel_Tools, 
					line7, 
					line8, 
					"Developer Tools", 40);
		}
		return jPanel_Tools;
	}

	/**
	 * creates or gets the label for the Send Pages To Confluence setting 
	 */
	private UWCLabel getJLabelSendToConf() {
		if (jLabelSendToConfluence == null) {
			jLabelSendToConfluence = new UWCLabel();
			String text = "<html>" +
					"Pages will be sent to Confluence" +
					"<br>" +
					"at the end of the conversion, if this is checked." +
					"</html>";
			jLabelSendToConfluence.setText(text);
			jLabelSendToConfluence.setPreferredSize(new Dimension(200, 50));
		}
		return jLabelSendToConfluence;
	}
	
	/**
	 * creates or gets the label for the Launch Feedback After Convert or Export option
	 */
	private UWCLabel getJLabelFeedbackOption() {
		if (jLabelFeedbackOption == null) {
			jLabelFeedbackOption = new UWCLabel();
			String text = 
					"<html>" +
					"Launch Feedback Window" +
					"<br>" +
					"after convert or export"
					+ "</html>";
			jLabelFeedbackOption.setText(text);
			jLabelFeedbackOption.setPreferredSize(new Dimension(200, 50));
		}
		return jLabelFeedbackOption;
	}
	
	/**
	 * creates or gets the label for the test connection button
	 */
	private UWCLabel getJLabelTestConnection() {
		if (jLabelTestConnection == null) {
			jLabelTestConnection = new UWCLabel();
			String text = "Test the Confluence Server Settings";
			jLabelTestConnection.setText(text);
		}
		return jLabelTestConnection;
	}
	
	/**
	 * creates or gets the label for the regex tester button
	 */
	private UWCLabel getJLabelLaunchRegexTester() {
		if (jLabelLaunchRegexTester == null) {
			jLabelLaunchRegexTester = new UWCLabel();
			String text = "Launch the Regex Tester";
			jLabelLaunchRegexTester.setText(text);
		}
		return jLabelLaunchRegexTester;
	}

	/**
	 * creates or gets the label for the launch feedback button
	 */
	private UWCLabel getJLabelLaunchFeedback() {
		if (jLabelLaunchFeedback == null) {
			jLabelLaunchFeedback = new UWCLabel();
			String text = "Launch the Feedback Window";
			jLabelLaunchFeedback.setText(text);
		}
		return jLabelLaunchFeedback;
	}

	/**
	 * creates or gets the Send to Confluence checkbox    
	 */
	private JCheckBox getJCheckBoxSendToConfluence() {
		if (jCheckBoxSendToConfluence == null) {
			jCheckBoxSendToConfluence = new JCheckBox();
			jCheckBoxSendToConfluence.setFont(UWCLabel.getUWCFont());
			
			jCheckBoxSendToConfluence.setText(""); //No label - we'll add one somewhere else
			loadSetting(jCheckBoxSendToConfluence, Setting.SEND_TO_CONFLUENCE);
			jCheckBoxSendToConfluence.addItemListener(getSendPagesCheckboxListener());

			AbstractButton otherUiElement = getSendPagesCheckboxMenuItem();
			CheckboxObserver observer = new CheckboxObserver(
					this, otherUiElement, Setting.SEND_TO_CONFLUENCE);
			getSendPagesCheckboxListener().addObserver(observer);

			
		}
		return jCheckBoxSendToConfluence;
	}

	/**
	 * creates or gets the get feedback option checkbox    
	 */
	private JCheckBox getJCheckBoxFeedbackOption() {
		if (jCheckBoxFeedbackOption == null) {
			jCheckBoxFeedbackOption = new JCheckBox();
			jCheckBoxFeedbackOption.setFont(UWCLabel.getUWCFont());
			
			jCheckBoxFeedbackOption.setText(""); //No label - we'll add one somewhere else
			loadSetting(jCheckBoxFeedbackOption, Setting.FEEDBACK_OPTION);
			jCheckBoxFeedbackOption.addItemListener(getLaunchFeedbackCheckboxListener());
			
			AbstractButton otherUiElement = getLaunchFeedbackCheckBoxMenuItem();
			CheckboxObserver observer = new CheckboxObserver(
					this, otherUiElement, Setting.FEEDBACK_OPTION);
			getLaunchFeedbackCheckboxListener().addObserver(observer);

		}
		return jCheckBoxFeedbackOption;
	}
	/**
	 * creates or gets the test connection button    
	 */
	private JButton getJButtonTestConnection() {
		if (jButtonTestConnection == null) {
			jButtonTestConnection = new JButton();
			jButtonTestConnection.setFont(UWCLabel.getUWCFont());
			jButtonTestConnection.setText("Test Connection");
			//use this listener to get feedback about the test
			Vector<JTextField> testableSettingsObjects = getTestableSettingsObjects();
			testSettingsListener = new TestSettingsListener(testableSettingsObjects, this.model, this.getFeedbackWindow());
			jButtonTestConnection.addActionListener(testSettingsListener);
		}
		return jButtonTestConnection;
	}

	/**
	 * @return a vector of To Confluence textfields (address, login, pass, space)
	 */
	private Vector<JTextField> getTestableSettingsObjects() {
		Vector<JTextField> testables = new Vector<JTextField>();
		testables.add(getJTextfieldAddress());
		testables.add(getJTextFieldLogin());
		testables.add(getJPasswordField());
		testables.add(getJTextFieldSpace());
		return testables;
	}

	/**
	 * creates or gets the launch regex tester button    
	 */
	private JButton getJButtonLaunchRegexTester() {
		if (jButtonLaunchRegexTester == null) {
			jButtonLaunchRegexTester = new JButton();
			jButtonLaunchRegexTester.setFont(UWCLabel.getUWCFont());
			jButtonLaunchRegexTester.setText("Regex Tester");
			jButtonLaunchRegexTester.addActionListener(getRegexLauncher());
			
		}
		return jButtonLaunchRegexTester;
	}

	/**
	 * creates or gets the launch feedback button    
	 */
	private JButton getJButtonLaunchFeedback() {
		if (jButtonLaunchFeedback == null) {
			jButtonLaunchFeedback = new JButton();
			jButtonLaunchFeedback.setFont(UWCLabel.getUWCFont());
			jButtonLaunchFeedback.setText("Launch Feedback");
			jButtonLaunchFeedback.addActionListener(getLaunchFeedbackListener());
		}
		return jButtonLaunchFeedback;
	}
	
	/**
	 * creates or gets the feedback window.
	 */
	private FeedbackWindow getFeedbackWindow() {
		if (feedbackWindow == null) {
			feedbackWindow = new FeedbackWindow();
		}
		return feedbackWindow;
	}
	
	/**
	 * creates or gets the panel containing the convert and close buttons   
	 */
	private JPanel getJPanelMain() {
		if (jPanelMain == null) {
			jPanelMain = new JPanel();
			jPanelMain.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 5));
			jPanelMain.add(getJButtonConvert());
			jPanelMain.add(getJButtonClose());
		}
		return jPanelMain;
	}

	/**
	 * creates or gets the convert button    
	 */
	private JButton getJButtonConvert() {
		if (jButtonConvert == null) {
			jButtonConvert = new JButton();
			jButtonConvert.setFont(UWCLabel.getUWCFont());
			jButtonConvert.setText("Convert");
			jButtonConvert.addActionListener(
					new ConvertListener(jComboBox_WikiType, model, getPropsDir(), getFeedbackWindow()));
			jButtonConvert.setEnabled(hasSetAllConverterSettings());
		}
		return jButtonConvert;
	}

	/**
	 * @return true if all the conditions necessary to attempt conversion exist.
	 * Which wikitype, at least one page chosen, and all the To Confluence settings aren't empty  
	 */
	public boolean hasSetAllConverterSettings() {
		boolean hasWikitype = hasItem((String) getJComboBox_WikiType().getSelectedItem());
		boolean hasAddress = hasItem(getJTextfieldAddress().getText());
		boolean hasLogin = hasItem(getJTextFieldLogin().getText());
		boolean hasPass = getJPasswordField().getDocument().getLength() > 0; //doesn't respond to hasItem like textfield
		boolean hasSpace = hasItem(getJTextFieldSpace().getText());
		boolean hasOnePage = !model.getPageFiles().isEmpty();
		
		boolean hasSettings = hasWikitype && hasAddress && hasLogin && hasPass && hasSpace && hasOnePage;
		return hasSettings;
	}

	/**
	 * @param item
	 * @return true if given item represent an existing value
	 */
	private boolean hasItem(String item) {
		return (item != null && !"".equals(item));
	}

	/**
	 * creates or gets the close button    
	 */
	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton();
			jButtonClose.setFont(UWCLabel.getUWCFont());
			jButtonClose.setText("Close");
			jButtonClose.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					shutdownUWC();
				}
			});
			
		}
		return jButtonClose;
	}


	/* Listeners */
	
	/**
	 * creates or gets the listener which controls the add button's behavior.
	 * - handles launching the file dialog and choosing a page
	 * - handles notifying the remove button that it needs to check for enabled/disabled status
	 * - handles notifying the convert button that it needs to check for enabled/disabled status  
	 */
	private AddPagesListener getAddPagesListener() {
		if (this.addPagesListener == null ) {
			this.addPagesListener = new AddPagesListener(jScrollPanePages, model);
			//remove button's enabled state is dependant on whether there are currently pages or not
			this.addPagesListener.addObserver(getRemoveButtonEnabledListener());  
			//convert button's enabled state is dependant on whether there are currently pages or not
			this.addPagesListener.addObserver(getHasAllListener());
			this.addPagesListener.addObserver(new SaveListener(
					getJListPages(),
					this.model,
					Setting.PAGES,
					this.feedbackWindow
			));
		}
		return this.addPagesListener;
	}

	/**
	 * @return Observer which will check to see if the remove button should be enabled/disabled
	 */
	private Observer getRemoveButtonEnabledListener() {
		return (new Observer() {
			public void update(Observable o, Object arg) {
				UWCGuiModel model = getModel();
				boolean removeIsEnabled = !model.getPageFiles().isEmpty();
				getJButtonRemovePages().setEnabled(removeIsEnabled);
			}
		});
	}
	
	/**
	 * creates or gets the listener which controls the remove button's behavior.
	 * - handles remove chosen pages from the pages scrollpane
	 * - handles notifying the remove button that it needs to check for enabled/disabled status
	 * - handles notifying the convert button that it needs to check for enabled/disabled status  
	 */
	private RemovePagesListener getRemovePagesListener() {
		if (this.removePagesListener == null) {
			this.removePagesListener = new RemovePagesListener(jScrollPanePages, model);
			this.removePagesListener.addObserver(getHasAllListener());
			this.removePagesListener.addObserver(getRemoveButtonEnabledListener());
			this.removePagesListener.addObserver(new SaveListener(
					getJListPages(),
					this.model,
					Setting.PAGES,
					this.feedbackWindow
			));
		}
		return this.removePagesListener;
	}
	
	/**
	 * @return the listener that will enable/disable the given component based on
	 * the settings provided by the user
	 */
	private HasAllConvertSettingsListener getHasAllListener() {
		if (this.hasAllConvertSettingsListener == null) {
			this.hasAllConvertSettingsListener = new HasAllConvertSettingsListener(this);
			this.hasAllConvertSettingsListener.registerComponent(getJButtonConvert());
			this.hasAllConvertSettingsListener.registerComponent(getConvertMenuItem());
		}
		return this.hasAllConvertSettingsListener;
	}

	/**
	 * creates or gets the regex launcher listener.
	 * Calling the listener
	 * - launches the regex app    
	 */
	private ActionListener getRegexLauncher() {
		if (this.regexLauncher == null) {
			regexLauncher = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jButtonLaunchRegexTester.setEnabled(false);
					AppRegexDemo demo = new AppRegexDemo(); 
					regexFrame = demo.getFrame();
					//so we can handle enabling it when the frame is closed
					regexFrame.setLaunchPoint(jButtonLaunchRegexTester);
				}
			};
		}
		return this.regexLauncher;
	}

	/**
	 * creates or gets the launch feedback listener. When called it:
	 * - launches the feedback window.
	 */
	private LaunchFeedbackListener getLaunchFeedbackListener() {
		if (this.launchFeedbackListener == null) {
			this.launchFeedbackListener = new LaunchFeedbackListener(getFeedbackWindow());
		}
		return this.launchFeedbackListener;
	}

	/* Layout Controls */

	/**
	 * creates a set of basic layout constraints used by
	 * practically all the swing objects
	 */
	private GridBagConstraints getBaseConstraints() {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(BASE_INSET, BASE_INSET, BASE_INSET, BASE_INSET);
		return constraints;
	}

	/**
	 * creates a set of basic layout constraints used by 
	 * textfields
	 * Used in conjunction with GridBagLayout
	 * @param row 
	 * @return constraints for the given row
	 */
	private GridBagConstraints getBaseSettingsConstraints(int row) {
		GridBagConstraints constraints = getBaseConstraints();
		constraints.gridx = 1;
		constraints.gridy = row;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		return constraints;
	}
	
	/**
	 * creates a set of layout constraints used by the connection settings textfields
	 * using the given row
	 * @param row
	 * @return
	 */
	private GridBagConstraints getToConfluenceTextfieldContraints(int row) {
		GridBagConstraints constraints = getBaseSettingsConstraints(row);
		constraints.gridwidth = 3;
		constraints.insets = new Insets(BASE_INSET, BASE_INSET, BASE_INSET, BASIC_RIGHT_MARGIN);
		return constraints;
	}

	/**
	 * creates a set of layout constraints with the given row,
	 * for the Header Labels like 
	 * 'Convert From Wiki'. 
	 * @param row
	 */
	private GridBagConstraints getHeaderLabelConstraints(int row) {
		GridBagConstraints constraints = getBaseConstraints();
		constraints.gridx = 0;
		constraints.gridy = row;
		constraints.gridwidth = 4;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.PAGE_START;
		return constraints;
	}

	/**
	 * creates a set of layout constraints for the labels
	 * @param row
	 */
	private GridBagConstraints getBaseLabelConstraints(int row) {
		GridBagConstraints constraints = getBaseConstraints();
		constraints.gridx = 0;
		constraints.gridy = row;
		constraints.gridwidth = 1;
		constraints.anchor = GridBagConstraints.LINE_END; //right align
		constraints.insets = new Insets(BASE_INSET, LABEL_LEFT_MARGIN, BASE_INSET, LABEL_RIGHT_MARGIN);
		return constraints;
	}
	
	/**
	 * creates constraints for the ui on the advanced tab and
	 * on the given row
	 * @param row
	 * @return
	 */
	private GridBagConstraints getAdvancedButtonConstraints(int row) {
		GridBagConstraints constraints = getBaseConstraints();
		constraints.gridx = 0;
		constraints.gridy = row;
		constraints.anchor = GridBagConstraints.LINE_START; //left align
		int leftMargin = LABEL_LEFT_MARGIN - 10;
		int bottomMargin = BASE_INSET + 5;
		constraints.insets = new Insets(BASE_INSET, leftMargin, bottomMargin, LABEL_RIGHT_MARGIN);
		
		return constraints;
	}
	
	/**
	 * creates constraints for the labels on the advanced tab and on
	 * the given row
	 * @param row
	 * @return
	 */
	private GridBagConstraints getAdvancedLabelConstraints(int row) {
		GridBagConstraints constraints = getBaseConstraints();
		constraints.gridx = 1;
		constraints.gridy = row;
		constraints.anchor = GridBagConstraints.LINE_START; //left align
		return constraints;
	}
	
	/**
	 * creates a specialty constraint that should be used with the
	 * Attachment Size restriction label. The constraints will use the given row.
	 * @param row which row on the grid this label will be located
	 * @return
	 */
	private GridBagConstraints getAttachSizeLabelConstraints(int row) {
		GridBagConstraints constraints = getAdvancedLabelConstraints(row);
		
		//make the bottom of the label be just adjacent to the associated textfield
		constraints.insets.bottom = 0;
		
		return constraints;
	}
	
	/**
	 * creates a specialty constraint that should be used with the
	 * Attachment Size restriction textfield. The constraints will
	 * use the given row.
	 * @param row which row on the grid will be used
	 * @return
	 */
	private GridBagConstraints getAttachSizeTextFieldConstraints(int row) {
		GridBagConstraints constraints = getAdvancedLabelConstraints(row);
		// fix the width
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets.right = 160;  
		
		//make the top of the textfield be just adjacent to the associated label
		constraints.insets.top = 0;
		
		return constraints;
	}

	/**
	 * creates the basic constraints for the section headers
	 * that span the width of the app, with a default CENTER anchor
	 * @param column
	 * @param weight
	 * @return
	 */
	private GridBagConstraints getHeaderConstraintsByColumn(int column, double weight) {
		return getHeaderConstraintsByColumn(column, weight, GridBagConstraints.CENTER); 
	}

	/**
	 * creates the basic constraints for the section headers
	 * that span the width of the app
	 * @param column
	 * @param weight weightx for the component. 
	 * (handles how much space it gets as a column)
	 * @param anchor
	 * @return
	 */
	private GridBagConstraints getHeaderConstraintsByColumn(int column, double weight, int anchor) {
		GridBagConstraints constraints = getBaseConstraints();
		constraints.gridx = column;
		constraints.gridy = 0;
		constraints.anchor = anchor;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = weight;
		constraints.weighty = 0.0;
		constraints.insets = new Insets(0,0,0,0);
		return constraints;
	}

	/* Menu Getters */
	
	/**
	 * This method initializes converterMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getConverterMenu() {
		if (converterMenu == null) {
			converterMenu = new JMenu();
			converterMenu.setFont(UWCLabel.getUWCFont());
			converterMenu.setText("Convert");
			converterMenu.add(getConvertMenuItem());
			converterMenu.add(getExportMenuItem());
			addSeparator(converterMenu);
			converterMenu.add(getAddMenuItem());
			converterMenu.add(getRemoveMenuItem());
			addAccelerator(converterMenu, KeyEvent.VK_C);
		}
		return converterMenu;
	}

	/**
	 * This method initializes View	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getView() {
		if (viewMenu == null) {
			viewMenu = new JMenu();
			viewMenu.setFont(UWCLabel.getUWCFont());
			viewMenu.setText("View");
			viewMenu.add(getConversionSettingMenuItem());
			viewMenu.add(getOtherToolsTabMenuItem());
			addAccelerator(viewMenu, KeyEvent.VK_V);
		}
		return viewMenu;
	}

	/**
	 * This method initializes toolsMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getToolsMenu() {
		if (toolsMenu == null) {
			toolsMenu = new JMenu();
			toolsMenu.setFont(UWCLabel.getUWCFont());
			toolsMenu.setText("Tools");
			toolsMenu.add(getWhenConvertingMenu());
			addSeparator(toolsMenu);
			toolsMenu.add(getTestConnectionMenuItem());
			toolsMenu.add(getRegexTesterMenuItem());
			toolsMenu.add(getLaunchFeedbackMenuItem());
			addAccelerator(toolsMenu, KeyEvent.VK_T);
		}
		return toolsMenu;
	}

	/**
	 * This method initializes convertMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getConvertMenuItem() {
		if (convertMenuItem == null) {
			convertMenuItem = new JMenuItem();
			convertMenuItem.setFont(UWCLabel.getUWCFont());
			convertMenuItem.setText("Convert");
			addAccelerator(convertMenuItem, KeyEvent.VK_C, getShiftAccelerator());
			convertMenuItem.setPreferredSize(getIncreasedHeight(convertMenuItem));
			convertMenuItem.addActionListener(
					new ConvertListener(jComboBox_WikiType, model, getPropsDir(), getFeedbackWindow()));
			convertMenuItem.setEnabled(hasSetAllConverterSettings()); 
			
		}
		return convertMenuItem;
	}

	/**
	 * This method initializes exportMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getExportMenuItem() {
		if (exportMenuItem == null) {
			exportMenuItem = new JMenuItem();
			exportMenuItem.setFont(UWCLabel.getUWCFont());
			exportMenuItem.setText("Export");
			addAccelerator(exportMenuItem, KeyEvent.VK_E, getOsAccelerator());
			exportMenuItem.setPreferredSize(getIncreasedHeight(exportMenuItem));
			exportMenuItem.setEnabled(false); //only available when appropriate wiki is being used
			exportMenuItem.addActionListener(
					new ExportWikiListener(
							jComboBox_WikiType,
							this.model,
							getPropsDir(),
							getFeedbackWindow()));
		}
		return exportMenuItem;
	}

	/**
	 * This method initializes addMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAddMenuItem() {
		if (addMenuItem == null) {
			addMenuItem = new JMenuItem();
			addMenuItem.setFont(UWCLabel.getUWCFont());
			addMenuItem.setText("Add Pages");
			addAccelerator(addMenuItem, KeyEvent.VK_A, getOsAccelerator());
			addMenuItem.setPreferredSize(getIncreasedHeight(addMenuItem));
			addMenuItem.addActionListener(getAddPagesListener());
		}
		return addMenuItem;
	}

	/**
	 * This method initializes removeMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getRemoveMenuItem() {
		if (removeMenuItem == null) {
			removeMenuItem = new JMenuItem();
			removeMenuItem.setFont(UWCLabel.getUWCFont());
			removeMenuItem.setText("Remove Pages");
			addAccelerator(removeMenuItem, KeyEvent.VK_R, getOsAccelerator());
			removeMenuItem.setPreferredSize(getIncreasedHeight(removeMenuItem));
			removeMenuItem.addActionListener(getRemovePagesListener());
		}
		return removeMenuItem;
	}

	/**
	 * This method initializes conversionSettingMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getConversionSettingMenuItem() {
		if (conversionTabMenuItem == null) {
			conversionTabMenuItem = new JMenuItem();
			conversionTabMenuItem.setFont(UWCLabel.getUWCFont());
			conversionTabMenuItem.setText("Conversion Settings");
			addAccelerator(conversionTabMenuItem, KeyEvent.VK_1, getOsAccelerator());
			conversionTabMenuItem.setPreferredSize(getIncreasedHeight(removeMenuItem));
			conversionTabMenuItem.setPreferredSize(getIncreasedWidth(removeMenuItem, 40));
			conversionTabMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JTabbedPane tabs = getJTabbedPane();
					JPanel conTab = getJPanelBasic();
					tabs.setSelectedComponent(conTab);
				}
			});
		}
		return conversionTabMenuItem;
	}

	/**
	 * This method initializes otherToolsTabMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getOtherToolsTabMenuItem() {
		if (otherToolsTabMenuItem == null) {
			otherToolsTabMenuItem = new JMenuItem();
			otherToolsTabMenuItem.setFont(UWCLabel.getUWCFont());
			otherToolsTabMenuItem.setText("Other Tools");
			addAccelerator(otherToolsTabMenuItem, KeyEvent.VK_2, getOsAccelerator());
			otherToolsTabMenuItem.setPreferredSize(getIncreasedHeight(otherToolsTabMenuItem));
		}
		otherToolsTabMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTabbedPane tabs = getJTabbedPane();
				JPanel otherTab = getJPanelAdvanced();
				tabs.setSelectedComponent(otherTab);
			}
		});
		return otherToolsTabMenuItem;
	}

	/**
	 * This method initializes sendPagesMenuItem	
	 * 	
	 * @return javax.swing.JCheckBoxMenuItem	
	 */
	private JCheckBoxMenuItem getSendPagesCheckboxMenuItem() {
		if (sendPagesCheckboxMenuItem == null) {
			sendPagesCheckboxMenuItem = new JCheckBoxMenuItem();
			sendPagesCheckboxMenuItem.setFont(UWCLabel.getUWCFont());
			sendPagesCheckboxMenuItem.setText("Send Pages");
			sendPagesCheckboxMenuItem.setPreferredSize(getIncreasedHeight(sendPagesCheckboxMenuItem));

			loadSetting(sendPagesCheckboxMenuItem, Setting.SEND_TO_CONFLUENCE);
			
			sendPagesCheckboxMenuItem.addItemListener(getSendPagesCheckboxListener());

			JCheckBox otherUiElement = getJCheckBoxSendToConfluence();
			CheckboxObserver observer = new CheckboxObserver(
					this, otherUiElement, Setting.SEND_TO_CONFLUENCE);
			getSendPagesCheckboxListener().addObserver(observer);
			
		}
		return sendPagesCheckboxMenuItem;
	}

	/**
	 * This method initializes attachmentSizeCheckboxMenuItem
	 * @return javaz.swing.JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem getAttachmentSizeCheckBoxMenuItem() {
		if (attachmentSizeCheckboxMenuItem == null) {
			attachmentSizeCheckboxMenuItem = new JCheckBoxMenuItem();
			attachmentSizeCheckboxMenuItem.setFont(UWCLabel.getUWCFont());
			attachmentSizeCheckboxMenuItem.setText("Restrict Attachment Size");
			attachmentSizeCheckboxMenuItem.setPreferredSize(getIncreasedHeight(attachmentSizeCheckboxMenuItem));

			attachmentSizeCheckboxMenuItem.setSelected(getBooleanSettingFromString(Setting.ATTACHMENT_SIZE, UWCUserSettings.DEFAULT_ATTACHMENT_SIZE));
			
			attachmentSizeCheckboxMenuItem.addItemListener(getAttachmentSizeMenuItemListener()); //FILE_ATTACH_Z1

			JCheckBox otherUiElement = getJCheckBoxAttachmentSize(); //FILE_ATTACH_Z1
			CheckboxObserver observer = new CheckboxObserver(
					this, otherUiElement, Setting.ATTACHMENT_SIZE);
			getAttachmentSizeMenuItemListener().addObserver(observer); //FILE_ATTACH_Z1
			
			
		}
		return attachmentSizeCheckboxMenuItem;

	}
	
	/**
	 * loads selectable with the boolean value associated with the setting
	 * as it is currently represented in the model
	 * @param selectable
	 * @param setting
	 */
	public void loadSetting(AbstractButton selectable, Setting setting) {
		boolean settingBool = Boolean.parseBoolean(this.model.getSetting(setting));
		selectable.setSelected(settingBool);
	}
	
	/**
	 * loads the given ui element with the boolean based on the value for the given setting 
	 * and given defaultValue.
	 * @param element ui element to be loaded with a boolean value
	 * @param setting the key representing the current setting
	 * @param defaultValue default value for the setting 
	 */
	public void loadBooleanSettingFromString(AbstractButton element, Setting setting, String defaultValue) {
		boolean booleanSettingFromString = getBooleanSettingFromString(setting, defaultValue);
		element.setSelected(booleanSettingFromString);
	}
	
	/**
	 * @param setting key representing which setting is being examined
	 * @param defaultValue default value for this setting
	 * @return true if the model value for the given setting is not the default value
	 */
	public boolean getBooleanSettingFromString(Setting setting, String defaultValue ) {
		String intString = this.model.getSetting(setting);
		return (defaultValue.equals(intString))?false:true;
	}

	/**
	 * @return gets or creates the sendPagesCheckboxListener
	 */
	private ChangeCheckboxListener getSendPagesCheckboxListener() {
		if (this.sendPagesCheckboxListener == null)
			this.sendPagesCheckboxListener = new ChangeCheckboxListener(this.model, Setting.SEND_TO_CONFLUENCE);
		return this.sendPagesCheckboxListener;
	}

	/**
	 * @return gets or returns the attachmentSizeCheckboxListener
	 */
	private ChangeCheckboxListener getAttachmentSizeCheckboxListener() {
		if (this.attachmentSizeCheckboxListener == null)
			this.attachmentSizeCheckboxListener = new ChangeAttachmentCheckboxListener(this.model, Setting.ATTACHMENT_SIZE, getJTextFieldAttachmentSize());
		return this.attachmentSizeCheckboxListener;
	}

	/**
	 * @return gets or creates the attachmentSizeMenuItemListener
	 */
	private ChangeCheckboxListener getAttachmentSizeMenuItemListener() { 
		if (this.attachmentSizeMenuItemListener == null)
			this.attachmentSizeMenuItemListener = new ChangeAttachmentCheckboxListener(this.model, Setting.ATTACHMENT_SIZE, getJTextFieldAttachmentSize());
		return this.attachmentSizeMenuItemListener;
	}

	/**
	 * This method initializes launchFeedbackCheckBoxMenuItem	
	 * 	
	 * @return javax.swing.JCheckBoxMenuItem	
	 */
	private JCheckBoxMenuItem getLaunchFeedbackCheckBoxMenuItem() {
		if (launchFeedbackCheckboxMenuItem == null) {
			launchFeedbackCheckboxMenuItem = new JCheckBoxMenuItem();
			launchFeedbackCheckboxMenuItem.setFont(UWCLabel.getUWCFont());
			launchFeedbackCheckboxMenuItem.setText("Launch Feedback");
			launchFeedbackCheckboxMenuItem.setPreferredSize(getIncreasedHeight(launchFeedbackCheckboxMenuItem));
			loadSetting(launchFeedbackCheckboxMenuItem, Setting.FEEDBACK_OPTION);
			launchFeedbackCheckboxMenuItem.addItemListener(getLaunchFeedbackCheckboxListener());
			
			AbstractButton otherUiElement = getJCheckBoxFeedbackOption();
			CheckboxObserver observer = new CheckboxObserver(
					this, otherUiElement, Setting.FEEDBACK_OPTION);
			getLaunchFeedbackCheckboxListener().addObserver(observer);

		}
		return launchFeedbackCheckboxMenuItem;
	}

	/**
	 * @return gets or creates launchFeedbackCheckboxListener
	 */
	private ChangeCheckboxListener getLaunchFeedbackCheckboxListener() {
		if (this.launchFeedbackCheckboxListener == null)
			this.launchFeedbackCheckboxListener = new ChangeCheckboxListener(this.model, Setting.FEEDBACK_OPTION);
		return this.launchFeedbackCheckboxListener;
	}

	/**
	 * This method initializes testConnectionMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getTestConnectionMenuItem() {
		if (testConnectionMenuItem == null) {
			testConnectionMenuItem = new JMenuItem();
			testConnectionMenuItem.setFont(UWCLabel.getUWCFont());
			testConnectionMenuItem.setText("Test Connection");
			testConnectionMenuItem.setPreferredSize(getIncreasedHeight(testConnectionMenuItem));			
			testConnectionMenuItem.addActionListener(testSettingsListener);
			addAccelerator(testConnectionMenuItem, KeyEvent.VK_T, getOsAccelerator());
		}
		return testConnectionMenuItem;
	}

	/**
	 * This method initializes regexTesterMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getRegexTesterMenuItem() {
		if (regexTesterMenuItem == null) {
			regexTesterMenuItem = new JMenuItem();
			regexTesterMenuItem.setFont(UWCLabel.getUWCFont());
			regexTesterMenuItem.setText("Regex Tester");
			regexTesterMenuItem.setPreferredSize(getIncreasedHeight(regexTesterMenuItem));
			regexTesterMenuItem.addActionListener(getRegexLauncher());
		}
		return regexTesterMenuItem;
	}

	/**
	 * This method initializes launchFeedbackMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getLaunchFeedbackMenuItem() {
		if (launchFeedbackMenuItem == null) {
			launchFeedbackMenuItem = new JMenuItem();
			launchFeedbackMenuItem.setFont(UWCLabel.getUWCFont());
			launchFeedbackMenuItem.setText("Feedback Window");
			launchFeedbackMenuItem.setPreferredSize(getIncreasedHeight(launchFeedbackMenuItem));
			launchFeedbackMenuItem.setPreferredSize(getIncreasedWidth(launchFeedbackMenuItem, 40));
			launchFeedbackMenuItem.addActionListener(getLaunchFeedbackListener());
			addAccelerator(launchFeedbackMenuItem, KeyEvent.VK_F, getOsAccelerator());
		}
		return launchFeedbackMenuItem;
	}

	/**
	 * This method initializes whenConvertingMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getWhenConvertingMenu() {
		if (whenConvertingMenu == null) {
			whenConvertingMenu = new JMenu();
			whenConvertingMenu.setFont(UWCLabel.getUWCFont());
			whenConvertingMenu.setText("When Converting...");
			whenConvertingMenu.add(getSendPagesCheckboxMenuItem());
			whenConvertingMenu.add(getLaunchFeedbackCheckBoxMenuItem());
			whenConvertingMenu.add(getAttachmentSizeCheckBoxMenuItem());
			whenConvertingMenu.setPreferredSize(getIncreasedHeight(whenConvertingMenu));
		}
		return whenConvertingMenu;
	}

	/**
	 * This method initializes onlineDocMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getOnlineDocMenu() {
		if (onlineDocMenu == null) {
			onlineDocMenu = new JMenu();
			onlineDocMenu.setFont(UWCLabel.getUWCFont());
			onlineDocMenu.setText("Online Doc");
			onlineDocMenu.add(getUwcMainDoc());
			onlineDocMenu.add(getUwcGuiDoc());
			onlineDocMenu.add(getQuickDoc());
			onlineDocMenu.add(getSSLDoc());
			onlineDocMenu.setPreferredSize(getIncreasedHeight(onlineDocMenu));
			onlineDocMenu.setMnemonic(KeyEvent.VK_O);

			addSeparator(onlineDocMenu);

			//wiki specific doc pages
			Vector<String> wikitypes = model.getWikiTypesList(getPropsDir());
			for (String wikitype : wikitypes) { 
				//ignore known test files
				if (wikitype.equals("test") ||
						wikitype.equals("testHierarchy") ||
						wikitype.equals("NoSyntaxConversions") ||
						wikitype.equals("moinmoini"))
					continue;
				//add a menu item pointed at doc page for that wiki type
				JMenuItem item = getWikitypeDocMenuItem(wikitype);
				if (item != null) onlineDocMenu.add(item);
			}
		}
		return onlineDocMenu;
	}

	/**
	 * This method initializes uwcMainDoc	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getUwcMainDoc() {
		if (uwcMainDoc == null) {
			uwcMainDoc = new JMenuItem();
			uwcMainDoc.setFont(UWCLabel.getUWCFont());
			uwcMainDoc.setText("UWC Doc");
			uwcMainDoc.setPreferredSize(getIncreasedHeight(uwcMainDoc));
			uwcMainDoc.setMnemonic(KeyEvent.VK_U);
			String link = UWC_DOC_WEBSITE;
			uwcMainDoc.addActionListener(new UrlLauncher(link, this.feedbackWindow));
		}
		return uwcMainDoc;
	}

	/**
	 * This method initializes quickDoc	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getQuickDoc() {
		if (quickDoc == null) {
			quickDoc = new JMenuItem();
			quickDoc.setFont(UWCLabel.getUWCFont());
			quickDoc.setText("Quick Start Guide");
			quickDoc.setPreferredSize(getIncreasedHeight(quickDoc));
			quickDoc.setMnemonic(KeyEvent.VK_Q);
			String link = UWC_DOC_URL + "UWC+Quick+Start";
			quickDoc.addActionListener(new UrlLauncher(link, this.feedbackWindow));
		}
		return quickDoc;
	}
	
	/**
	 * This method initializes sslDoc	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSSLDoc() {
		if (sslDoc == null) {
			sslDoc = new JMenuItem();
			sslDoc.setFont(UWCLabel.getUWCFont());
			sslDoc.setText("SSL Support");
			sslDoc.setPreferredSize(getIncreasedHeight(sslDoc));
			String link = UWC_DOC_URL + "UWC+SSL+Support";
			sslDoc.addActionListener(new UrlLauncher(link, this.feedbackWindow));
		}
		return sslDoc;
	}
	
	/**
	 * @return creates or gets guiDoc
	 */
	private JMenuItem getUwcGuiDoc() {
		if (this.guiDoc == null) {
			guiDoc = new JMenuItem();
			guiDoc.setFont(UWCLabel.getUWCFont());
			guiDoc.setText("New UI Doc");
			guiDoc.setPreferredSize(getIncreasedHeight(guiDoc));
			guiDoc.setMnemonic(KeyEvent.VK_N);
			String link = UWC_DOC_URL + "UWC+GUI+v3+Documentation";
			guiDoc.addActionListener(new UrlLauncher(link, this.feedbackWindow));
		}
		return guiDoc;
	}
	
	/**
	 * creates or gets a menu item for a particular wikitype's doc
	 * @param wikitype
	 * @return menu item linked to the wiki specific doc
	 * for the given wikitype
	 */
	private JMenuItem getWikitypeDocMenuItem(String wikitype) {
		getWikiHelpMenuItems(); //if object is undefined, will define it.
		if (this.wikiHelpMenuItems.containsKey(wikitype)) //return the existing one
			return this.wikiHelpMenuItems.get(wikitype);
		JMenuItem item = createJMenuItem(wikitype); //or if doesn't exist, create it
		if (item == null) return null;
		this.wikiHelpMenuItems.put(wikitype, item);
		return item;
	}
	
	/**
	 * @return gets or creates wikiHelpMenuItems
	 */
	private HashMap<String,JMenuItem> getWikiHelpMenuItems() {
		if (this.wikiHelpMenuItems == null)
			this.wikiHelpMenuItems = new HashMap<String,JMenuItem>();
		return this.wikiHelpMenuItems;
	}

	/**
	 * @param wikitype
	 * @return a new menu item corresponding to the given wikitype, 
	 * and linked to that wikitype's doc site
	 */
	private JMenuItem createJMenuItem(String wikitype) {
		JMenuItem item = new JMenuItem();
		item.setFont(UWCLabel.getUWCFont());
		item.setText(getFirstCharUpperCase(wikitype) + " Doc"); 
		item.setPreferredSize(getIncreasedHeight(item));
		int mnemonic = getAvailableMnemonic(wikitype);
		if (mnemonic > -1) //only set mnemonic if an appropriate one was found
			item.setMnemonic(mnemonic);
		String link = getWikiDocLink(wikitype);
		if (link == null) return null;
		item.addActionListener(new UrlLauncher(link, this.feedbackWindow));
		return item;
	}

	/**
	 * transforms the given input to have the first letter be upper case
	 * @param input
	 * @return transformed string
	 */
	protected String getFirstCharUpperCase(String input) {
		String first = input.substring(0,1);
		String last = input.substring(1);
		first = first.toUpperCase();
		return first + last;
	}

	/**
	 * chooses an appropriate character based on the given input
	 * @param input
	 * @return an acceptable int value for a character from the given input
	 * that can be used as a mnemonic. character must be from given word, and
	 * must not be already in use with this set. if cannot find one, returns -1;
	 */
	protected int getAvailableMnemonic(String input) {
		Vector<Integer> available = this.availableWikiHelpDocMnemonics;
		if (available == null) { //create it if it doesn't exist
			available = new Vector<Integer>();
			//add already existing Mnemonics
			available.add((int)"U".toCharArray()[0]); //uwc doc
			available.add((int)"Q".toCharArray()[0]); //quick start
		}
		input = input.toUpperCase(); //keyevents are associated with upper case chars
		for (int i = 0; i < input.length(); i++) { 	//foreach character in the word
			char c = input.charAt(i);
			int charAsInt = (int) c ;					
			if (available.contains(charAsInt)) {		//char already in use. choose another one.
				continue;
			}
			available.add(charAsInt);					//found a char. add it to the vector.
			this.availableWikiHelpDocMnemonics = available;
			return charAsInt;
		}
		return -1;										//give up. can't find suitable unused char.
	}

	Pattern nodocwiki = Pattern.compile("(test)|(-)");
	/**
	 * builds the url for the given wikitype's doc
	 * @param wikitype
	 * @return url
	 */
	protected String getWikiDocLink(String wikitype) { 
		//hide wikitypes that have the word "test" or a "-"
		Matcher nodocFinder = nodocwiki.matcher(wikitype);
		if (nodocFinder.find()) return null;
		//create link
		String link = 
				UWC_DOC_URL +
				"UWC" +
				"+" +
				wikitype +
				"+" +
				"Notes";
		return link;
	}
	
	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getFileMenu());
			jJMenuBar.add(getConverterMenu());
			jJMenuBar.add(getView());
			jJMenuBar.add(getToolsMenu());
			jJMenuBar.add(getHelpMenu());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setFont(UWCLabel.getUWCFont());
			fileMenu.setText("File");
			fileMenu.add(getSaveMenuItem());
			fileMenu.add(getExitMenuItem());
			addAccelerator(fileMenu, KeyEvent.VK_F);
		}
		return fileMenu;
	}

	/**
	 * @return text for the close button/menu item.
	 * Should reflect user's OS
	 */
	private String getCloseText() {
		boolean isMac = isMac();
		if (isMac)
			return "Quit";
		return "Close";
	}
	
	/**
	 * @return keyboard accel key for the close menu item.
	 * Should reflect user's OS (X for Win, Q for Mac, etc.)
	 */
	private int getCloseKey() {
		if (isMac())
			return KeyEvent.VK_Q;
		return KeyEvent.VK_X;
	}

	/**
	 * @return true if the system env variable os.name 
	 * indicates the system is a mac
	 */
	private boolean isMac() {
		String os = System.getProperty("os.name");
		String osRegex = 	"(?i)" +	//case insensitive
							"mac"; 		//the string: "mac"
		return Pattern.compile(osRegex).matcher(os).find(); 
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu();
			helpMenu.setFont(UWCLabel.getUWCFont());
			helpMenu.setText("Help");
			helpMenu.add(getOnlineDocMenu());
			helpMenu.add(getAboutMenuItem());
			addAccelerator(helpMenu, KeyEvent.VK_H);
		}
		return helpMenu;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			exitMenuItem.setFont(UWCLabel.getUWCFont());
			exitMenuItem.setText(getCloseText());
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					shutdownUWC();
				}

			});
			addAccelerator(exitMenuItem, getCloseKey(), getOsAccelerator()); 
			exitMenuItem.setPreferredSize(getIncreasedHeight(exitMenuItem));	
		}
		return exitMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAboutMenuItem() {
		if (aboutMenuItem == null) {
			aboutMenuItem = new JMenuItem();
			aboutMenuItem.setFont(UWCLabel.getUWCFont());
			aboutMenuItem.setText("About");
			aboutMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFrame aboutDialog = getAboutDialog();
					aboutDialog.setVisible(true);
				}
			});
			aboutMenuItem.setPreferredSize(getIncreasedHeight(aboutMenuItem));
			aboutMenuItem.setMnemonic(KeyEvent.VK_A);
		}
		return aboutMenuItem;
	}

	/**
	 * This method initializes aboutDialog	
	 * 	
	 * @return javax.swing.JDialog
	 */
	protected JFrame getAboutDialog() {
		if (aboutDialog == null) {
			aboutDialog = new SupportWindow("About " + APP_NAME);
		}
		return aboutDialog;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem();
			saveMenuItem.setFont(UWCLabel.getUWCFont());
			saveMenuItem.setText("Save");
			addAccelerator(saveMenuItem, KeyEvent.VK_S, getOsAccelerator());
			saveMenuItem.setPreferredSize(getIncreasedHeight(saveMenuItem));
			
			saveMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					save(); 
				}
			});
		}
		return saveMenuItem;
	}

	/**
	 * calculates an increased height for the given item
	 * @param item
	 * @return Dimension representing an increased height for the given item
	 */
	private Dimension getIncreasedHeight(JComponent item) {
		return getIncreasedHeight(item, 6);
	}

	/**
	 * calculates an increased height for the given item, with the given increase amount
	 * @param item
	 * @param increase
	 * @return Dimension representing an increased height for the given item
	 */
	private Dimension getIncreasedHeight(JComponent item, int increase) {
		Dimension size = item.getPreferredSize();
		size.height += increase;
		return size;
	}
	
	/**
	 * calculates an increased width for the given item with the given increase amount
	 * @param item
	 * @param increase
	 * @return Dimension representing an increased width for the given item
	 */
	private Dimension getIncreasedWidth(JComponent item, int increase) {
		Dimension size = item.getPreferredSize();
		size.width += increase;
		return size;
	}

	/**
	 * @return the correct accelerator mask for the system.
	 * For example: Command for Macs, Control for Windows, etc.
	 */
	public static int getOsAccelerator() {
		return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	}
	
	/**
	 * @return Shift Mask + correct accelerator for the system.
	 * For example: Shift-Command for Macs
	 */
	private int getShiftAccelerator() {
		int osAccel = getOsAccelerator();
		return InputEvent.SHIFT_MASK | osAccel;
	}
	
	/**
	 * sets the given item's accelerator with the given key, and the given accelerator key
	 * @param item menu item which is getting the accelerator set
	 * @param key KeyEvent constant representing a key, Example: KeyEvent.VK_A
	 * @param accelerator KeyEvent constant representing a combo key. Example: InputEvent.CTRL_MASK
	 */
	public static void addAccelerator(JMenuItem item, int key, int accelerator) {
		item.setAccelerator(KeyStroke.getKeyStroke(key, accelerator, true));
	}
	
	/**
	 * sets the given menu's mnemonic with the given key
	 * @param menu menu item which is getting the mnemonic set
	 * @param key KeyEvent constant representing a key, Example: KeyEvent.VK_S
	 */
	private void addAccelerator(JMenu menu, int key) {
		menu.setMnemonic(key);
	}
	
	/**
	 * adds a separator to the given menu.
	 * @param menu
	 */
	public void addSeparator(JMenu menu) {
		JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
		separator.setForeground(new Color(200, 200, 200)); //224
		separator.setPreferredSize(getIncreasedHeight(separator, -1));
		menu.add(separator);
	}
	
	/**
	 * This method initializes jCheckBoxAttachmentSize	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBoxAttachmentSize() {
		if (jCheckBoxAttachmentSize == null) {
			jCheckBoxAttachmentSize = new JCheckBox();
			jCheckBoxAttachmentSize.setFont(UWCLabel.getUWCFont());
			
			jCheckBoxAttachmentSize.setText(""); //No label - we'll add one somewhere else
			jCheckBoxAttachmentSize.setSelected(getBooleanSettingFromString(Setting.ATTACHMENT_SIZE, UWCUserSettings.DEFAULT_ATTACHMENT_SIZE));
			
			jCheckBoxAttachmentSize.addItemListener(getAttachmentSizeCheckboxListener());
			
			Observer enableTextFieldObserver = getEnableTextFieldObserver(getJTextFieldAttachmentSize());
			getAttachmentSizeCheckboxListener().addObserver(enableTextFieldObserver);
			
			AbstractButton otherUiElement = getAttachmentSizeCheckBoxMenuItem();
			CheckboxObserver observer = new CheckboxObserver(
					this, otherUiElement, Setting.ATTACHMENT_SIZE);
			getAttachmentSizeCheckboxListener().addObserver(observer);

		}
		return jCheckBoxAttachmentSize;
	}

	/**
	 * @param textfield
	 * @return new EnableTextFieldObserver instantiated with the given textfield
	 */
	private Observer getEnableTextFieldObserver(JTextField textfield) {
		EnableTextFieldObserver observer = new EnableTextFieldObserver(textfield);
		return observer;
	}

	/**
	 * This method initializes jTextFieldAttachmentSize	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextFieldAttachmentSize() {
		if (jTextFieldAttachmentSize == null) {
			jTextFieldAttachmentSize = new JTextField();
			jTextFieldAttachmentSize.setFont(UWCLabel.getUWCFont());
			SaveListener saveListener = new SaveListener(
					this.jTextFieldAttachmentSize, 
					this.model, 
					Setting.ATTACHMENT_SIZE,
					this.feedbackWindow
			);
			jTextFieldAttachmentSize.addActionListener(saveListener);
			jTextFieldAttachmentSize.addFocusListener(saveListener);
			jTextFieldAttachmentSize.addKeyListener(new IgnoreAltListener(jTextFieldAttachmentSize));
			
			jTextFieldAttachmentSize.setText(this.model.getSetting(Setting.ATTACHMENT_SIZE));
			jTextFieldAttachmentSize.setEnabled(getBooleanSettingFromString(Setting.ATTACHMENT_SIZE, UWCUserSettings.DEFAULT_ATTACHMENT_SIZE));
		}
		return jTextFieldAttachmentSize;
	}

	/* General Methods */
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) setPropsDir(args[0]); //useful for testing
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UWCForm3 application = new UWCForm3();
				application.getJFrame().setVisible(true);
			}
		});
	}

	/**
	 * sets the properties directory with the given value.
	 * All proeprties loading will use this value unless it's null
	 * @param value directory containing properties
	 */
	private static void setPropsDir(String value) {
		commandLineArgDir = value;
	}

	/**
	 * any necessary init code that isn't handled by getJFrame
	 */
	private void init() {
		//init the logging
		PropertyConfigurator.configure("log4j.properties");
		//init the model
		model = new UWCGuiModel();
		//setup shutdown hooks, important for OS-initiated shutdowns
		Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownController(this)));
	}

	/**
	 * This method initializes jFrame
	 * 
	 * @return javax.swing.JFrame
	 */
	protected JFrame getJFrame() {
		if (jFrame == null) {
			init();
			jFrame = new JFrame();
			jFrame.setSize(450, 640);
			jFrame.setLocation(20, 0); //not quite flush with the left edge of the screen
			jFrame.setContentPane(getJContentPane());
			jFrame.setTitle(getAppTitle());
			// can't use default close op, 'cause we need to save before closing
			// jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			// so instead we do this:
			jFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					shutdownUWC();
				}
			});
			jFrame.setJMenuBar(getJJMenuBar()); //this has to be last, as necessary objects are defined earlier
		}
		return jFrame;
	}

	/**
	 * @return UWC's official name as displayed on the UI, includes version number
	 */
	private String getAppTitle() {
		String title = APP_NAME;
		title += " " + VERSION_INDICATOR + VERSION_NUMBER;
		return title;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getJTabbedPane(), BorderLayout.NORTH); 
			jContentPane.add(getJPanelMain(), BorderLayout.EAST);
		}
		return jContentPane;
	}

	/* Convenience Methods */

	/**
	 * convenience method for setting a colored border for
	 * a particular component. Useful for layout development.
	 * @param obj
	 * @param color
	 */
	private void setBorder(JComponent obj, Color color) {
		obj.setBorder(new LineBorder(color));		
	}
	
	/**
	 * @return Map of conversion setting components.
	 * Keys are:
	 * attachmentsText
	 * attachmentsButton
	 * pages
	 * add
	 * remove
	 * addMenu
	 * removeMenu
	 * address
	 * login
	 * password
	 * space
	 */
	public HashMap<String, Component> getConversionSettingsComponents() {
		HashMap<String, Component> components = new HashMap<String, Component>();
		components.put("attachmentsText", getJTextFieldAttachments());
		components.put("attachmentsButton", getJButtonAttachments());
		components.put("pages", getJScrollPanePages());
		components.put("add", getJButtonAdd());
		components.put("remove", getJButtonRemovePages());
		components.put("addMenu", getAddMenuItem());
		components.put("removeMenu", getRemoveMenuItem());
		components.put("address", getJTextfieldAddress());
		components.put("login", getJTextFieldLogin());
		components.put("password", getJPasswordField());
		components.put("space", getJTextFieldSpace());
		return components;
	}
	
	/* Inner Classes */
	
	/**
	 * handles any tasks that need to be done before shutdown
	 */
	private class ShutDownController implements Runnable {
		
		private UWCForm3 form;
		public ShutDownController(UWCForm3 form) {
			this.form = form;
		}
		public void run() {
			this.form.preShutdownCleanup();
		}
		
	}
}

