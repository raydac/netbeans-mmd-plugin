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

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.ide.commons.SwingUtils;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.HasPreferredFocusComponent;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class UriEditPanel extends javax.swing.JPanel implements HasPreferredFocusComponent {
  private static final Logger LOGGER = LoggerFactory.getLogger(UriEditPanel.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");

  private static final UIComponentFactory UI_COMPO_FACTORY = UIComponentFactoryProvider.findInstance();

  private static final long serialVersionUID = -6683682013891751388L;
  private javax.swing.JLabel labelBrowseCurrentLink;
  private javax.swing.JLabel labelValidator;
  private javax.swing.JTextField textFieldURI;

  public UriEditPanel(final String uri) {
    initComponents();

    this.textFieldURI.setText(uri == null ? "" : uri);

    this.textFieldURI.getDocument().addDocumentListener(new DocumentListener() {

      @Override
      public void insertUpdate(DocumentEvent e) {
        validateUri();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        validateUri();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        validateUri();
      }
    });

    validateUri();
  }

  @Override
  public JComponent getComponentPreferredForFocus() {
    return this.textFieldURI;
  }

  public String getText() {
    return this.textFieldURI.getText().trim();
  }

  private void validateUri() {
    final String text = this.textFieldURI.getText().trim();
    this.labelValidator.setText("");
    if (text.isEmpty()) {
      this.labelValidator.setIcon(AllIcons.Buttons.QUESTION);
    } else {
      this.labelValidator.setIcon(Utils.isUriCorrect(text) ? AllIcons.Buttons.TICK : AllIcons.Buttons.CANCEL);
    }
  }

  @SuppressWarnings("unchecked")
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    labelBrowseCurrentLink = UI_COMPO_FACTORY.makeLabel();
    textFieldURI = UI_COMPO_FACTORY.makeTextField();
    textFieldURI.setComponentPopupMenu(SwingUtils.addTextActions(UI_COMPO_FACTORY.makePopupMenu()));
    labelValidator = UI_COMPO_FACTORY.makeLabel();

    setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setLayout(new java.awt.GridBagLayout());

    labelBrowseCurrentLink.setIcon(AllIcons.Buttons.URL_LINK_BIG);
    java.util.ResourceBundle bundle = BUNDLE;
    labelBrowseCurrentLink.setToolTipText(bundle.getString("UriEditPanel.labelBrowseCurrentLink.toolTipText")); // NOI18N
    labelBrowseCurrentLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    labelBrowseCurrentLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    labelBrowseCurrentLink.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        labelBrowseCurrentLinkMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 10;
    add(labelBrowseCurrentLink, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1000.0;
    add(textFieldURI, gridBagConstraints);

    labelValidator.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    labelValidator.setIcon(AllIcons.Buttons.QUESTION);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 10;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    add(labelValidator, gridBagConstraints);

    final JButton resetButton = new JButton(AllIcons.Buttons.CROSS);
    gridBagConstraints.ipadx = 0;
    add(resetButton, gridBagConstraints);

    resetButton.setFocusable(false);

    resetButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        textFieldURI.setText("");
      }
    });
  }

  private void labelBrowseCurrentLinkMouseClicked(java.awt.event.MouseEvent evt) {
    if (evt.getClickCount() > 1) {
      try {
        IdeaUtils.browseURI(new URI(this.getText().trim()), false);
      } catch (URISyntaxException ex) {
        LOGGER.error("Can't start browser for URI syntax error", ex);
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }
}
