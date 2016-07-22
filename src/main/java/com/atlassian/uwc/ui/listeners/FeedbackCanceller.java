package com.atlassian.uwc.ui.listeners;

/**
 * interface for handling cancellation
 */
public interface FeedbackCanceller {

	/**
	 * gets called when an action needs to be cancelled
	 */
	public void cancel();
}
