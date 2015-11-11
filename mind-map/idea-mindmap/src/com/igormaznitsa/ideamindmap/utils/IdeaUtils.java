package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.igormaznitsa.ideamindmap.swing.ColorAttributePanel;
import com.igormaznitsa.ideamindmap.swing.ColorChooserButton;
import com.igormaznitsa.ideamindmap.swing.FileEditPanel;
import com.igormaznitsa.ideamindmap.swing.PlainTextEditor;
import com.igormaznitsa.ideamindmap.swing.UriEditPanel;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColorChooser;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;

public enum IdeaUtils {
  ;
  private static final Logger LOGGER = Logger.getInstance(IdeaUtils.class);

  private static final PluginPreferences PREFERENCES = new PluginPreferences();

  public static final MMapURI EMPTY_URI;

  static {
    try {
      EMPTY_URI = new MMapURI("http://igormaznitsa.com/specialuri#empty"); //NOI18N
    }
    catch (URISyntaxException ex) {
      throw new Error("Unexpected exception", ex); //NOI18N
    }
  }

  public static Preferences getPreferences() {
    return PREFERENCES;
  }

  public static boolean browseURI(final URI uri, final boolean useInsideBrowser) {
    try {
      BrowserUtil.browse(uri);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
    return true;
  }

  public static VirtualFile findInFolder(@NotNull final VirtualFile folder, @NotNull final MMapURI uri) {
    VirtualFile result = null;
    if (uri.isAbsolute()) {
      final VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(uri.asFile(null));
      if (file != null) {
        result = VfsUtilCore.isAncestor(folder, file, false) ? file : null;
      }
    }
    else {
      final VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(uri.asFile(vfile2iofile(folder)));
      if (file != null) {
        result = file.exists() ? file : null;
      }
    }
    return result;
  }

  public static void openInSystemViewer(@NotNull final DialogProvider dialogProvider, @NotNull final VirtualFile theFile) {
    final File file = vfile2iofile(theFile);

    if (file == null) {
      LOGGER.error("Can't find file to open, null provided");
      dialogProvider.msgError("Can't find file to open");
    }
    else {
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
              }
              catch (Throwable ex) {
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

  public static boolean isDarkTheme() {
    return UIUtil.isUnderDarcula();
  }


  private static class DialogComponent extends DialogWrapper {
    private final JComponent component;

    public DialogComponent(final Project project, final String title, final JComponent component, final boolean defaultButtonEnabled) {
      super(project, false, IdeModalityType.PROJECT);
      this.component = component;
      init();
      setTitle(title);
      if (!defaultButtonEnabled) getRootPane().setDefaultButton(null);
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

    final DialogComponent dialog = new DialogComponent(project, title, editor, false);

    return dialog.showAndGet() ? editor.getText() : null;
  }

  public static boolean plainMessageOkCancel(final Project project, final String title, final JComponent centerComponent) {
    final DialogComponent dialog = new DialogComponent(project,title,centerComponent, true);
    return dialog.showAndGet();
  }

  @Nullable
  public static File vfile2iofile(@Nullable final VirtualFile vfFile) {
    return vfFile == null ? null : VfsUtilCore.virtualToIoFile(vfFile);
  }

  public static Color extractCommonColorForColorChooserButton(final String colorAttribute, final Topic[] topics) {
    Color result = null;
    for (final Topic t : topics) {
      final Color color = html2color(t.getAttribute(colorAttribute), false);
      if (result == null) {
        result = color;
      }
      else {
        if (!result.equals(color)) {
          return ColorChooserButton.DIFF_COLORS;
        }
      }
    }
    return result;
  }

  public static Color html2color (final String str, final boolean hasAlpha) {
    Color result = null;
    if (str != null && !str.isEmpty() && str.charAt(0) == '#') {
      try {
        result = new Color(Integer.parseInt(str.substring(1), 16), hasAlpha);
      }
      catch (NumberFormatException ex) {
        LOGGER.warn(String.format("Can't convert %s to color", str));
      }
    }
    return result;
  }

  public static FileEditPanel.DataContainer editFilePath(final MindMapDocumentEditor editor, final String title, final File projectFolder, final FileEditPanel.DataContainer data) {
    final FileEditPanel filePathEditor = new FileEditPanel(editor.getDialogProvider(), projectFolder, data);

    filePathEditor.doLayout();
    filePathEditor.setPreferredSize(new Dimension(450, filePathEditor.getPreferredSize().height));

    if (plainMessageOkCancel(editor.getProject(),title,filePathEditor)) {
      return filePathEditor.getData();
    }
    else {
      return null;
    }
  }

  public static MMapURI editURI(final MindMapDocumentEditor editor, final String title, final MMapURI uri) {
    final UriEditPanel uriEditor = new UriEditPanel(uri == null ? null : uri.asString(false, false));

    uriEditor.doLayout();
    uriEditor.setPreferredSize(new Dimension(450, uriEditor.getPreferredSize().height));

    if (plainMessageOkCancel(editor.getProject(), title, uriEditor)){
      final String text = uriEditor.getText();
      if (text.isEmpty()) {
        return EMPTY_URI;
      }
      try {
        return new MMapURI(text.trim());
      }
      catch (URISyntaxException ex) {
        editor.getDialogProvider().msgError(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("NbUtils.errMsgIllegalURI"), text));
        return null;
      }
    }else{
      return null;
    }
  }


}
