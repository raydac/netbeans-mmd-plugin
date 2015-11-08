package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.ideamindmap.swing.PlainTextEditor;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.ide.browsers.WebBrowserService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.DocumentUtil;
import com.intellij.util.config.StorageAccessors;
import com.intellij.util.io.URLUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.prefs.Preferences;

public enum IdeaUtils  {
    ;

    private static final PluginPreferences PREFERENCES = new PluginPreferences();

    public static Preferences getPreferences() {
        return PREFERENCES;
    }

    public static boolean browseURI(final URI uri, final boolean useInsideBrowser) {
        try {
            BrowserUtil.browse(uri);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static void openInSystemViewer(final VirtualFile theFile) {
    }

    private static class DialogComponent extends DialogWrapper {
        private final JComponent component;

        public DialogComponent(final Project project, final String title, final JComponent component) {
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

    public static String editText(final Project project, final String title, final String text) {
        final PlainTextEditor editor = new PlainTextEditor(project, text);
        editor.setPreferredSize(new Dimension(550, 450));

        final DialogComponent dialog = new DialogComponent(project, title, editor);

        return dialog.showAndGet() ? editor.getText() : null;
    }

}
