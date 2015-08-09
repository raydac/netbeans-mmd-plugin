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
package com.igormaznitsa.nbmindmap.mmgui;

import java.awt.Color;
import java.awt.Font;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Configuration {

  public interface ConfigurationListener {

    void onConfigurationPropertyChanged(final Configuration source);
  }

  private final List<WeakReference<ConfigurationListener>> listeners = new ArrayList<WeakReference<ConfigurationListener>>();

  private int collapsatorSize = 16;
  private int textMargins = 10;
  private int otherLevelVerticalInset = 16;
  private int otherLevelHorizontalInset = 32;
  private int firstLevelVerticalInset = 32;
  private int firstLevelHorizontalInset = 48;
  private int paperMargins = 20;
  private int selectLineGap = 5;
  private int horizontalBlockGap = 5;

  private boolean drawBackground = true;
  private Color paperColor = new Color(0x617B94);
  private Color gridColor = paperColor.darker();
  private boolean showGrid = true;
  private int gridStep = 32;
  private Color rootBackgroundColor = new Color(0x031A31);
  private Color firstLevelBackgroundColor = new Color(0xB1BFCC);
  private Color otherLevelBackgroundColor = new Color(0xFDFDFD);
  private Color rootTextColor = Color.WHITE;
  private Color firstLevelTextColor = Color.BLACK;
  private Color otherLevelTextColor = Color.BLACK;
  private Color elementBorderColor = Color.BLACK;
  private Color connectorColor = Color.WHITE;
  private Color shadowColor = new Color(0x30000000, true);
  private Color collapsatorBorderColor = Color.DARK_GRAY;
  private Color collapsatorBackgroundColor = Color.WHITE;
  private Color selectLineColor = Color.ORANGE;

  private float elementBorderWidth = 1.0f;
  private float collapsatorBorderWidth = 1.0f;
  private float connectorWidth = 1.5f;
  private float selectLineWidth = 2.0f;

  private Font font = new Font("Arial", Font.BOLD, 14);
  private float scale = 1.0f;
  private boolean dropShadow = true;

  private transient volatile boolean notificationEnabled = true;

  public Configuration(final Configuration cfg, final boolean copyListeners) {
    this();
    this.makeFullCopyOf(cfg, copyListeners, false);
  }

  public void makeAtomicChange(final Runnable runnable) {
    this.notificationEnabled = false;
    try{
      runnable.run();
    }finally{
      this.notificationEnabled = true;
      notifyCfgListenersAboutChange();
    }
  }

  public void makeFullCopyOf(final Configuration src, final boolean copyListeners, final boolean makeNotification) {
    for (final Field f : Configuration.class.getDeclaredFields()) {
      if (f.getName().equals("listeners")) {
        if (copyListeners) {
          this.listeners.clear();
          for (final WeakReference<ConfigurationListener> weakContainer : src.listeners) {
            final ConfigurationListener theListener = weakContainer.get();
            if (theListener != null) {
              this.listeners.add(new WeakReference<ConfigurationListener>(theListener));
            }
          }
        }
      }
      else {
        try {
          f.set(this, f.get(src));
        }
        catch (Exception ex) {
          throw new Error("Unexpected state during cloning");
        }
      }
    }
    if (makeNotification) {
      this.notifyCfgListenersAboutChange();
    }
  }

  public void addConfigurationListener(final ConfigurationListener l) {
    this.listeners.add(new WeakReference<ConfigurationListener>(l));
  }

  public void removeConfigurationListener(final ConfigurationListener l) {
    final Iterator<WeakReference<ConfigurationListener>> iter = this.listeners.iterator();
    while (iter.hasNext()) {
      final WeakReference<ConfigurationListener> wr = iter.next();
      final ConfigurationListener c = wr.get();
      if (c == null || c == l) {
        iter.remove();
      }
    }
  }

  private void notifyCfgListenersAboutChange() {
    if (this.notificationEnabled) {
      for (final WeakReference<ConfigurationListener> l : this.listeners) {
        final ConfigurationListener c = l.get();
        if (c != null) {
          c.onConfigurationPropertyChanged(this);
        }
      }
    }
  }

  public Configuration() {
  }

  public int getHorizontalBlockGap() {
    return this.horizontalBlockGap;
  }

  public void setHorizontalBlockGap(final int gap) {
    this.horizontalBlockGap = gap;
    notifyCfgListenersAboutChange();
  }

  public float getSelectLineWidth() {
    return this.selectLineWidth;
  }

  public void setSelectLineWidth(final float f) {
    this.selectLineWidth = f;
    notifyCfgListenersAboutChange();
  }

  public Color getSelectLineColor() {
    return this.selectLineColor;
  }

  public void setSelectLineColor(final Color color) {
    this.selectLineColor = color;
    notifyCfgListenersAboutChange();
  }

  public void setPaperMargins(final int size) {
    this.paperMargins = size;
    notifyCfgListenersAboutChange();
  }

  public int getPaperMargins() {
    return this.paperMargins;
  }

  public boolean isDrawBackground() {
    return this.drawBackground;
  }

  public void setDrawBackground(final boolean flag) {
    this.drawBackground = flag;
    notifyCfgListenersAboutChange();
  }

  public void setOtherLevelVerticalInset(final int value) {
    this.otherLevelVerticalInset = value;
    notifyCfgListenersAboutChange();
  }

  public int getOtherLevelVerticalInset() {
    return this.otherLevelVerticalInset;
  }

  public void setOtherLevelHorizontalInset(final int value) {
    this.otherLevelHorizontalInset = value;
    notifyCfgListenersAboutChange();
  }

  public int getOtherLevelHorizontalInset() {
    return this.otherLevelHorizontalInset;
  }

  public void setFirstLevelVerticalInset(final int value) {
    this.firstLevelVerticalInset = value;
    notifyCfgListenersAboutChange();
  }

  public int getFirstLevelVerticalInset() {
    return this.firstLevelVerticalInset;
  }

  public void setFirstLevelHorizontalInset(final int value) {
    this.firstLevelHorizontalInset = value;
    notifyCfgListenersAboutChange();
  }

  public int getFirstLevelHorizontalInset() {
    return this.firstLevelHorizontalInset;
  }

  public Color getPaperColor() {
    return this.paperColor;
  }

  public void setPaperColor(final Color color) {
    this.paperColor = color;
    notifyCfgListenersAboutChange();
  }

  public void setGridColor(final Color color) {
    this.gridColor = color;
    notifyCfgListenersAboutChange();
  }

  public Color getGridColor() {
    return this.gridColor;
  }

  public void setShowGrid(final boolean flag) {
    this.showGrid = flag;
    notifyCfgListenersAboutChange();
  }

  public boolean isShowGrid() {
    return this.showGrid;
  }

  public void setGridStep(final int step) {
    this.gridStep = step;
    notifyCfgListenersAboutChange();
  }

  public int getGridStep() {
    return this.gridStep;
  }

  public void setRootBackgroundColor(final Color color) {
    this.rootBackgroundColor = color;
    notifyCfgListenersAboutChange();
  }

  public Color getRootBackgroundColor() {
    return this.rootBackgroundColor;
  }

  public Color getFirstLevelBackgroundColor() {
    return this.firstLevelBackgroundColor;
  }

  public void setFirstLevelBackgroundColor(final Color color) {
    this.firstLevelBackgroundColor = color;
    notifyCfgListenersAboutChange();
  }

  public void setOtherLevelBackgroundColor(final Color color) {
    this.otherLevelBackgroundColor = color;
    notifyCfgListenersAboutChange();
  }

  public Color getOtherLevelBackgroundColor() {
    return this.otherLevelBackgroundColor;
  }

  public Color getRootTextColor() {
    return this.rootTextColor;
  }

  public void setRootTextColor(final Color color) {
    this.rootTextColor = color;
    notifyCfgListenersAboutChange();
  }

  public void setFirstLevelTextColor(final Color color) {
    this.firstLevelTextColor = color;
    notifyCfgListenersAboutChange();
  }

  public Color getFirstLevelTextColor() {
    return this.firstLevelTextColor;
  }

  public Color getOtherLevelTextColor() {
    return this.otherLevelTextColor;
  }

  public void setOtherLevelTextColor(final Color color) {
    this.otherLevelTextColor = color;
    notifyCfgListenersAboutChange();
  }

  public Color getElementBorderColor() {
    return this.elementBorderColor;
  }

  public void setElementBorderColor(final Color color) {
    this.elementBorderColor = color;
    notifyCfgListenersAboutChange();
  }

  public void setConnectorColor(final Color color) {
    this.connectorColor = color;
    notifyCfgListenersAboutChange();
  }

  public Color getConnectorColor() {
    return this.connectorColor;
  }

  public void setShadowColor(final Color color) {
    this.shadowColor = color;
    notifyCfgListenersAboutChange();
  }

  public Color getShadowColor() {
    return this.shadowColor;
  }

  public Color getCollapsatorBorderColor() {
    return this.collapsatorBorderColor;
  }

  public void setCollapsatorBorderColor(final Color color) {
    this.collapsatorBorderColor = color;
    notifyCfgListenersAboutChange();
  }

  public Color getCollapsatorBackgroundColor() {
    return this.collapsatorBackgroundColor;
  }

  public void setCollapsatorBackgroundColor(final Color color) {
    this.collapsatorBackgroundColor = color;
    notifyCfgListenersAboutChange();
  }

  public void setElementBorderWidth(final float value) {
    this.elementBorderWidth = value;
    notifyCfgListenersAboutChange();
  }

  public float getElementBorderWidth() {
    return this.elementBorderWidth;
  }

  public float getCollapsatorBorderWidth() {
    return this.collapsatorBorderWidth;
  }

  public void setCollapsatorBorderWidth(final float width) {
    this.collapsatorBorderWidth = width;
    notifyCfgListenersAboutChange();
  }

  public float getConnectorWidth() {
    return this.connectorWidth;
  }

  public void setConnectorWidth(final float value) {
    this.connectorWidth = value;
    notifyCfgListenersAboutChange();
  }

  public void setFont(final Font f) {
    this.font = f;
    notifyCfgListenersAboutChange();
  }

  public Font getFont() {
    return this.font;
  }

  public float getScale() {
    return this.scale;
  }

  public void setScale(final float value) {
    this.scale = Math.max(0.3f, Math.min(15f, value));
    notifyCfgListenersAboutChange();
  }

  public boolean isDropShadow() {
    return this.dropShadow;
  }

  public void setDropShadow(final boolean value) {
    this.dropShadow = value;
    notifyCfgListenersAboutChange();
  }

  public int getCollapsatorSize() {
    return this.collapsatorSize;
  }

  public void setCollapsatorSize(final int size) {
    this.collapsatorSize = size;
    notifyCfgListenersAboutChange();
  }

  public int getTextMargins() {
    return this.textMargins;
  }

  public void setTextMargins(final int value) {
    this.textMargins = value;
    notifyCfgListenersAboutChange();
  }

  public int getSelectLineGap() {
    return this.selectLineGap;
  }

  public void setSelectLineGap(final int value) {
    this.selectLineGap = value;
    notifyCfgListenersAboutChange();
  }

}
