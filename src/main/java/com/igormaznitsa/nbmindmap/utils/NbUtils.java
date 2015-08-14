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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

public enum NbUtils {

  ;

  public static final URI EMPTY_URI;

  static {
    try {
      EMPTY_URI = new URI("http://igormaznitsa.com/specialuri#empty");
    }
    catch (URISyntaxException ex) {
      throw new Error("Unexpected exception", ex);
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

  public static boolean msgComponentOkCancel(final String title, final JComponent component) {
    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(component, title, NotifyDescriptor.OK_CANCEL_OPTION);
    return DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION;
  }

  public static void msgInfo(final JComponent component) {
    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(component, NotifyDescriptor.INFORMATION_MESSAGE));
  }

  public static String editText(final String title, final String text) {
    final PlainTextEditor textEditor = new PlainTextEditor(text);

    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(textEditor, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE);
    if (DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION) {
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
        msgError("Illegal URI [" + text + ']');
        return null;
      }
    }
    else {
      return null;
    }
  }

  public static boolean browseURI(final URI uri, final boolean insideBrowser) {
    try {
      if (insideBrowser) {
        HtmlBrowser.URLDisplayer.getDefault().showURL(uri.toURL());
      }
      else {
        HtmlBrowser.URLDisplayer.getDefault().showURLExternal(uri.toURL());
      }
      return true;
    }
    catch (MalformedURLException ex) {
      Logger.error("MalformedURLException", ex);
      return false;
    }
  }
}
