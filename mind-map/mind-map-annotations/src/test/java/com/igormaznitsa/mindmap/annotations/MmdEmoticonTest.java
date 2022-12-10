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

package com.igormaznitsa.mindmap.annotations;

import static org.junit.Assert.assertEquals;

import com.igormaznitsa.mindmap.swing.panel.utils.MiscIcons;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class MmdEmoticonTest {
  @Test
  public void testAllEmoticonsInMindMapPanel() {
    final Set<String> panelIconNames = Stream.of(MiscIcons.getNames()).collect(Collectors.toSet());
    final Set<String> emoticonNames = Stream.of(MmdEmoticon.values())
        .filter(x -> x != MmdEmoticon.EMPTY)
        .map(MmdEmoticon::getId).collect(
            Collectors.toSet());
    assertEquals(panelIconNames.size(), emoticonNames.size());
    assertEquals(panelIconNames, emoticonNames);
  }
}