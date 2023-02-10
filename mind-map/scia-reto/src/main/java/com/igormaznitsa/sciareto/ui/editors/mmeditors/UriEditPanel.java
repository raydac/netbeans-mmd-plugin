package com.igormaznitsa.sciareto.ui.editors.mmeditors;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.ide.commons.editors.AbstractUriEditPanel;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.sciareto.ui.UiUtils;
import java.net.URI;
import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class UriEditPanel extends AbstractUriEditPanel {

  public UriEditPanel(
      @Nonnull final UIComponentFactory uiComponentFactory,
      @Nonnull final String uri,
      final boolean preferInternalBrowser) {
    super(uiComponentFactory, uri, preferInternalBrowser);
  }

  @Nonnull
  private Icon loadIcon(@Nonnull final String name) {
    return new ImageIcon(requireNonNull(UiUtils.loadIcon(name)));
  }

  @Override
  public Icon findIcon(@Nonnull final IconId id) {
    switch (id) {
      case BUTTON_RESET:
        return this.loadIcon("cross16.png");
      case BROWSE_LINK:
        return this.loadIcon("url_link.png");
      case INDICATOR_URI_BAD:
        return this.loadIcon("cancel.png");
      case INDICATOR_URI_OK:
        return this.loadIcon("tick16.png");
      case INDICATOR_URI_UNKNOWN:
        return this.loadIcon("question16.png");
      default:
        return null;
    }
  }

  @Override
  public void browseUri(@Nonnull final URI uri, final boolean preferInternalBrowser) {
    UiUtils.browseURI(uri, preferInternalBrowser);
  }
}
