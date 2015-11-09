package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.ideamindmap.swing.PlainTextEditor;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.prefs.Preferences;

public enum IdeaUtils  {
    ;
    private static final Logger LOGGER = Logger.getInstance(IdeaUtils.class);

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

    public static VirtualFile findInFolder(@NotNull final VirtualFile folder, @NotNull final MMapURI uri){
        VirtualFile result = null;
        if (uri.isAbsolute()){
            final VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(uri.asFile(null));
            if (file!=null){
                result = VfsUtilCore.isAncestor(folder,file,false) ? file : null;
            }
        } else {
            final VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(uri.asFile(vfile2iofile(folder)));
            if (file!=null){
                result = file.exists() ? file : null;
            }
        }
        return result;
    }

    public static void openInSystemViewer(@NotNull  final DialogProvider dialogProvider, @NotNull final VirtualFile theFile) {
        final File file = vfile2iofile(theFile);

        if (file == null){
            LOGGER.error("Can't find file to open, null provided");
            dialogProvider.msgError("Can't find file to open");
        }else {
            final Runnable startEdit = new Runnable() {
                @Override
                public void run() {
                    boolean ok = false;
                    if (Desktop.isDesktopSupported()) {
                        final Desktop dsk = Desktop.getDesktop();
                        if (dsk.isSupported(Desktop.Action.OPEN)) {
                            try {
                                dsk.open(file);
                                ok = true;
                            } catch (Throwable ex) {
                                LOGGER.error("Can't open file in system viewer : " + file, ex);//NOI18N
                            }
                        }
                    }
                    if (!ok) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                dialogProvider.msgError("Can't open file in system viewer! See the log!");//NOI18N
                                Toolkit.getDefaultToolkit().beep();
                            }
                        });
                    }
                }
            };
            final Thread thr = new Thread(startEdit, " MMDStartFileEdit");//NOI18N
            thr.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(final Thread t, final Throwable e) {
                    LOGGER.error("Detected uncaught exception in openInSystemViewer() for file " + file, e);
                }
            });

            thr.setDaemon(true);
            thr.start();
        }
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

    public static File vfile2iofile(@NotNull  final VirtualFile vfFile) {
        return VfsUtilCore.virtualToIoFile(vfFile);
    }

}
