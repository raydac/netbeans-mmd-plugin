/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.nb.swing;

import com.igormaznitsa.mindmap.swing.panel.utils.KeyShortcut;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class KeyShortCutEditPanel extends javax.swing.JPanel implements TableModel {

  private static final long serialVersionUID = -8892558469392323517L;

  private final List<KeyShortcut> listOfKeys;
  private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();
  
  public KeyShortCutEditPanel (final List<KeyShortcut> startList) {
    initComponents();
    this.listOfKeys = new ArrayList<KeyShortcut>(startList);
    this.tableKeyShortcuts.setModel(this);
    this.tableKeyShortcuts.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged (final ListSelectionEvent e) {
        updateForSelected();
      }
    });

    this.tableKeyShortcuts.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter"); //NOI18N
    this.tableKeyShortcuts.getActionMap().put("Enter", new AbstractAction(){//NOI18N 
      private static final long serialVersionUID = -5644390861803492172L;
      @Override
      public void actionPerformed (final ActionEvent e) {
        buttonEditKeyCode.requestFocus();
        buttonEditKeyCode.doClick();
      }
    });
    
    updateForSelected();
    
    this.tableKeyShortcuts.requestFocus();
  
    final KeyShortCutEditPanel theInstance = this;
    
    final ActionListener actionCheckBox = new ActionListener() {
      @Override
      public void actionPerformed (final ActionEvent e) {
        final int selectedRow = tableKeyShortcuts.getSelectedRow();
        if (selectedRow>=0){
          final KeyShortcut oldShortCut = listOfKeys.get(selectedRow);
          
          int modifiers = oldShortCut.getModifiers();
          final JCheckBox source = (JCheckBox) e.getSource();
          if (e.getSource() == checkBoxALT){
            modifiers = source.isSelected() ? modifiers | KeyEvent.ALT_MASK : modifiers & ~KeyEvent.ALT_MASK;
          } else if (e.getSource() == checkBoxCTRL){
            modifiers = source.isSelected() ? modifiers | KeyEvent.CTRL_MASK : modifiers & ~KeyEvent.CTRL_MASK;
          } else if (e.getSource() == checkBoxMeta) {
            modifiers = source.isSelected() ? modifiers | KeyEvent.META_MASK : modifiers & ~KeyEvent.META_MASK;
          } else if (e.getSource() == checkBoxSHIFT) {
            modifiers = source.isSelected() ? modifiers | KeyEvent.SHIFT_MASK : modifiers & ~KeyEvent.SHIFT_MASK;
          }
          
          listOfKeys.set(selectedRow, new KeyShortcut(oldShortCut.getID(), oldShortCut.getKeyCode(), modifiers));
          
          for(final TableModelListener l : listeners){
            l.tableChanged(new TableModelEvent(theInstance,selectedRow));
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

  private void updateCurrentSelectedForKey (final KeyEvent evt) {
    final int index = this.tableKeyShortcuts.getSelectedRow();
    if (index>=0){
      final KeyShortcut oldShortcut = this.listOfKeys.get(index);
      final int keyCode = evt.getKeyCode();
      final int modifiers = evt.getModifiers() & (KeyEvent.META_MASK | KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK);
      final KeyShortcut newShortCut = new KeyShortcut(oldShortcut.getID(),keyCode,modifiers);
      this.listOfKeys.set(index, newShortCut);
      for(final TableModelListener l:this.listeners){
        l.tableChanged(new TableModelEvent(this,index));
      }
    }
    
    updateForSelected();
  }  
  
  private KeyShortcut getSelectedRow(){
    final int index = this.tableKeyShortcuts.getSelectedRow();
    return index < 0 ? null : this.listOfKeys.get(index);
  }
  
  private void updateForSelected(){
    final KeyShortcut shortcut = getSelectedRow();
    if (shortcut == null){
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
    }else{
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
  
  public List<KeyShortcut> getResult(){
    return this.listOfKeys;
  }
  
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings ("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScrollPane1 = new javax.swing.JScrollPane();
    tableKeyShortcuts = new javax.swing.JTable();
    jPanel1 = new javax.swing.JPanel();
    labelKeyCode = new javax.swing.JLabel();
    checkBoxALT = new javax.swing.JCheckBox();
    checkBoxCTRL = new javax.swing.JCheckBox();
    checkBoxSHIFT = new javax.swing.JCheckBox();
    checkBoxMeta = new javax.swing.JCheckBox();
    textFieldKeyCode = new javax.swing.JTextField();
    buttonEditKeyCode = new javax.swing.JToggleButton();

    setLayout(new java.awt.BorderLayout());

    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
    jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("KeyShortCutEditPanel.ScrollPaneBorderTitle"))); // NOI18N

    tableKeyShortcuts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    tableKeyShortcuts.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        tableKeyShortcutsMouseClicked(evt);
      }
    });
    jScrollPane1.setViewportView(tableKeyShortcuts);

    add(jScrollPane1, java.awt.BorderLayout.CENTER);

    org.openide.awt.Mnemonics.setLocalizedText(labelKeyCode, bundle.getString("KeyShortCutEditPanel.labelKeyCode.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(checkBoxALT, bundle.getString("KeyShortCutEditPanel.checkBoxALT.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(checkBoxCTRL, bundle.getString("KeyShortCutEditPanel.checkBoxCTRL.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(checkBoxSHIFT, bundle.getString("KeyShortCutEditPanel.checkBoxSHIFT.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(checkBoxMeta, bundle.getString("KeyShortCutEditPanel.checkBoxMeta.text")); // NOI18N

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

    org.openide.awt.Mnemonics.setLocalizedText(buttonEditKeyCode, bundle.getString("KeyShortCutEditPanel.buttonEditKeyCode.text")); // NOI18N
    buttonEditKeyCode.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonEditKeyCodeActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addContainerGap(44, Short.MAX_VALUE)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(labelKeyCode)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(textFieldKeyCode))
          .addGroup(jPanel1Layout.createSequentialGroup()
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
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(buttonEditKeyCode, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(labelKeyCode)
              .addComponent(textFieldKeyCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(checkBoxALT)
              .addComponent(checkBoxCTRL)
              .addComponent(checkBoxSHIFT)
              .addComponent(checkBoxMeta))))
        .addContainerGap(44, Short.MAX_VALUE))
    );

    add(jPanel1, java.awt.BorderLayout.PAGE_END);
  }// </editor-fold>//GEN-END:initComponents

  private void buttonEditKeyCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonEditKeyCodeActionPerformed
    if (this.buttonEditKeyCode.isSelected()){
      this.textFieldKeyCode.requestFocus();
    }
  }//GEN-LAST:event_buttonEditKeyCodeActionPerformed

  private void textFieldKeyCodeFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_textFieldKeyCodeFocusLost
    if (this.buttonEditKeyCode.isSelected()){
      this.buttonEditKeyCode.setSelected(false);
    }
  }//GEN-LAST:event_textFieldKeyCodeFocusLost

  private void textFieldKeyCodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldKeyCodeKeyPressed
    if (this.buttonEditKeyCode.isSelected()){
      switch(evt.getKeyCode()){
        case KeyEvent.VK_META:
        case KeyEvent.VK_ALT:
        case KeyEvent.VK_SHIFT:
        case KeyEvent.VK_CONTROL: evt.consume();break;
        default:{
          updateCurrentSelectedForKey(evt);
          this.buttonEditKeyCode.setSelected(false);
          this.tableKeyShortcuts.requestFocus();
        }break;
      }
    }
    evt.consume();
  }//GEN-LAST:event_textFieldKeyCodeKeyPressed

  private void textFieldKeyCodeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldKeyCodeKeyTyped
    evt.consume();
  }//GEN-LAST:event_textFieldKeyCodeKeyTyped

  private void tableKeyShortcutsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableKeyShortcutsMouseClicked
    if (evt.getClickCount()>1){
      this.buttonEditKeyCode.requestFocus();
      this.buttonEditKeyCode.doClick();
    }
  }//GEN-LAST:event_tableKeyShortcutsMouseClicked


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JToggleButton buttonEditKeyCode;
  private javax.swing.JCheckBox checkBoxALT;
  private javax.swing.JCheckBox checkBoxCTRL;
  private javax.swing.JCheckBox checkBoxMeta;
  private javax.swing.JCheckBox checkBoxSHIFT;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JLabel labelKeyCode;
  private javax.swing.JTable tableKeyShortcuts;
  private javax.swing.JTextField textFieldKeyCode;
  // End of variables declaration//GEN-END:variables

  @Override
  public int getRowCount () {
    return this.listOfKeys.size();
  }

  @Override
  public int getColumnCount () {
    return 2;
  }

  @Override
  public String getColumnName (int columnIndex) {
    switch(columnIndex){
      case 0 : return java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("KeyShortCutEditPanel.ColumnName");
      case 1 : return java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("KeyShortCutEditPanel.ColumnKey");
      default: return null;
    }
  }

  @Override
  public Class<?> getColumnClass (int columnIndex) {
    return String.class;
  }

  @Override
  public boolean isCellEditable (int rowIndex, int columnIndex) {
    return false;
  }

  @Override
  public Object getValueAt (int rowIndex, int columnIndex) {
    final KeyShortcut key = this.listOfKeys.get(rowIndex);
    switch(columnIndex){
      case 0 : return Utils.convertCamelCasedToHumanForm(key.getID(),true);
      case 1 : return key.toString();
      default: return null;
    }
  }

  @Override
  public void setValueAt (Object aValue, int rowIndex, int columnIndex) {
  }

  @Override
  public void addTableModelListener (TableModelListener l) {
    this.listeners.add(l);
  }

  @Override
  public void removeTableModelListener (TableModelListener l) {
    this.listeners.remove(l);
  }


}
