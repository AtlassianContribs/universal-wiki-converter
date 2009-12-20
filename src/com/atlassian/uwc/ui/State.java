package com.atlassian.uwc.ui;

import java.util.Observable;

import org.apache.log4j.Logger;

/**
 * represents the model used by the feedback window to change the feedback.
 */
public class State extends Observable {

	public enum Type {
		NOTE,
		STEP,
		MAX
	}
	
	/**
	 * the text displayed by the feedback
	 */
	private String note;
	/**
	 * the current position of the progress bar
	 */
	private int step;
	/**
	 * the number of steps that represents the progress bar at 100%
	 */
	private int maxSteps;
	Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * instantiates the state with no note, and the progress bar
	 * at 0 out of 100 steps.
	 */
	public State() {
		this("", 0, 100);
	}
	
	/**
	 * instantiates the state with the given note and the progress bar
	 * at the given step out of 100 steps.
	 * @param note
	 * @param step
	 */
	public State(String note, int step) {
		this(note, step, 100);
	}
	
	/**
	 * instantiates the state with the given note and the progress bar
	 * at the given step out of the given max number of steps
	 * @param note
	 * @param step
	 * @param maxSteps
	 */
	public State(String note, int step, int maxSteps) {
		this.step = step;
		this.maxSteps = maxSteps;
		updateNote(note);
	}
	
	/**
	 * sets the progress monitor's note, and notifies observers of the change
	 * @param note 
	 */
	public void updateNote(String note) {
		this.note = note;
		setChanged();
		notifyObservers(Type.NOTE);
	}
	
	/**
	 * increates the current position of the progress bar by 1 step,
	 * and notifies observers of the change
	 */
	public void updateProgress() {
		updateProgress(1);
	}

	/**
	 * increates the current position of the progress bar by the
	 * given amount of steps,
	 * and notifies observers of the change
	 * @param amount number of steps to increase the progress bar's position
	 */
	public void updateProgress(int amount) {
		step += amount;
		setChanged();
		notifyObservers(Type.STEP);
	}

	/**
	 * increases the max number of steps the progress bar will use
	 * by the given max, and notifies observers of the change
	 * @param max
	 */
	public void updateMax(int max) {
		this.maxSteps = max;
		setChanged();
		notifyObservers(Type.MAX);
	}
	
	/**
	 * @return the current position of the progress bar as a percentage 
	 */
	public double getPercentage() {
		int num = getStep();
		int denom = getMax();
		double val = (num*100)/denom;
		return val;
	}
	
	/**
	 * @return the current note
	 */
	public String getNote() {
		return this.note;
	}
	
	/**
	 * @return the current position of the progress bar
	 */
	public int getStep() {
		return this.step;
	}

	/**
	 * @return the current max number of steps used by the progress bar
	 */
	public int getMax() {
		return this.maxSteps;
	}

}
