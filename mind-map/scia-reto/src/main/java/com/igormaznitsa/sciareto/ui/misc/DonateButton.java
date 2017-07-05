package com.igormaznitsa.sciareto.ui.misc;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.Icons;
import com.igormaznitsa.sciareto.ui.UiUtils;

public class DonateButton extends JButton {
  private static final long serialVersionUID = -6096783678529379785L;

  private static final URI LINK = URI.create("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2"); //NOI18N
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");

  private static final ActionListener LISTENER = new ActionListener() {
    @Override
    public void actionPerformed (@Nonnull final ActionEvent e) {
      try {
        UiUtils.browseURI(LINK, false);
      }
      catch (Exception ex) {
        DialogProviderManager.getInstance().getDialogProvider().msgError(null, "Can't open link! You can try to open it manually:\n" + LINK.toASCIIString());
      }
    }
  };

  public DonateButton () {
    super(BUNDLE.getString("DonateButton.Text"), Icons.COINS.getIcon());
    this.addActionListener(LISTENER);
    setToolTipText(BUNDLE.getString("DonateButton.ToolTip"));
  }

}
