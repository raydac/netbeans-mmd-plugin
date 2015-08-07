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

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public enum NbUtils {

  ;
        
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

  public static String editText(final String title, final String text) {
    final PlainTextEditor textEditor = new PlainTextEditor(text);
    
    final NotifyDescriptor desc = new NotifyDescriptor.Confirmation(textEditor, title, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE);
    if (DialogDisplayer.getDefault().notify(desc) == NotifyDescriptor.OK_OPTION){
      return textEditor.getText();
    }else{
      return null;
    }
  }
}
