package com.igormaznitsa.mindmap.ide.commons.editors;

import com.igormaznitsa.mindmap.ide.commons.preferences.ColorSelectButton;
import com.igormaznitsa.mindmap.ide.commons.preferences.MmcI18n;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Panel allows select colors for topic draw elements.
 *
 * @since 1.6.2
 */
public final class ColorAttributePanel {

  private final JPanel panel;
  private final ColorSelectButton buttonBorder;
  private final ColorSelectButton buttonText;
  private final ColorSelectButton buttonFill;

  /**
   * Constructor.
   *
   * @param componentFactory    component factory, can't be null
   * @param dialogProvider      dialog provider, can't be null
   * @param map                 base mind map, can't be null
   * @param colorBorder         topic border color value, can be null
   * @param colorFill           topic fill color value, can be null
   * @param colorText           topic text color value, can be null
   * @param iconReset           reset icon for reset buttons, can be null
   * @param buttonPreprocessors array of preprocessing functions to be apply to any button before add to panel, boolean argument shows that color select button if true, reset button if false
   */
  @SafeVarargs
  public ColorAttributePanel(
      final UIComponentFactory componentFactory,
      final DialogProvider dialogProvider,
      final MindMap map,
      final Color colorBorder,
      final Color colorFill,
      final Color colorText,
      final Icon iconReset,
      final BiFunction<JButton, Boolean, JButton>... buttonPreprocessors
  ) {
    final ResourceBundle bundle = MmcI18n.getInstance().findBundle();

    this.panel = componentFactory.makePanel();
    this.buttonBorder = new ColorSelectButton(this.panel, componentFactory, dialogProvider);
    this.buttonBorder.setText(bundle.getString("panelColorAttribute.buttonBorderColor"));

    this.buttonText = new ColorSelectButton(this.panel, componentFactory, dialogProvider);
    this.buttonText.setText(bundle.getString("panelColorAttribute.buttonTextColor"));

    this.buttonFill = new ColorSelectButton(this.panel, componentFactory, dialogProvider);
    this.buttonFill.setText(bundle.getString("panelColorAttribute.buttonFillColor"));

    final String textResetTooltip = bundle.getString("panelColorAttribute.buttonResetValue");

    final JButton buttonResetBorder = componentFactory.makeButton();
    buttonResetBorder.setToolTipText(textResetTooltip);
    final JButton buttonResetText = componentFactory.makeButton();
    buttonResetText.setToolTipText(textResetTooltip);
    final JButton buttonResetFill = componentFactory.makeButton();
    buttonResetFill.setToolTipText(textResetTooltip);

    buttonResetBorder.setAlignmentX(JButton.CENTER_ALIGNMENT);
    buttonResetText.setAlignmentX(JButton.CENTER_ALIGNMENT);
    buttonResetFill.setAlignmentX(JButton.CENTER_ALIGNMENT);

    buttonResetBorder.addActionListener(a -> this.buttonBorder.setValue(null));
    buttonResetText.addActionListener(a -> this.buttonText.setValue(null));
    buttonResetFill.addActionListener(a -> this.buttonFill.setValue(null));

    if (iconReset == null) {
      buttonResetText.setText("X");
      buttonResetFill.setText("X");
      buttonResetBorder.setText("X");
    } else {
      buttonResetBorder.setIcon(iconReset);
      buttonResetText.setIcon(iconReset);
      buttonResetFill.setIcon(iconReset);
    }

    this.buttonBorder.setValue(colorBorder);
    this.buttonBorder.setUsedColors(
        MindMapUtils.findAllTopicColors(map, MindMapUtils.ColorType.BORDER));

    this.buttonFill.setValue(colorFill);
    this.buttonFill.setUsedColors(
        MindMapUtils.findAllTopicColors(map, MindMapUtils.ColorType.FILL));

    this.buttonText.setValue(colorText);
    this.buttonText.setUsedColors(
        MindMapUtils.findAllTopicColors(map, MindMapUtils.ColorType.TEXT));

    this.panel.setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();

    constraints.fill = GridBagConstraints.BOTH;
    constraints.weightx = 1;
    constraints.insets = new Insets(8, 8, 8, 8);

    constraints.gridy = 0;
    this.panel.add(preprocess(this.buttonFill.asButton(), true, buttonPreprocessors), constraints);
    this.panel.add(preprocess(buttonResetFill, false, buttonPreprocessors), constraints);

    constraints.gridy = 1;
    this.panel.add(preprocess(this.buttonText.asButton(), true, buttonPreprocessors), constraints);
    this.panel.add(preprocess(buttonResetText, false, buttonPreprocessors), constraints);

    constraints.gridy = 2;
    this.panel.add(preprocess(this.buttonBorder.asButton(), true, buttonPreprocessors),
        constraints);
    this.panel.add(preprocess(buttonResetBorder, false, buttonPreprocessors), constraints);
  }

  private JButton preprocess(final JButton button, final boolean colorChooseButton,
                             final BiFunction<JButton, Boolean, JButton>... colorButtonPreprocessors) {
    JButton result = button;
    for (final BiFunction<JButton, Boolean, JButton> p : colorButtonPreprocessors) {
      if (p != null) {
        result = p.apply(result, colorChooseButton);
      }
    }
    return result;
  }

  public JPanel getPanel() {
    return this.panel;
  }

  public Result getResult() {
    return new Result(
        this.buttonBorder.getValue(),
        this.buttonText.getValue(),
        this.buttonFill.getValue());
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
