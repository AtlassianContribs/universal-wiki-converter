package com.atlassian.uwc.ui;

import com.atlassian.uwc.converters.Converter;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Stack;

/**
 * Drives the regular expression testing tool. This uses the exact same
 * infrastructure as what happens when you hit the 'Send to Confluence'
 * button so in theory the results should be very similar.
 */
public class RegExTestTool {
    protected JButton revertButton;
    protected JButton runButton;
    protected JTextField regexField;
    protected JTextArea inputArea;
    protected JTextArea outputArea;
    protected UWCForm2 uwcForm;
    protected JPanel regExTestToolPanel;
    Stack<String> regexStack = new Stack<String>();

    public RegExTestTool(UWCForm2 uwcForm) {
        this.uwcForm = uwcForm;
        init();
    }

    protected void init() {
        createActionListeners();
    }

    protected void createActionListeners() {
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String regex = getRegexField().getText();
                regexStack.push(regex);
                // create a converter using what is in the text field
                Converter converter = uwcForm.converterEngine.getConverterFromString(regex);
                // run the converter against the input text
                File dummyFile = new File("dummyFile.txt");
                Page page = new Page(dummyFile);
                page.setOriginalText(inputArea.getText());
                converter.convert(page);
                // populate the output box
                outputArea.setText(page.getConvertedText());

            }
        });

        revertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String regex = regexStack.pop();
                regexField.setText(regex);
            }

        });

    }

    public JTextArea getInputArea() {
        return inputArea;
    }

    public void setInputArea(JTextArea inputArea) {
        this.inputArea = inputArea;
    }

    public JTextArea getOutputArea() {
        return outputArea;
    }

    public void setOutputArea(JTextArea outputArea) {
        this.outputArea = outputArea;
    }

    public JTextField getRegexField() {
        return regexField;
    }

    public void setRegexField(JTextField regexField) {
        this.regexField = regexField;
    }

    public JPanel getRegExTestToolPanel() {
        return regExTestToolPanel;
    }

    public void setRegExTestToolPanel(JPanel regExTestToolPanel) {
        this.regExTestToolPanel = regExTestToolPanel;
    }

    public JButton getRevertButton() {
        return revertButton;
    }

    public void setRevertButton(JButton revertButton) {
        this.revertButton = revertButton;
    }

    public JButton getRunButton() {
        return runButton;
    }

    public void setRunButton(JButton runButton) {
        this.runButton = runButton;
    }
}
