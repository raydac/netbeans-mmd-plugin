package com.igormaznitsa.sciareto.ui.editors;

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

final class ScalableImage extends JComponent {

  private static final long serialVersionUID = 6804581090800919466L;
  private static final float SCALE_STEP = 0.2f;
  private final MindMapPanelConfig config = new MindMapPanelConfig();
  private BufferedImage image;
  private float scale = 1.0f;

  public ScalableImage() {
    super();
    this.addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(@Nonnull final MouseWheelEvent e) {
        if (!e.isConsumed() && ((e.getModifiers() & config.getScaleModifiers()) == config.getScaleModifiers())) {
          scale = Math.max(0.2f, Math.min(scale + (SCALE_STEP * -e.getWheelRotation()), 10.0f));
          revalidate();
          repaint();
        }
      }
    });
  }

  public void updateConfig() {
    this.config.loadFrom(PreferencesManager.getInstance().getPreferences());
  }

  @Nonnull
  public MindMapPanelConfig getConfig() {
    return this.config;
  }

  @Override
  @Nonnull
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override
  @Nonnull
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  @Override
  @Nonnull
  public Dimension getPreferredSize() {
    if (image == null) {
      return new Dimension(16, 16);
    } else {
      return new Dimension(Math.round(this.image.getWidth() * this.scale), Math.round(this.image.getHeight() * this.scale));
    }
  }

  @Override
  public void paintComponent(@Nonnull final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g;
    gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    gfx.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

    final Rectangle bounds = this.getBounds();
    if (this.image == null) {
      gfx.setColor(Color.BLACK);
      gfx.fillRect(0, 0, bounds.width, bounds.height);
      gfx.setColor(Color.RED);
      final String text = "Can't load image, check the log!";
      gfx.drawString(text, (bounds.width - gfx.getFontMetrics().stringWidth(text)) / 2, (bounds.height - gfx.getFontMetrics().getMaxAscent()) / 2);
    } else {
      final Dimension size = getPreferredSize();
      gfx.drawImage(this.image, Math.max(0, (bounds.width - size.width) / 2), Math.max(0, (bounds.height - size.height) / 2), size.width, size.height, null);
    }
  }

  @Nullable
  public BufferedImage getImage() {
    return this.image;
  }

  public void setImage(@Nullable final BufferedImage image) {
    this.image = image;
    this.scale = 1.0f;
    revalidate();
    repaint();
  }

}
