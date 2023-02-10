package com.igormaznitsa.mindmap.ide.commons.editors;

import com.igormaznitsa.mindmap.ide.commons.AbstractUiStarter;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.net.URI;
import javax.swing.JPanel;

public class AbstractUriEditPanelTest extends AbstractUiStarter {

  @Override
  public JPanel makePanel(UIComponentFactory componentFactory,
                          DialogProvider dialogProvider) {
    final AbstractUriEditPanel editPanel =
        new AbstractUriEditPanel(componentFactory, "https://sciareto.com", false) {
          @Override
          public void browseUri(URI uri, boolean preferInternalBrowser) {
            System.out.println("Browse: " + uri);
          }

        };
    return editPanel.getPanel();
  }

  public static void main(String... args) {
    AbstractUiStarter.main(AbstractUriEditPanelTest.class.getName());
  }

}