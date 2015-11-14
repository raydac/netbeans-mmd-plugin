package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URI;
import java.util.ResourceBundle;

public class AboutForm {
  private JPanel mainPanel;
  private JHtmlLabel htmlLabelText;

  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");

  private static class DialogComponent extends DialogWrapper {
    private final JComponent component;

    public DialogComponent(final Component parent, final String title, final JComponent component) {
      super(parent, true);
      this.component = component;
      init();
      setTitle(title);
    }

    protected Action[] createActions(){
        return new Action[]{getOKAction()};
    }

    protected void init(){
      setResizable(false);
      setModal(true);
      super.init();
    }

    public DialogComponent(final Project project, final String title, final JComponent component) {
      super(project, true);
      this.component = component;
      init();
      setTitle(title);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
      return this.component;
    }
  }


  public AboutForm() {
    this.htmlLabelText.setText(BUNDLE.getString("AboutText"));
    this.htmlLabelText.addLinkListener(new JHtmlLabel.LinkListener() {
      @Override public void onLinkActivated(final JHtmlLabel source, final String link) {
        try {
          IdeaUtils.browseURI(URI.create(link), false);
        }catch(Exception ex){
          ex.printStackTrace();
        }
      }
    });
    this.mainPanel.setPreferredSize(new Dimension(600,345));
  }

  public static void show(final Component parent) {
    new DialogComponent(parent,"About",new AboutForm().mainPanel).show();
  }

  public static void show(final Project project) {
    new DialogComponent(project,"About",new AboutForm().mainPanel).show();
  }
}
