package com.igormaznitsa.nbmindmap.nb.swing;

import com.igormaznitsa.nbmindmap.utils.Icons;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

public class DonateButton extends JButton {
  private static final long serialVersionUID = -6096783678529379785L;

  private static final URI LINK = URI.create("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2");

  private static final ActionListener LISTENER = new ActionListener() {
    @Override
    public void actionPerformed (ActionEvent e) {
      try {
        NbUtils.browseURI(LINK, false);
      }
      catch (Exception ex) {
        NbUtils.msgError("Can't open link! You can try to open it manually:\n" + LINK.toASCIIString());
      }
    }
  };

  public DonateButton () {
    super("Donate", Icons.COINS.getIcon());
    this.addActionListener(LISTENER);
    setToolTipText("Make donation to the author of the project");
  }

}
