/*
 * Copyright (C) 2015-2024 Igor A. Maznitsa
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

package com.igormaznitsa.ksrender;

import static java.lang.String.format;

import java.util.regex.Pattern;

public final class KsRenderUtils {
  private static final Pattern GLOBAL_STORAGE_SUBTOPOLOGY =
      Pattern.compile(".*global.*store.*", Pattern.CASE_INSENSITIVE);

  private KsRenderUtils() {
  }

  public static String unicodeString(final String text) {
    final StringBuilder result = new StringBuilder(text.length() + 4);
    final char[] chars = text.toCharArray();
    for (final char c : chars) {
      if (c > 127 || c == '&' || c == '|') {
        result.append("&#").append((int) c).append(';');
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  public static boolean isGlobalStorageSubTopology(
      final KStreamsTopologyDescriptionParser.SubTopology topology) {
    final String comment = topology.comment;
    if (comment == null || comment.isEmpty()) {
      return false;
    }
    return GLOBAL_STORAGE_SUBTOPOLOGY.matcher(comment).matches();
  }

  public static String makeCommentNote(final String componentId,
                                       final KStreamsTopologyDescriptionParser.TopologyElement element) {
    if (element.comment == null || element.comment.isEmpty()) {
      return "";
    }
    final String noteId = "nte_" + Integer.toHexString(element.id.hashCode()) + '_' +
        Long.toHexString(System.nanoTime());
    return format("note \"%s\" as %s%n%s%n", unicodeString(element.comment), noteId,
        componentId == null ? "" : componentId + " --> " + noteId);
  }


  public static String preprocessId(final IdType idType, final String elementId) {
    switch (idType) {
      case STORES:
        return "store_" + elementId;
      case TOPICS:
        return "topic_" + elementId;
      default:
        return elementId;
    }
  }


  public static String makePumlMultiline(final String text,
                                         final int maxNonSplittedLength) {
    if (text.length() < maxNonSplittedLength) {
      return text;
    }
    return text.replace("-", "-\\n")
        .replace(" ", "\\n")
        .replace("_", "_\\n");
  }

}
