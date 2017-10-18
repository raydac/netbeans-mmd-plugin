package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.plugins.api.HasOptions;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.JBCheckboxMenuItem;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.components.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

public class IdeaUIComponentFactory implements UIComponentFactory {

    private static class JOptionablePanel extends JBPanel implements HasOptions {

        private static final long serialVersionUID = 7315573532005732183L;
        private final HasOptions optionsProcessor;

        private JOptionablePanel(@Nonnull final HasOptions optionsProcessor) {
            super();
            this.optionsProcessor = optionsProcessor;
        }

        @Override
        public boolean doesSupportKey(@Nonnull final String key) {
            return this.optionsProcessor.doesSupportKey(key);
        }

        @Override
        @Nonnull
        @MustNotContainNull
        public String[] getOptionKeys() {
            return this.optionsProcessor.getOptionKeys();
        }

        @Override
        @Nonnull
        public String getOptionKeyDescription(@Nonnull final String key) {
            return this.optionsProcessor.getOptionKeyDescription(key);
        }

        @Override
        public void setOption(@Nonnull final String key, @Nullable final String value) {
            this.optionsProcessor.setOption(key, value);
        }

        @Override
        @Nullable
        public String getOption(@Nonnull final String key) {
            return this.optionsProcessor.getOption(key);
        }
    }


    @Override
    @Nonnull
    public JPanel makePanel() {
        return new JBPanel();
    }

    @Nonnull
    @Override
    public JPanel makePanelWithOptions(@Nonnull HasOptions hasOptions) {
        return new JOptionablePanel(hasOptions);
    }

    @Nonnull
    @Override
    public JToggleButton makeToggleButton() {
        return new JToggleButton();
    }

    @Nonnull
    @Override
    public JRadioButton makeRadioButton() {
        return new JBRadioButton();
    }

    @Override
    @Nonnull
    public JComboBox makeComboBox() {
        return new ComboBox();
    }

    @Override
    @Nonnull
    public JButton makeButton() {
        return new JButton();
    }

    @Override
    @Nonnull
    public JToolBar makeToolBar() {
        return new JToolBar();
    }

    @Override
    @Nonnull
    public JScrollPane makeScrollPane() {
        return new JBScrollPane();
    }

    @Override
    @Nonnull
    public JCheckBox makeCheckBox() {
        return new JBCheckBox();
    }

    @Override
    @Nonnull
    public JLabel makeLabel() {
        return new JBLabel();
    }

    @Override
    @Nonnull
    public JPopupMenu makePopupMenu() {
        return new JBPopupMenu();
    }

    @Override
    @Nonnull
    public JTextArea makeTextArea() {
        return new JTextArea();
    }

    @Override
    @Nonnull
    public JSpinner makeSpinner() {
        return new JSpinner();
    }

    @Override
    @Nonnull
    public JEditorPane makeEditorPane() {
        return new JEditorPane();
    }

    @Override
    @Nonnull
    public JMenuItem makeMenuItem(@Nonnull final String s, final Icon icon) {
        return new JBMenuItem(s, icon);
    }

    @Override
    @Nonnull
    public JCheckBoxMenuItem makeCheckboxMenuItem(@Nonnull final String s, final Icon icon, final boolean b) {
        return new JBCheckboxMenuItem(s, icon, b);
    }

    @Override
    @Nonnull
    public JSeparator makeMenuSeparator() {
        return new JSeparator();
    }

    @Override
    @Nonnull
    public JMenu makeMenu(@Nonnull final String s) {
        return new JMenu(s);
    }

    @Override
    @Nonnull
    public JSlider makeSlider() {
        return new JSlider();
    }

    @Nonnull
    @Override
    public JTextField makeTextField() {
        return new JBTextField();
    }
}
