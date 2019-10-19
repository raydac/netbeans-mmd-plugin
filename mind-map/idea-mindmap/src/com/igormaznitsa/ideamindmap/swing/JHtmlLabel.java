/*
 * Copyright 2015-2018 Igor Maznitsa.
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

package com.igormaznitsa.ideamindmap.swing;

import com.intellij.ui.components.JBLabel;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Icon;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

/**
 * Small adaptation of javax.swing.JLabel to catch clicks on HTML links in the
 * label.
 * <b>The Component supports only HTML text and if the text is not into &lt;html&gt;
 * tags then such tags will be added automatically.</b>
 *
 * @author Igor Maznitsa (http://www.igormaznitsa.com)
 */
public class JHtmlLabel extends JBLabel {

  private static final long serialVersionUID = -166975925687523220L;
  private final List<LinkListener> linkListeners = new CopyOnWriteArrayList<LinkListener>();
  /**
   * Inside cache of detected link elements.
   */
  private transient List<HtmlLinkAddress> linkCache = null;
  private boolean showLinkAddressInToolTip = false;
  private int minClickCountToActivateLink = 1;
  public JHtmlLabel(final String text, final Icon icon, final int horizontalAlignment) {
    super(text, icon, horizontalAlignment);

    final JHtmlLabel theInstance = this;

    final MouseAdapter mouseAdapter = new MouseAdapter() {
      @Override
      public void mouseMoved(final MouseEvent e) {
        final String link = getLinkAtPosition(e.getPoint());
        if (link == null) {
          if (showLinkAddressInToolTip) {
            setToolTipText(null);
          }
          setCursor(Cursor.getDefaultCursor());
        } else {
          if (showLinkAddressInToolTip) {
            setToolTipText(link);
          }
          setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
      }

      @Override
      public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() >= minClickCountToActivateLink) {
          final String link = getLinkAtPosition(e.getPoint());
          if (link != null) {
            for (final LinkListener l : linkListeners) {
              l.onLinkActivated(theInstance, link);
            }
          }
        }
      }
    };

    this.addMouseListener(mouseAdapter);
    this.addMouseMotionListener(mouseAdapter);
  }
  public JHtmlLabel(final String text, final int horizontalAlignment) {
    this(text, null, horizontalAlignment);
  }

  public JHtmlLabel(final String text) {
    this(text, null, LEADING);
  }

  public JHtmlLabel(final Icon image, final int horizontalAlignment) {
    this(null, image, horizontalAlignment);
  }

  public JHtmlLabel(final Icon image) {
    this(null, image, CENTER);
  }

  public JHtmlLabel() {
    this("", null, LEADING);
  }

  public int getMinClickCountToActivateLink() {
    return this.minClickCountToActivateLink;
  }

  public void setMinClickCountToActivateLink(final int clickNumber) {
    this.minClickCountToActivateLink = Math.max(1, clickNumber);
  }

  public boolean isShowLinkAddressInTooltip() {
    return this.showLinkAddressInToolTip;
  }

  public void setShowLinkAddressInTooltip(final boolean flag) {
    if (this.showLinkAddressInToolTip) {
      if (!flag) {
        this.setToolTipText(null);
      }
    }
    this.showLinkAddressInToolTip = flag;
  }

  public void addLinkListener(final LinkListener l) {
    this.linkListeners.add(l);
  }

  public void removeLinkListener(final LinkListener l) {
    this.linkListeners.remove(l);
  }

  public void replaceMacroses(final Properties properties) {
    String text = this.getText();
    for (final String k : properties.stringPropertyNames()) {
      text = text.replace("${" + k + "}", properties.getProperty(k));
    }
    this.setText(text);
  }

  @Override
  public void setText(final String text) {
    super.setText(text.toLowerCase(Locale.ENGLISH).trim().startsWith("<html>") ? text : "<html>" + text + "</html>");
    this.linkCache = null;
  }

  private void cacheLinkElements() {
    this.linkCache = new ArrayList<HtmlLinkAddress>();
    final View view = (View) this.getClientProperty("html");
    if (view != null) {
      final HTMLDocument doc = (HTMLDocument) view.getDocument();
      final HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.A);
      while (it.isValid()) {
        final SimpleAttributeSet s = (SimpleAttributeSet) it.getAttributes();
        final String link = (String) s.getAttribute(HTML.Attribute.HREF);
        if (link != null) {
          this.linkCache.add(new HtmlLinkAddress(link, it.getStartOffset(), it.getEndOffset()));
        }
        it.next();
      }
    }
  }

  private String getLinkAtPosition(final Point point) {
    if (this.linkCache == null) {
      cacheLinkElements();
    }

    final AccessibleJLabel accessibleJLabel = (AccessibleJLabel) this.getAccessibleContext().getAccessibleComponent();
    final int textIndex = accessibleJLabel.getIndexAtPoint(point);
    for (final HtmlLinkAddress l : this.linkCache) {
      if (l.checkPosition(textIndex)) {
        return l.getHREF();
      }
    }
    return null;
  }

  /**
   * Listener to get notification about activation of a link.
   */
  public interface LinkListener extends Serializable {
    /**
     * Called if detected activation of a link placed on the label.
     *
     * @param source the label, must not be null
     * @param link   the link to be processed, must not be null
     */
    void onLinkActivated(JHtmlLabel source, String link);
  }

  /**
   * Internal auxiliary class to keep cached parameters of found link elements.
   */
  private static final class HtmlLinkAddress {

    private final String address;
    private final int start;
    private final int end;

    HtmlLinkAddress(final String address, final int startOffset, final int endOffset) {
      this.address = address;
      this.start = startOffset;
      this.end = endOffset;
    }

    String getHREF() {
      return this.address;
    }

    boolean checkPosition(final int position) {
      return position >= this.start && position < this.end;
    }
  }
}
