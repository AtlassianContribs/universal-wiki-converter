package com.atlassian.uwc.ui;

import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class supporting the form which lets you choose
 * which converters to use, also add and remove converts.
 * @deprecated
 */
public class ConverterListForm {
    protected JTextField converterExpressionField;
    protected JButton deleteConverter;
    protected JButton addNewConverter;
    protected JButton updateConverter;
    protected JList converterList;
    protected JTextField converterNameField;
    protected JScrollPane converterListScrollPane;
    protected JPanel converterListPanel;
    private UWCForm2 uwcForm = null;
    Logger log = Logger.getLogger("ConverterListForm");
    private JButton addToEngine;
    protected DefaultListModel converterListModel = new DefaultListModel();

    public ConverterListForm(UWCForm2 uwcForm) {
        this.uwcForm = uwcForm;
        init();
    }

    public void init() {
        createActionListeners();
        // set up converterListModel for engineSelectedConvertersJList
    }

    /**
     * create the button listeners
     */
    public void createActionListeners() {
        log.debug("initializing listeners");
        converterExpressionField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        addToEngine.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // get selected items

                try {
                    Object selectedConverters[] = converterList.getSelectedValues();
                    for (Object selectedConverter1 : selectedConverters) {
                        String selectedConverter = (String) selectedConverter1;
                        String key = selectedConverter.substring(0, selectedConverter.indexOf("="));
                        String value = uwcForm.allConverters.get(key);
                        uwcForm.engineSelectedConverterList.add(key + "=" + value);
                        uwcForm.updateConverterListModel();
                        uwcForm.converterListFrame.setVisible(false);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                // add those to the converterListModel for the engine selected converters
            }
        });
        deleteConverter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String key = converterNameField.getText().trim();
                uwcForm.allConverters.remove(key);
                uwcForm.serializeOutConverterPropFile();
                uwcForm.populateAllConverterList();
                converterNameField.setText("");
                converterExpressionField.setText("");
            }
        });
        addNewConverter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // read values
                String newKey = converterNameField.getText().trim();
                String newValue = converterExpressionField.getText().trim();
                // add to the all converters list
                uwcForm.allConverters.put(newKey, newValue);
                // serialize out the all converters list
                uwcForm.serializeOutConverterPropFile();
                // update the screen
                uwcForm.populateAllConverterList();
                // clear the fields
                converterNameField.setText("");
                converterExpressionField.setText("");
            }

        });
        updateConverter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newKey = converterNameField.getText().trim();
                String newValue = converterExpressionField.getText().trim();
                uwcForm.allConverters.put(newKey, newValue);
                uwcForm.serializeOutConverterPropFile();
                // update the screen
                uwcForm.populateAllConverterList();
                // clear the fields
                converterNameField.setText("");
                converterExpressionField.setText("");
            }
        });
        converterList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int selected = e.getLastIndex();
                String key = (String) (converterList.getSelectedValue());
                if (key == null) {
                    return;
                }
                key = key.substring(0, key.indexOf("="));
                String value = uwcForm.allConverters.get(key);
                converterNameField.setText(key);
                converterExpressionField.setText(value);
            }
        });
        converterNameField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // includePagesButton button pressed
            }
        });
    }

}
