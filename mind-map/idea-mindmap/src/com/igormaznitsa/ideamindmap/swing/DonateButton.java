/*
 * Copyright 2015-2018 Igor Maznitsa.
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

package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.intellij.openapi.ui.Messages;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.JButton;

public class DonateButton extends JButton {
  private static final long serialVersionUID = -7732647214349379363L;
  private static final URI LINK = URI.create("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2");

  private static final ActionListener LISTENER = e -> {
    try {
      IdeaUtils.browseURI(LINK, false);
    } catch (Exception ex) {
      Messages.showErrorDialog((Component) e.getSource(), "Can't open link! You can try to open it manually:\n" + LINK.toASCIIString(), "Error");
    }
  };

  public DonateButton() {
    super("Donate", AllIcons.Buttons.COINS);
    this.addActionListener(LISTENER);
    setToolTipText("If you like the plugin, you could maake some donation");
  }


}
