/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.mindmap.ide.commons.preferences;

import static java.util.Objects.requireNonNull;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import static javax.swing.LayoutStyle.ComponentPlacement.UNRELATED;

import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyShortcut;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public final class KeyShortCutEditor implements TableModel {

  private final List<TableModelListener> listeners = new ArrayList<>();
  private final List<KeyShortCutValue> listOfKeys;
  private final String columnNameFirst =
      MmcI18n.getInstance().findBundle().getString("KeyShortCutEditPanel.ColumnName");
  private final String columnNameSecond =
      MmcI18n.getInstance().findBundle().getString("KeyShortCutEditPanel.ColumnKey");
  private final JPanel mainPanel;
  private final JToggleButton buttonEditKeyCode;
  private final JCheckBox checkBoxALT;
  private final JCheckBox checkBoxCTRL;
  private final JCheckBox checkBoxMeta;
  private final JCheckBox checkBoxSHIFT;
  private final JPanel panelModifiers;
  private final JScrollPane scrollPaneShortcuts;
  private final JLabel labelKeyCode;
  private final JTable tableKeyShortcuts;
  private final JTextField textFieldKeyCode;

  public KeyShortCutEditor(final UIComponentFactory componentFactory,
                           final List<KeyShortcut> startList,
                           final boolean allowsModifiersOnly) {
    this.mainPanel = componentFactory.makePanel();
    this.buttonEditKeyCode = componentFactory.makeToggleButton();
    this.checkBoxALT = componentFactory.makeCheckBox();
    this.checkBoxCTRL = componentFactory.makeCheckBox();
    this.checkBoxMeta = componentFactory.makeCheckBox();
    this.checkBoxSHIFT = componentFactory.makeCheckBox();
    this.panelModifiers = componentFactory.makePanel();
    this.scrollPaneShortcuts = componentFactory.makeScrollPane();
    this.labelKeyCode = componentFactory.makeLabel();
    this.tableKeyShortcuts = new JTable();
    this.textFieldKeyCode = componentFactory.makeTextField();

    fillComponents();
    this.listOfKeys = startList.stream().filter(x -> !x.isModifiersOnly() || allowsModifiersOnly).map(KeyShortCutValue::new)
        .sorted(Comparator.comparing(a -> a.text))
        .collect(Collectors.toList());
    this.tableKeyShortcuts.setModel(this);
    this.tableKeyShortcuts.getSelectionModel().addListSelectionListener(e -> updateForSelected());

    this.tableKeyShortcuts.getInputMap(JComponent.WHEN_FOCUSED)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
    this.tableKeyShortcuts.getActionMap().put("Enter", new AbstractAction() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        buttonEditKeyCode.requestFocus();
        buttonEditKeyCode.doClick();
      }
    });

    updateForSelected();
    this.tableKeyShortcuts.requestFocus();

    final ActionListener actionCheckBox = (final ActionEvent e) -> {
      final int selectedRow = this.tableKeyShortcuts.getSelectedRow();
      if (selectedRow >= 0) {
        final KeyShortCutValue oldShortCut = this.listOfKeys.get(selectedRow);

        int modifiers = oldShortCut.getShortcut().getModifiers();
        final JCheckBox source = (JCheckBox) e.getSource();
        if (e.getSource() == this.checkBoxALT) {
          modifiers =
              source.isSelected() ? modifiers | KeyEvent.ALT_MASK : modifiers & ~KeyEvent.ALT_MASK;
        } else if (e.getSource() == this.checkBoxCTRL) {
          modifiers = source.isSelected() ? modifiers | KeyEvent.CTRL_MASK :
              modifiers & ~KeyEvent.CTRL_MASK;
        } else if (e.getSource() == this.checkBoxMeta) {
          modifiers = source.isSelected() ? modifiers | KeyEvent.META_MASK :
              modifiers & ~KeyEvent.META_MASK;
        } else if (e.getSource() == this.checkBoxSHIFT) {
          modifiers = source.isSelected() ? modifiers | KeyEvent.SHIFT_MASK :
              modifiers & ~KeyEvent.SHIFT_MASK;
        }

        oldShortCut.setShortcut(new KeyShortcut(oldShortCut.getShortcut().getID(),
            oldShortCut.getShortcut().getKeyCode(), modifiers));

        for (final TableModelListener l : this.listeners) {
          l.tableChanged(new TableModelEvent(KeyShortCutEditor.this, selectedRow));
        }

        updateForSelected();
      }
    };

    this.checkBoxALT.addActionListener(actionCheckBox);
    this.checkBoxCTRL.addActionListener(actionCheckBox);
    this.checkBoxMeta.addActionListener(actionCheckBox);
    this.checkBoxSHIFT.addActionListener(actionCheckBox);
  }

  public JPanel asPanel() {
    return this.mainPanel;
  }

  private void updateCurrentSelectedForKey(final KeyEvent evt) {
    final int index = this.tableKeyShortcuts.getSelectedRow();
    if (index >= 0) {
      final KeyShortCutValue oldShortcut = this.listOfKeys.get(index);
      final int keyCode = evt.getKeyCode();
      final int modifiers = evt.getModifiers() &
          (KeyEvent.META_MASK | KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK);
      final KeyShortcut newShortCut;
      if (oldShortcut.isModifiersOnly()) {
        newShortCut = new KeyShortcut(oldShortcut.getShortcut().getID(), modifiers);
      } else {
        newShortCut = new KeyShortcut(oldShortcut.getShortcut().getID(), keyCode, modifiers);
      }
      oldShortcut.setShortcut(newShortCut);
      for (final TableModelListener l : this.listeners) {
        l.tableChanged(new TableModelEvent(this, index));
      }
    }

    updateForSelected();
  }

  private KeyShortCutValue getSelectedRow() {
    final int index = this.tableKeyShortcuts.getSelectedRow();
    return index < 0 ? null : this.listOfKeys.get(index);
  }

  private void updateForSelected() {
    final KeyShortCutValue shortcut = getSelectedRow();
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

      this.textFieldKeyCode.setText("");
      this.textFieldKeyCode.setEnabled(false);
    } else {
      this.buttonEditKeyCode.setEnabled(!shortcut.isModifiersOnly());
      this.buttonEditKeyCode.setSelected(false);

      this.textFieldKeyCode.setEnabled(!shortcut.isModifiersOnly());
      this.checkBoxALT.setEnabled(true);
      this.checkBoxCTRL.setEnabled(true);
      this.checkBoxMeta.setEnabled(true);
      this.checkBoxSHIFT.setEnabled(true);

      this.textFieldKeyCode.setText(
          shortcut.isModifiersOnly() ? "..." : shortcut.getShortcut().getKeyCodeName());
      this.checkBoxALT.setSelected(shortcut.getShortcut().isAlt());
      this.checkBoxSHIFT.setSelected(shortcut.getShortcut().isShift());
      this.checkBoxMeta.setSelected(shortcut.getShortcut().isMeta());
      this.checkBoxCTRL.setSelected(shortcut.getShortcut().isCtrl());
    }
  }

  public List<KeyShortcut> getResult() {
    return this.listOfKeys.stream().map(KeyShortCutValue::getShortcut).collect(Collectors.toList());
  }

  private void fillComponents() {
    final ResourceBundle bundle = MmcI18n.getInstance().findBundle();
    this.mainPanel.setLayout(new BorderLayout());

    this.scrollPaneShortcuts.setBorder(
        BorderFactory.createTitledBorder(bundle.getString("KeyShortCutEditPanel.ScrollPaneBorderTitle")));

    this.tableKeyShortcuts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    this.tableKeyShortcuts.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        tableKeyShortcutsMouseClicked(evt);
      }
    });
    this.scrollPaneShortcuts.setViewportView(this.tableKeyShortcuts);

    this.mainPanel.add(this.scrollPaneShortcuts, BorderLayout.CENTER);

    this.labelKeyCode.setText(bundle.getString("KeyShortCutEditPanel.labelKeyCode.text"));
    this.checkBoxALT.setText("ALT");
    this.checkBoxCTRL.setText("CTRL");
    this.checkBoxSHIFT.setText("SHIFT");
    this.checkBoxMeta.setText("META");

    this.textFieldKeyCode.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent evt) {
        textFieldKeyCodeFocusLost(evt);
      }
    });
    this.textFieldKeyCode.addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent evt) {
        textFieldKeyCodeKeyTyped(evt);
      }

      public void keyPressed(KeyEvent evt) {
        textFieldKeyCodeKeyPressed(evt);
      }
    });

    this.buttonEditKeyCode.setText(bundle.getString("KeyShortCutEditPanel.buttonEditKeyCode.text"));
    this.buttonEditKeyCode.addActionListener(this::buttonEditKeyCodeActionPerformed);

    final GroupLayout panelLayout = new GroupLayout(this.panelModifiers);
    this.panelModifiers.setLayout(panelLayout);
    panelLayout.setHorizontalGroup(
        panelLayout.createParallelGroup(LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING,
                panelLayout.createSequentialGroup()
                    .addContainerGap(44, Short.MAX_VALUE)
                    .addGroup(
                        panelLayout.createParallelGroup(LEADING,
                                false)
                            .addGroup(panelLayout.createSequentialGroup()
                                .addComponent(this.labelKeyCode)
                                .addPreferredGap(RELATED)
                                .addComponent(this.textFieldKeyCode))
                            .addGroup(panelLayout.createSequentialGroup()
                                .addComponent(this.checkBoxALT)
                                .addPreferredGap(RELATED)
                                .addComponent(this.checkBoxCTRL)
                                .addPreferredGap(RELATED)
                                .addComponent(this.checkBoxSHIFT)
                                .addPreferredGap(RELATED)
                                .addComponent(this.checkBoxMeta)))
                    .addPreferredGap(RELATED)
                    .addComponent(this.buttonEditKeyCode)
                    .addContainerGap())
    );
    panelLayout.setVerticalGroup(
        panelLayout.createParallelGroup(LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(
                    panelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(this.buttonEditKeyCode, GroupLayout.PREFERRED_SIZE, 56,
                            GroupLayout.PREFERRED_SIZE)
                        .addGroup(LEADING,
                            panelLayout.createSequentialGroup()
                                .addGroup(panelLayout.createParallelGroup(
                                        BASELINE)
                                    .addComponent(this.labelKeyCode)
                                    .addComponent(this.textFieldKeyCode,
                                        GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(
                                    UNRELATED)
                                .addGroup(panelLayout.createParallelGroup(
                                        BASELINE)
                                    .addComponent(this.checkBoxALT)
                                    .addComponent(this.checkBoxCTRL)
                                    .addComponent(this.checkBoxSHIFT)
                                    .addComponent(checkBoxMeta))))
                .addContainerGap(44, Short.MAX_VALUE))
    );

    this.mainPanel.add(this.panelModifiers, BorderLayout.PAGE_END);
  }

  private void buttonEditKeyCodeActionPerformed(final ActionEvent a) {
    if (this.buttonEditKeyCode.isSelected()) {
      this.textFieldKeyCode.requestFocus();
      this.textFieldKeyCode.setFocusTraversalKeysEnabled(false);
    }
  }

  private void textFieldKeyCodeFocusLost(final FocusEvent a) {
    if (this.buttonEditKeyCode.isSelected()) {
      this.buttonEditKeyCode.setSelected(false);
    }
  }

  private void textFieldKeyCodeKeyPressed(final KeyEvent a) {
    if (this.buttonEditKeyCode.isSelected()) {
      switch (a.getKeyCode()) {
        case KeyEvent.VK_META:
        case KeyEvent.VK_ALT:
        case KeyEvent.VK_SHIFT:
        case KeyEvent.VK_CONTROL:
          a.consume();
          break;
        default: {
          updateCurrentSelectedForKey(a);
          this.buttonEditKeyCode.setSelected(false);
          this.textFieldKeyCode.setFocusTraversalKeysEnabled(true);
          this.tableKeyShortcuts.requestFocus();
        }
        break;
      }
    }
    a.consume();
  }

  private void textFieldKeyCodeKeyTyped(final KeyEvent a) {
    a.consume();
  }

  private void tableKeyShortcutsMouseClicked(final MouseEvent a) {
    if (a.getClickCount() > 1) {
      this.buttonEditKeyCode.requestFocus();
      this.buttonEditKeyCode.doClick();
    }
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
        return this.columnNameFirst;
      case 1:
        return this.columnNameSecond;
      default:
        return null;
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return false;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final KeyShortCutValue key = this.listOfKeys.get(rowIndex);
    switch (columnIndex) {
      case 0:
        return key.toString();
      case 1:
        return key.getShortcut().toString();
      default:
        return null;
    }
  }

  @Override
  public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
  }

  @Override
  public void addTableModelListener(final TableModelListener l) {
    this.listeners.add(l);
  }

  @Override
  public void removeTableModelListener(final TableModelListener l) {
    this.listeners.remove(l);
  }

  private static class KeyShortCutValue {
    private final String text;
    private KeyShortcut shortcut;

    public KeyShortCutValue(final KeyShortcut shortcut) {
      this.shortcut = requireNonNull(shortcut);
      this.text = MmdI18n.getInstance().findBundle().getString("KeyShortcut." + shortcut.getID());
    }

    public boolean isModifiersOnly() {
      return this.shortcut.isModifiersOnly();
    }

    @Override
    public String toString() {
      return this.text;
    }

    public KeyShortcut getShortcut() {
      return this.shortcut;
    }

    public void setShortcut(final KeyShortcut shortcut) {
      this.shortcut = requireNonNull(shortcut);
    }
  }
}
