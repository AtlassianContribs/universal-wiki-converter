package com.atlassian.uwc.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.atlassian.uwc.ui.FeedbackWindow;
import com.atlassian.uwc.util.BareBonesBrowserLaunch;

/**
 * handles launching a web browser to show a URL when an event triggers this
 */
public class UrlLauncher implements ActionListener {

	/**
	 * url to be shown by the browser after its launched
	 */
	public String url;
	private FeedbackWindow feedbackWindow;

	public UrlLauncher(String url) {
		this.url = url;
	}
	
	public UrlLauncher(String url, FeedbackWindow feedbackWindow) {
		this.url = url;
		this.feedbackWindow = feedbackWindow;
	}
	
	/**
	 * launches the associated url in the user's default browser
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		try {
			BareBonesBrowserLaunch.openURL(this.url);
		} catch (RuntimeException e) { //in case something weird happens.
			String errorMessage = "\n" +
					"Error: Problem launching browser:\n" +
					e.getMessage();
			if (this.feedbackWindow != null) {
				this.feedbackWindow.launch();
				this.feedbackWindow.updateFeedback(errorMessage);
				this.feedbackWindow.end();
			}
		}
	}

}
