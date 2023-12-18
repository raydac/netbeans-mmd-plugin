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

package com.igormaznitsa.nbmindmap.utils;

import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.html2color;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.ide.commons.editors.AbstractNoteEditor;
import com.igormaznitsa.mindmap.ide.commons.editors.AbstractNoteEditorData;
import com.igormaznitsa.mindmap.ide.commons.preferences.ColorSelectButton;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.CustomTextEditor;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.igormaznitsa.nbmindmap.nb.explorer.MMKnowledgeSources;
import com.igormaznitsa.nbmindmap.nb.options.MMDCfgOptionsPanelController;
import com.igormaznitsa.nbmindmap.nb.swing.FileEditPanel;
import com.igormaznitsa.nbmindmap.nb.swing.UriEditPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.Coloring;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.lookup.Lookups;

public final class NbUtils {

  public static final MMapURI EMPTY_URI;
  public static final boolean DARK_THEME;
  // List of all found types of source groups in NetBeans sources.
  private static final List<String> ALL_KNOWN_SCOPE_TYPES =
      Arrays.asList("Resources", "TestResources", "GeneratedSources", "java", "resources", "main",
          "test", "doc_root", "web_inf", "PHPSOURCE", "groovy", "grails", "grails_unknown",
          "HTML5-Sources", "HTML5-Tests");
  private static final Logger LOGGER = LoggerFactory.getLogger(NbUtils.class);

  static {
    try {
      EMPTY_URI = new MMapURI("http://igormaznitsa.com/specialuri#empty"); //NOI18N
    } catch (URISyntaxException ex) {
      throw new Error("Unexpected exception", ex); //NOI18N
    }

    final Color color = UIManager.getColor("Panel.background");
    if (color == null) {
      DARK_THEME = false;
    } else {
      DARK_THEME = calculateBrightness(color) < 150;
    }
  }

  private NbUtils() {
  }

  @Nullable
  public static DataObject extractDataObject(@Nullable final Object object) {
    if (object instanceof DataObject) {
      return (DataObject) object;
    } else if (object instanceof FileObject) {
      try {
        return DataObject.find((FileObject) object);
      } catch (DataObjectNotFoundException ex) {
        return null;
      }
    } else if (object instanceof Node) {
      return ((Node) object).getLookup().lookup(DataObject.class);
    } else if (object instanceof Lookup) {
      return ((Lookup) object).lookup(DataObject.class);
    }
    return null;
  }

  @Nullable
  public static FileObject extractFileObject(@Nullable final Object object) {
    if (object instanceof DataObject) {
      return ((DataObject) object).getPrimaryFile();
    } else if (object instanceof FileObject) {
      return (FileObject) object;
    } else if (object instanceof Node) {
      final DataObject dobj = extractDataObject(object);
      return dobj == null ? ((Node) object).getLookup().lookup(FileObject.class) :
          dobj.getPrimaryFile();
    } else if (object instanceof Lookup) {
      return ((Lookup) object).lookup(FileObject.class);
    }
    return null;
  }

  @Nullable
  public static Node extractNode(@Nullable final Object object) {
    if (object instanceof DataObject) {
      return ((DataObject) object).getNodeDelegate();
    } else if (object instanceof FileObject) {
      try {
        final DataObject dobj = DataObject.find((FileObject) object);
        return dobj == null ? null : dobj.getNodeDelegate();
      } catch (DataObjectNotFoundException ex) {
        return null;
      }
    } else if (object instanceof Node) {
      return (Node) object;
    } else if (object instanceof Lookup) {
      return ((Lookup) object).lookup(Node.class);
    } else if (object instanceof Project) {
      try {
        final DataObject dobj = DataObject.find(((Project) object).getProjectDirectory());
        return dobj.getNodeDelegate();
      } catch (DataObjectNotFoundException ex) {
        // to do nothing
      }
    }
    return null;
  }

  public static int calculateBrightness(@Nonnull final Color color) {
    return (int) Math.sqrt(
        color.getRed() * color.getRed() * .241d
            + color.getGreen() * color.getGreen() * .691d
            + color.getBlue() * color.getBlue() * .068d);
  }

  @Nullable
  public static Color extractCommonColorForColorChooserButton(@Nonnull final String colorAttribute,
                                                              @Nonnull @MustNotContainNull
                                                              final Topic[] topics) {
    Color result = null;
    for (final Topic t : topics) {
      final Color color = html2color(t.getAttribute(colorAttribute), false);
      if (result == null) {
        result = color;
      } else if (!result.equals(color)) {
        return ColorSelectButton.DIFF_COLORS;
      }
    }
    return result;
  }

  @Nonnull
  public static Preferences getPreferences() {
    return NbPreferences.forModule(MMDCfgOptionsPanelController.class);
  }

  public static void msgError(@Nullable Component parentComponent, @Nonnull final String text) {
    DialogDisplayer.getDefault()
        .notify(new NotifyDescriptor.Message(text, NotifyDescriptor.ERROR_MESSAGE));
  }

  public static void msgInfo(@Nullable Component parentComponent, @Nonnull final String text) {
    DialogDisplayer.getDefault()
        .notify(new NotifyDescriptor.Message(text, NotifyDescriptor.INFORMATION_MESSAGE));
  }

  public static void msgWarn(@Nullable Component parentComponent, @Nonnull final String text) {
    DialogDisplayer.getDefault()
        .notify(new NotifyDescriptor.Message(text, NotifyDescriptor.WARNING_MESSAGE));
  }

  public static boolean msgConfirmOkCancel(@Nullable Component parentComponent,
                                           @Nonnull final String title,
                                           @Nonnull final String query) {
    final NotifyDescriptor desc =
        new NotifyDescriptor.Confirmation(query, title, NotifyDescriptor.OK_CANCEL_OPTION);
    final Object obj = DialogDisplayer.getDefault().notify(desc);
    return NotifyDescriptor.OK_OPTION.equals(obj);
  }

  public static boolean msgConfirmYesNo(@Nullable Component parentComponent,
                                        @Nonnull final String title, @Nonnull final String query) {
    final NotifyDescriptor desc =
        new NotifyDescriptor.Confirmation(query, title, NotifyDescriptor.YES_NO_OPTION);
    final Object obj = DialogDisplayer.getDefault().notify(desc);
    return NotifyDescriptor.YES_OPTION.equals(obj);
  }

  @Nullable
  public static Boolean msgConfirmYesNoCancel(@Nullable Component parentComponent,
                                              @Nonnull final String title,
                                              @Nonnull final String query) {
    final NotifyDescriptor desc =
        new NotifyDescriptor.Confirmation(query, title, NotifyDescriptor.YES_NO_CANCEL_OPTION);
    final Object obj = DialogDisplayer.getDefault().notify(desc);
    if (NotifyDescriptor.CANCEL_OPTION.equals(obj)) {
      return null;
    }
    return NotifyDescriptor.YES_OPTION.equals(obj);
  }

  public static boolean msgComponentOkCancel(@Nullable Component parentComponent,
                                             @Nonnull final String title,
                                             @Nonnull final JComponent component) {
    final NotifyDescriptor desc =
        new NotifyDescriptor.Confirmation(component, title, NotifyDescriptor.OK_CANCEL_OPTION,
            NotifyDescriptor.PLAIN_MESSAGE);
    return DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION;
  }

  public static void msgInfo(@Nullable Component parentComponent,
                             @Nonnull final JComponent component) {
    DialogDisplayer.getDefault()
        .notify(new NotifyDescriptor.Message(component, NotifyDescriptor.INFORMATION_MESSAGE));
  }

  public static boolean plainMessageOkCancel(@Nullable Component parentComponent,
                                             @Nonnull final String title,
                                             @Nonnull final JComponent compo) {
    final NotifyDescriptor desc =
        new NotifyDescriptor.Confirmation(compo, title, NotifyDescriptor.OK_CANCEL_OPTION,
            NotifyDescriptor.PLAIN_MESSAGE);
    return DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION;
  }

  public static boolean plainMessageOkCancel(@Nullable Component parentComponent,
                                             @Nonnull final String title,
                                             @Nonnull final JComponent compo,
                                             final boolean noDefaultClose) {
    final NotifyDescriptor desc =
        new NotifyDescriptor.Confirmation(compo, title, NotifyDescriptor.OK_CANCEL_OPTION,
            NotifyDescriptor.PLAIN_MESSAGE);
    desc.setNoDefaultClose(noDefaultClose);
    return DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION;
  }

  public static void plainMessageOk(@Nullable Component parentComponent,
                                    @Nonnull final String title, @Nonnull final JComponent compo) {
    final NotifyDescriptor desc =
        new NotifyDescriptor.Message(compo, NotifyDescriptor.PLAIN_MESSAGE);
    desc.setTitle(title);
    DialogDisplayer.getDefault().notify(desc);
  }

  @Nullable
  public static AbstractNoteEditorData editText(@Nullable Component parentComponent,
                                                @Nonnull final DialogProvider provider,
                                                @Nonnull final String title,
                                                @Nonnull final AbstractNoteEditorData data) {
    final AbstractNoteEditor textEditor = new AbstractNoteEditor(() -> parentComponent,
      UIComponentFactoryProvider.findInstance(), provider, data) {
          
      @Override
      public CustomTextEditor makeCustomTextEditor() {
          final CustomTextEditor editor = super.makeCustomTextEditor();

          if (editor.getComponent() instanceof JTextComponent) {
              final String mimeType = DocumentUtilities.getMimeType((JTextComponent)editor.getComponent());
              final FontColorSettings fontColorSettings = MimeLookup.getLookup(MimePath.get(mimeType)).lookup(FontColorSettings.class);
              if (fontColorSettings != null) {
                  final Coloring defaultColoring = Coloring.fromAttributeSet(fontColorSettings.getFontColors(FontColorNames.DEFAULT_COLORING));
                  if (defaultColoring != null) {
                      editor.getComponent().setFont(defaultColoring.getFont());
                  }
              }
          }
          return editor;
      }

      @Nullable
      @Override
      protected Icon findToolbarIconForId(@Nonnull final IconId iconId) {
        switch (iconId) {
          case BROWSE:
            return Icons.BROWSE.getIcon();
          case CLEARALL:
            return Icons.CLEAR_ALL.getIcon();
          case COPY:
            return Icons.COPY.getIcon();
          case EXPORT:
            return Icons.EXPORT.getIcon();
          case IMPORT:
            return Icons.IMPORT.getIcon();
          case PASSWORD_OFF:
            return Icons.PASSWORD_OFF.getIcon();
          case PASSWORD_ON:
            return Icons.PASSWORD_ON.getIcon();
          case PASTE:
            return Icons.PASTE.getIcon();
          case REDO:
            return Icons.REDO.getIcon();
          case UNDO:
            return Icons.UNDO.getIcon();
          default:
            return null;
        }
      }

      @Override
      public void onBrowseUri(@Nonnull final URI uri, final boolean flag) throws Exception {
        NbUtils.browseURI(uri, flag);
      }
    };

    try {
      Utils.catchEscInParentDialog(textEditor.getPanel(), provider, d -> textEditor.isTextChanged(),
          x -> {
            textEditor.cancel();
          });
      if (plainMessageOkCancel(parentComponent, title, textEditor.getPanel(), true)) {
        return textEditor.getData();
      } else {
        return null;
      }
    } finally {
      textEditor.dispose();
    }
  }

  @Nullable
  public static MMapURI editURI(@Nullable Component parentComponent, @Nonnull final String title,
                                @Nullable final MMapURI uri) {
    final UriEditPanel textEditor =
        new UriEditPanel(UIComponentFactoryProvider.findInstance(),
            uri == null ? null : uri.asString(false, false), false);

    final NotifyDescriptor desc =
        new NotifyDescriptor.Confirmation(textEditor.getPanel(), title,
            NotifyDescriptor.OK_CANCEL_OPTION,
            NotifyDescriptor.PLAIN_MESSAGE);
    if (DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION) {
      final String text = textEditor.getText();
      if (text.isEmpty()) {
        return EMPTY_URI;
      }
      try {
        return new MMapURI(text.trim());
      } catch (URISyntaxException ex) {
        msgError(parentComponent, String.format(
            java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle")
                .getString("NbUtils.errMsgIllegalURI"), text));
        return null;
      }
    } else {
      return null;
    }
  }

  @Nullable
  public static FileEditPanel.DataContainer editFilePath(@Nullable Component parentComponent,
                                                         @Nonnull final String title,
                                                         @Nullable final File projectFolder,
                                                         @Nullable
                                                         final FileEditPanel.DataContainer data) {
    final FileEditPanel filePathEditor = new FileEditPanel(
        UIComponentFactoryProvider.findInstance(),
        DialogProviderManager.getInstance().getDialogProvider(),
        projectFolder, data);

    final NotifyDescriptor desc =
        new NotifyDescriptor.Confirmation(filePathEditor.getPanel(), title,
            NotifyDescriptor.OK_CANCEL_OPTION,
            NotifyDescriptor.PLAIN_MESSAGE);
    FileEditPanel.DataContainer result = null;
    if (DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION) {
      result = filePathEditor.getData();
      if (!result.isValid()) {
        NbUtils.msgError(parentComponent, String.format(
            java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle")
                .getString("MMDGraphEditor.editFileLinkForTopic.errorCantFindFile"),
            result.getFilePathWithLine()));
        result = null;
      }
    }
    return result;
  }

  public static boolean browseURI(@Nonnull final URI uri,
                                  final boolean preferInsideBrowserIfPossible) {
    try {
      if (preferInsideBrowserIfPossible) {
        HtmlBrowser.URLDisplayer.getDefault().showURL(uri.toURL());
      } else {
        HtmlBrowser.URLDisplayer.getDefault().showURLExternal(uri.toURL());
      }
      return true;
    } catch (MalformedURLException ex) {
      LOGGER.error("MalformedURLException", ex); //NOI18N
      return false;
    }
  }

  public static void openInSystemViewer(@Nullable final Component parentComponent,
                                        @Nonnull final File file) {
    final Runnable startEdit = () -> {
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
        SwingUtilities.invokeLater(() -> {
          NbUtils.msgError(parentComponent,
              "Can't open file in system viewer! See the log!");//NOI18N
          Toolkit.getDefaultToolkit().beep();
        });
      }
    };
    final Thread thr = new Thread(startEdit, " MMDStartFileEdit");//NOI18N
    thr.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Detected uncaught exception in openInSystemViewer() for file " + file, e));

    thr.setDaemon(true);
    thr.start();
  }

  @Nonnull
  @MustNotContainNull
  public static Collection<SourceGroup> findAllSourceGroups(@Nonnull final Project project) {
    final Sources sources = ProjectUtils.getSources(project);
    final Set<SourceGroup> result = new HashSet<SourceGroup>();
    for (final String scopeType : ALL_KNOWN_SCOPE_TYPES) {
      for (final SourceGroup s : sources.getSourceGroups(scopeType)) {
        result.add(s);
      }
    }

    if (result.isEmpty() &&
        !project.getClass().getName().equals("org.netbeans.modules.maven.NbMavenProjectImpl")) {
      for (final SourceGroup s : sources.getSourceGroups(Sources.TYPE_GENERIC)) {
        result.add(s);
      }
    }

    return result;
  }

  public static boolean isInProjectKnowledgeFolder(@Nonnull final Project project,
                                                   @Nonnull final FileObject file) {
    final FileObject projectKnowledgeFolder =
        MMKnowledgeSources.findProjectKnowledgeFolder(project);
    return projectKnowledgeFolder != null && FileUtil.isParentOf(projectKnowledgeFolder, file);
  }

  public static boolean isFileInProjectScope(@Nonnull final Project project,
                                             @Nonnull final FileObject file) {
    final FileObject projectFolder = project.getProjectDirectory();

    if (FileUtil.isParentOf(projectFolder, file)) {
      for (final SourceGroup srcGroup : findAllSourceGroups(project)) {
        final FileObject root = srcGroup.getRootFolder();
        if (root != null) {
          if (FileUtil.isParentOf(root, file)) {
            return true;
          }
        }
      }
      return false;
    } else {
      return false;
    }
  }

  @Nonnull
  public static Clipboard findClipboard() {
    Clipboard result = Lookup.getDefault().lookup(ExClipboard.class);
    if (result == null) {
      result = Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    return Assertions.assertNotNull("Clipbard is not found", result);
  }

  public enum SelectIn {

    PROJECTS("org-netbeans-modules-project-ui-SelectInProjects.instance"),
    FILES("org-netbeans-modules-project-ui-SelectInFiles.instance"),
    FAVORITES("org-netbeans-modules-favorites-Select.instance");
    private final String actionName;

    private SelectIn(@Nonnull final String actionInstance) {
      this.actionName = actionInstance;
    }

    public boolean select(@Nullable final Object source, @Nonnull final Object object) {
      boolean result = false;
      final Action action =
          FileUtil.getConfigObject("Actions/Window/SelectDocumentNode/" + actionName,
              ContextAwareAction.class); //NOI18N
      if (action != null) {
        try {
          switch (this) {
            case FAVORITES: {
              final Node node = extractNode(object);
              if (node != null) {
                final Action contextAction =
                    ((ContextAwareAction) action).createContextAwareInstance(
                        Lookups.singleton(node));
                contextAction.actionPerformed(
                    new ActionEvent(source, ActionEvent.ACTION_PERFORMED, null));
                result = true;
              }
            }
            break;
            case FILES:
            case PROJECTS: {
              final Action contextAction = ((ContextAwareAction) action).createContextAwareInstance(
                  Lookups.singleton(object));
              contextAction.actionPerformed(
                  new ActionEvent(source, ActionEvent.ACTION_PERFORMED, null));
              result = true;
            }
            break;
            default:
              throw new Error("Unexpected type " + this);
          }
        } catch (Exception ex) {
          LOGGER.error("Error during SelectIn(" + this.name() + ')', ex);
        }
      }
      return result;
    }
  }

}
