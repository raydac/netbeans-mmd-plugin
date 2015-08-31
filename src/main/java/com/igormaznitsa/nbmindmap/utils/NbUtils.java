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

import com.igormaznitsa.nbmindmap.nb.MMDCfgOptionsPanelController;
import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbPreferences;

public enum NbUtils {

  ;

  public static final URI EMPTY_URI;

  static {
    try {
      EMPTY_URI = new URI("http://igormaznitsa.com/specialuri#empty"); //NOI18N
    }
    catch (URISyntaxException ex) {
      throw new Error("Unexpected exception", ex); //NOI18N
    }
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

  public static URI editURI(final String title, final URI uri) {
    final UriEditPanel textEditor = new UriEditPanel(uri == null ? null : uri.toString());

    textEditor.doLayout();
    textEditor.setPreferredSize(new Dimension(450, textEditor.getPreferredSize().height));

    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(textEditor, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE);
    if (DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION) {
      final String text = textEditor.getText();
      if (text.isEmpty()) {
        return EMPTY_URI;
      }
      try {
        return new URI(text.trim());
      }
      catch (URISyntaxException ex) {
        msgError(String.format(java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18/Bundle").getString("NbUtils.errMsgIllegalURI"), text));
        return null;
      }
    }
    else {
      return null;
    }
  }

  public static String editFilePath(final String title, final File projectFolder, final String path) {
    final FileEditPanel textEditor = new FileEditPanel(projectFolder, path);

    textEditor.doLayout();
    textEditor.setPreferredSize(new Dimension(450, textEditor.getPreferredSize().height));

    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(textEditor, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE);
    if (DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION) {
      final String text = textEditor.getPath();
      if (text.isEmpty()) {
        return ""; //NOI18N
      }
      return text.trim();
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
      Logger.error("MalformedURLException", ex); //NOI18N
      return false;
    }
  }
}
