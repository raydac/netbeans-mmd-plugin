/*
 * Copyright 2017 Igor Maznitsa.
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
package com.igormaznitsa.mindmap.print;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;

/**
 * Panel to tune parameters of print.
 * 
 * @since 1.4.1
 */
public final class MMDPrintOptionsPanel extends JPanel {

  private static final long serialVersionUID = 4095304247486153265L;

  protected static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/mindmap/swing/panel/Bundle");

  private final MMDPrintOptions options;

  private final JRadioButton radioZoomTo = UIComponentFactoryProvider.findInstance().makeRadioButton();
  private final JRadioButton radioFitWidthTo = UIComponentFactoryProvider.findInstance().makeRadioButton();
  private final JRadioButton radioFitHeightTo = UIComponentFactoryProvider.findInstance().makeRadioButton();
  private final JRadioButton radioFitToPage = UIComponentFactoryProvider.findInstance().makeRadioButton();
  private final JComboBox comboZoom =  UIComponentFactoryProvider.findInstance().makeComboBox();
  private final JSpinner spinnerFitWidth = UIComponentFactoryProvider.findInstance().makeSpinner();
  private final JSpinner spinnerFitHeight = UIComponentFactoryProvider.findInstance().makeSpinner();

  public MMDPrintOptionsPanel(@Nonnull final MMDPrintOptions options) {
    super(new GridBagLayout());
    
    this.radioZoomTo.setText(BUNDLE.getString("MMDPrintOptionsPanel.ZoomTo"));
    this.radioFitWidthTo.setText(BUNDLE.getString("MMDPrintOptionsPanel.FitWithTo"));
    this.radioFitHeightTo.setText(BUNDLE.getString("MMDPrintOptionsPanel.FitHeightTo"));
    this.radioFitToPage.setText(BUNDLE.getString("MMDPrintOptionsPanel.FitToPage"));
    
    this.spinnerFitHeight.setModel(new SpinnerNumberModel(1, 1, 100, 1));
    this.spinnerFitWidth.setModel(new SpinnerNumberModel(1, 1, 100, 1));
    
    final List<String> zoom = new ArrayList<String>();
    for (int i = 25; i <= 500; i += 25) {
      zoom.add(Integer.toString(i) + " %");
    }
    this.comboZoom.setModel(new DefaultComboBoxModel(zoom.toArray()));

    this.options = new MMDPrintOptions(options);

    final GridBagConstraints gbc = new GridBagConstraints();

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    gbc.gridx = 0;
    gbc.gridy = 0;
    final JLabel titleLabel = UIComponentFactoryProvider.findInstance().makeLabel();
    titleLabel.setText(BUNDLE.getString("MMDPrintOptionsPanel.ZoomSectionTitle")+ ' ');
    this.add(titleLabel, gbc);
    gbc.gridx = 1;
    gbc.weightx = 1000;
    this.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
    gbc.weightx = 1;

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.BOTH;
    this.add(makeZoomPanel(), gbc);

    final ButtonGroup radioGroup = new ButtonGroup();
    radioGroup.add(this.radioFitHeightTo);
    radioGroup.add(this.radioFitWidthTo);
    radioGroup.add(this.radioZoomTo);
    radioGroup.add(this.radioFitToPage);

    selectZoomButton();
    enableZoomComponentsForState();

    final ChangeListener zoomChangeListener = new ChangeListener() {
      @Override
      public void stateChanged(@Nonnull ChangeEvent e) {
        fillZoomData();
      }
    };

    final ActionListener zoomActionListener = new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull ActionEvent e) {
        fillZoomData();
      }
    };

    this.radioFitHeightTo.addActionListener(zoomActionListener);
    this.radioFitWidthTo.addActionListener(zoomActionListener);
    this.radioZoomTo.addActionListener(zoomActionListener);
    this.radioFitToPage.addActionListener(zoomActionListener);

    this.comboZoom.addActionListener(zoomActionListener);
    this.spinnerFitHeight.addChangeListener(zoomChangeListener);
    this.spinnerFitWidth.addChangeListener(zoomChangeListener);

    
    this.doLayout();
  }

  @Nonnull
  private JPanel makeZoomPanel() {
    final JPanel result =  UIComponentFactoryProvider.findInstance().makePanel();
    result.setLayout(new GridBagLayout());

    final GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    result.add(this.radioZoomTo, gbc);

    gbc.gridx = 1;
    result.add(this.comboZoom, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 2;
    result.add(this.radioFitToPage, gbc);

    gbc.anchor = GridBagConstraints.EAST;

    gbc.gridwidth = 1;

    gbc.gridx = 2;
    gbc.gridy = 0;
    result.add(Box.createHorizontalStrut(32),gbc);
    gbc.gridy = 1;
    result.add(Box.createHorizontalStrut(32),gbc);
    
    gbc.gridx = 3;
    gbc.gridy = 0;
    result.add(this.radioFitWidthTo, gbc);

    gbc.gridx = 4;
    result.add(this.spinnerFitWidth, gbc);
    gbc.gridx = 5;
    final JLabel page1 =  UIComponentFactoryProvider.findInstance().makeLabel();
    page1.setText(' '+BUNDLE.getString("MMDPrintOptionsPanel.Page_s"));
    result.add(page1, gbc);

    gbc.gridx = 3;
    gbc.gridy = 1;
    result.add(this.radioFitHeightTo, gbc);

    gbc.gridx = 4;
    gbc.gridy = 1;
    result.add(this.spinnerFitHeight, gbc);
    gbc.gridx = 5;
    final JLabel page2 =  UIComponentFactoryProvider.findInstance().makeLabel();
    page2.setText(' '+BUNDLE.getString("MMDPrintOptionsPanel.Page_s"));
    result.add(page2, gbc);

    this.comboZoom.setSelectedIndex(Math.max(0, Math.min(this.comboZoom.getModel().getSize() - 1, ((int) this.options.getScale() * 100) / 25) - 1));
    this.spinnerFitWidth.getModel().setValue(this.options.getPagesInRow());
    this.spinnerFitHeight.getModel().setValue(this.options.getPagesInColumn());

    enableZoomComponentsForState();

    return result;
  }

  private void selectZoomButton() {
    switch (this.options.getScaleType()) {
      case ZOOM:
        this.radioZoomTo.setSelected(true);
        break;
      case FIT_HEIGHT_TO_PAGES:
        this.radioFitHeightTo.setSelected(true);
        break;
      case FIT_WIDTH_TO_PAGES:
        this.radioFitWidthTo.setSelected(true);
        break;
      case FIT_TO_SINGLE_PAGE:
        this.radioFitToPage.setSelected(true);
        break;
    }
  }

  private void fillZoomData() {
    this.options.setPagesInColumn((Integer) this.spinnerFitHeight.getValue());
    this.options.setPagesInRow((Integer) this.spinnerFitWidth.getValue());
    this.options.setScale(((double)(this.comboZoom.getSelectedIndex() + 1) * 25)/100.0d);
    if (this.radioFitHeightTo.isSelected()) {
      this.options.setScaleType(MMDPrintOptions.ScaleType.FIT_HEIGHT_TO_PAGES);
    } else if (this.radioFitToPage.isSelected()) {
      this.options.setScaleType(MMDPrintOptions.ScaleType.FIT_TO_SINGLE_PAGE);
    } else if (this.radioFitWidthTo.isSelected()) {
      this.options.setScaleType(MMDPrintOptions.ScaleType.FIT_WIDTH_TO_PAGES);
    } else if (this.radioZoomTo.isSelected()) {
      this.options.setScaleType(MMDPrintOptions.ScaleType.ZOOM);
    }
    enableZoomComponentsForState();
  }

  private void enableZoomComponentsForState() {
    this.comboZoom.setEnabled(false);
    this.spinnerFitHeight.setEnabled(false);
    this.spinnerFitWidth.setEnabled(false);

    switch (this.options.getScaleType()) {
      case ZOOM:
        this.comboZoom.setEnabled(true);
        break;
      case FIT_HEIGHT_TO_PAGES:
        this.spinnerFitHeight.setEnabled(true);
        break;
      case FIT_WIDTH_TO_PAGES:
        this.spinnerFitWidth.setEnabled(true);
        break;
      case FIT_TO_SINGLE_PAGE:
        break;
      default:
        throw new Error("Unexpected state");
    }
  }

  @Nonnull
  public MMDPrintOptions getOptions() {
    return this.options;
  }

}
