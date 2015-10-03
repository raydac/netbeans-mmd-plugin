/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.utils;

import com.igormaznitsa.nbmindmap.nb.swing.FileEditPanel;
import com.igormaznitsa.nbmindmap.nb.swing.UriEditPanel;
import com.igormaznitsa.nbmindmap.nb.swing.PlainTextEditor;
import com.igormaznitsa.nbmindmap.nb.swing.ColorChooserButton;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.Topic;
import static com.igormaznitsa.mindmap.swing.panel.utils.Utils.html2color;
import com.igormaznitsa.nbmindmap.nb.options.MMDCfgOptionsPanelController;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.api.project.Project;
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
import org.openide.util.lookup.Lookups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NbUtils {

  public enum SelectIn {

    PROJECTS("org-netbeans-modules-project-ui-SelectInProjects.instance"),
    FILES("org-netbeans-modules-project-ui-SelectInFiles.instance"),
    FAVORITES("org-netbeans-modules-favorites-Select.instance");
    private final String actionName;

    private SelectIn(final String actionInstance) {
      this.actionName = actionInstance;
    }

    public boolean select(final Object source, final Object object) {
      boolean result = false;
      final Action action = FileUtil.getConfigObject("Actions/Window/SelectDocumentNode/" + actionName, ContextAwareAction.class); //NOI18N
      if (action != null) {
        try {
          switch (this) {
            case FAVORITES: {
              final Node node = extractNode(object);
              if (node != null) {
                final Action contextAction = ((ContextAwareAction) action).createContextAwareInstance(Lookups.singleton(node));
                contextAction.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED, null));
                result = true;
              }
            }
            break;
            case PROJECTS: {
              final Action contextAction = ((ContextAwareAction) action).createContextAwareInstance(Lookups.singleton(object));
              contextAction.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED, null));
              result = true;
            }
            break;
            case FILES: {
              final Action contextAction = ((ContextAwareAction) action).createContextAwareInstance(Lookups.singleton(object));
              contextAction.actionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED, null));
              result = true;
            }
            break;
            default:
              throw new Error("Unexpected type " + this);
          }
        }
        catch (Exception ex) {
          logger.error("Error during SelectIn(" + this.name() + ')', ex);
        }
      }
      return result;
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(NbUtils.class);

  public static final MMapURI EMPTY_URI;
  public static final boolean DARK_THEME;

  static {
    try {
      EMPTY_URI = new MMapURI("http://igormaznitsa.com/specialuri#empty"); //NOI18N
    }
    catch (URISyntaxException ex) {
      throw new Error("Unexpected exception", ex); //NOI18N
    }

    final Color color = UIManager.getColor("Panel.background");
    if (color == null) {
      DARK_THEME = false;
    }
    else {
      DARK_THEME = calculateBrightness(color) < 150;
    }
  }

  private NbUtils() {
  }

  public static DataObject extractDataObject(final Object object) {
    if (object instanceof DataObject) {
      return (DataObject) object;
    }
    else if (object instanceof FileObject) {
      try {
        return DataObject.find((FileObject) object);
      }
      catch (DataObjectNotFoundException ex) {
        return null;
      }
    }
    else if (object instanceof Node) {
      return ((Node) object).getLookup().lookup(DataObject.class);
    }
    else if (object instanceof Lookup) {
      return ((Lookup) object).lookup(DataObject.class);
    }
    return null;
  }

  public static FileObject extractFileObject(final Object object) {
    if (object instanceof DataObject) {
      return ((DataObject) object).getPrimaryFile();
    }
    else if (object instanceof FileObject) {
      return (FileObject) object;
    }
    else if (object instanceof Node) {
      final DataObject dobj = extractDataObject(object);
      return dobj == null ? ((Node) object).getLookup().lookup(FileObject.class) : dobj.getPrimaryFile();
    }
    else if (object instanceof Lookup) {
      return ((Lookup) object).lookup(FileObject.class);
    }
    return null;
  }

  public static Node extractNode(final Object object) {
    if (object instanceof DataObject) {
      return ((DataObject) object).getNodeDelegate();
    }
    else if (object instanceof FileObject) {
      try {
        final DataObject dobj = DataObject.find((FileObject) object);
        return dobj == null ? null : dobj.getNodeDelegate();
      }
      catch (DataObjectNotFoundException ex) {
        return null;
      }
    }
    else if (object instanceof Node) {
      return (Node) object;
    }
    else if (object instanceof Lookup) {
      return ((Lookup) object).lookup(Node.class);
    } else if (object instanceof Project){
      try{
        final DataObject dobj = DataObject.find(((Project)object).getProjectDirectory());
        return dobj.getNodeDelegate();
      }catch(DataObjectNotFoundException ex){
        // to do nothing
      }
    }
    return null;
  }

  public static int calculateBrightness(final Color color) {
    return (int) Math.sqrt(
            color.getRed() * color.getRed() * .241d
            + color.getGreen() * color.getGreen() * .691d
            + color.getBlue() * color.getBlue() * .068d);
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

  public static Preferences getPreferences() {
    return NbPreferences.forModule(MMDCfgOptionsPanelController.class);
  }

  public static void msgError(final String text) {
    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(text, NotifyDescriptor.ERROR_MESSAGE));
  }

  public static void msgInfo(final String text) {
    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(text, NotifyDescriptor.INFORMATION_MESSAGE));
  }

  public static void msgWarn(final String text) {
    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(text, NotifyDescriptor.WARNING_MESSAGE));
  }

  public static boolean msgConfirmOkCancel(final String title, final String query) {
    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(query, title, NotifyDescriptor.OK_CANCEL_OPTION);
    final Object obj = DialogDisplayer.getDefault().notify(desc);
    return NotifyDescriptor.OK_OPTION.equals(obj);
  }

  public static boolean msgConfirmYesNo(final String title, final String query) {
    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(query, title, NotifyDescriptor.YES_NO_OPTION);
    final Object obj = DialogDisplayer.getDefault().notify(desc);
    return NotifyDescriptor.YES_OPTION.equals(obj);
  }

  public static Boolean msgConfirmYesNoCancel(final String title, final String query) {
    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(query, title, NotifyDescriptor.YES_NO_CANCEL_OPTION);
    final Object obj = DialogDisplayer.getDefault().notify(desc);
    if (NotifyDescriptor.CANCEL_OPTION.equals(obj)) {
      return null;
    }
    return NotifyDescriptor.YES_OPTION.equals(obj);
  }

  public static boolean msgComponentOkCancel(final String title, final JComponent component) {
    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(component, title, NotifyDescriptor.OK_CANCEL_OPTION);
    return DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION;
  }

  public static void msgInfo(final JComponent component) {
    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(component, NotifyDescriptor.INFORMATION_MESSAGE));
  }

  public static boolean plainMessageOkCancel(final String title, final JComponent compo) {
    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(compo, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE);
    return DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION;
  }

  public static void plainMessageOk(final String title, final JComponent compo) {
    final NotifyDescriptor desc = new NotifyDescriptor.Message(compo, NotifyDescriptor.PLAIN_MESSAGE);
    desc.setTitle(title);
    DialogDisplayer.getDefault().notify(desc);
  }

  public static String editText(final String title, final String text) {
    final PlainTextEditor textEditor = new PlainTextEditor(text);
    if (plainMessageOkCancel(title, textEditor)) {
      return textEditor.getText();
    }
    else {
      return null;
    }
  }

  public static MMapURI editURI(final String title, final MMapURI uri) {
    final UriEditPanel textEditor = new UriEditPanel(uri == null ? null : uri.asString(false, false));

    textEditor.doLayout();
    textEditor.setPreferredSize(new Dimension(450, textEditor.getPreferredSize().height));

    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(textEditor, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE);
    if (DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION) {
      final String text = textEditor.getText();
      if (text.isEmpty()) {
        return EMPTY_URI;
      }
      try {
        return new MMapURI(text.trim());
      }
      catch (URISyntaxException ex) {
        msgError(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle").getString("NbUtils.errMsgIllegalURI"), text));
        return null;
      }
    }
    else {
      return null;
    }
  }

  public static FileEditPanel.DataContainer editFilePath(final String title, final File projectFolder, final FileEditPanel.DataContainer data) {
    final FileEditPanel filePathEditor = new FileEditPanel(projectFolder, data);

    filePathEditor.doLayout();
    filePathEditor.setPreferredSize(new Dimension(450, filePathEditor.getPreferredSize().height));

    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(filePathEditor, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE);
    if (DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION) {
      return filePathEditor.getData();
    }
    else {
      return null;
    }
  }

  public static boolean browseURI(final URI uri, final boolean preferInsideBrowserIfPossible) {
    try {
      if (preferInsideBrowserIfPossible) {
        HtmlBrowser.URLDisplayer.getDefault().showURL(uri.toURL());
      }
      else {
        HtmlBrowser.URLDisplayer.getDefault().showURLExternal(uri.toURL());
      }
      return true;
    }
    catch (MalformedURLException ex) {
      logger.error("MalformedURLException", ex); //NOI18N
      return false;
    }
  }

  public static void openInSystemViewer(final File file) {
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
              logger.error("Can't open file in system viewer : " + file, ex);//NOI18N
            }
          }
        }
        if (!ok) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              NbUtils.msgError("Can't open file in system viewer! See the log!");//NOI18N
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
        logger.error("Detected uncaught exception in openInSystemViewer() for file " + file, e);
      }
    });

    thr.setDaemon(true);
    thr.start();
  }
}
