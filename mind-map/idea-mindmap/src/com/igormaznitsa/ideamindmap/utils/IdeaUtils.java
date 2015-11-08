package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.ideamindmap.swing.PlainTextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public enum IdeaUtils {
;

    private static class DialogComponent extends DialogWrapper {
        private final JComponent component;

        public DialogComponent(final Project project, final String title, final JComponent component){
            super(project, false, IdeModalityType.PROJECT);
            this.component = component;
            init();
            setTitle(title);
            getRootPane().setDefaultButton(null);
        }

        @Nullable
        @Override
        public JComponent getPreferredFocusedComponent() {
            return this.component;
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return this.component;
        }
    }

    public static boolean msgConfirmYesNo(final Project project, final String title, final String query) {
        return Messages.showYesNoDialog(project,query,title,Messages.getQuestionIcon()) == Messages.YES;
    }

    public static String editText(final Project project, final String title, final String text) {
        final PlainTextEditor editor = new PlainTextEditor(project, text);
        editor.setPreferredSize(new Dimension(550,450));

        final DialogComponent dialog = new DialogComponent(project, title, editor);

        return dialog.showAndGet() ? editor.getText() : null;
    }
}
