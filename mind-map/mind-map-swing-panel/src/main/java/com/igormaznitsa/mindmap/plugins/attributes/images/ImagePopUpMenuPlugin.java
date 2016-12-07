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
package com.igormaznitsa.mindmap.plugins.attributes.images;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractPopupMenuItem;
import com.igormaznitsa.mindmap.plugins.api.CustomJob;
import com.igormaznitsa.mindmap.swing.panel.DialogProvider;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;

public class ImagePopUpMenuPlugin extends AbstractPopupMenuItem {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImagePopUpMenuPlugin.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/mindmap/swing/panel/Bundle");//NOI18N
  private static final Icon ICON = ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_IMAGES);

  private static File lastSelectedFile = null;
  private static int lastSelectedImportIndex = 0;

  private static final FileFilter IMAGE_FILE_FILTER = new FileFilter() {
    @Override
    public boolean accept(@Nonnull final File f) {
      final String text = f.getName().toLowerCase(Locale.ENGLISH);
      return f.isDirectory() || text.endsWith(".png") || text.endsWith(".jpg") || text.endsWith(".gif"); //NOI18N
    }

    @Override
    @Nonnull
    public String getDescription() {
      return BUNDLE.getString("Images.Plugin.FilterDescription");
    }

  };

  @Override
  @Nullable
  public JMenuItem makeMenuItem(@Nonnull final MindMapPanel panel, @Nonnull final DialogProvider dialogProvider, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] selectedTopics, @Nullable final CustomJob customProcessor) {
    final boolean hasAttribute = containAttribute(topic, selectedTopics);

    final JMenuItem result;
    if (hasAttribute) {
      result = new JMenuItem(BUNDLE.getString("Images.Plugin.MenuTitle.Remove"), ICON);//NOI18N
      result.setToolTipText(BUNDLE.getString("Images.Plugin.MenuTitle.Remove.Tooltip"));//NOI18N
      result.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          if (dialogProvider.msgConfirmYesNo(BUNDLE.getString("Images.Plugin.Remove.Dialog.Title"), BUNDLE.getString("Images.Plugin.Remove.Dialog.Text"))) {//NOI18N
            setAttribute(null, topic, selectedTopics);
            ImageVisualAttributePlugin.clearCachedImages();
            panel.notifyModelChanged();
          }
        }
      });
    } else {
      result = new JMenuItem(BUNDLE.getString("Images.Plugin.MenuTitle.Add"), ICON);//NOI18N
      result.setToolTipText(BUNDLE.getString("Images.Plugin.MenuTitle.Add.Tooltip"));//NOI18N

      result.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {

          final Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

          boolean loadFromFile = true;

          if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {

            final AtomicInteger selectedItem = new AtomicInteger(-1);

            if (dialogProvider.msgOkCancel(BUNDLE.getString("Images.Plugin.Select.DialogTitle"), makeSelectPanel(new String[]{BUNDLE.getString("Images.Plugin.Select.FromClipboard"), BUNDLE.getString("Images.Plugin.Select.FromFile")}, selectedItem))) {
              lastSelectedImportIndex = selectedItem.get();

              if (selectedItem.get() == 0) {
                try {
                  setAttribute(Utils.rescaleImageAndEncodeAsBase64((Image) transferable.getTransferData(DataFlavor.imageFlavor), Utils.getMaxImageSize()), topic, selectedTopics);
                  panel.notifyModelChanged();
                }
                catch (final IllegalArgumentException ex) {
                  dialogProvider.msgError(BUNDLE.getString("Images.Plugin.Error"));
                  LOGGER.error("Can't import from clipboard image", ex); //NOI18N
                }
                catch (final Exception ex) {
                  dialogProvider.msgError(BUNDLE.getString("Images.Plugin.Error"));
                  LOGGER.error("Unexpected error during image import from clipboard", ex); //NOI18N
                }
                loadFromFile = false;
              }
            } else {
              loadFromFile = false;
            }
          }

          if (loadFromFile) {
            final File selected = dialogProvider.msgOpenFileDialog("select-image-file", BUNDLE.getString("Images.Plugin.Load.DialogTitle"), lastSelectedFile, true, IMAGE_FILE_FILTER, BUNDLE.getString("Images.Plugin.Load.Dialog.Button.Open")); //NOI18N
            if (selected != null) {
              lastSelectedFile = selected;
              try {
                setAttribute(Utils.rescaleImageAndEncodeAsBase64(selected, Utils.getMaxImageSize()), topic, selectedTopics);
                panel.notifyModelChanged();
              }
              catch (final IllegalArgumentException ex) {
                dialogProvider.msgError(BUNDLE.getString("Images.Plugin.Error"));
                LOGGER.warn("Can't load image file : " + selected); //NOI18N
              }
              catch (final Exception ex) {
                dialogProvider.msgError(BUNDLE.getString("Images.Plugin.Error"));
                LOGGER.error("Unexpected error during loading of image file : " + selected, ex); //NOI18N
              }
            }
          }
        }
      });
    }

    return result;
  }

  @Nonnull
  private JPanel makeSelectPanel(@Nonnull @MustNotContainNull final String[] options, @Nonnull final AtomicInteger selected) {
    final JPanel panel = new JPanel(new GridBagLayout());

    final GridBagConstraints constraint = new GridBagConstraints();
    constraint.gridx = 0;
    constraint.fill = GridBagConstraints.HORIZONTAL;
    constraint.anchor = GridBagConstraints.WEST;

    final ButtonGroup group = new ButtonGroup();

    int selectedIndex = this.lastSelectedImportIndex;

    for (int i = 0; i < options.length; i++) {
      final JRadioButton button = new JRadioButton(options[i]);
      if (selectedIndex == i) {
        button.setSelected(true);
        selected.set(i);
      }
      group.add(button);

      final int currentIndex = i;

      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          if (button.isSelected()) {
            selected.set(currentIndex);
          }
        }
      });

      panel.add(button, constraint);
    }

    return panel;
  }

  private boolean containAttribute(@Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] topics) {
    boolean result = false;
    if (topic != null) {
      result |= topic.getAttribute(ImageVisualAttributePlugin.ATTR_KEY) != null;
    }
    if (!result) {
      for (final Topic t : topics) {
        result |= t.getAttribute(ImageVisualAttributePlugin.ATTR_KEY) != null;
        if (result) {
          break;
        }
      }
    }
    return result;
  }

  private void setAttribute(@Nullable final String value, @Nullable final Topic topic, @Nonnull @MustNotContainNull final Topic[] topics) {
    if (topic != null) {
      topic.setAttribute(ImageVisualAttributePlugin.ATTR_KEY, value);
    }
    for (final Topic t : topics) {
      t.setAttribute(ImageVisualAttributePlugin.ATTR_KEY, value);
    }
  }

  @Override
  @Nonnull
  public PopUpSection getSection() {
    return PopUpSection.EXTRAS;
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return true;
  }

  @Override
  public boolean needsSelectedTopics() {
    return true;
  }

  @Override
  public int getOrder() {
    return CUSTOM_PLUGIN_START - 2;
  }

}
