package regexdemo;

import javax.swing.UIManager;

import org.apache.log4j.Logger;

import sun.tools.tree.ThisExpression;

import java.awt.*;

/**
 * <p>Regular Expressions Demo</p>
 * <p>Demonstration showing how to use the java.util.regex package that is part of JDK 1.4 and later</p>
 * <p>Copyright (c) 2003 Jan Goyvaerts.  All rights reserved.</p>
 * <p>Visit http://www.regular-expressions.info for a detailed tutorial to regular expressions.</p>
 * @author Jan Goyvaerts
 * @version 1.0
 */

public class AppRegexDemo {
	boolean packFrame = false;

	FrameRegexDemo frame;

	private Logger log = Logger.getLogger(this.getClass());
	//Construct the application
	public AppRegexDemo() {
		getFrame();
	}
	public FrameRegexDemo getFrame() {
		log.debug("getting frame and making it visible");
		if (frame != null) return frame;
		frame = new FrameRegexDemo();
		//Validate frames that have preset sizes
		//Pack frames that have useful preferred size info, e.g. from their layout
		if (packFrame) {
			frame.pack();
		}
		else {
			frame.validate();
		}
		//Center the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		frame.setVisible(true);
		return frame;
	}
	//Main method
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		new AppRegexDemo();
	}
}