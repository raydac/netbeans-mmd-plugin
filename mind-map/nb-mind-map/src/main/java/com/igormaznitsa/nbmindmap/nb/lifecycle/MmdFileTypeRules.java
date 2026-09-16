/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
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

package com.igormaznitsa.nbmindmap.nb.lifecycle;

import static java.util.Locale.ROOT;

import com.igormaznitsa.nbmindmap.nb.editor.MMDDataObject;
import java.util.Collection;
import java.util.Set;

public final class MmdFileTypeRules {

  private static final Set<String> FALLBACK_MIME_TYPES = Set.of(
      "text/plain",
      "content/unknown",
      "application/octet-stream");

  private static final Set<String> SKIPPED_FOLDER_NAMES = Set.of(
      "target",
      ".git",
      "node_modules");

  private MmdFileTypeRules() {
  }

  public static boolean isMindMapExtension(final String extension) {
    return extension != null && MMDDataObject.MMD_EXT.equalsIgnoreCase(extension);
  }

  public static boolean isFallbackMime(final String mimeType) {
    return mimeType != null && FALLBACK_MIME_TYPES.contains(mimeType.toLowerCase(ROOT));
  }

  public static boolean isSkippedFolderName(final String name) {
    return name != null && SKIPPED_FOLDER_NAMES.contains(name.toLowerCase(ROOT));
  }

  public static boolean shouldInvalidateWrongDataObject(
      final String extension,
      final boolean mindMapDataObject) {
    return isMindMapExtension(extension) && !mindMapDataObject;
  }

  public static boolean shouldClearUserMimeMapping(final Collection<String> mappedExtensions) {
    return mappedExtensions != null
        && mappedExtensions.stream().anyMatch(MmdFileTypeRules::isMindMapExtension);
  }
}
