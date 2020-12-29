/*
 * Copyright 2015-2018 Igor Maznitsa.
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

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractPopupMenuItem;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.swing.panel.utils.PathStore;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
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
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;

public class ImagePopUpMenuPlugin extends AbstractPopupMenuItem {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImagePopUpMenuPlugin.class);
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("com/igormaznitsa/mindmap/swing/panel/Bundle");//NOI18N
  private static final Icon ICON = ImageIconServiceProvider.findInstance().getIconForId(IconID.ICON_IMAGES);
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
  private static final PathStore PATH_STORE = new PathStore();
  private static int lastSelectedImportIndex = 0;

  @Override
  @Nullable
  public JMenuItem makeMenuItem(@Nonnull final PluginContext context, @Nullable final Topic activeTopic) {
    final boolean hasAttribute = containAttribute(context, activeTopic);

    final JMenuItem result;
    if (hasAttribute) {
      result = UI_COMPO_FACTORY.makeMenuItem(BUNDLE.getString("Images.Plugin.MenuTitle.Remove"), ICON);//NOI18N
      result.setToolTipText(BUNDLE.getString("Images.Plugin.MenuTitle.Remove.Tooltip"));//NOI18N
      result.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          if (context.getDialogProvider().msgConfirmYesNo(null, BUNDLE.getString("Images.Plugin.Remove.Dialog.Title"), BUNDLE.getString("Images.Plugin.Remove.Dialog.Text"))) {//NOI18N
            setAttribute(context, activeTopic, null, null, null);
            ImageVisualAttributePlugin.clearCachedImages();
            context.getPanel().doNotifyModelChanged(true);
          }
        }
      });
    } else {
      result = UI_COMPO_FACTORY.makeMenuItem(BUNDLE.getString("Images.Plugin.MenuTitle.Add"), ICON);//NOI18N
      result.setToolTipText(BUNDLE.getString("Images.Plugin.MenuTitle.Add.Tooltip"));//NOI18N

      result.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {

          final Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

          boolean loadFromFile = true;

          if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {

            final AtomicInteger selectedItem = new AtomicInteger(-1);

            if (context.getDialogProvider().msgOkCancel(SwingUtilities.windowForComponent(context.getPanel()), BUNDLE.getString("Images.Plugin.Select.DialogTitle"), makeSelectPanel(new String[] {BUNDLE.getString("Images.Plugin.Select.FromClipboard"), BUNDLE.getString("Images.Plugin.Select.FromFile")}, selectedItem))) {
              lastSelectedImportIndex = selectedItem.get();

              if (selectedItem.get() == 0) {
                try {
                  final String rescaledImageAsBase64 = Utils.rescaleImageAndEncodeAsBase64((Image) transferable.getTransferData(DataFlavor.imageFlavor), Utils.getMaxImageSize());
                  final String filePath = null;
                  setAttribute(context, activeTopic, rescaledImageAsBase64, filePath, null);
                  context.getPanel().doNotifyModelChanged(true);
                } catch (final IllegalArgumentException ex) {
                  context.getDialogProvider().msgError(null, BUNDLE.getString("Images.Plugin.Error"));
                  LOGGER.error("Can't import from clipboard image", ex); //NOI18N
                } catch (final Exception ex) {
                  context.getDialogProvider().msgError(null, BUNDLE.getString("Images.Plugin.Error"));
                  LOGGER.error("Unexpected error during image import from clipboard", ex); //NOI18N
                }
                loadFromFile = false;
              }
            } else {
              loadFromFile = false;
            }
          }

          if (loadFromFile) {
            final File selected = PATH_STORE.put(context.getPanel().getUuid().toString(),
                context.getDialogProvider().msgOpenFileDialog(context.getPanel(), context, ImagePopUpMenuPlugin.class.getName(), BUNDLE.getString("Images.Plugin.Load.DialogTitle"), PATH_STORE.find(context, context.getPanel().getUuid().toString()), true, new FileFilter[] {IMAGE_FILE_FILTER}, BUNDLE.getString("Images.Plugin.Load.Dialog.Button.Open"))); //NOI18N
            if (selected != null) {
              try {
                final String rescaledImageAsBase64 =
                    Utils.rescaleImageAndEncodeAsBase64(selected, Utils.getMaxImageSize());
                final String fileName = FilenameUtils.getBaseName(selected.getName());
                final String filePath;
                if (context.getDialogProvider().msgConfirmYesNo(SwingUtilities.windowForComponent(context.getPanel()), BUNDLE.getString("Images.Plugin.Question.AddFilePath.Title"), BUNDLE.getString("Images.Plugin.Question.AddFilePath"))) {
                  filePath = MMapURI.makeFromFilePath(context.getProjectFolder(), selected.getAbsolutePath(), null).toString();
                } else {
                  filePath = null;
                }
                setAttribute(context, activeTopic, rescaledImageAsBase64, filePath, fileName);
                context.getPanel().doNotifyModelChanged(true);
              } catch (final IllegalArgumentException ex) {
                context.getDialogProvider().msgError(null, BUNDLE.getString("Images.Plugin.Error"));
                LOGGER.warn("Can't load image file : " + selected); //NOI18N
              } catch (final Exception ex) {
                context.getDialogProvider().msgError(null, BUNDLE.getString("Images.Plugin.Error"));
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

    final ButtonGroup group = UI_COMPO_FACTORY.makeButtonGroup();

    int selectedIndex = lastSelectedImportIndex;

    for (int i = 0; i < options.length; i++) {
      final JRadioButton button = UI_COMPO_FACTORY.makeRadioButton();
      button.setText(options[i]);
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

  private boolean containAttribute(@Nonnull PluginContext context, @Nullable final Topic activeTopic) {
    boolean result = false;
    if (activeTopic != null) {
      result |= activeTopic.getAttribute(ImageVisualAttributePlugin.ATTR_KEY) != null;
    }
    if (!result) {
      for (final Topic t : context.getSelectedTopics()) {
        result |= t.getAttribute(ImageVisualAttributePlugin.ATTR_KEY) != null;
        if (result) {
          break;
        }
      }
    }
    return result;
  }

  private void setAttributeToTopic(
      @Nonnull final Topic topic,
      @Nullable final String packedImage,
      @Nullable final String imageFilePath,
      @Nullable final String imageName
  ) {
    topic.setAttribute(ImageVisualAttributePlugin.ATTR_KEY, packedImage);
    topic.setAttribute(ImageVisualAttributePlugin.ATTR_IMAGE_NAME, imageName);
    topic.setAttribute(ImageVisualAttributePlugin.ATTR_IMAGE_URI_KEY, imageFilePath);
  }

  private void setAttribute(@Nullable final PluginContext context,
                            @Nullable final Topic activeTopic,
                            @Nullable final String packedImage,
                            @Nullable final String imageFilePath,
                            @Nullable final String imageName
  ) {
    if (activeTopic != null) {
      setAttributeToTopic(activeTopic, packedImage, imageFilePath, imageName);
    }
    for (final Topic t : context.getSelectedTopics()) {
      this.setAttributeToTopic(t, packedImage, imageFilePath, imageName);
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
