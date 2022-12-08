/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.swing.services;

import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.BooleanParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.DoubleParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.FileParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.IntegerParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.StringParameter;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;

public class DefaultParametersPanelFactory extends JPanel {

  private static final DefaultParametersPanelFactory INSTANCE = new DefaultParametersPanelFactory();

  public static DefaultParametersPanelFactory getInstance() {
    return INSTANCE;
  }

  public JPanel make(final DialogProvider dialogProvider,
                     final Set<AbstractParameter<?>> parameters) {
    return this.makeParametersPanelFactory(UIComponentFactoryProvider.findInstance(),
        dialogProvider, parameters);
  }

  private JPanel makeParametersPanelFactory(final UIComponentFactory uiComponentFactory,
                                            final DialogProvider dialogProvider,
                                            final Set<AbstractParameter<?>> parameters) {
    final JPanel panel = uiComponentFactory.makePanel();
    panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    panel.setLayout(new GridBagLayout());

    final AtomicInteger layoutY = new AtomicInteger(0);

    parameters.stream()
        .sorted()
        .forEach(p -> {
          final JLabel label = uiComponentFactory.makeLabel();
          label.setText(p.getTitle() + ":");
          final JComponent changer;
          if (p instanceof IntegerParameter) {
            final IntegerParameter integerParameter = (IntegerParameter) p;
            changer = uiComponentFactory.makeSpinner();
            final JSpinner spinner = (JSpinner) changer;
            spinner.setModel(new SpinnerNumberModel((Number) integerParameter.getValue(),
                integerParameter.getMin(), integerParameter.getMax(), 1L));
            spinner.addChangeListener(x -> integerParameter.setValue(
                ((Number) spinner.getModel().getValue()).longValue()));
          } else if (p instanceof BooleanParameter) {
            final BooleanParameter booleanParameter = (BooleanParameter) p;
            changer = uiComponentFactory.makeCheckBox();
            final JCheckBox checkBox = (JCheckBox) changer;
            checkBox.setSelected(booleanParameter.getValue());
            checkBox.addChangeListener(x -> booleanParameter.setValue(checkBox.isSelected()));
          } else if (p instanceof FileParameter) {
            final FileParameter fileParameter = (FileParameter) p;
            changer = new FileChooserCombo(uiComponentFactory, fileParameter,
                dialogProvider).asComponent();
          } else if (p instanceof StringParameter) {
            final StringParameter stringParameter = (StringParameter) p;
            changer = uiComponentFactory.makeTextField();
            final JTextField textField = (JTextField) changer;
            textField.setColumns(16);
            textField.setText(stringParameter.getValue());
            textField.getDocument().addDocumentListener(new DocumentListener() {
              private String getText(final Document document) {
                try {
                  return document.getText(0, document.getLength());
                } catch (Exception ex) {
                  throw new RuntimeException(ex);
                }
              }

              @Override
              public void insertUpdate(DocumentEvent documentEvent) {
                stringParameter.setValue(this.getText(documentEvent.getDocument()));
              }

              @Override
              public void removeUpdate(DocumentEvent documentEvent) {
                stringParameter.setValue(this.getText(documentEvent.getDocument()));
              }

              @Override
              public void changedUpdate(DocumentEvent documentEvent) {
                stringParameter.setValue(this.getText(documentEvent.getDocument()));
              }
            });
          } else if (p instanceof DoubleParameter) {
            final DoubleParameter doubleParameter = (DoubleParameter) p;
            changer = uiComponentFactory.makeSpinner();
            final JSpinner spinner = (JSpinner) changer;
            spinner.setModel(new SpinnerNumberModel((Number) doubleParameter.getValue(),
                doubleParameter.getMin(), doubleParameter.getMax(), 0.1d));
            spinner.addChangeListener(x -> doubleParameter.setValue(
                ((Number) spinner.getModel().getValue()).doubleValue()));
          } else {
            throw new Error("Unsupported parameter type: " + p.getClass().getName());
          }
          changer.putClientProperty("mmd.parameter.id", p.getId());
          label.setToolTipText(p.getComment());
          changer.setToolTipText(p.getComment());

          final GridBagConstraints constraints = new GridBagConstraints();
          constraints.insets = new Insets(0, 4, 8, 4);

          constraints.gridy = layoutY.getAndIncrement();
          constraints.gridx = 0;
          constraints.anchor = GridBagConstraints.EAST;
          panel.add(label, constraints);

          constraints.gridx = 1;
          constraints.anchor = GridBagConstraints.WEST;
          panel.add(changer, constraints);

        });

    return panel;
  }

  private static final class FileChooserCombo {
    private final JPanel panel;
    private final JTextField textField;
    private final JButton buttonReset;
    private final JButton buttonSelect;

    private final FileParameter parameter;

    private FileChooserCombo(
        final UIComponentFactory uiComponentFactory,
        final FileParameter fileParameter,
        final DialogProvider dialogProvider) {
      this.parameter = fileParameter;

      this.panel = uiComponentFactory.makePanel();
      this.panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
      this.buttonReset = uiComponentFactory.makeButton();
      this.buttonReset.setText("X");
      this.buttonReset.setToolTipText(MmdI18n.getInstance().findBundle().getString("DefaultParametersPanelFactory.menuItem.buttonResetValue.tooltip"));

      this.buttonSelect = uiComponentFactory.makeButton();
      this.buttonSelect.setText("...");
      this.buttonSelect.setToolTipText(MmdI18n.getInstance().findBundle().getString("DefaultParametersPanelFactory.menuItem.buttonSelectFile.tooltip"));

      this.textField = uiComponentFactory.makeTextField();
      this.textField.setColumns(16);

      this.panel.add(this.textField);
      this.panel.add(this.buttonReset);
      this.panel.add(this.buttonSelect);

      this.textField.setText(
          fileParameter.getValue() == null ? "" : fileParameter.getValue().getAbsolutePath());

      this.textField.getDocument().addDocumentListener(new DocumentListener() {
        private void updateFileForDocument(final Document document) {
          try {
            parameter.setValue(new File(document.getText(0, document.getLength()).trim()));
          }catch (Exception ex){
            // ignore
          }
        }

        @Override
        public void insertUpdate(final DocumentEvent documentEvent) {
          this.updateFileForDocument(documentEvent.getDocument());
        }

        @Override
        public void removeUpdate(final DocumentEvent documentEvent) {
          this.updateFileForDocument(documentEvent.getDocument());
        }

        @Override
        public void changedUpdate(final DocumentEvent documentEvent) {
          this.updateFileForDocument(documentEvent.getDocument());
        }
      });

      this.buttonReset.addActionListener(x -> {
        this.textField.setText("");
        this.parameter.setValue(null);
      });

      this.buttonSelect.addActionListener(e -> {
        final File file = dialogProvider.msgOpenFileDialog(
            this.panel,
            null,
            "pngexporter.filechooser.preferences.file",
            fileParameter.getFileChooserParamsProvider().getTitle(),
            this.parameter.getValue(),
            fileParameter.getFileChooserParamsProvider().isFilesOnly(),
            Stream.of(fileParameter.getFileChooserParamsProvider().getFileFilters())
                .map(x -> new FileFilter() {
                  @Override
                  public boolean accept(File file) {
                    return x.accept(file);
                  }

                  @Override
                  public String getDescription() {
                    return x.toString();
                  }
                })
                .toArray(FileFilter[]::new),
            fileParameter.getFileChooserParamsProvider().getApproveText()
        );
        if (file != null) {
          this.parameter.setValue(file);
          this.textField.setText(file.getAbsolutePath());
        }
      });
    }

    public JComponent asComponent() {
      return this.panel;
    }
  }
}
