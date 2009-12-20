package com.atlassian.uwc.ui;

import java.awt.Font;

import javax.swing.JLabel;

/**
 * extended label used by the UWC which sets all the default settings for that label:
 * font, fontsize, etc. 
 */
public class UWCLabel extends JLabel {

	static final int ATLASSIAN_FONTSIZE = 11;
	static final String ATLASSIAN_FONT = "Arial sans serif";

	public UWCLabel() {
		super();
		this.setFont(UWCLabel.getUWCFont());
	}
	
	/**
	 * @return the standardized font for the UWC
	 */
	public static Font getUWCFont() {
		return new Font(ATLASSIAN_FONT, Font.PLAIN, ATLASSIAN_FONTSIZE);
	}

	/**
	 * @param size
	 * @return the standardized font for the UWC, but using the given size, in points
	 */
	public static Font getUWCFont(int size) {
		return new Font(ATLASSIAN_FONT, Font.PLAIN, size);
	}
}
