#set($symbol_pound='#')
#set($symbol_dollar='$')
#set($symbol_escape='\')
package ${package};

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JMenuItem;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractPopupMenuItem;
import com.igormaznitsa.mindmap.plugins.api.CustomJob;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;

public class Plugin extends AbstractPopupMenuItem {

  public Plugin() {
  }

  @Override
  @Nullable
  public JMenuItem makeMenuItem(@Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics, @Nullable CustomJob customProcessor) {
    // form your menu item here
    return null;
  }

  @Override
  @Nonnull
  public PopUpSection getSection() {
    return PopUpSection.MISC;
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return true;
  }

  @Override
  public boolean needsSelectedTopics() {
    return false;
  }

  @Override
  public int getOrder() {
    return CUSTOM_PLUGIN_START + 100;
  }
}
