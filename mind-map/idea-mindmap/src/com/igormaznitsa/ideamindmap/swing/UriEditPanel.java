package com.igormaznitsa.ideamindmap.swing;

import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.ide.commons.editors.AbstractUriEditPanel;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import java.net.URI;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.Icon;

public class UriEditPanel extends AbstractUriEditPanel {

    public UriEditPanel(@Nonnull final UIComponentFactory uiComponentFactory, @Nullable final String uri, final boolean prefeeInternalBrowser) {
        super(uiComponentFactory, uri, prefeeInternalBrowser);
    }

    @Nullable
    @Override
    public Icon findIcon(@Nonnull final IconId id) {
        switch (id) {
            case BROWSE_LINK: return AllIcons.Buttons.URL_LINK_BIG;
            case BUTTON_RESET: return AllIcons.Buttons.CROSS;
            case INDICATOR_URI_BAD: 
                return AllIcons.Buttons.CANCEL;
            case INDICATOR_URI_OK: 
                return AllIcons.Buttons.TICK;
            case INDICATOR_URI_UNKNOWN: 
                return AllIcons.Buttons.QUESTION;
            default: return null;    
        }
    }

    
    
    @Override
    public void browseUri(@Nullable final URI uri, final boolean preferInternalBrowser) {
        IdeaUtils.browseURI(uri, false);
    }
    
    
    
}
