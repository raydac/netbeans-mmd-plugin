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
package com.igormaznitsa.mindmap.ide.commons;

import org.junit.Test;
import static org.junit.Assert.*;

public class FilePathWithLineTest {
  
  @Test
  public void testConstructor_Null() {
    final FilePathWithLine path = new FilePathWithLine(null);
    assertEquals(null, path.getPath());
    assertEquals(-1, path.getLine());
    assertEquals("", path.toString());
  }
  
  @Test
  public void testConstructor_OnlyPath() {
    final FilePathWithLine path = new FilePathWithLine("/hello/world.txt");
    assertEquals("/hello/world.txt", path.getPath());
    assertEquals(-1, path.getLine());
    assertEquals("/hello/world.txt", path.toString());
  }
  
  @Test
  public void testConstructor_PathWithZeroLine() {
    final FilePathWithLine path = new FilePathWithLine("/hello/world.txt:0");
    assertEquals("/hello/world.txt", path.getPath());
    assertEquals(-1, path.getLine());
    assertEquals("/hello/world.txt", path.toString());
  }
  
  @Test
  public void testConstructor_PathWithLine() {
    final FilePathWithLine path = new FilePathWithLine("/hello/world.txt:123");
    assertEquals("/hello/world.txt", path.getPath());
    assertEquals(123, path.getLine());
    assertEquals("/hello/world.txt:123", path.toString());
  }
  
  @Test
  public void testConstructor_PathWithNegativeLine() {
    final FilePathWithLine path = new FilePathWithLine("/hello/world.txt:-123");
    assertEquals("/hello/world.txt:-123", path.getPath());
    assertEquals(-1, path.getLine());
    assertEquals("/hello/world.txt:-123", path.toString());
  }
  
  @Test
  public void testConstructor_PathWithWrongCharsInLine() {
    final FilePathWithLine path = new FilePathWithLine("/hello/world.txt:1a23");
    assertEquals("/hello/world.txt:1a23", path.getPath());
    assertEquals(-1, path.getLine());
    assertEquals("/hello/world.txt:1a23", path.toString());
  }
  
  @Test
  public void testConstructor_PathWithTooBigLineNum() {
    final FilePathWithLine path = new FilePathWithLine("/hello/world.txt:1232387423423423334");
    assertEquals("/hello/world.txt", path.getPath());
    assertEquals(-1, path.getLine());
    assertEquals("/hello/world.txt", path.toString());
  }
  
  @Test
  public void testConstructor_PathWithDelimiter() {
    final FilePathWithLine path = new FilePathWithLine("/hello/world.txt:123:345");
    assertEquals("/hello/world.txt:123", path.getPath());
    assertEquals(345, path.getLine());
    assertEquals("/hello/world.txt:123:345", path.toString());
  }
  
  @Test
  public void testConstructor_PathWithDelimiterButLineZero() {
    final FilePathWithLine path = new FilePathWithLine("/hello/world.txt:123:0");
    assertEquals("/hello/world.txt:123", path.getPath());
    assertEquals(-1, path.getLine());
    assertEquals("/hello/world.txt:123:0", path.toString());
  }
  
}
