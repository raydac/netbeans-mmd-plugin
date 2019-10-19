/*
 * Copyright 2018 Igor Maznitsa.
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

package com.igormaznitsa.mindmap.print;

import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import java.awt.Image;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PrintableObject {

  private final Image image;
  private final MindMapPanel panel;

  private PrintableObject(@Nullable final Image image, @Nullable final MindMapPanel panel) {
    this.panel = panel;
    this.image = image;
  }

  @Nonnull
  public static Builder newBuild() {
    return new Builder();
  }

  @Nullable
  public MindMapPanel getPanel() {
    return this.panel;
  }

  @Nullable
  public Image getImage() {
    return this.image;
  }

  public boolean isMmdPanel() {
    return this.panel != null;
  }

  public boolean isImage() {
    return this.image != null;
  }

  public static class Builder {

    private Image image;
    private MindMapPanel panel;

    private Builder() {
    }

    @Nonnull
    public Builder image(@Nullable final Image image) {
      Assertions.assertNull("Panel already set", this.panel);
      this.image = image;
      return this;
    }

    @Nonnull
    public Builder mmdpanel(@Nullable final MindMapPanel panel) {
      Assertions.assertNull("Image already set", this.image);
      this.panel = panel;
      return this;
    }

    @Nonnull
    public PrintableObject build() {
      Assertions.assertTrue("One object must be set", this.image != null || this.panel != null);
      return new PrintableObject(this.image, this.panel);
    }
  }
}
