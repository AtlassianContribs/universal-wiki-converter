package regexdemo;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

/**
 * <p>Regular Expressions Demo</p>
 * <p>Demonstration showing how to use the java.util.regex package that is part of JDK 1.4 and later</p>
 * <p>Copyright (c) 2003 Jan Goyvaerts.  All rights reserved.</p>
 * <p>Visit <A HREF="http://www.regular-expressions.info">http://www.regular-expressions.info</A>
 *    for a detailed tutorial to regular expressions.</p>
 * <p>This source code is provided for educational purposes only, without any warranty of any kind.
 *    Distribution of this source code and/or the application compiled from this source code is prohibited.
 *    Please refer everybody interested in getting a copy of the source code to
 *    <A HREF="http://www.regular-expressions.info">http://www.regular-expressions.info</A>.</p>
 * @author Jan Goyvaerts
 * @version 1.0
 */

public class FrameRegexDemo extends JFrame {
	JPanel contentPane;
	BorderLayout borderLayout1 = new BorderLayout();
	JSplitPane jSplitPane1 = new JSplitPane();
	JSplitPane jSplitPane2 = new JSplitPane();
	JPanel jPanel1 = new JPanel();
	BorderLayout borderLayout2 = new BorderLayout();
	JPanel jPanel2 = new JPanel();
	JLabel jLabel1 = new JLabel();
	BorderLayout borderLayout3 = new BorderLayout();
	JLabel jLabel2 = new JLabel();
	JTextArea textRegex = new JTextArea();
	JPanel jPanel3 = new JPanel();
	GridLayout gridLayout1 = new GridLayout();
	JCheckBox checkDotAll = new JCheckBox();
	JCheckBox checkCanonEquivalence = new JCheckBox();
	JCheckBox checkMultiLine = new JCheckBox();
	JCheckBox checkCaseInsensitive = new JCheckBox();
	JPanel jPanel4 = new JPanel();
	BorderLayout borderLayout4 = new BorderLayout();
	JPanel jPanel5 = new JPanel();
	JPanel jPanel6 = new JPanel();
	GridLayout gridLayout2 = new GridLayout();
	JButton btnMatch = new JButton();
	JButton btnSplit = new JButton();
	JButton btnObjects = new JButton();
	JButton btnNextMatch = new JButton();
	JButton btnObjReplace = new JButton();
	JButton btnObjSplit = new JButton();
	JButton btnReplace = new JButton();
	JLabel jLabel3 = new JLabel();
	BorderLayout borderLayout5 = new BorderLayout();
	JLabel jLabel4 = new JLabel();
	JTextArea textSubject = new JTextArea();
	JPanel jPanel8 = new JPanel();
	GridLayout gridLayout3 = new GridLayout();
	BorderLayout borderLayout6 = new BorderLayout();
	JPanel jPanel7 = new JPanel();
	JLabel jLabel5 = new JLabel();
	JTextArea textReplace = new JTextArea();
	JPanel jPanel9 = new JPanel();
	GridLayout gridLayout4 = new GridLayout();
	JPanel jPanel10 = new JPanel();
	JPanel jPanel11 = new JPanel();
	BorderLayout borderLayout7 = new BorderLayout();
	BorderLayout borderLayout8 = new BorderLayout();
	JLabel jLabel6 = new JLabel();
	JTextArea textResults = new JTextArea();
	JLabel jLabel7 = new JLabel();
	JTextArea textReplaceResults = new JTextArea();
	private Logger log = Logger.getLogger(this.getClass());
	private Component launchPoint = null;
	
	//Construct the frame
	public FrameRegexDemo() {
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			jbInit();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setLaunchPoint(Component launchPoint) {
		this.launchPoint = launchPoint;
	}
	
	//Component initialization
	private void jbInit() throws Exception  {
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(borderLayout1);
		this.setFont(new java.awt.Font("Dialog", 0, 12));
		this.setSize(new Dimension(629, 523));
		this.setTitle("Regular Expressions Demo");
		jPanel1.setAlignmentY((float) 0.5);
		jPanel1.setLayout(borderLayout2);
		jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setBottomComponent(jSplitPane2);
		jSplitPane1.setLeftComponent(jPanel1);
		jSplitPane1.setRightComponent(null);
		jPanel2.setLayout(borderLayout3);
		jLabel1.setAlignmentX((float) 0.0);
		jLabel1.setMinimumSize(new Dimension(97, 15));
		jLabel1.setLabelFor(textRegex);
		jLabel1.setText("Regular Expression:");
		jLabel2.setText("Visit http://www.regular-expressions.info for a complete regex tutorial");
		textRegex.setFont(new java.awt.Font("Monospaced", 0, 12));
		textRegex.setBorder(BorderFactory.createLoweredBevelBorder());
		textRegex.setText("t[a-z]+");
		textRegex.setLineWrap(true);
		jPanel3.setLayout(gridLayout1);
		gridLayout1.setColumns(2);
		gridLayout1.setHgap(2);
		gridLayout1.setRows(2);
		gridLayout1.setVgap(2);
		checkDotAll.setText("Dot matches newlines");
		checkCanonEquivalence.setText("Ignore differences in Unicode encoding");
		checkMultiLine.setText("^ and $ match at embedded newlines");
		checkCaseInsensitive.setText("Case insensitive");
		contentPane.setPreferredSize(new Dimension(438, 142));
		jPanel4.setLayout(borderLayout4);
		jPanel6.setAlignmentX((float) 0.5);
		jPanel6.setLayout(borderLayout5);
		jPanel5.setLayout(gridLayout2);
		gridLayout2.setColumns(5);
		gridLayout2.setHgap(2);
		gridLayout2.setRows(2);
		gridLayout2.setVgap(2);
		btnMatch.setText("Match Test");
		btnMatch.addActionListener(new FrameRegexDemo_btnMatch_actionAdapter(this));
		btnSplit.setText("Split");
		btnSplit.addActionListener(new FrameRegexDemo_btnSplit_actionAdapter(this));
		btnObjects.setAlignmentY((float) 0.5);
		btnObjects.setActionCommand("Create Objects");
		btnObjects.setText("Create Objects");
		btnObjects.addActionListener(new FrameRegexDemo_btnObjects_actionAdapter(this));
		btnNextMatch.setText("Next Match");
		btnNextMatch.addActionListener(new FrameRegexDemo_btnNextMatch_actionAdapter(this));
		btnObjReplace.setSelected(false);
		btnObjReplace.setText("Obj Replace");
		btnObjReplace.addActionListener(new FrameRegexDemo_btnObjReplace_actionAdapter(this));
		btnObjSplit.setText("Obj Split");
		btnObjSplit.addActionListener(new FrameRegexDemo_btnObjSplit_actionAdapter(this));
		btnReplace.setText("Replace");
		btnReplace.addActionListener(new FrameRegexDemo_btnReplace_actionAdapter(this));
		jLabel3.setPreferredSize(new Dimension(0, 0));
		jLabel3.setRequestFocusEnabled(true);
		jLabel3.setText("");
		jLabel4.setLabelFor(textSubject);
		jLabel4.setText("Test Subject:");
		textSubject.setBorder(BorderFactory.createLoweredBevelBorder());
		textSubject.setToolTipText("");
		textSubject.setText("This is the default test subject for our regex test.");
		textSubject.setLineWrap(true);
		textSubject.setWrapStyleWord(true);
		jPanel8.setLayout(gridLayout3);
		gridLayout3.setColumns(2);
		gridLayout3.setHgap(4);
		jPanel7.setLayout(borderLayout6);
		jLabel5.setMaximumSize(new Dimension(89, 15));
		jLabel5.setLabelFor(textReplace);
		jLabel5.setText("Replacement Text:");
		textReplace.setBorder(BorderFactory.createLoweredBevelBorder());
		textReplace.setToolTipText("");
		textReplace.setText("replacement");
		textReplace.setLineWrap(true);
		textReplace.setWrapStyleWord(true);
		borderLayout4.setVgap(4);
		borderLayout2.setVgap(4);
		borderLayout1.setHgap(0);
		borderLayout1.setVgap(0);
		jPanel9.setLayout(gridLayout4);
		gridLayout4.setColumns(2);
		gridLayout4.setHgap(4);
		jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jPanel10.setLayout(borderLayout7);
		jPanel11.setLayout(borderLayout8);
		jLabel6.setToolTipText("");
		jLabel6.setLabelFor(textResults);
		jLabel6.setText("Results:");
		textResults.setBorder(BorderFactory.createLoweredBevelBorder());
		textResults.setText("");
		textResults.setLineWrap(true);
		textResults.setWrapStyleWord(true);
		jLabel7.setLabelFor(textReplaceResults);
		jLabel7.setText("Replacement Results:");
		textReplaceResults.setBorder(BorderFactory.createLoweredBevelBorder());
		textReplaceResults.setText("");
		textReplaceResults.setLineWrap(true);
		textReplaceResults.setWrapStyleWord(true);
		jLabel8.setRequestFocusEnabled(true);
		jLabel8.setText("");
		btnAdvancedReplace.setText("Advanced Replace");
		btnAdvancedReplace.addActionListener(new FrameRegexDemo_btnAdvancedReplace_actionAdapter(this));
		contentPane.add(jSplitPane1,  BorderLayout.CENTER);
		jSplitPane1.add(jSplitPane2, JSplitPane.RIGHT);
		jSplitPane1.add(jPanel1, JSplitPane.LEFT);
		jPanel1.add(jPanel2, BorderLayout.NORTH);
		jPanel2.add(jLabel1, BorderLayout.CENTER);
		jPanel2.add(jLabel2,  BorderLayout.EAST);
		jPanel1.add(textRegex, BorderLayout.CENTER);
		jPanel1.add(jPanel3,  BorderLayout.SOUTH);
		jPanel3.add(checkDotAll, null);
		jPanel3.add(checkCaseInsensitive, null);
		jPanel3.add(checkMultiLine, null);
		jPanel3.add(checkCanonEquivalence, null);
		jSplitPane2.add(jPanel4, JSplitPane.LEFT);
		jPanel4.add(jPanel5,  BorderLayout.SOUTH);
		jPanel5.add(btnMatch, null);
		jPanel5.add(jLabel3, null);
		jPanel5.add(jLabel8, null);
		jPanel5.add(btnReplace, null);
		jPanel5.add(btnSplit, null);
		jPanel5.add(btnObjects, null);
		jPanel5.add(btnNextMatch, null);
		jPanel5.add(btnObjReplace, null);
		jPanel5.add(btnAdvancedReplace, null);
		jPanel5.add(btnObjSplit, null);
		jPanel6.add(jLabel4, BorderLayout.NORTH);
		jPanel6.add(textSubject,  BorderLayout.CENTER);
		jPanel4.add(jPanel8, BorderLayout.CENTER);
		jPanel8.add(jPanel6, null);
		jSplitPane1.setDividerLocation(150);
		jSplitPane2.setDividerLocation(200);
		jPanel8.add(jPanel7, null);
		jPanel7.add(jLabel5, BorderLayout.NORTH);
		jPanel7.add(textReplace, BorderLayout.CENTER);
		jSplitPane2.add(jPanel9, JSplitPane.RIGHT);
		jPanel9.add(jPanel10, null);
		jPanel10.add(jLabel6, BorderLayout.NORTH);
		jPanel10.add(textResults,  BorderLayout.CENTER);
		jPanel9.add(jPanel11, null);
		jPanel11.add(jLabel7, BorderLayout.NORTH);
		jPanel11.add(textReplaceResults,  BorderLayout.CENTER);
	}
	//Overridden so we can exit when window is closed
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
//			System.exit(0);
			log .debug("Closing");
			this.setVisible(false);
			this.launchPoint.setEnabled(true);
		}
	}

	/** The easiest way to check if a particular string matches a regular expression
	 *  is to simply call String.matches() passing the regular expression to it.
	 *  It is not possible to set matching options this way, so the checkboxes
	 *  in this demo are ignored when clicking btnMatch.<p>
	 *
	 *  One disadvantage of this method is that it will only return true if the regex
	 *  matches the *entire* string.  In other words, an implicit caret is prepended to
	 *  the regex and an implicit dollar sign is appended to it.
	 *  So you cannot use matches() to test if a substring anywhere in the string
	 *  matches the regex.<p>
	 *
	 *  Note that when typing in a regular expression into textSubject,
	 *  backslashes are interpreted at the regex level.
	 *  So typing in \( will match a literal ( character and \\ matches a literal backslash.
	 *  When passing literal strings in your source code, you need to escape backslashes in strings as usual.
	 *  So the string "\\(" matches a literal ( and "\\\\" matches a single literal backslash.
	 */
	void btnMatch_actionPerformed(ActionEvent e) {
		textReplaceResults.setText("n/a");
		// Calling the Pattern.matches static method is an alternative way to achieve the same
		// if (Pattern.matches(textRegex.getText(), textSubject.getText())) {
		try {
			if (textSubject.getText().matches(textRegex.getText())) {
				textResults.setText("The regex matches the entire subject");
			}
			else {
				textResults.setText("The regex does not match the entire subject");
			}
		} catch (PatternSyntaxException ex) {
			textResults.setText("You have an error in your regular expression:\n" +
					ex.getDescription());
		}
	}

	/** The easiest way to perform a regular expression search-and-replace on a string
	 *  is to call the string's replaceFirst() and replaceAll() methods.
	 *  replaceAll() will replace all substrings that match the regular expression
	 *  with the replacement string, while replaceFirst() will only replace the first match.<p>
	 *
	 *  Again, you cannot set matching options this way, so the checkboxes
	 *  in this demo are ignored when clicking btnMatch.<p>
	 *
	 *  In the replacement text, you can use $0 to insert the entire regex match,
	 *  and $1, $2, $3, etc. for the backreferences (text matched by the part in the regex
	 *  between the first, second, third, etc. pair of round brackets)<br>
	 *  \$ inserts a single $ character.<p>
	 *
	 *  $$ or other improper use of the $ sign will raise an IllegalArgumentException.
	 *  If you reference a group that does not exist (e.g. $4 if there are only 3 groups),
	 *  will raise an IndexOutOfBoundsException.  Be sure to properly handle these exceptions
	 *  if you allow the end user to type in the replacement text.<p>
	 *
	 *  Note that in the memo control, you type \$ to insert a dollar sign,
	 *  and \\ to insert a backslash.  If you provide the replacement string as a
	 *  string literal in your Java code, you need to use "\\$" and "\\\\" respectively.
	 *  This is because backslashes need to be escaped in Java string literals as well.
	 */
	void btnReplace_actionPerformed(ActionEvent e) {
		try {
			textReplaceResults.setText(
					textSubject.getText().replaceAll(textRegex.getText(), textReplace.getText())
			);
			textResults.setText("n/a");
		} catch (PatternSyntaxException ex) {
			// textRegex does not contain a valid regular expression
			textResults.setText("You have an error in your regular expression:\n" +
					ex.getDescription());
			textReplaceResults.setText("n/a");
		} catch (IllegalArgumentException ex) {
			// textReplace contains inapropriate dollar signs
			textResults.setText("You have an error in the replacement text:\n" +
					ex.getMessage());
			textReplaceResults.setText("n/a");
		} catch (IndexOutOfBoundsException ex) {
			// textReplace contains a backreference that does not exist
			// (e.g. $4 if there are only three groups)
			textResults.setText("You have used a non-existent group in the replacement text:\n" +
					ex.getMessage());
			textReplaceResults.setText("n/a");
		}
	}

	/** Show the results of splitting a string. */
	void printSplitArray(String[] array) {
		textResults.setText(null);
		for (int i = 0; i < array.length; i++) {
			textResults.append(Integer.toString(i) + ": \"" + array[i] + "\"\r\n");
		}
	}

	/** The easiest way to split a string into an array of strings is by calling
	 *  the string's split() method.  The string will be split at each substring
	 *  that matches the regular expression.  The regex matches themselves are
	 *  thrown away.<p>
	 *
	 *  If the split would result in trailing empty strings, i.e. when the regex matches
	 *  at the end of the string, the trailing empty strings are also thrown away.
	 *  If you want to keep the empty strings, call split(regex, -1).  The -1 tells
	 *  the split() method to add trailing empty strings to the resulting array.<p>
	 *
	 *  You can limit the number of items in the resulting array by specifying a
	 *  positive number as the second parameter to split().  The limit you specify
	 *  it the number of items the array will at most contain.  So the regex is applied
	 *  at most limit-1 times, and the last item in the array contains the unsplit
	 *  remainder of the original string.  If you are only interested in the first
	 *  3 items in the array, specify a limit of 4 and disregard the last item.
	 *  This is more efficient than having the string split completely.
	 */
	void btnSplit_actionPerformed(ActionEvent e) {
		textReplaceResults.setText("n/a");
		try {
			printSplitArray(textSubject.getText().split(textRegex.getText() /*, Limit*/ ));
		} catch (PatternSyntaxException ex) {
			// textRegex does not contain a valid regular expression
			textResults.setText("You have an error in your regular expression:\n" +
					ex.getDescription());
		}
	}

	/** Figure out the regex options to be passed to the Pattern.compile()
	 *  class factory based on the state of the checkboxes.
	 */
	int getRegexOptions() {
		int Options = 0;
		if (checkCanonEquivalence.isSelected()) {
			// In Unicode, certain characters can be encoded in more than one way.
			// Many letters with diacritics can be encoded as a single character
			// identifying the letter with the diacritic, and encoded as two
			// characters: the letter by itself followed by the diacritic by itself
			// Though the internal representation is different, when the string is
			// rendered to the screen, the result is exactly the same.
			Options |= Pattern.CANON_EQ;
		}
		if (checkCaseInsensitive.isSelected()) {
			// Omitting UNICODE_CASE causes only US ASCII characters to be matched case insensitively
			// This is appropriate if you know beforehand that the subject string will only contain
			// US ASCII characters as it speeds up the pattern matching.
			Options |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		}
		if (checkDotAll.isSelected()) {
			// By default, the dot will not match line break characters.
			// Specify this option to make the dot match all characters, including line breaks
			Options |= Pattern.DOTALL;
		}
		if (checkMultiLine.isSelected()) {
			// By default, the caret ^, dollar $ as well as \A, \z and \Z
			// only match at the start and the end of the string
			// Specify this option to make ^ and \A also match after line breaks in the string,
			// and make $, \z and \Z match before line breaks in the string
			Options |= Pattern.MULTILINE;
		}
		return Options;
	}

	/** Pattern constructed by btnObject */
	Pattern compiledRegex;

	/** Matcher object that will search the subject string using compiledRegex */
	Matcher regexMatcher;
	JLabel jLabel8 = new JLabel();
	JButton btnAdvancedReplace = new JButton();

	/** If you will be using a particular regular expression often,
	 *  you should create a Pattern object to store the regular expression.
	 *  You can then reuse the regex as often as you want by reusing the
	 *  Pattern object.<p>
	 *
	 *  To use the regular expression on a string, create a Matcher object
	 *  by calling compiledRegex.matcher() passing the subject string to it.
	 *  The Matcher will do the actual searching, replacing or splitting.<p>
	 *
	 *  You can create as many Matcher objects from a single Pattern object
	 *  as you want, and use the Matchers at the same time.  To apply the regex
	 *  to another subject string, either create a new Matcher using
	 *  compiledRegex.matcher() or tell the existing Matcher to work on a new
	 *  string by calling regexMatcher.reset(subjectString).
	 */
	void btnObjects_actionPerformed(ActionEvent e) {
		compiledRegex = null;
		textReplaceResults.setText("n/a");
		try {
			// If you do not want to specify any options (this is the case when
			// all checkboxes in this demo are unchecked), you can omit the
			// second parameter for the Pattern.compile() class factory.
			compiledRegex = Pattern.compile(textRegex.getText(), getRegexOptions());
			// Create the object that will search the subject string
			// using the regular expression.
			regexMatcher = compiledRegex.matcher(textSubject.getText());
			textResults.setText("Pattern and Matcher objects created.");
		} catch (PatternSyntaxException ex) {
			// textRegex does not contain a valid regular expression
			textResults.setText("You have an error in your regular expression:\n" +
					ex.getDescription());
		} catch (IllegalArgumentException ex) {
			// This exception indicates a bug in getRegexOptions
			textResults.setText("Undefined bit values are set in the regex options");
		}
	}

	/** Print the results of a search produced by regexMatcher.find()
	 *  and stored in regexMatcher.
	 */
	void printMatch() {
		try {
			textResults.setText("Index of the first character in the match: " +
					Integer.toString(regexMatcher.start()) + "\n");
			textResults.append("Index of the first character after the match: " +
					Integer.toString(regexMatcher.end()) + "\n");
			textResults.append("Length of the match: " +
					Integer.toString(regexMatcher.end() - regexMatcher.start()) + "\n");
			textResults.append("Matched text: " + regexMatcher.group() + "\n");
			if (regexMatcher.groupCount() > 0) {
				// Capturing parenthesis are numbered 1..groupCount()
				// group number zero is the entire regex match
				for (int i = 1; i <= regexMatcher.groupCount(); i++) {
					String groupLabel = new String("Group " + Integer.toString(i));
					if (regexMatcher.start(i) < 0) {
						textResults.append(groupLabel + " did not participate in the overall match\n");
					} else {
						textResults.append(groupLabel + " start: " +
								Integer.toString(regexMatcher.start(i)) + "\n");
						textResults.append(groupLabel + " end: " +
								Integer.toString(regexMatcher.end(i)) + "\n");
						textResults.append(groupLabel + " length: " +
								Integer.toString(regexMatcher.end(i) - regexMatcher.start(i)) + "\n");
						textResults.append(groupLabel + " matched text: " + regexMatcher.group(i) + "\n");
					}
				}
			}
		} catch (IllegalStateException ex) {
			// Querying the results of a Matcher object before calling find()
			// or after a call to find() returned False, throws an IllegalStateException
			// This indicates a bug in our application
			textResults.setText("Cannot print match results if there aren't any");
		} catch (IndexOutOfBoundsException ex) {
			// Querying the results of groups (capturing parenthesis or backreferences)
			// that do not exist throws an IndexOutOfBoundsException
			// This indicates a bug in our application
			textResults.setText("Cannot print match results of non-existent groups");
		}
	}

	/** Finds the first match if this is the first search, or if the previous search came up empty.
	 *  Otherwise, it finds the next match after the previous match.<p>
	 *
	 *  Note that even if you typed in new text for the regex or subject,
	 *  btnNextMatch uses the subject and regex as they were when you clicked btnCreateObjects.
	 */
	void btnNextMatch_actionPerformed(ActionEvent e) {
		textReplaceResults.setText("n/a");
		if (regexMatcher == null) {
			textResults.setText("Please click Create Objects to create the Matcher object");
		} else {
			// Caling Matcher.find() without any parameters continues the search at Matcher.end()
			// or starts from the beginning of the string if this is the first search using
			// the Matcher or if the previous search did not find any (further) matches.
			if (regexMatcher.find()) {
				printMatch();
			} else {
				// This also resets the starting position for find() to the start of the subject string
				textResults.setText("No further matches");
			}
		}
	}

	/** Perform a regular expression search-and-replace using a Matcher object.
	 *  This is the recommended way if you often use the same regular expression
	 *  to do a search-and-replace.  You should also reuse the Matcher object
	 *  by calling Matcher.reset(nextSubjectString) for improved efficiency.<p>
	 *
	 *  You also need to use the Pattern and Matcher objects for the search-and-replace
	 *  if you want to use the regex options such as "case insensitive" or "dot all".<p>
	 *
	 *  See the notes with btnReplace for the special $-syntax in the replacement text.
	 */
	void btnObjReplace_actionPerformed(ActionEvent e) {
		if (regexMatcher == null) {
			textResults.setText("Please click Create Objects to create the Matcher object");
		} else {
			try {
				textReplaceResults.setText(regexMatcher.replaceAll(textReplace.getText()));
			} catch (IllegalArgumentException ex) {
				// textReplace contains inapropriate dollar signs
				textResults.setText("You have an error in the replacement text:\n" +
						ex.getMessage());
				textReplaceResults.setText("n/a");
			} catch (IndexOutOfBoundsException ex) {
				// textReplace contains a backreference that does not exist
				// (e.g. $4 if there are only three groups)
				textResults.setText("You have used a non-existent group in the replacement text:\n" +
						ex.getMessage());
				textReplaceResults.setText("n/a");
			}
		}
	}

	/** Using Matcher.appendReplacement() and Matcher.appendTail() you can implement
	 *  a search-and-replace of arbitrary complexity.  These routines allow you
	 *  to compute the replacement string in your own code.  So the replacement text
	 *  can be whatever you want.<p>
	 *
	 *  To do this, simply call Matcher.find() in a loop.  For each match returned
	 *  by find(), call appendReplacement() with whatever replacement text you want.
	 *  When find() can no longer find matches, call appendTail().<p>
	 *
	 *  appendReplacement() appends the substring between the end of the previous match that
	 *  was replaced with appendReplacement() and the current match.  If this is the
	 *  first call to appendReplacement() since creating the Matcher or calling reset(),
	 *  then the appended substring starts at the start of the string.  Then, the specified
	 *  replacement text is appended.  If the replacement text contains dollar signs,
	 *  they will be interpreted as usual.  E.g. $1 is replaced with the match between the
	 *  first pair of capturing parentheses.<p>
	 *
	 *  appendTail() appends the substring between the end of the previous match that
	 *  was replaceced with appendReplacement() and the end of the string.
	 *  If appendReplacement() was not called since creating the Matcher or calling reset(),
	 *  the entire subject string is appended.<p>
	 *
	 *  The above means that you should call Matcher.reset() before starting the operation,
	 *  unless you're sure the Matcher is freshly constructed.  If certain matches do not
	 *  need to be replaced, simply skip calling appendReplacement() for those matches.
	 *  (Calling appendReplacement() with Matcher.group() as the replacement text will
	 *  only hurt performance and may get you into trouble with dollar signs that may appear
	 *  in the regex match.)
	 */
	void btnAdvancedReplace_actionPerformed(ActionEvent e) {
		if (regexMatcher == null) {
			textResults.setText("Please click Create Objects to create the Matcher object");
		} else {
			// We will store the replacement text here
			StringBuffer replaceResult = new StringBuffer();
			while (regexMatcher.find()) {
				try {
					// In this example, we simply replace the regex match with the same text in uppercase.
					// Note that appendReplacement parses the replacement text to substitute $1, $2, etc.
					// with the contents of the corresponding capturing parenthesis just like replaceAll()
					regexMatcher.appendReplacement(replaceResult, regexMatcher.group().toUpperCase());
				} catch (IllegalStateException ex) {
					// appendReplacement() was called without a preceding successful call to find()
					// This exception indicates a bug in your source code
					textResults.setText("appendReplacement() called without a prior successful call to find()");
					textReplaceResults.setText("n/a");
					return;
				} catch (IllegalArgumentException ex) {
					// Replacement text contains inapropriate dollar signs
					textResults.setText("Error in the replacement text:\n" +
							ex.getMessage());
					textReplaceResults.setText("n/a");
					return;
				} catch (IndexOutOfBoundsException ex) {
					// Replacement text contains a backreference that does not exist
					// (e.g. $4 if there are only three groups)
					textResults.setText("Non-existent group in the replacement text:\n" +
							ex.getMessage());
					textReplaceResults.setText("n/a");
					return;
				}
			}
			regexMatcher.appendTail(replaceResult);
			textReplaceResults.setText(replaceResult.toString());
			textResults.setText("n/a");
			// After using appendReplacement and appendTail, the Matcher object must be reset
			// so we can use appendReplacement and appendTail again.
			// In practice, you will probably put this call at the start of the routine
			// where you want to use appendReplacement and appendTail.
			// I did not do that here because this way you can click on the Next Match button
			// a couple of times to skip a few matches, and then click on the
			// Advanced Replace button to observe that appendReplace() will copy the skipped
			// matches unchanged.
			regexMatcher.reset();
		}
	}

	/** If you want to split many strings using the same regular expression,
	 *  you should create a Pattern object and call Pattern.split()
	 *  rather than String.split().  Both methods produce exactly the same results.
	 *  However, when creating a Pattern object, you can specify options such as
	 *  "case insensitive" and "dot all".<p>
	 *
	 *  Note that no Matcher object is used.
	 */
	void btnObjSplit_actionPerformed(ActionEvent e) {
		textReplaceResults.setText("n/a");
		if (compiledRegex == null) {
			textResults.setText("Please click Create Objects to compile the regular expression");
		} else {
			printSplitArray(compiledRegex.split(textSubject.getText() /*, Limit*/));
		}
	}
}

class FrameRegexDemo_btnMatch_actionAdapter implements java.awt.event.ActionListener {
	FrameRegexDemo adaptee;

	FrameRegexDemo_btnMatch_actionAdapter(FrameRegexDemo adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.btnMatch_actionPerformed(e);
	}
}

class FrameRegexDemo_btnReplace_actionAdapter implements java.awt.event.ActionListener {
	FrameRegexDemo adaptee;

	FrameRegexDemo_btnReplace_actionAdapter(FrameRegexDemo adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.btnReplace_actionPerformed(e);
	}
}

class FrameRegexDemo_btnSplit_actionAdapter implements java.awt.event.ActionListener {
	FrameRegexDemo adaptee;

	FrameRegexDemo_btnSplit_actionAdapter(FrameRegexDemo adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.btnSplit_actionPerformed(e);
	}
}

class FrameRegexDemo_btnObjects_actionAdapter implements java.awt.event.ActionListener {
	FrameRegexDemo adaptee;

	FrameRegexDemo_btnObjects_actionAdapter(FrameRegexDemo adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.btnObjects_actionPerformed(e);
	}
}

class FrameRegexDemo_btnNextMatch_actionAdapter implements java.awt.event.ActionListener {
	FrameRegexDemo adaptee;

	FrameRegexDemo_btnNextMatch_actionAdapter(FrameRegexDemo adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.btnNextMatch_actionPerformed(e);
	}
}

class FrameRegexDemo_btnObjReplace_actionAdapter implements java.awt.event.ActionListener {
	FrameRegexDemo adaptee;

	FrameRegexDemo_btnObjReplace_actionAdapter(FrameRegexDemo adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.btnObjReplace_actionPerformed(e);
	}
}

class FrameRegexDemo_btnAdvancedReplace_actionAdapter implements java.awt.event.ActionListener {
	FrameRegexDemo adaptee;

	FrameRegexDemo_btnAdvancedReplace_actionAdapter(FrameRegexDemo adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.btnAdvancedReplace_actionPerformed(e);
	}
}

class FrameRegexDemo_btnObjSplit_actionAdapter implements java.awt.event.ActionListener {
	FrameRegexDemo adaptee;

	FrameRegexDemo_btnObjSplit_actionAdapter(FrameRegexDemo adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.btnObjSplit_actionPerformed(e);
	}
}