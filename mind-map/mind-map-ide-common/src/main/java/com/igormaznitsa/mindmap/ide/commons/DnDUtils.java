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

package com.igormaznitsa.mindmap.ide.commons;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

public final class DnDUtils {

  private static final String MIME_TEXT_PLAIN = "text/plain";
  private static final String MIME_MOZ_URL = "text/x-moz-url";
  private static final String MIME_TEXT_HTML = "text/html";

  private static final Pattern HTML_LINK_PATTERN = Pattern.compile(
      "^\\s*\\<\\s*a\\s[^>]*?href\\s*=\\s*\\\"(.*?)\\\"[^>]*?\\>.*?\\<\\s*\\/\\s*a\\s*\\>\\s*$",
      Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private DnDUtils() {
  }

  public static boolean isFileOrLinkOrText(@Nonnull final DropTargetDragEvent dtde) {
    boolean result = false;
    for (final DataFlavor flavor : dtde.getCurrentDataFlavors()) {
      if (flavor.isFlavorJavaFileListType() || flavor.isFlavorTextType() ||
          flavor.isMimeTypeEqual(MIME_MOZ_URL) || flavor.isMimeTypeEqual(MIME_TEXT_PLAIN) ||
          flavor.isMimeTypeEqual(MIME_TEXT_HTML)) {
        result = true;
        break;
      }
    }
    return result;
  }

  @Nonnull
  public static String removeZeroChars(@Nonnull final String str) {
    final StringBuilder buffer = new StringBuilder(str.length());
    for (final char c : str.toCharArray()) {
      if (c != 0) {
        buffer.append(c);
      }
    }
    return buffer.toString();
  }

  @Nullable
  public static String extractHtmlLink(final boolean removeZeroChars, @Nullable final String text) {
    if (text == null) {
      return null;
    }
    final String textToProcess;
    if (removeZeroChars) {
      textToProcess = removeZeroChars(text);
    } else {
      textToProcess = text;
    }

    final Matcher matcher = HTML_LINK_PATTERN.matcher(textToProcess);
    String result = null;
    if (matcher.find()) {
      result = matcher.group(1);
    }
    return result;
  }

  @Nullable
  public static String extractDropLink(@Nonnull final DropTargetDropEvent dtde) throws Exception {
    String foundHtmlLink = null;
    String foundMozLink = null;
    for (final DataFlavor df : dtde.getCurrentDataFlavors()) {
      if (df.getRepresentationClass() == String.class) {
        if (foundHtmlLink == null && df.isMimeTypeEqual(MIME_TEXT_HTML)) {
          final String link =
              extractHtmlLink(true, (String) dtde.getTransferable().getTransferData(df));
          if (link != null) {
            foundHtmlLink = link;
          }
        }
      } else if (df.getRepresentationClass() == InputStream.class &&
          df.isMimeTypeEqual(MIME_MOZ_URL)) {
        if (foundMozLink == null) {
          final InputStream in = ((InputStream) dtde.getTransferable().getTransferData(df));
          if (in != null) {
            final StringWriter string = new StringWriter();
            IOUtils.copy(in, string, Charset.defaultCharset());
            IOUtils.closeQuietly(in);
            foundMozLink = removeZeroChars(string.toString().split("\\n")[0]).trim();
          }
        }
      }
    }
    return foundMozLink == null ? foundHtmlLink : foundMozLink;
  }

  private static final Pattern EMPTY_OR_WHITE = Pattern.compile("[\\s\\n]");

  public static boolean isUriString(@Nullable final String str) {
    if (str == null) {
      return false;
    }
    try {
      if (EMPTY_OR_WHITE.matcher(str).find()) {
        return false;
      }
      final URI uri = new URI(str);
      return uri.isAbsolute();
    } catch (URISyntaxException ex) {
      return false;
    }
  }

  @Nullable
  public static String extractDropNote(@Nonnull final DropTargetDropEvent dtde) throws Exception {
    String result;
    try {
      result = (String) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
      result = result == null ? null : removeZeroChars(result);
    } catch (NotSerializableException | UnsupportedFlavorException ex) {
      result = null;
    }
    return result;
  }

  @Nullable
  public static URI extractUrlLinkFromFile(@Nonnull final File file) {
    URI result = null;
    if (file.isFile()) {
      if (file.getName().endsWith(".url") || (SystemUtils.IS_OS_WINDOWS && file.length() < 1024)) {
        try {
          final String uri = new UrlFile(file).getURL();
          result = uri == null ? null : new URI(uri);
        } catch (URISyntaxException | IOException ex) {
          result = null;
        }
      }
    }
    return result;
  }

}
