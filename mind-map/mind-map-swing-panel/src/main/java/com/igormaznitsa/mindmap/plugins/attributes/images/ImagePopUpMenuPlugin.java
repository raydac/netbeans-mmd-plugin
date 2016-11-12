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

import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FileUtils;
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
  private static final int MAX_IMAGE_SIDE_SIZE_IN_PIXELS = 350;

  public static final String PROPERTY_MAX_EMBEDDED_IMAGE_SIDE_SIZE = "mmap.max.image.side.size"; //NOI18N

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
                  setAttribute(prepareImageAndEncodeInBase64((Image) transferable.getTransferData(DataFlavor.imageFlavor)), topic, selectedTopics);
                  panel.notifyModelChanged();
                }
                catch (final IllegalArgumentException ex) {
                  dialogProvider.msgError(BUNDLE.getString("Images.Plugin.Error"));
                  LOGGER.error("Can't import from clipboard image",ex); //NOI18N
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
                setAttribute(prepareImageAndEncodeInBase64(selected), topic, selectedTopics);
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

  private static int getMaxImageSize() {
    int result = MAX_IMAGE_SIDE_SIZE_IN_PIXELS;
    try {
      final String defined = System.getProperty(PROPERTY_MAX_EMBEDDED_IMAGE_SIDE_SIZE);
      if (defined != null) {
        LOGGER.info("Detected redefined max size for embedded image side : " + defined); //NOI18N
        result = Math.max(8, Integer.parseInt(defined.trim()));
      }
    }
    catch (NumberFormatException ex) {
      LOGGER.error("Error during image size decoding : ", ex); //NOI18N
    }
    return result;
  }

  @Nonnull
  private static String prepareImageAndEncodeInBase64(@Nonnull Image image) throws Exception {
    final int width = image.getWidth(null);
    final int height = image.getHeight(null);

    final int maxImageSideSize = getMaxImageSize();

    final float imageScale = width > maxImageSideSize || height > maxImageSideSize ? (float) maxImageSideSize / (float) Math.max(width, height) : 1.0f;

    if (!(image instanceof RenderedImage) || Float.compare(imageScale, 1.0f) != 0) {
      final int swidth;
      final int sheight;

      if (Float.compare(imageScale, 1.0f) == 0) {
        swidth = width;
        sheight = height;
      } else {
        swidth = Math.round(imageScale * width);
        sheight = Math.round(imageScale * height);
      }

      final BufferedImage buffer = new BufferedImage(swidth, sheight, BufferedImage.TYPE_INT_ARGB);
      final Graphics2D gfx = (Graphics2D) buffer.createGraphics();

      gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      gfx.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
      gfx.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

      gfx.drawImage(image, AffineTransform.getScaleInstance(imageScale, imageScale), null);
      gfx.dispose();
      image = buffer;
    }

    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write((RenderedImage) image, "png", bos); //NOI18N
    bos.close();

    return Utils.base64encode(bos.toByteArray());
  }

  @Nonnull
  private static String prepareImageAndEncodeInBase64(@Nonnull final File file) throws Exception {
    final byte[] data = FileUtils.readFileToByteArray(file);

    final Image image = ImageIO.read(new ByteArrayInputStream(data));
    if (image == null) {
      throw new IllegalArgumentException("Can't load image file : " + file); //NOI18N
    }

    return prepareImageAndEncodeInBase64(image);
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
