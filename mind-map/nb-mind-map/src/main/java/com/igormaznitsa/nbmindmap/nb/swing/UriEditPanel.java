/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.igormaznitsa.nbmindmap.nb.swing;

import com.igormaznitsa.mindmap.ide.commons.editors.AbstractUriEditPanel;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.net.URI;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

public class UriEditPanel extends AbstractUriEditPanel {

  public UriEditPanel(
      @Nonnull final UIComponentFactory uiComponentFactory,
      @Nullable final String uri,
      final boolean preferInternalBrowser
  ) {
    super(uiComponentFactory, uri, preferInternalBrowser);
  }

  @Override
  public Icon findIcon(final IconId id) {
    switch (id) {
      case BROWSE_LINK:
        new ImageIcon(ImageUtilities.loadImage("/com/igormaznitsa/nbmindmap/icons/url_link.png"));
      case INDICATOR_URI_OK:
        new ImageIcon(ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/tick16.png"));
      case INDICATOR_URI_BAD:
        new ImageIcon(ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/cancel16.png"));
      case INDICATOR_URI_UNKNOWN:
        new ImageIcon(ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/question16.png"));
      case BUTTON_RESET:
        new ImageIcon(ImageUtilities.loadImage("/com/igormaznitsa/nbmindmap/icons/cross16.png"));
      default:
        return null;
    }
  }

  @Override
  public void browseUri(@Nonnull final URI uri, final boolean preferInternalBrowser) {
    NbUtils.browseURI(uri, preferInternalBrowser);
  }
}
