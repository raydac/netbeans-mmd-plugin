/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyShortcut;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class KeyShortCutEditPanel extends JBPanel implements TableModel {

  private static final long serialVersionUID = -8892558469392323517L;

  private static final Logger LOGGER = LoggerFactory.getLogger(FileEditPanel.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("i18n/Bundle");

  private final List<KeyShortcut> listOfKeys;
  private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();
  private JToggleButton buttonEditKeyCode;
  private JBCheckBox checkBoxALT;
  private JBCheckBox checkBoxCTRL;
  private JBCheckBox checkBoxMeta;
  private JBCheckBox checkBoxSHIFT;
  private JBPanel mainPanel;
  private JBScrollPane scrollPaneTable;
  private JBLabel labelKeyCode;
  private JBTable tableKeyShortcuts;
  private JBTextField textFieldKeyCode;

  public KeyShortCutEditPanel(final List<KeyShortcut> list) {
    super();
    initComponents();
    this.listOfKeys = new ArrayList<KeyShortcut>(list);
    this.tableKeyShortcuts.setModel(this);
    this.tableKeyShortcuts.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(final ListSelectionEvent e) {
        updateForSelected();
      }
    });

    this.tableKeyShortcuts.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter"); //NOI18N
    this.tableKeyShortcuts.getActionMap().put("Enter", new AbstractAction() { //NOI18N
      private static final long serialVersionUID = -5644390861803492172L;

      @Override
      public void actionPerformed(final ActionEvent e) {
        buttonEditKeyCode.requestFocus();
        buttonEditKeyCode.doClick();
      }
    });

    updateForSelected();

    this.tableKeyShortcuts.requestFocus();

    final KeyShortCutEditPanel theInstance = this;

    final ActionListener actionCheckBox = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final int selectedRow = tableKeyShortcuts.getSelectedRow();
        if (selectedRow >= 0) {
          final KeyShortcut oldShortCut = listOfKeys.get(selectedRow);

          int modifiers = oldShortCut.getModifiers();
          final JBCheckBox source = (JBCheckBox) e.getSource();
          if (e.getSource() == checkBoxALT) {
            modifiers = source.isSelected() ? modifiers | KeyEvent.ALT_MASK : modifiers & ~KeyEvent.ALT_MASK;
          } else if (e.getSource() == checkBoxCTRL) {
            modifiers = source.isSelected() ? modifiers | KeyEvent.CTRL_MASK : modifiers & ~KeyEvent.CTRL_MASK;
          } else if (e.getSource() == checkBoxMeta) {
            modifiers = source.isSelected() ? modifiers | KeyEvent.META_MASK : modifiers & ~KeyEvent.META_MASK;
          } else if (e.getSource() == checkBoxSHIFT) {
            modifiers = source.isSelected() ? modifiers | KeyEvent.SHIFT_MASK : modifiers & ~KeyEvent.SHIFT_MASK;
          }

          listOfKeys.set(selectedRow, new KeyShortcut(oldShortCut.getID(), oldShortCut.getKeyCode(), modifiers));

          for (final TableModelListener l : listeners) {
            l.tableChanged(new TableModelEvent(theInstance, selectedRow));
          }

          updateForSelected();
        }
      }
    };

    this.checkBoxALT.addActionListener(actionCheckBox);
    this.checkBoxCTRL.addActionListener(actionCheckBox);
    this.checkBoxMeta.addActionListener(actionCheckBox);
    this.checkBoxSHIFT.addActionListener(actionCheckBox);
  }

  private void updateCurrentSelectedForKey(final KeyEvent evt) {
    final int index = this.tableKeyShortcuts.getSelectedRow();
    if (index >= 0) {
      final KeyShortcut oldShortcut = this.listOfKeys.get(index);
      final int keyCode = evt.getKeyCode();
      final int modifiers = evt.getModifiers() & (KeyEvent.META_MASK | KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK);
      final KeyShortcut newShortCut = new KeyShortcut(oldShortcut.getID(), keyCode, modifiers);
      this.listOfKeys.set(index, newShortCut);
      for (final TableModelListener l : this.listeners) {
        l.tableChanged(new TableModelEvent(this, index));
      }
    }

    updateForSelected();
  }

  private KeyShortcut getSelectedRow() {
    final int index = this.tableKeyShortcuts.getSelectedRow();
    return index < 0 ? null : this.listOfKeys.get(index);
  }

  private void updateForSelected() {
    final KeyShortcut shortcut = getSelectedRow();
    if (shortcut == null) {
      this.buttonEditKeyCode.setEnabled(false);
      this.buttonEditKeyCode.setSelected(false);

      this.checkBoxALT.setSelected(false);
      this.checkBoxSHIFT.setSelected(false);
      this.checkBoxCTRL.setSelected(false);
      this.checkBoxMeta.setSelected(false);

      this.checkBoxALT.setEnabled(false);
      this.checkBoxCTRL.setEnabled(false);
      this.checkBoxSHIFT.setEnabled(false);
      this.checkBoxMeta.setEnabled(false);

      this.textFieldKeyCode.setText(""); //NOI18N
      this.textFieldKeyCode.setEnabled(false);
    } else {
      this.buttonEditKeyCode.setEnabled(true);
      this.buttonEditKeyCode.setSelected(false);

      this.textFieldKeyCode.setEnabled(true);
      this.checkBoxALT.setEnabled(true);
      this.checkBoxCTRL.setEnabled(true);
      this.checkBoxMeta.setEnabled(true);
      this.checkBoxSHIFT.setEnabled(true);

      this.textFieldKeyCode.setText(shortcut.getKeyCodeName());
      this.checkBoxALT.setSelected(shortcut.isAlt());
      this.checkBoxSHIFT.setSelected(shortcut.isShift());
      this.checkBoxMeta.setSelected(shortcut.isMeta());
      this.checkBoxCTRL.setSelected(shortcut.isCtrl());
    }
  }

  public List<KeyShortcut> getResult() {
    return this.listOfKeys;
  }

  @SuppressWarnings("unchecked")
  private void initComponents() {

    scrollPaneTable = new JBScrollPane();
    tableKeyShortcuts = new JBTable();
    mainPanel = new JBPanel();
    labelKeyCode = new JBLabel();
    checkBoxALT = new JBCheckBox();
    checkBoxCTRL = new JBCheckBox();
    checkBoxSHIFT = new JBCheckBox();
    checkBoxMeta = new JBCheckBox();
    textFieldKeyCode = new JBTextField();
    buttonEditKeyCode = new JToggleButton();

    setLayout(new java.awt.BorderLayout());

    scrollPaneTable.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("KeyShortCutEditPanel.ScrollPaneBorderTitle"))); // NOI18N

    tableKeyShortcuts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    tableKeyShortcuts.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        tableKeyShortcutsMouseClicked(evt);
      }
    });
    scrollPaneTable.setViewportView(tableKeyShortcuts);

    add(scrollPaneTable, java.awt.BorderLayout.CENTER);

    labelKeyCode.setText(BUNDLE.getString("KeyShortCutEditPanel.labelKeyCode.text")); // NOI18N
    checkBoxALT.setText(BUNDLE.getString("KeyShortCutEditPanel.checkBoxALT.text")); // NOI18N
    checkBoxCTRL.setText(BUNDLE.getString("KeyShortCutEditPanel.checkBoxCTRL.text")); // NOI18N
    checkBoxSHIFT.setText(BUNDLE.getString("KeyShortCutEditPanel.checkBoxSHIFT.text")); // NOI18N
    checkBoxMeta.setText(BUNDLE.getString("KeyShortCutEditPanel.checkBoxMeta.text")); // NOI18N

    textFieldKeyCode.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent evt) {
        textFieldKeyCodeFocusLost(evt);
      }
    });
    textFieldKeyCode.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyTyped(java.awt.event.KeyEvent evt) {
        textFieldKeyCodeKeyTyped(evt);
      }

      public void keyPressed(java.awt.event.KeyEvent evt) {
        textFieldKeyCodeKeyPressed(evt);
      }
    });

    buttonEditKeyCode.setText(BUNDLE.getString("KeyShortCutEditPanel.buttonEditKeyCode.text")); // NOI18N
    buttonEditKeyCode.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonEditKeyCodeActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
    mainPanel.setLayout(mainPanelLayout);
    mainPanelLayout.setHorizontalGroup(
        mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap(44, Short.MAX_VALUE)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(labelKeyCode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldKeyCode))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(checkBoxALT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxCTRL)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxSHIFT)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkBoxMeta)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonEditKeyCode)
                .addContainerGap())
    );
    mainPanelLayout.setVerticalGroup(
        mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonEditKeyCode, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelKeyCode)
                            .addComponent(textFieldKeyCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(checkBoxALT)
                            .addComponent(checkBoxCTRL)
                            .addComponent(checkBoxSHIFT)
                            .addComponent(checkBoxMeta))))
                .addContainerGap(44, Short.MAX_VALUE))
    );

    add(mainPanel, java.awt.BorderLayout.PAGE_END);
  }

  private void buttonEditKeyCodeActionPerformed(java.awt.event.ActionEvent evt) {
    if (this.buttonEditKeyCode.isSelected()) {
      this.textFieldKeyCode.requestFocus();
    }
  }

  private void textFieldKeyCodeFocusLost(java.awt.event.FocusEvent evt) {
    if (this.buttonEditKeyCode.isSelected()) {
      this.buttonEditKeyCode.setSelected(false);
    }
  }

  private void textFieldKeyCodeKeyPressed(java.awt.event.KeyEvent evt) {
    if (this.buttonEditKeyCode.isSelected()) {
      switch (evt.getKeyCode()) {
        case KeyEvent.VK_META:
        case KeyEvent.VK_ALT:
        case KeyEvent.VK_SHIFT:
        case KeyEvent.VK_CONTROL:
          evt.consume();
          break;
        default: {
          updateCurrentSelectedForKey(evt);
          this.buttonEditKeyCode.setSelected(false);
          this.tableKeyShortcuts.requestFocus();
        }
        break;
      }
    }
    evt.consume();
  }

  private void textFieldKeyCodeKeyTyped(java.awt.event.KeyEvent evt) {
    evt.consume();
  }

  private void tableKeyShortcutsMouseClicked(java.awt.event.MouseEvent evt) {
    if (evt.getClickCount() > 1) {
      this.buttonEditKeyCode.requestFocus();
      this.buttonEditKeyCode.doClick();
    }
  }

  public JBTable getTableComponent() {
    return this.tableKeyShortcuts;
  }

  @Override
  public int getRowCount() {
    return this.listOfKeys.size();
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(int columnIndex) {
    switch (columnIndex) {
      case 0:
        return BUNDLE.getString("KeyShortCutEditPanel.ColumnName");
      case 1:
        return BUNDLE.getString("KeyShortCutEditPanel.ColumnKey");
      default:
        return null;
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    final KeyShortcut key = this.listOfKeys.get(rowIndex);
    switch (columnIndex) {
      case 0:
        return Utils.convertCamelCasedToHumanForm(key.getID(), true);
      case 1:
        return key.toString();
      default:
        return null;
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
  }

  @Override
  public void addTableModelListener(TableModelListener l) {
    this.listeners.add(l);
  }

  @Override
  public void removeTableModelListener(TableModelListener l) {
    this.listeners.remove(l);
  }
}
