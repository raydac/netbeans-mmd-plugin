/*
 * Copyright 2016 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.sciareto.ui;

import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.doesContainOnlyStandardAttributes;
import static com.igormaznitsa.sciareto.ui.UiUtils.BUNDLE;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.annotation.ToDo;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.MindMapController;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.api.CustomJob;
import com.igormaznitsa.mindmap.plugins.api.PopUpMenuItemPlugin;
import com.igormaznitsa.mindmap.plugins.misc.AboutPlugin;
import com.igormaznitsa.mindmap.plugins.misc.OptionsPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraFilePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraJumpPlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraNotePlugin;
import com.igormaznitsa.mindmap.plugins.processors.ExtraURIPlugin;
import com.igormaznitsa.mindmap.plugins.tools.ChangeColorPlugin;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapListener;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelController;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractElement;
import com.igormaznitsa.mindmap.swing.panel.ui.ElementPart;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;

public class MMDPanel extends JScrollPane implements MindMapPanelController, MindMapController, TabProvider, MindMapListener {

  private static final long serialVersionUID = -1011638261448046208L;

  private final MindMapPanel mindMapPanel;

  private final TabTitle title;
  private volatile File file;
  private volatile boolean changed = false;

  public MMDPanel(@Nullable File file, @Nonnull final MindMap map) {
    super();
    this.file = file;
    this.title = new TabTitle();
    this.title.setTitle("Some mind map", true);
    this.mindMapPanel = new MindMapPanel(this);
    this.mindMapPanel.addMindMapListener(this);
    this.setViewportView(this.mindMapPanel);
    this.mindMapPanel.setModel(Assertions.assertNotNull(map));
    this.changed = file == null;
    this.updateTitle();
  }

  public void setFile(@Nullable final File file) {
    this.file = file;
    updateTitle();
  }

  @Nullable
  public File getFile() {
    return this.file;
  }

  public boolean isChanged() {
    return this.changed;
  }

  @Override
  public boolean isUnfoldCollapsedTopicDropTarget(@Nonnull final MindMapPanel source) {
    return PreferencesManager.getInstance().getPreferences().getBoolean("unfoldCollapsedTarget", true);
  }

  @Override
  public boolean isCopyColorInfoFromParentToNewChildAllowed(@Nonnull final MindMapPanel source) {
    return PreferencesManager.getInstance().getPreferences().getBoolean("copyColorInfoToNewChildAllowed", true);
  }

  @Override
  public boolean isSelectionAllowed(@Nonnull final MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isElementDragAllowed(@Nonnull final MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isMouseMoveProcessingAllowed(@Nonnull final MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isMouseWheelProcessingAllowed(@Nonnull final MindMapPanel source) {
    return true;
  }

  @Override
  public boolean isMouseClickProcessingAllowed(@Nonnull final MindMapPanel source) {
    return true;
  }

  @Override
  @Nonnull
  public MindMapPanelConfig provideConfigForMindMapPanel(@Nonnull final MindMapPanel source) {
    final MindMapPanelConfig config = new MindMapPanelConfig();
    config.loadFrom(PreferencesManager.getInstance().getPreferences());
    return config;
  }

  private Map<Class<? extends PopUpMenuItemPlugin>, CustomJob> customProcessors = null;

  @Override
  @Nonnull
  public TabTitle getTabTitle() {
    return this.title;
  }

  @Override
  @Nonnull
  public JComponent getMainComponent() {
    return this;
  }

  @Override
  public void requestFocus() {
    this.mindMapPanel.requestFocus();
  }

  public void topicToCentre(@Nullable final Topic topic) {
    if (topic != null) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          final AbstractElement element = (AbstractElement) topic.getPayload();
          if (element != null) {
            final Rectangle2D bounds = element.getBounds();
            final Dimension viewPortSize = getViewport().getExtentSize();

            final int x = Math.max(0, (int) Math.round(bounds.getX() - (viewPortSize.getWidth() - bounds.getWidth()) / 2));
            final int y = Math.max(0, (int) Math.round(bounds.getY() - (viewPortSize.getHeight() - bounds.getHeight()) / 2));

            getViewport().setViewPosition(new Point(x, y));
          }
        }
      });
    }
  }

  private void updateTitle() {
    final File f = this.file;
    this.title.setTitle(f == null ? "unitled" : f.getName(), this.changed);
  }

  @Override
  public void onMindMapModelChanged(@Nonnull final MindMapPanel source) {
    this.getViewport().revalidate();
    this.repaint();
  }

  @Override
  public void onMindMapModelRealigned(@Nonnull final MindMapPanel source, @Nonnull final Dimension coveredAreaSize) {
    this.getViewport().revalidate();
    this.repaint();
  }

  @Override
  public void onEnsureVisibilityOfTopic(@Nonnull final MindMapPanel source, @Nullable final Topic topic) {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        if (topic == null) {
          return;
        }

        final AbstractElement element = (AbstractElement) topic.getPayload();
        if (element == null) {
          return;
        }

        final Rectangle2D orig = element.getBounds();
        final int GAP = 30;

        final Rectangle bounds = orig.getBounds();
        bounds.setLocation(Math.max(0, bounds.x - GAP), Math.max(0, bounds.y - GAP));
        bounds.setSize(bounds.width + GAP * 2, bounds.height + GAP * 2);

        final JViewport viewport = getViewport();
        final Rectangle visible = viewport.getViewRect();

        if (visible.contains(bounds)) {
          return;
        }

        bounds.setLocation(bounds.x - visible.x, bounds.y - visible.y);

        viewport.scrollRectToVisible(bounds);
      }

    });
  }

  @ToDo
  @Override
  public void onClickOnExtra(@Nonnull final MindMapPanel source, final int clicks, @Nonnull final Topic topic, @Nonnull final Extra<?> extra) {
    if (clicks > 1) {
      switch (extra.getType()) {
        case FILE: {

        }
        break;
        case LINK: {

        }
        break;
        case NOTE: {
          editTextForTopic(topic);
        }
        break;
        case TOPIC: {

        }
        break;
      }
    }
  }

  @Override
  public void onChangedSelection(@Nonnull final MindMapPanel source, @Nonnull @MustNotContainNull final Topic[] currentSelectedTopics) {
  }

  @Override
  public boolean allowedRemovingOfTopics(@Nonnull final MindMapPanel source, @Nonnull @MustNotContainNull final Topic[] topics) {
    boolean topicsNotImportant = true;

    for (final Topic t : topics) {
      topicsNotImportant &= t.canBeLost();
      if (!topicsNotImportant) {
        break;
      }
    }

    final boolean result;

    if (topicsNotImportant) {
      result = true;
    } else {
      result = DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNo(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics,title"), String.format(BUNDLE.getString("MMDGraphEditor.allowedRemovingOfTopics.message"), topics.length));
    }
    return result;
  }

  @Nonnull
  private Map<Class<? extends PopUpMenuItemPlugin>, CustomJob> getCustomProcessors() {
    if (this.customProcessors == null) {
      this.customProcessors = new HashMap<>();
      this.customProcessors.put(ExtraNotePlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          editTextForTopic(topic);
          panel.requestFocus();
        }
      });
      this.customProcessors.put(ExtraFilePlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          editFileLinkForTopic(topic);
          panel.requestFocus();
        }
      });
      this.customProcessors.put(ExtraURIPlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          editLinkForTopic(topic);
          panel.requestFocus();
        }
      });
      this.customProcessors.put(ExtraJumpPlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          editTopicLinkForTopic(topic);
          panel.requestFocus();
        }
      });
      this.customProcessors.put(ChangeColorPlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          processColorDialogForTopics(panel, selectedTopics.length > 0 ? selectedTopics : new Topic[]{topic});
        }
      });
      this.customProcessors.put(AboutPlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          showAbout();
        }
      });
      this.customProcessors.put(OptionsPlugin.class, new CustomJob() {
        @Override
        public void doJob(@Nonnull final PopUpMenuItemPlugin plugin, @Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics) {
          startOptionsEdit();
        }
      });
    }
    return this.customProcessors;
  }

  private void editTextForTopic(@Nonnull final Topic topic) {
    final ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);
    final String result;
    if (note == null) {
      // create new
      result = UiUtils.editText(String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlfAddNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), ""); //NOI18N
    } else {
      // edit
      result = UiUtils.editText(String.format(BUNDLE.getString("MMDGraphEditor.editTextForTopic.dlgEditNoteTitle"), Utils.makeShortTextVersion(topic.getText(), 16)), note.getValue());
    }
    if (result != null) {
      if (result.isEmpty()) {
        topic.removeExtra(Extra.ExtraType.NOTE);
      } else {
        topic.setExtra(new ExtraNote(result));
      }
      this.mindMapPanel.invalidate();
      this.mindMapPanel.repaint();
      onMindMapModelChanged(this.mindMapPanel);
    }
  }

  private void editFileLinkForTopic(@Nullable final Topic topic) {

  }

  private void editTopicLinkForTopic(@Nullable final Topic topic) {

  }

  private void editLinkForTopic(@Nullable final Topic topic) {

  }

  @ToDo
  private void processColorDialogForTopics(@Nonnull final MindMapPanel source, @Nonnull @MustNotContainNull final Topic[] topics) {

  }

  @ToDo
  private void showAbout() {

  }

  @ToDo
  private void startOptionsEdit() {

  }

  @Override
  @Nonnull
  public JPopupMenu makePopUpForMindMapPanel(@Nonnull final MindMapPanel source, @Nonnull final Point point, @Nullable final AbstractElement elementUnderMouse, @Nullable final ElementPart elementPartUnderMouse) {
    return Utils.makePopUp(source, DialogProviderManager.getInstance().getDialogProvider(), elementUnderMouse == null ? null : elementUnderMouse.getModel(), source.getSelectedTopics(), getCustomProcessors());

  }

  @Override
  @Nonnull
  public DialogProvider getDialogProvider(@Nonnull final MindMapPanel source) {
    return DialogProviderManager.getInstance().getDialogProvider();
  }

  @Override
  public boolean processDropTopicToAnotherTopic(@Nonnull final MindMapPanel source, @Nonnull final Point dropPoint, @Nullable final Topic draggedTopic, @Nullable final Topic destinationTopic) {
    boolean result = false;
    if (draggedTopic != null && destinationTopic != null && draggedTopic != destinationTopic) {
      if (destinationTopic.getExtras().containsKey(Extra.ExtraType.TOPIC)) {
        if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmTitle"), BUNDLE.getString("MMDGraphEditor.addTopicToElement.confirmMsg"))) {
          return result;
        }
      }

      final ExtraTopic topicLink = ExtraTopic.makeLinkTo(this.mindMapPanel.getModel(), draggedTopic);
      destinationTopic.setExtra(topicLink);

      result = true;
    }
    return result;
  }

  @Override
  public boolean canBeDeletedSilently(@Nonnull final MindMap map, @Nonnull final Topic topic) {
    return topic.getText().isEmpty() && topic.getExtras().isEmpty() && doesContainOnlyStandardAttributes(topic);
  }
}
