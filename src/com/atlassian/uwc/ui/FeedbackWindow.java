package com.atlassian.uwc.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.State.Type;
import com.atlassian.uwc.ui.listeners.FeedbackCanceller;
import com.atlassian.uwc.ui.listeners.FeedbackHandler;

/**
 * window that displays feedback to the user
 */
public class FeedbackWindow extends SupportWindow implements FeedbackHandler, Observer{

	
	private JTextArea jTextFeedbackDisplay = null;
	private JPanel jPanel;
	private JScrollPane jScrollPane = null;
	private JButton jButtonClose = null;
	private State state;  //  @jve:decl-index=0:
	private JProgressBar jProgressBar = null;
	private JButton cancel = null;
	private FeedbackCanceller currentAction = null;  //  @jve:decl-index=0:

	public FeedbackWindow() {
		super("Feedback");
		init();
	}

	/**
	 * initializes the feedback window
	 */
	private void init() {
		jPanel = null; 						//override the SupportWindow's usage
		this.setContentPane(getJPanel());	//add all the UI and layout
		this.add(getJJMenuBar()); 			//add a (hidden) menubar which will instantiate the Esc keyboard
											//command to close the window
	}

	/**
	 * handles feedback window close 
	 * @see javax.swing.JFrame#processWindowEvent(java.awt.event.WindowEvent)
	 */
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			this.setVisible(false); 
		}
	}
	
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints constraints_progressbar = new GridBagConstraints();
			constraints_progressbar.fill = GridBagConstraints.BOTH;
			constraints_progressbar.gridx = 0;
			constraints_progressbar.gridy = 0;
			constraints_progressbar.gridwidth = 2;
			constraints_progressbar.ipady = 10;
			
			GridBagConstraints constraints_scrollpane = new GridBagConstraints();
			constraints_scrollpane.fill = GridBagConstraints.BOTH;
			constraints_scrollpane.gridx = 0;
			constraints_scrollpane.gridy = 1;
			constraints_scrollpane.gridwidth = 2;
			
			GridBagConstraints constraints_close = new GridBagConstraints();
			constraints_close.gridx = 1;
			constraints_close.gridy = 2;
			int topSeperator = 12;
			int pushToRight = 0;//with only this one button, use 220;
			constraints_close.insets = new Insets(topSeperator, pushToRight, 0, 0);
			
			GridBagConstraints constraints_cancel = new GridBagConstraints();
			constraints_cancel.gridx = 0;
			constraints_cancel.gridy = 2;
			pushToRight = 150;
			constraints_cancel.insets = new Insets(topSeperator, pushToRight, 0, 0);
			
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getJProgressBar(), constraints_progressbar);
			jPanel.add(getJScrollPane(), constraints_scrollpane);
			jPanel.add(getCancel(), constraints_cancel);
			jPanel.add(getJButtonClose(), constraints_close);
			
		}
		return jPanel;
	}

	/**
	 * This method initializes jTextFeedbackDisplay	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getJTextFeedbackDisplay() {
		if (jTextFeedbackDisplay == null) {
			jTextFeedbackDisplay = new JTextArea();
			jTextFeedbackDisplay.setFont(UWCLabel.getUWCFont(12));
			jTextFeedbackDisplay.setSize(new Dimension(320, 200));
			jTextFeedbackDisplay.setEditable(false);
			jTextFeedbackDisplay.setBackground(getFeedbackBackground());
			jTextFeedbackDisplay.setBorder(getFeedbackBorder());
			jTextFeedbackDisplay.setLineWrap(true);
			jTextFeedbackDisplay.setWrapStyleWord(true);
			end();
		}
		return jTextFeedbackDisplay;
	}

	/**
	 * @return color that the feedback panel's background should be
	 */
	private Color getFeedbackBackground() {
		return new Color(252, 252, 252);
	}

	/**
	 * @return border for the feedback panel
	 */
	private Border getFeedbackBorder() {
		return BorderFactory.createLoweredBevelBorder();
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTextFeedbackDisplay());
			jScrollPane.setBorder(getFeedbackBorder());
			jScrollPane.setPreferredSize(new Dimension(300, 190));
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jButtonClose	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButtonClose() {
		if (jButtonClose == null) {
			jButtonClose = new JButton();
			jButtonClose.setFont(UWCLabel.getUWCFont());
			jButtonClose.setToolTipText("or type Escape to Close");
			jButtonClose.setText("Close");
			jButtonClose.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					close();
				}
			});
		}
		return jButtonClose;
	}

	/**
	 * This method initializes jProgressBar	
	 * 	
	 * @return javax.swing.JProgressBar	
	 */
	private JProgressBar getJProgressBar() {
		if (jProgressBar == null) {
			jProgressBar = new JProgressBar();
			jProgressBar.setStringPainted(true);//show percentage left
		}
		return jProgressBar;
	}

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	protected JMenuBar getJJMenuBar() {
		return super.getJJMenuBar();
	}

	/**
	 * This method initializes Cancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancel() {
		if (cancel == null) {
			cancel = new JButton();
			cancel.setFont(UWCLabel.getUWCFont());
			cancel.setText("Cancel");
			cancel.setEnabled(false);
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancel();
				}
			});
		}
		return cancel;
	}

	/**
	 * convenience launching point
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				FeedbackWindow window = new FeedbackWindow();
				window.setVisible(true);
			}
		});
	}
	
	/**
	 * @param text updates the feedback display using the given text
	 * @return the string that was used in the feedback display
	 */
	public String updateFeedback(String text) {
		String feedback =
			this.jTextFeedbackDisplay.getText() +
			text;
		this.jTextFeedbackDisplay.setText(feedback);
		return feedback;
	}
	
	/**
	 * clears the feedback display
	 */
	public void clear() {
		this.jTextFeedbackDisplay.setText("");
		clearProgressBar();
	}
	
	/**
	 * resets the progress bar
	 */
	public void clearProgressBar() {
		this.jProgressBar.setValue(0);
	}

	/**
	 * launches the feedback window
	 */
	public void launch() {
		this.setVisible(true);
	}

	/**
	 * Used to determine which (if any) object threw a ClassCastException in the update method
	 */
	public enum CastProblem {
		NOT_STATE,
		NOT_TYPE
	};
	/**
	 * updates the feedback text area and progress bar using
	 * the given parameters
	 * @param stateObs should be an instance of com.atlassian.uwc.ui.State
	 * @param methodObj should be a State.Type object
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable stateObs, Object methodObj) {
		State state;
		Type method;
		CastProblem problem = CastProblem.NOT_STATE;
		try {
			state = (State) stateObs;
			problem = CastProblem.NOT_TYPE; //state cast didn't fail; let's try methodObj. Used by ClassCastException catch block 
			method = (State.Type) methodObj;
		} catch (ClassCastException e) {
			String problemStr = (problem == CastProblem.NOT_STATE)
					?
						"observable object is not a State object.\n" +  //not a State object
						"Observable = " + stateObs.getClass().toString()
					:
						"passed object is not a State.Type object.\n" +	//not a Type object
						"It's a " + methodObj.getClass().toString()
					;

			log.error("Problem updating feedback window: " + problemStr);
			return;
		}
		updateProgressBar(state, method);
		updateFeedbackTextArea(state, method);
	}

	/**
	 * updates the feedback textarea using the given state and method
	 * @param state provides data about the update
	 * @param method indicates what type of update is occurring, only NOTE updates will 
	 * affect the textarea
	 */
	private void updateFeedbackTextArea(State state, Type method) {
		if (method == State.Type.NOTE) { 
			String text = state.getNote();
			text = jTextFeedbackDisplay.getText() + "\n" + text;
			this.jTextFeedbackDisplay.setText(text );
			this.jScrollPane.setViewportView(getJTextFeedbackDisplay());
			this.jTextFeedbackDisplay.setCaretPosition(text.length());
		}
	}

	/**
	 * updates the progress bar using the given state and method
	 * @param state provides data about the update
	 * @param method indicate what type of update is occurring.
	 * Only step and max updates will affect the progress bar
	 */
	private synchronized void updateProgressBar(State state, Type method) {
		if (method == State.Type.STEP) {
			int val = getStep(state);
			jProgressBar.setValue(val);
		}
		else if (method == State.Type.MAX) {
			int step = state.getStep();
			int max = state.getMax();
			clearProgressBar();
			jProgressBar.setMaximum(max);
			jProgressBar.setValue(step);
		}
	}

	/**
	 * gets the number of steps from the given state
	 * @param state
	 * @return the number of steps
	 */
	private int getStep(State state) {
		return state.getStep();
	}

	/**
	 * connects this window and the given state in the following ways:
	 * * sets the maximum used by the progress bar
	 * * makes this window observe changes in the given state
	 * @param state
	 */
	public void setState(State state) {
		this.state = state;
		this.jProgressBar.setMaximum(state.getMax());
		state.addObserver(this);
	}

	/**
	 * moves the caret of the textarea to the end of the textarea.
	 * (So most recent information is displayed with no user intervention 
	 * necessary)
	 */
	public void end() {
		int end = this.jTextFeedbackDisplay.getText().length();
		this.jTextFeedbackDisplay.setCaretPosition(end);
	}
	
	/**
	 * cancels the action being reported on by this window,
	 * and makes any necessary changes to the ui to reflect that
	 */
	public void cancel() {
		if (this.currentAction != null) {
			this.currentAction.cancel();
			this.currentAction = null;
		}
		cancelOff();
	}

	/**
	 * setter
	 * @param current
	 */
	public void setCurrent(FeedbackCanceller current) {
		this.currentAction = current;
	}
	
	/**
	 * enables the cancel button
	 */
	public void cancelOn() {
		getCancel().setEnabled(true);
	}
	
	/**
	 * disables the cancel button
	 */
	public void cancelOff() {
		getCancel().setEnabled(false);
	}
}
