package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.intellij.openapi.ui.Messages;

import javax.swing.JButton;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

public class DonateButton extends JButton {
  private static final URI LINK = URI.create("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2");

  private static final ActionListener LISTENER = new ActionListener() {
    @Override public void actionPerformed(ActionEvent e) {
        try {
          IdeaUtils.browseURI(LINK, false);
        }
        catch (Exception ex) {
          Messages.showErrorDialog((Component)e.getSource(),"Can't open link! You can try to open it manually:\n"+LINK.toASCIIString(),"Error");
        }
    }
  };

  public DonateButton(){
    super("Donate", AllIcons.Buttons.COINS);
    this.addActionListener(LISTENER);
    setToolTipText("Make donation to the author of the project");
  }


}
