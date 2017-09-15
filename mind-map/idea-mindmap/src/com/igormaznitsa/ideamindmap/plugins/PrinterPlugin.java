package com.igormaznitsa.ideamindmap.plugins;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;

import com.igormaznitsa.ideamindmap.print.IdeaMMDPrintPanelAdaptor;
import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractPopupMenuItem;
import com.igormaznitsa.mindmap.plugins.api.CustomJob;
import com.igormaznitsa.mindmap.print.MMDPrintPanel;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.intellij.openapi.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class PrinterPlugin extends AbstractPopupMenuItem {
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("/i18n/Bundle");

  @Nullable @Override public JMenuItem makeMenuItem(@Nonnull final MindMapPanel mindMapPanel, @Nonnull final DialogProvider dialogProvider, @Nullable Topic topic,
    @Nullable @MustNotContainNull Topic[] topics, @Nullable CustomJob mindMapPopUpItemCustomProcessor) {

    final JMenuItem printAction = UI_COMPO_FACTORY.makeMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miPrintPreview"), AllIcons.PopUp.PRINTER);
    final Project project = (Project)assertNotNull(mindMapPanel.findTmpObject("project"));
    printAction.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        final MMDPrintPanel panel = new MMDPrintPanel(dialogProvider, new IdeaMMDPrintPanelAdaptor(project), mindMapPanel);
        IdeaUtils.plainMessageClose(project,"Print mind map",panel);
      }
    });
    return printAction;
  }

  @Override public boolean isEnabled(@Nonnull MindMapPanel panel, @Nullable Topic topic, @Nonnull @MustNotContainNull Topic[] selectedTopics) {
    return !panel.getModel().isEmpty();
  }

  @Nonnull @Override public PopUpSection getSection() {
    return PopUpSection.MISC;
  }

  @Override public boolean needsTopicUnderMouse() {
    return false;
  }

  @Override public boolean needsSelectedTopics() {
    return false;
  }

  @Override public int getOrder() {
    return CUSTOM_PLUGIN_START+100;
  }
}
