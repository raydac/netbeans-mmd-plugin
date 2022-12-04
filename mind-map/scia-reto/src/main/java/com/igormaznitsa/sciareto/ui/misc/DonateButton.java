/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.igormaznitsa.sciareto.ui.misc;

import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.Icons;
import com.igormaznitsa.sciareto.ui.SrI18n;
import com.igormaznitsa.sciareto.ui.UiUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ResourceBundle;

public final class DonateButton extends JButton {
  private static final long serialVersionUID = -6096783678529379785L;

  private static final URI LINK = URI.create("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2"); //NOI18N
  private static final ActionListener LISTENER = e -> {
    try {
      UiUtils.browseURI(LINK, false);
    }
    catch (Exception ex) {
      DialogProviderManager.getInstance().getDialogProvider().msgError(SciaRetoStarter.getApplicationFrame(), SrI18n.getInstance().findBundle().getString("DonateButton.error") + LINK.toASCIIString());
    }
  };

  public DonateButton () {
    super(SrI18n.getInstance().findBundle().getString("DonateButton.Text"), Icons.COINS.getIcon());
    this.addActionListener(LISTENER);
    setToolTipText(SrI18n.getInstance().findBundle().getString("DonateButton.ToolTip"));
  }

}
