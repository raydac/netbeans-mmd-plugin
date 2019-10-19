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
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import java.awt.Color;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JButton;
import javax.swing.JPanel;

public class ColorAttributePanel extends JPanel {
  private static final long serialVersionUID = -3436912869455827455L;

  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");
  private final DialogProvider dialogProvider;
  private JButton buttonResetBorder;
  private JButton buttonResetFill;
  private JButton buttonResetText;
  private ColorChooserButton colorChooserBorder;
  private ColorChooserButton colorChooserFill;
  private ColorChooserButton colorChooserText;

  public ColorAttributePanel(@Nonnull final MindMap map,
                             @Nonnull final DialogProvider dialogProvider,
                             @Nullable final Color border,
                             @Nullable final Color fill,
                             @Nullable final Color text
  ) {
    this.dialogProvider = dialogProvider;
    initComponents();
    this.colorChooserBorder.setValue(border);
    this.colorChooserFill.setValue(fill);
    this.colorChooserText.setValue(text);

    this.colorChooserBorder.setUsedColors(MindMapUtils.findAllTopicColors(map, MindMapUtils.ColorType.BORDER));
    this.colorChooserFill.setUsedColors(MindMapUtils.findAllTopicColors(map, MindMapUtils.ColorType.FILL));
    this.colorChooserText.setUsedColors(MindMapUtils.findAllTopicColors(map, MindMapUtils.ColorType.TEXT));
  }

  public Result getResult() {
    return new Result(
        this.colorChooserBorder.getValue(),
        this.colorChooserText.getValue(),
        this.colorChooserFill.getValue());
  }

  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  private void initComponents() {

    colorChooserBorder = new ColorChooserButton(this.dialogProvider);
    colorChooserFill = new ColorChooserButton(this.dialogProvider);
    colorChooserText = new ColorChooserButton(this.dialogProvider);
    buttonResetBorder = new javax.swing.JButton();
    buttonResetFill = new javax.swing.JButton();
    buttonResetText = new javax.swing.JButton();

    java.util.ResourceBundle bundle = BUNDLE;
    colorChooserBorder.setText(bundle.getString("ColorAttributePanel.colorChooserBorder.text"));
    colorChooserBorder.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

    colorChooserFill.setText(bundle.getString("ColorAttributePanel.colorChooserFill.text"));
    colorChooserFill.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

    colorChooserText.setText(bundle.getString("ColorAttributePanel.colorChooserText.text"));
    colorChooserText.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

    buttonResetBorder.setIcon(AllIcons.Buttons.CROSS);
    buttonResetBorder.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonResetBorderActionPerformed(evt);
      }
    });

    buttonResetFill.setIcon(AllIcons.Buttons.CROSS);
    buttonResetFill.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonResetFillActionPerformed(evt);
      }
    });

    buttonResetText.setIcon(AllIcons.Buttons.CROSS);
    buttonResetText.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonResetTextActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(colorChooserText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(colorChooserFill, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(colorChooserBorder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonResetBorder, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonResetFill, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(buttonResetText, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonResetBorder)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colorChooserBorder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(colorChooserFill, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonResetFill))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonResetText)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colorChooserText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>

  private void buttonResetBorderActionPerformed(java.awt.event.ActionEvent evt) {
    this.colorChooserBorder.setValue(null);
  }

  private void buttonResetFillActionPerformed(java.awt.event.ActionEvent evt) {
    this.colorChooserFill.setValue(null);
  }

  private void buttonResetTextActionPerformed(java.awt.event.ActionEvent evt) {
    this.colorChooserText.setValue(null);
  }

  public static class Result {
    private final Color borderColor;
    private final Color textColor;
    private final Color fillColor;

    private Result(final Color border, final Color text, final Color fill) {
      this.borderColor = border;
      this.textColor = text;
      this.fillColor = fill;
    }

    public Color getTextColor() {
      return this.textColor;
    }

    public Color getFillColor() {
      return this.fillColor;
    }

    public Color getBorderColor() {
      return this.borderColor;
    }
  }
}
