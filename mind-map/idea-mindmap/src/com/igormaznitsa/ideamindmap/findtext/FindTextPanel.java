/*
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package com.igormaznitsa.ideamindmap.findtext;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import static com.igormaznitsa.ideamindmap.utils.IdeaUtils.string2pattern;

public final class FindTextPanel extends JBPanel implements FindTextScopeProvider {

  private static final long serialVersionUID = -2286996344502363552L;

  private static final int TEXT_FIELD_WIDTH = 300;

  private static boolean stateCaseSensitive = false;
  private static boolean stateInTopicText = true;
  private static boolean stateInNote = true;
  private static boolean stateInFile = true;
  private static boolean stateInURI = true;
  private final MindMapDocumentEditor documentEditor;
  private JButton buttonNext;
  private JButton buttonPrev;
  private Box.Filler filler1;
  private JLabel labelClose;
  private JLabel labelTitle;
  private JPanel panelButtonsForMap;
  private JTextField textFieldSearchText;
  private JToggleButton toggleButtonCaseSensitive;
  private JToggleButton toggleButtonFile;
  private JToggleButton toggleButtonNote;
  private JToggleButton toggleButtonTopicText;
  private JToggleButton toggleButtonURI;

  public FindTextPanel(@Nonnull final MindMapDocumentEditor documentEditor) {
    super();
    initComponents();

    this.toggleButtonCaseSensitive.setSelected(stateCaseSensitive);
    this.toggleButtonTopicText.setSelected(stateInTopicText);
    this.toggleButtonFile.setSelected(stateInFile);
    this.toggleButtonNote.setSelected(stateInNote);
    this.toggleButtonURI.setSelected(stateInURI);

    final ActionListener stateListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        stateCaseSensitive = toggleButtonCaseSensitive.isSelected();
        stateInTopicText = toggleButtonTopicText.isSelected();
        stateInFile = toggleButtonFile.isSelected();
        stateInNote = toggleButtonNote.isSelected();
        stateInURI = toggleButtonURI.isSelected();
      }
    };

    this.toggleButtonCaseSensitive.addActionListener(stateListener);
    this.toggleButtonFile.addActionListener(stateListener);
    this.toggleButtonNote.addActionListener(stateListener);
    this.toggleButtonTopicText.addActionListener(stateListener);
    this.toggleButtonURI.addActionListener(stateListener);

    this.textFieldSearchText.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, this.textFieldSearchText.getPreferredSize().height));
    this.textFieldSearchText.setMinimumSize(new Dimension(TEXT_FIELD_WIDTH, this.textFieldSearchText.getMinimumSize().height));
    this.textFieldSearchText.setMaximumSize(new Dimension(TEXT_FIELD_WIDTH, this.textFieldSearchText.getMaximumSize().height));

    this.textFieldSearchText.setText(""); //NOI18N

    this.textFieldSearchText.setFocusTraversalPolicy(new FocusTraversalPolicy() {
      @Override
      @Nonnull
      public Component getComponentAfter(@Nonnull final Container aContainer, @Nonnull final Component aComponent) {
        return textFieldSearchText;
      }

      @Override
      @Nonnull
      public Component getComponentBefore(@Nonnull final Container aContainer, @Nonnull final Component aComponent) {
        return textFieldSearchText;
      }

      @Override
      @Nonnull
      public Component getFirstComponent(@Nonnull final Container aContainer) {
        return textFieldSearchText;
      }

      @Override
      @Nonnull
      public Component getLastComponent(@Nonnull final Container aContainer) {
        return textFieldSearchText;
      }

      @Override
      @Nonnull
      public Component getDefaultComponent(@Nonnull final Container aContainer) {
        return textFieldSearchText;
      }
    });

    this.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    this.setVisible(false);
    this.documentEditor = documentEditor;
  }

  public void requestFocus() {
    this.textFieldSearchText.requestFocus();
  }

  @Override
  public boolean toSearchIn(@Nonnull final SearchTextScope scope) {
    switch (scope) {
      case CASE_INSENSETIVE:
        return !this.toggleButtonCaseSensitive.isSelected();
      case IN_TOPIC_NOTES:
        return this.toggleButtonNote.isSelected();
      case IN_TOPIC_TEXT:
        return this.toggleButtonTopicText.isSelected();
      case IN_TOPIC_FILES:
        return this.toggleButtonFile.isSelected();
      case IN_TOPIC_URI:
        return this.toggleButtonURI.isSelected();
      default:
        return false;
    }
  }

  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    labelTitle = new JBLabel();
    textFieldSearchText = new JBTextField();
    buttonPrev = new JButton(AllIcons.FindText.PREV);
    buttonNext = new JButton(AllIcons.FindText.NEXT);
    labelClose = new JBLabel();
    filler1 = new Box.Filler(new java.awt.Dimension(0, 0), new Dimension(0, 0), new Dimension(32767, 0));
    toggleButtonCaseSensitive = new JToggleButton(AllIcons.FindText.CASE_DISABLED);
    toggleButtonCaseSensitive.setSelectedIcon(AllIcons.FindText.CASE);

    panelButtonsForMap = new JBPanel();

    toggleButtonTopicText = new JToggleButton(AllIcons.FindText.TEXT_DISABLED);
    toggleButtonTopicText.setSelectedIcon(AllIcons.FindText.TEXT);

    toggleButtonNote = new JToggleButton(AllIcons.FindText.NOTE_DISABLED);
    toggleButtonNote.setSelectedIcon(AllIcons.FindText.NOTE);

    toggleButtonFile = new JToggleButton(AllIcons.FindText.FILE_DISABLED);
    toggleButtonFile.setSelectedIcon(AllIcons.FindText.FILE);

    toggleButtonURI = new JToggleButton(AllIcons.FindText.URL_DISABLED);
    toggleButtonURI.setSelectedIcon(AllIcons.FindText.URL);

    setLayout(new GridBagLayout());

    labelTitle.setText("<html><b>Find text:</b></html>");
    labelTitle.setFocusable(false);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 10.0;
    gridBagConstraints.insets = new Insets(0, 16, 0, 8);
    add(labelTitle, gridBagConstraints);

    textFieldSearchText.setFocusTraversalPolicyProvider(true);
    textFieldSearchText.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent evt) {
        textFieldSearchTextKeyPressed(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new Insets(0, 0, 0, 16);
    add(textFieldSearchText, gridBagConstraints);

    buttonPrev.setToolTipText("Find previous (SHIFT+ENTER)"); // NOI18N
    buttonPrev.setFocusable(false);
    buttonPrev.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(final ActionEvent evt) {
        buttonPrevActionPerformed(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 10.0;
    add(buttonPrev, gridBagConstraints);

    buttonNext.setToolTipText("Find next (ENTER)"); // NOI18N
    buttonNext.setFocusable(false);
    buttonNext.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonNextActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 10.0;
    gridBagConstraints.insets = new Insets(0, 0, 0, 16);
    add(buttonNext, gridBagConstraints);

    labelClose.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    labelClose.setIcon(AllIcons.FindText.CLOSE);
    labelClose.setToolTipText("Close search form (ESC)"); // NOI18N
    labelClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    labelClose.setFocusable(false);
    labelClose.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        labelCloseMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 7;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.insets = new Insets(0, 0, 0, 8);
    add(labelClose, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 6;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 100000.0;
    add(filler1, gridBagConstraints);

    toggleButtonCaseSensitive.setToolTipText("Case sensitive mode"); // NOI18N
    toggleButtonCaseSensitive.setFocusable(false);
    toggleButtonCaseSensitive.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toggleButtonCaseSensitiveActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new Insets(0, 0, 0, 8);
    add(toggleButtonCaseSensitive, gridBagConstraints);

    panelButtonsForMap.setLayout(new java.awt.GridBagLayout());

    toggleButtonTopicText.setToolTipText("Find in topic title"); // NOI18N
    toggleButtonTopicText.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    panelButtonsForMap.add(toggleButtonTopicText, gridBagConstraints);

    toggleButtonNote.setToolTipText("Find in topic notes"); // NOI18N
    toggleButtonNote.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    panelButtonsForMap.add(toggleButtonNote, gridBagConstraints);

    toggleButtonFile.setToolTipText("Find in file links"); // NOI18N
    toggleButtonFile.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    panelButtonsForMap.add(toggleButtonFile, gridBagConstraints);

    toggleButtonURI.setToolTipText("Find in URI links");
    toggleButtonURI.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    panelButtonsForMap.add(toggleButtonURI, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(panelButtonsForMap, gridBagConstraints);
  }

  private void textFieldSearchTextKeyPressed(java.awt.event.KeyEvent evt) {
    switch (evt.getKeyCode()) {
      case KeyEvent.VK_ESCAPE: {
        this.setVisible(false);
        evt.consume();
      }
      break;
      case KeyEvent.VK_ENTER: {
        if (evt.isShiftDown()) {
          findPrev();
        } else {
          findNext();
        }
        evt.consume();
      }
      break;
    }
  }

  private void findNext() {
    final String text = this.textFieldSearchText.getText();
    if (!text.isEmpty()) {
      this.documentEditor.findNext(string2pattern(text, this.toggleButtonCaseSensitive.isSelected() ? Pattern.UNICODE_CASE : (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)), this);
    }
  }

  private void findPrev() {
    final String text = this.textFieldSearchText.getText();
    if (!text.isEmpty()) {
      this.documentEditor.findPrev(string2pattern(text, this.toggleButtonCaseSensitive.isSelected() ? Pattern.UNICODE_CASE : (Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE)), this);
    }
  }

  private void labelCloseMouseClicked(java.awt.event.MouseEvent evt) {
    this.setVisible(false);
  }

  private void buttonPrevActionPerformed(java.awt.event.ActionEvent evt) {
    findPrev();
  }

  private void buttonNextActionPerformed(java.awt.event.ActionEvent evt) {
    findNext();
  }

  private void toggleButtonCaseSensitiveActionPerformed(java.awt.event.ActionEvent evt) {
    stateCaseSensitive = this.toggleButtonCaseSensitive.isSelected();
  }
}
