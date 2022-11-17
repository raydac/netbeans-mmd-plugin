package com.igormaznitsa.mindmap.swing.services;

import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.BooleanParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.DoubleParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.IntegerParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.StringParameter;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class DefaultParametersPanelFactory extends JPanel {

  private static final DefaultParametersPanelFactory INSTANCE = new DefaultParametersPanelFactory();

  public static DefaultParametersPanelFactory getInstance() {
    return INSTANCE;
  }

  public JPanel make(final Set<AbstractParameter<?>> parameters) {
    return this.makeParametersPanelFactory(UIComponentFactoryProvider.findInstance(), parameters);
  }

  private JPanel makeParametersPanelFactory(final UIComponentFactory uiComponentFactory,
                                            final Set<AbstractParameter<?>> parameters) {
    final JPanel panel = uiComponentFactory.makePanel();
    panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    panel.setLayout(new GridBagLayout());

    final AtomicInteger layoutY = new AtomicInteger(0);

    parameters.stream()
        .sorted(Comparator.comparing(AbstractParameter::getId))
        .forEach(p -> {
          final JLabel label = uiComponentFactory.makeLabel();
          label.setText(p.getTitle());
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
}
