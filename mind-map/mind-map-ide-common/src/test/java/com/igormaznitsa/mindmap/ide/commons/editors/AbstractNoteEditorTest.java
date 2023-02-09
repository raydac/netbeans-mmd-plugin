package com.igormaznitsa.mindmap.ide.commons.editors;

import com.igormaznitsa.mindmap.ide.commons.AbstractUiStarter;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.net.URI;
import javax.swing.JPanel;

public class AbstractNoteEditorTest extends AbstractUiStarter {
  @Override
  public JPanel makePanel(final UIComponentFactory componentFactory,
                          final DialogProvider dialogProvider) {
    final MindMap map = new MindMap(true);
    final AbstractNoteEditor panel =
        new AbstractNoteEditor(() -> null, componentFactory, dialogProvider,
            new AbstractNoteEditorData("Hello world", null, null)) {
          @Override
          public void onBrowseUri(URI uri, boolean flag) throws Exception {

          }
        };
    return panel.getPanel();
  }

  public static void main(final String... args) {
    AbstractUiStarter.main(AbstractNoteEditorTest.class.getName());
  }

}