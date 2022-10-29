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

import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import java.awt.Image;

public final class PrintableObject {

  private final Image image;
  private final MindMapPanel panel;

  private PrintableObject(final Image image, final MindMapPanel panel) {
    this.panel = panel;
    this.image = image;
  }

  public static Builder newBuild() {
    return new Builder();
  }

  public MindMapPanel getPanel() {
    return this.panel;
  }

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

    public Builder image(final Image image) {
      if (this.panel != null) {
        throw new IllegalStateException("Panel already set");
      }
      this.image = image;
      return this;
    }

    public Builder mmdpanel(final MindMapPanel panel) {
      if (this.image != null) {
        throw new IllegalStateException("Image already set");
      }
      this.panel = panel;
      return this;
    }

    public PrintableObject build() {
      if (!(this.image != null || this.panel != null)) {
        throw new IllegalStateException("One object must be set");
      }
      return new PrintableObject(this.image, this.panel);
    }
  }
}
