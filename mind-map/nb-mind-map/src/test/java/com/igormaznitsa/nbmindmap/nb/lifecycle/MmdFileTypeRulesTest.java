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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class MmdFileTypeRulesTest {

  @Test
  public void testIsMindMapExtension() {
    assertTrue(MmdFileTypeRules.isMindMapExtension("mmd"));
    assertTrue(MmdFileTypeRules.isMindMapExtension("MMD"));
    assertTrue(MmdFileTypeRules.isMindMapExtension("Mmd"));
    assertFalse(MmdFileTypeRules.isMindMapExtension("txt"));
    assertFalse(MmdFileTypeRules.isMindMapExtension(""));
    assertFalse(MmdFileTypeRules.isMindMapExtension(null));
  }

  @Test
  public void testIsFallbackMime() {
    assertTrue(MmdFileTypeRules.isFallbackMime("text/plain"));
    assertTrue(MmdFileTypeRules.isFallbackMime("TEXT/PLAIN"));
    assertTrue(MmdFileTypeRules.isFallbackMime("content/unknown"));
    assertTrue(MmdFileTypeRules.isFallbackMime("application/octet-stream"));
    assertFalse(MmdFileTypeRules.isFallbackMime("text/x-nbmmd+plain"));
    assertFalse(MmdFileTypeRules.isFallbackMime(""));
    assertFalse(MmdFileTypeRules.isFallbackMime(null));
  }

  @Test
  public void testIsSkippedFolderName() {
    assertTrue(MmdFileTypeRules.isSkippedFolderName("target"));
    assertTrue(MmdFileTypeRules.isSkippedFolderName("TARGET"));
    assertTrue(MmdFileTypeRules.isSkippedFolderName(".git"));
    assertTrue(MmdFileTypeRules.isSkippedFolderName("node_modules"));
    assertFalse(MmdFileTypeRules.isSkippedFolderName("src"));
    assertFalse(MmdFileTypeRules.isSkippedFolderName("nbproject"));
    assertFalse(MmdFileTypeRules.isSkippedFolderName(""));
    assertFalse(MmdFileTypeRules.isSkippedFolderName(null));
  }

  @Test
  public void testShouldInvalidateWrongDataObject() {
    assertTrue(MmdFileTypeRules.shouldInvalidateWrongDataObject("mmd", false));
    assertTrue(MmdFileTypeRules.shouldInvalidateWrongDataObject("MMD", false));
    assertFalse(MmdFileTypeRules.shouldInvalidateWrongDataObject("mmd", true));
    assertFalse(MmdFileTypeRules.shouldInvalidateWrongDataObject("txt", false));
    assertFalse(MmdFileTypeRules.shouldInvalidateWrongDataObject(null, false));
  }

  @Test
  public void testShouldClearUserMimeMapping() {
    assertTrue(MmdFileTypeRules.shouldClearUserMimeMapping(List.of("mmd")));
    assertTrue(MmdFileTypeRules.shouldClearUserMimeMapping(List.of("txt", "MMD")));
    assertFalse(MmdFileTypeRules.shouldClearUserMimeMapping(List.of("txt", "md")));
    assertFalse(MmdFileTypeRules.shouldClearUserMimeMapping(List.of()));
    assertFalse(MmdFileTypeRules.shouldClearUserMimeMapping(null));
  }
}
