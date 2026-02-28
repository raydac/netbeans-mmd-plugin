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

package com.igormaznitsa.nbmindmap.nb.swing;

import com.igormaznitsa.nbmindmap.utils.Icons;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ResourceBundle;
import javax.swing.JButton;

public class DonateButton extends JButton {
  private static final long serialVersionUID = -6096783678529379785L;

  private static final URI LINK = URI.create("https://www.arthursacresanimalsanctuary.org/donate");
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");

  private static final ActionListener LISTENER = new ActionListener() {
    @Override
    public void actionPerformed (ActionEvent e) {
      try {
        NbUtils.browseURI(LINK, false);
      }
      catch (Exception ex) {
        NbUtils.msgError(null, "Can't open link! You can try to open it manually:\n" + LINK.toASCIIString());
      }
    }
  };

  public DonateButton () {
    super(BUNDLE.getString("DonateButton.Text"), Icons.COINS.getIcon());
    this.addActionListener(LISTENER);
    setToolTipText(BUNDLE.getString("DonateButton.ToolTip"));
  }

}
