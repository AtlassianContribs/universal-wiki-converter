package com.atlassian.uwc.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.listeners.UrlLauncher;

/**
 * Basic extra window which provides a uniform look and feel.
 * See FeedbackWindow.init to see how to use. 
 *
 */
public class SupportWindow extends JFrame {
	private JPanel jPanel = null;
	protected JMenuBar menubar = null;
	private JMenu File = null; 
	private JMenuItem close = null;
	Logger log;
	private AboutComponents aboutComponents;
	
	public SupportWindow(String title) {
		log = Logger.getLogger(this.getClass());
		init(title);
	}

	/**
	 * sets up the support window:
	 * - size
	 * - title with given title parameter
	 * - inits and lays out the ui elements
	 * - inits menu
	 * @param title
	 */
	private void init(String title) {
		this.setPreferredSize(new Dimension(350,300));
		this.setTitle(title);
		this.setContentPane(getJPanel());
		this.add(getJJMenuBar()); 
		this.pack();
	}

	/**
	 * @return default JPanel. subclasses should override this.
	 */
	private JPanel getJPanel() {
		if (this.jPanel == null) {
			if ((this.getTitle()).startsWith(("About")))
				this.jPanel = getAboutPanel();
			else 
				this.jPanel = new JPanel();
		}
		return this.jPanel;
	}

	/**
	 * @return creates the ui elements for the window
	 */
	private JPanel getAboutPanel() {
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());
		int row = 0;
		GridBagConstraints constraints_icon = getAboutConstraints(0, row, 2, 1, 10, GridBagConstraints.BOTH);
		row++;
		GridBagConstraints constraints_versionLabel = getAboutConstraints(0, row, 1, 1, 10, GridBagConstraints.BOTH);
		GridBagConstraints constraints_versionInfo = getAboutConstraints(1, row, 1, 1, 10, GridBagConstraints.BOTH);
		row++;
		GridBagConstraints constraints_authorsLabel = getAboutConstraints(0, row, 1, 1, 10, GridBagConstraints.BOTH);
		GridBagConstraints constraints_authorsInfo = getAboutConstraints(1, row, 1, 1, 10, GridBagConstraints.BOTH);
		row++;
		//		GridBagConstraints constraints_licenseLabel = getAboutConstraints(0, row, 1, 1, 10, GridBagConstraints.BOTH);
		//		GridBagConstraints constraints_licenseInfo = getAboutConstraints(1, row, 1, 1, 10, GridBagConstraints.BOTH);
		//		row++;
		GridBagConstraints constraints_websiteLabel = getAboutConstraints(0, row, 1, 1, 10, GridBagConstraints.BOTH);
		GridBagConstraints constraints_websiteInfo = getAboutConstraints(1, row, 1, 1, 0, GridBagConstraints.NONE, GridBagConstraints.LINE_START);
		row++;
		GridBagConstraints constraints_close = getAboutConstraints(1, row, 1, 1, 0, GridBagConstraints.NONE, GridBagConstraints.LINE_END);

		jPanel.add(getAboutIcon(), constraints_icon);
		jPanel.add(getAboutVersionLabel(), constraints_versionLabel);
		jPanel.add(getAboutVersionInfo(), constraints_versionInfo);
		jPanel.add(getAboutAuthorsLabel(), constraints_authorsLabel);
		jPanel.add(getAboutAuthorsInfo(), constraints_authorsInfo);
		//		jPanel.add(getAboutLicenseLabel(), constraints_licenseLabel);
		//		jPanel.add(getAboutLicenseInfo(), constraints_licenseInfo);
		jPanel.add(getAboutWebsiteLabel(), constraints_websiteLabel);
		jPanel.add(getAboutWebsiteInfo(), constraints_websiteInfo);
		jPanel.add(getAboutClose(), constraints_close);
		return jPanel;
	}
	
	/**
	 * gets basic layout constraints for elements in this window
	 * @param x column position
	 * @param y row position
	 * @param width number of grid columns the element should use 
	 * @param height number of grid rows the element should use
	 * @param padding internal padding for both width and height of element
	 * @param fill how the element fills the grid cells it's been assigned. Choices include:
	 * NONE, HORIZONTAL, VERTICAL, BOTH
	 * @return a GridBagConstraints object that uses the given parameters
	 */
	private GridBagConstraints getAboutConstraints(int x, int y, int width, int height, int padding, int fill) {
		return getAboutConstraints(x, y, width, height, padding, fill, GridBagConstraints.CENTER);
	}
	
	/**
	 * gets basic layout constraints for elements in this window
	 * @param x column position
	 * @param y row position
	 * @param width number of grid columns the element should use 
	 * @param height number of grid rows the element should use
	 * @param padding internal padding for both width and height of element
	 * @param fill how the element fills the grid cells it's been assigned. Choices include:
	 * NONE, HORIZONTAL, VERTICAL, BOTH
	 * @param anchor places the element within the grid cells it's been assigned. Choices include:
	 * CENTER (the default), PAGE_START, PAGE_END, LINE_START, LINE_END, 
	 * FIRST_LINE_START, FIRST_LINE_END, LAST_LINE_END, and LAST_LINE_START.
	 * @return a GridBagConstraints object that uses the given parameters
	 */
	private GridBagConstraints getAboutConstraints(int x, int y, int width, int height, int padding, int fill, int anchor) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		constraints.ipadx = padding;
		constraints.ipady = padding;
		constraints.fill = fill;
		constraints.anchor = anchor; 
		return constraints;
	}

	/**
	 * @return creates or gets the icon used by the About Window
	 */
	private Component getAboutIcon() {
		if (this.aboutComponents == null) {
			this.aboutComponents = new AboutComponents();
		}
		return this.aboutComponents.icon;
	}

	/**
	 * @return creates or gets the version label
	 */
	private Component getAboutVersionLabel() {
		if (this.aboutComponents == null) {
			this.aboutComponents = new AboutComponents();
		}
		return this.aboutComponents.versionLabel;
	}
	
	/**
	 * @return creates or gets the version data
	 */
	private Component getAboutVersionInfo() {
		if (this.aboutComponents == null) {
			this.aboutComponents = new AboutComponents();
		}
		return this.aboutComponents.versionInfo;
	}
	
	/**
	 * @return creates or gets the authors label
	 */
	private Component getAboutAuthorsLabel() {
		if (this.aboutComponents == null) {
			this.aboutComponents = new AboutComponents();
		}
		return this.aboutComponents.authorsLabel;
	}
	
	/**
	 * @return creates or gets the authors data
	 */
	private Component getAboutAuthorsInfo() {
		if (this.aboutComponents == null) {
			this.aboutComponents = new AboutComponents();
		}
		return this.aboutComponents.authorsInfo;
	}
	
	/**
	 * @return creates or gets the license label
	 */
	private Component getAboutLicenseLabel() {
		if (this.aboutComponents == null) {
			this.aboutComponents = new AboutComponents();
		}
		return this.aboutComponents.licenseLabel;
	}
	
	/**
	 * @return creates or gets the license data
	 */
	private Component getAboutLicenseInfo() {
		if (this.aboutComponents == null) {
			this.aboutComponents = new AboutComponents();
		}
		return this.aboutComponents.licenseInfo;
	}
	
	/**
	 * @return creates or gets the website label
	 */
	private Component getAboutWebsiteLabel() {
		if (this.aboutComponents == null) {
			this.aboutComponents = new AboutComponents();
		}
		return this.aboutComponents.websiteLabel;
	}
	
	/**
	 * @return creates or gets the website data
	 */
	private Component getAboutWebsiteInfo() {
		if (this.aboutComponents == null) {
			this.aboutComponents = new AboutComponents();
		}
		return this.aboutComponents.websiteInfo;
	}
	
	/**
	 * @return creates or gets the close button
	 */
	private Component getAboutClose() {
		if (this.aboutComponents == null) {
			this.aboutComponents = new AboutComponents();
		}
		return this.aboutComponents.close;
	}
	
	/**
	 * The needed ui elements
	 */
	public class AboutComponents {
		public final JLabel icon;
		public final JLabel versionLabel;
		public final JLabel versionInfo;
		public final JLabel authorsLabel;
		public final JLabel authorsInfo;
		public final JLabel licenseLabel;
		public final JLabel licenseInfo;
		public final JLabel websiteLabel;
		public final JButton websiteInfo;
		public final JButton close;
		public AboutComponents() {
			icon = getIcon();
			versionLabel = getVersionLabel();
			versionInfo = getVersionInfo();
			authorsLabel = getAuthorsLabel();
			authorsInfo = getAuthorsInfo();
			licenseLabel = getLicenseLabel();
			licenseInfo = getLicenseInfo();
			websiteLabel = getWebsiteLabel();
			websiteInfo = getWebsiteInfo();
			close = getClose();
		}
		private JLabel getIcon() {
			JLabel label = new JLabel();
			String separator = System.getProperty("file.separator");
			String dir = System.getProperty("user.dir") + separator + "images";
			String file = "uwc.gif";
			Icon picture = new ImageIcon(dir + separator + file);
			label.setIcon(picture);
			return label;
		}
		private JLabel getVersionLabel() {
			UWCLabel label = new UWCLabel();
			label.setText("Version:");
			return label;
		}
		private JLabel getVersionInfo() {
			UWCLabel label = new UWCLabel();
			label.setText(UWCForm3.VERSION_NUMBER);
			return label;
		}
		private JLabel getAuthorsLabel() {
			UWCLabel label = new UWCLabel();
			label.setText("Authors:");
			return label;
		}
		private JLabel getAuthorsInfo() {
			UWCLabel label = new UWCLabel();
			label.setText("Brendan Patterson & Laura Kolker");
			return label;
		}
		private JLabel getLicenseLabel() {
			UWCLabel label = new UWCLabel();
			label.setText("License:");
			return label;
		}
		private JLabel getLicenseInfo() {
			UWCLabel label = new UWCLabel();
			label.setText(""); //FIXME?
			return label;
		}
		private JLabel getWebsiteLabel() {
			UWCLabel label = new UWCLabel();
			label.setText("Website:");
			return label;
		}
		private JButton getWebsiteInfo() {
			JButton button = new JButton();
			button.setText(UWCForm3.APP_NAME);
			button.setFont(UWCLabel.getUWCFont());
			button.addActionListener(new UrlLauncher(UWCForm3.UWC_DOC_WEBSITE));
			return button;
		}
		private JButton getClose() {
			JButton button = new JButton();
			button.setFont(UWCLabel.getUWCFont());
			button.setText("Close");
			button.setToolTipText("or type Escape to Close");
			button.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					close();
				}

			});
			return button;
		}
	}
	
	/* Close Functionality and Keyboard Commands */
	
	/**
	 * hides the window
	 */
	protected void close() {
		this.setVisible(false);
	}

	/**
	 * hides the given menubar by shrinking its size to nothing.
	 * XXX
	 * HACK ALERT:
	 * useful for providing accelerators without a visible menu
	 * @param menubar
	 */
	protected void hideMenu(JMenuBar menubar) {
		menubar.setPreferredSize(new Dimension(0,0));
	}
	
	/**
	 * @return creates or gets the menu
	 */
	protected JMenuBar getJJMenuBar() {
		if (menubar == null) {
			menubar = new JMenuBar();
			menubar.add(getFile()); 
			hideMenu(menubar);
		}
		return menubar;
	}

	/**
	 * @return creates or get the File menu
	 */
	protected JMenu getFile() {
		if (File == null) {
			File = new JMenu();
			File.add(getClose());
		}
		return File;
	}
	
	/**
	 * @return creates or gets the close button
	 */
	private JMenuItem getClose() {
		if (close == null) {
			close = new JMenuItem();
			UWCForm3.addAccelerator(close, KeyEvent.VK_ESCAPE, 0);
			close.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					close();
				}
			});
		}
		return close;
	}

}
