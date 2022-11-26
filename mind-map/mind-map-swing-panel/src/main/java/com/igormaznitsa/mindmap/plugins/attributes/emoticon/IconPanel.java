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

package com.igormaznitsa.mindmap.plugins.attributes.emoticon;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.utils.Focuser;
import com.igormaznitsa.mindmap.swing.panel.utils.MiscIcons;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalToggleButtonUI;
import javax.swing.text.BadLocationException;

public final class IconPanel extends JPanel {

  public static final String ICON_EMPTY = "empty";
  private static final long serialVersionUID = 4823626757838675154L;
  private static final BufferedImage EMPTY_ICON =
      new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
  private final ButtonGroup group = Utils.UI_COMPO_FACTORY.makeButtonGroup();
  private final JPanel iconPanel;
  private final JTextField textField;
  private final JScrollPane scrollPane;

  public IconPanel() {
    super(new BorderLayout());

    this.textField = Utils.UI_COMPO_FACTORY.makeTextField();
    this.textField.setToolTipText(
        MmdI18n.getInstance().findBundle().getString("Emoticons.panelTextField.tooltip"));
    this.textField.getDocument().addDocumentListener(new DocumentListener() {
      private void updateForEvent(final DocumentEvent event) {
        try {
          fillForName(
              event.getDocument().getText(0, event.getDocument().getLength()));
        } catch (BadLocationException ex) {
          fillForName("");
        }
      }

      @Override
      public void insertUpdate(final DocumentEvent documentEvent) {
        this.updateForEvent(documentEvent);
      }

      @Override
      public void removeUpdate(final DocumentEvent documentEvent) {
        this.updateForEvent(documentEvent);
      }

      @Override
      public void changedUpdate(final DocumentEvent documentEvent) {
        this.updateForEvent(documentEvent);
      }
    });

    this.iconPanel = Utils.UI_COMPO_FACTORY.makePanel();
    this.scrollPane = Utils.UI_COMPO_FACTORY.makeScrollPane();
    this.scrollPane.setViewportView(this.iconPanel);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(32);
    this.scrollPane.getVerticalScrollBar().setBlockIncrement(96);
    this.scrollPane.setPreferredSize(new Dimension(512, 400));

    this.add(this.textField, BorderLayout.NORTH);
    this.add(this.scrollPane, BorderLayout.CENTER);

    this.fillForName("");

    new Focuser(this.textField);
  }

  private boolean anyDifference(final List<String> names) {
    if (this.group.getButtonCount() != names.size()) {
      return true;
    }
    final Enumeration<AbstractButton> buttonEnumeration = this.group.getElements();
    int index = 0;
    while (buttonEnumeration.hasMoreElements()) {
      if (!buttonEnumeration.nextElement().getName().equals(names.get(index++))) {
        return true;
      }
    }
    return false;
  }

  private void fillForName(final String name) {
    final String normalized = name.trim().toLowerCase(Locale.ENGLISH);

    final List<String> newSet =
        Stream.of(MiscIcons.getNames())
            .sorted()
            .filter(x -> normalized.isEmpty() || x.contains(normalized))
            .collect(Collectors.toCollection(ArrayList::new));

    int emptyNumber = MiscIcons.getNames().length - newSet.size();
    while (emptyNumber > 0) {
      newSet.add(ICON_EMPTY);
      emptyNumber--;
    }

    newSet.add(0, ICON_EMPTY);

    if (anyDifference(newSet)) {
      final List<AbstractButton> allButtons = new ArrayList<>();
      final Enumeration<AbstractButton> buttonsEnum = this.group.getElements();
      while (buttonsEnum.hasMoreElements()) {
        allButtons.add(buttonsEnum.nextElement());
      }
      allButtons.forEach(this.group::remove);
      this.group.clearSelection();
      this.iconPanel.removeAll();
      this.iconPanel.setLayout(new GridLayout(0, 6));

      newSet.forEach(x -> this.iconPanel.add(makeIconButton(this.group, x)));

      this.scrollPane.getVerticalScrollBar().setValue(0);
      this.scrollPane.getHorizontalScrollBar().setValue(0);
      this.scrollPane.revalidate();
      this.scrollPane.repaint();
    }
  }

  public String getSelectedName() {
    final Enumeration<AbstractButton> iterator = this.group.getElements();
    while (iterator.hasMoreElements()) {
      final JToggleButton button = (JToggleButton) iterator.nextElement();
      if (button.isSelected()) {
        return button.getName();
      }
    }
    return null;
  }

  private JToggleButton makeIconButton(final ButtonGroup group,
                                       final String name) {
    final JToggleButton result = Utils.UI_COMPO_FACTORY.makeToggleButton();
    final Color panelColor = this.getBackground();

    result.setUI(new MetalToggleButtonUI() {
      @Override
      protected Color getSelectColor() {
        return panelColor.brighter();
      }
    });

    result.setBackground(panelColor.darker());

    result.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
        BorderFactory.createEmptyBorder(3, 3, 3, 3)));
    if (name.equals(ICON_EMPTY)) {
      result.setIcon(new ImageIcon(EMPTY_ICON));
    } else {
      result.setIcon(new ImageIcon(requireNonNull(MiscIcons.findForName(name))));
    }
    result.setName(name);
    result.setFocusPainted(false);
    result.setToolTipText(name);

    group.add(result);
    return result;
  }
}
