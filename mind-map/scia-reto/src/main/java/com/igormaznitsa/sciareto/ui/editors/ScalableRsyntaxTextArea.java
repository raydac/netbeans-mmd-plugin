/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.igormaznitsa.sciareto.ui.editors;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.sciareto.preferences.AdditionalPreferences;
import com.igormaznitsa.sciareto.ui.UiUtils;
import java.awt.Font;
import java.awt.event.MouseWheelEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RUndoManager;

public final class ScalableRsyntaxTextArea extends RSyntaxTextArea {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScalableRsyntaxTextArea.class);

  public static final Font DEFAULT_FONT =
      MindMapPanelConfig.findDefaultFont(Font.PLAIN, 14, Font.DIALOG,
          new String[] {"JetBrains Mono Medium"});
  private static final float SCALE_STEP = 0.5f;
  private static final float SCALE_MIN = 0.03f;
  private static final float SCALE_MAX = 10.0f;
  private static final Theme THEME_DARK = loadTheme("dark.xml");
  private static final Theme THEME_LIGHT = loadTheme("default.xml");
  private transient RUndoManagerExt undoManager;
  private float fontScale = 1.0f;
  private float fontOriginalSize;
  private MindMapPanelConfig config;

  public ScalableRsyntaxTextArea(@Nonnull final MindMapPanelConfig mmConfig) {
    super();
    this.createUndoManager();

    this.config = mmConfig;

    this.setFont(mmConfig.getOptionalProperty(AdditionalPreferences.PROPERTY_TEXT_EDITOR_FONT,
        DEFAULT_FONT));
    this.fontOriginalSize = this.getFont().getSize2D();

    this.addMouseWheelListener((@Nonnull final MouseWheelEvent e) -> {
      if (!e.isConsumed() && ((e.getModifiers() & this.config.getScaleModifiers()) ==
          this.config.getScaleModifiers())) {
        e.consume();
        this.fontScale = Math.max(SCALE_MIN,
            Math.min(SCALE_MAX, this.fontScale + SCALE_STEP * -e.getWheelRotation()));
        updateFontForScale();
      } else {
        this.getParent().dispatchEvent(e);
      }
    });

    if (UiUtils.figureOutThatDarkTheme()) {
      THEME_DARK.apply(this);
    } else {
      THEME_LIGHT.apply(this);
    }

    updateFontForScale();
  }

  @Nonnull
  private static Theme loadTheme(@Nonnull final String name) {
    try (final InputStream in = ScalableRsyntaxTextArea.class.getResourceAsStream(
        "/org/fife/ui/rsyntaxtextarea/themes/" + name)) {
      return Theme.load(in, DEFAULT_FONT);
    } catch (Exception ex) {
      throw new Error("Can't load theme: " + name);
    }
  }

  public void doZoomIn() {
    this.fontScale = Math.min(SCALE_MAX, this.fontScale + SCALE_STEP);
    updateFontForScale();
  }

  public void doZoomOut() {
    this.fontScale = Math.max(SCALE_MIN, this.fontScale - SCALE_STEP);
    updateFontForScale();
  }

  public void doZoomReset() {
    this.fontScale = 1.0f;
    updateFontForScale();
  }

  private void updateFontForScale() {
    final Font newFont = this.getFont().deriveFont(this.fontScale * this.fontOriginalSize);
    if (newFont.getSize() > 0) {
      this.setFont(newFont);
    } else {
      this.setFont(this.getFont().deriveFont(1.0f));
    }
  }

  public void updateConfig(@Nonnull final MindMapPanelConfig mmConfig) {
    this.config = mmConfig;
    this.setFont(mmConfig.getOptionalProperty(AdditionalPreferences.PROPERTY_TEXT_EDITOR_FONT,
        DEFAULT_FONT));
    this.fontOriginalSize = this.getFont().getSize2D();
    updateFontForScale();
    this.revalidate();
    this.repaint();
  }

  @Nonnull
  public RUndoManagerExt getRUndoManager() {
    return this.undoManager;
  }

  @Nonnull
  @Override
  protected RUndoManager createUndoManager() {
    if (this.undoManager == null) {
      this.undoManager = new RUndoManagerExt(ScalableRsyntaxTextArea.this);
    }
    return this.undoManager;
  }

  public static final class RUndoManagerExt extends RUndoManager {
    public RUndoManagerExt(@Nonnull final RTextArea textArea) {
      super(textArea);
    }

    @Override
    public void undoableEditHappened(@Nonnull final UndoableEditEvent e) {
      this.addEdit(e.getEdit());
      this.updateActions();
    }

    @Nonnull
    @MustNotContainNull
    public List<UndoableEdit> getEditHistory() {
      return this.edits;
    }
  }

  @Nonnull
  @MustNotContainNull
  public List<byte[]> serializeEditHistory(final int limit) throws IOException {
    final List<byte[]> result = new ArrayList<>();
    final List<UndoableEdit> edits = new ArrayList<>(this.getRUndoManager().getEditHistory());
    for (int i = 0; i < limit && i < edits.size(); i++) {
      final UndoableEdit edit = edits.get(i);
      if (!(edit instanceof Serializable)) {
        continue;
      }
      try {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
          objectOutputStream.writeObject(edit);
        }
        result.add(outputStream.toByteArray());
      } catch (NotSerializableException ex) {
        // ignore
      } catch (IOException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new IOException("", ex);
      }
    }
    return result;
  }

  public void deserializeEditHistory(@Nonnull @MustNotContainNull final List<byte[]> history) {
    this.getRUndoManager().discardAllEdits();
    try {
      final List<UndoableEdit> result = new ArrayList<>();
      for (final byte[] serialized : history) {
        try (final ObjectInputStream objectInputStream = new ObjectInputStream(
            new ByteArrayInputStream(serialized))) {
          result.add((UndoableEdit) objectInputStream.readObject());
        }
      }
      result.forEach(x -> this.getRUndoManager().addEdit(x));
    } catch (Exception ex) {
      LOGGER.error("Can't deserialize edit history", ex);
    }
  }
}
