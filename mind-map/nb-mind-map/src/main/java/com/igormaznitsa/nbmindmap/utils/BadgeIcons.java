/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.UIManager;

import org.netbeans.api.annotations.common.StaticResource;
import org.openide.util.ImageUtilities;

public final class BadgeIcons {
  private static final Image BADGE_ICON_10 = ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/logo/badge.png");
  private static final Image BADGE_ICON_16 = ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/logo/logo16.png");

  public static final Image BADGED_FOLDER = makeBadgedImage(getTreeFolderIcon(false));
  public static final Image BADGED_FOLDER_OPEN = makeBadgedImage(getTreeFolderIcon(true));

  private static final @StaticResource String ICON_PATH = "com/igormaznitsa/nbmindmap/icons/folder16.gif"; // NOI18N
  private static final @StaticResource String OPENED_ICON_PATH = "com/igormaznitsa/nbmindmap/icons/folderOpen16.gif"; // NOI18N
  
  private BadgeIcons(){};

  private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
  private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
  private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
  private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N

  @Nonnull
  private static Image makeBadgedImage(@Nonnull final Image original){
    final BufferedImage result = new BufferedImage(original.getWidth(null), original.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    final Graphics2D gfx = result.createGraphics();
    try{
      gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      gfx.drawImage(original, 0, 0, null);
      
      final Image badge = original.getWidth(null)>16 ? BADGE_ICON_16 : BADGE_ICON_10;
      
      gfx.drawImage(badge, result.getWidth()-badge.getWidth(null),result.getHeight()- badge.getHeight(null), null);
    }finally{
      gfx.dispose();
    }
    return result;
  }

  @Nonnull
  public static Image getTreeFolderIcon(boolean opened) {
    Image base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263;
    if (base == null) {
      Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
      if (baseIcon != null) {
        base = ImageUtilities.icon2Image(baseIcon);
      }
      else { // fallback to our owns
        base = ImageUtilities.loadImage(opened ? OPENED_ICON_PATH : ICON_PATH, true);
      }
    }
    return base;
  }

}
