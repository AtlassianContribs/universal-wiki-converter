package com.atlassian.uwc.ui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.atlassian.uwc.ui.FeedbackWindow;

/**
 * launches the feedback window when triggered
 */
public class LaunchFeedbackListener implements ActionListener {

	private FeedbackWindow feedbackWindow;

	public LaunchFeedbackListener(FeedbackWindow feedbackWindow) {
		this.feedbackWindow = feedbackWindow;
	}

	/**
	 * launches the feedback window. 
	 * Note: If the feedback window is null, a new one is created.
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (feedbackWindow == null)
			feedbackWindow = new FeedbackWindow();
		feedbackWindow.launch();
	}

}
