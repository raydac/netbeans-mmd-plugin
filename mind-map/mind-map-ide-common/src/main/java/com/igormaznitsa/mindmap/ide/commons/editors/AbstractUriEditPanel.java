/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.igormaznitsa.mindmap.ide.commons.editors;

import static javax.swing.BorderFactory.createEmptyBorder;

import com.igormaznitsa.mindmap.ide.commons.SwingUtils;
import com.igormaznitsa.mindmap.ide.commons.preferences.MmcI18n;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.HasPreferredFocusComponent;
import com.igormaznitsa.mindmap.swing.panel.utils.Focuser;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class AbstractUriEditPanel implements HasPreferredFocusComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUriEditPanel.class);

  private static final Map<IconId, Icon> ICON_CACHE = new EnumMap<>(IconId.class);

  protected final UIComponentFactory uiComponentFactory;
  private final ResourceBundle resourceBundle;
  private JButton butonReset;
  private JLabel labelBrowseCurrentLink;
  private JLabel labelValidator;
  private JTextField textFieldURI;
  private JPanel panel;

  private final boolean preferInternalBrowser;

  public AbstractUriEditPanel(final UIComponentFactory uiComponentFactory, final String uri, final boolean prefeeInternalBrowser) {
    this.preferInternalBrowser = prefeeInternalBrowser;
    this.resourceBundle = MmcI18n.getInstance().findBundle();
    this.uiComponentFactory = uiComponentFactory;
    initComponents();

    this.textFieldURI.setText(uri == null ? "" : uri);
    this.textFieldURI.setComponentPopupMenu(
        SwingUtils.addTextActions(uiComponentFactory.makePopupMenu()));

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

    new Focuser(this.textFieldURI);

    validateUri();
  }

  public JPanel getPanel() {
    return this.panel;
  }

  @Override
  public JComponent getComponentPreferredForFocus() {
    return this.panel;
  }

  private Icon getIcon(final IconId id) {
    return ICON_CACHE.computeIfAbsent(id, this::findIcon);
  }

  public Icon findIcon(final IconId id) {
    return null;
  }

  public String getText() {
    return this.textFieldURI.getText().trim();
  }

  private void validateUri() {
    final String text = this.textFieldURI.getText().trim();
    this.labelValidator.setText("");
    if (text.isEmpty()) {
      this.labelValidator.setIcon(this.getIcon(IconId.INDICATOR_URI_UNKNOWN));
    } else {
      this.labelValidator.setIcon(
          Utils.isUriCorrect(text) ? this.getIcon(IconId.INDICATOR_URI_OK) :
              this.getIcon(IconId.INDICATOR_URI_BAD));
    }
  }

  private void initComponents() {
    this.panel = this.uiComponentFactory.makePanel();

    GridBagConstraints gridBagConstraints;

    this.labelBrowseCurrentLink = this.uiComponentFactory.makeLabel();
    this.textFieldURI = this.uiComponentFactory.makeTextField();
    this.textFieldURI.setColumns(24);

    this.labelValidator = this.uiComponentFactory.makeLabel();
    this.butonReset = this.uiComponentFactory.makeButton();

    this.panel.setBorder(createEmptyBorder(10, 10, 10, 10));
    this.panel.setLayout(new GridBagLayout());

    this.labelBrowseCurrentLink.setIcon(this.getIcon(IconId.BROWSE_LINK));
    labelBrowseCurrentLink.setToolTipText(
        this.resourceBundle.getString("panelUriEditor.labelBrowseCurrentLink.toolTipText"));
    labelBrowseCurrentLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    labelBrowseCurrentLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    labelBrowseCurrentLink.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        labelBrowseCurrentLinkMouseClicked(evt);
      }
    });
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 10;
    this.panel.add(labelBrowseCurrentLink, gridBagConstraints);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1000.0;
    this.panel.add(textFieldURI, gridBagConstraints);

    labelValidator.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    labelValidator.setIcon(this.getIcon(IconId.INDICATOR_URI_UNKNOWN));
    labelValidator.setToolTipText(
        this.resourceBundle.getString("panelUriEditor.tooltipLabelValidator"));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 10;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    this.panel.add(labelValidator, gridBagConstraints);

    butonReset.setIcon(this.getIcon(IconId.BUTTON_RESET));
    butonReset.setToolTipText(this.resourceBundle.getString("panelUriEditor.tooltipButtonReset"));
    butonReset.setFocusable(false);
    butonReset.addActionListener(e -> this.textFieldURI.setText(""));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    this.panel.add(butonReset, gridBagConstraints);
  }

  public abstract void browseUri(URI uri, boolean preferInternalBrowser);

  private void labelBrowseCurrentLinkMouseClicked(
      java.awt.event.MouseEvent evt) {
    if (evt.getClickCount() > 1) {
      try {
        this.browseUri(new URI(this.getText().trim()), this.preferInternalBrowser);
      } catch (URISyntaxException ex) {
        LOGGER.error("Can't start browser for URI syntax error", ex);
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  public enum IconId {
    INDICATOR_URI_OK,
    INDICATOR_URI_BAD,
    INDICATOR_URI_UNKNOWN,
    BROWSE_LINK,
    BUTTON_RESET
  }
}
