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
package com.igormaznitsa.mindmap.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.net.URI;
import java.util.Properties;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

public class MMapURITest {

  private static void assumeWindows() {
    assumeTrue(SystemUtils.IS_OS_WINDOWS);
  }

  private static void assumeNotWindows() {
    assumeFalse(SystemUtils.IS_OS_WINDOWS);
  }

  @Test
  public void testEquals() throws Exception {
    assertEquals(new MMapURI("http://www.google.com"), new MMapURI("http://www.google.com"));
    assertEquals(new MMapURI("http://www.google.com?a=1"), new MMapURI("http://www.google.com?a=1"));
    assertNotEquals(new MMapURI("http://www.google.com?a=1"), new MMapURI("http://www.google.com"));
    assertNotEquals(new MMapURI("http://www.google.com?a=1"), new MMapURI("http://www.googler.com?a=1"));
  }
  
  @Test
  public void testReplaceName() throws Exception {
    assertEquals("universe.doc?query=123", new MMapURI("?query=123#eee").replaceName("universe.doc").asURI().toString());
    assertEquals("hello/universe.doc?query=123", new MMapURI("hello/world.txt?query=123#eee").replaceName("universe.doc").asURI().toString());
    assertEquals("hello?query=123", new MMapURI("universe/?query=123#eee").replaceName("hello").asURI().toString());
    assertEquals("hello?query=123", new MMapURI("universe?query=123#eee").replaceName("hello").asURI().toString());
    assertEquals("file:///folder/folder2/file.txt?query=123", new MMapURI("file:///folder/folder2/hoho.txt?query=123").replaceName("file.txt").asURI().toString());
    assertEquals("file:///folder/folder2/file%253A%253A%253Csome%253E.txt?query=123", new MMapURI("file:///folder/folder2/hoho.txt?query=123").replaceName("file::<some>.txt").asURI().toString());

    assertEquals("file:///folder1/folder2/some/new/fold/hello.txt?query=123", new MMapURI("file:///folder1/folder2/some/new/folder/hoho.txt?query=123").replaceName("new/fold/hello.txt").asURI().toString());
  }

  @Test
  public void testReplaceBaseInPath() throws Exception {
    assertEquals("hello/world/test", new MMapURI("test").replaceBaseInPath(false, new URI("hello/world"), 0).asURI().toString());
    assertEquals("hello/world/some/test", new MMapURI("some/test").replaceBaseInPath(false, new URI("hello/world"), 1).asURI().toString());
    assertEquals("http://some/world/test", new MMapURI("http://some/test").replaceBaseInPath(false, new URI("http://hello/world"), 0).asURI().toString());
    assertEquals("file:///newfolder/newfolder1/hello/universe/and/world", new MMapURI("file:///server/folder/hello/universe/and/world").replaceBaseInPath(false, new URI("file:///newfolder/newfolder1"), 3).asURI().toString());
    assertEquals("/newfolder/newfolder1/hello/universe/and/world", new MMapURI("server/folder/hello/universe/and/world").replaceBaseInPath(false, new URI("file:///newfolder/newfolder1"), 3).asURI().toString());
  }

  @Test(expected = NullPointerException.class)
  public void testCreate_StrNull() throws Exception {
    new MMapURI((String) null);
  }

  @Test(expected = NullPointerException.class)
  public void testCreate_URINull() {
    new MMapURI((URI) null);
  }

  @Test
  public void testCreate_Str() throws Exception {
    MMapURI uri = new MMapURI("http://www.hello.world");
    assertEquals("http://www.hello.world", uri.asString(false, false));
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());

    uri = new MMapURI("/some/test");
    assertEquals("/some/test", uri.asString(false, false));
    assertFalse(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());

    uri = new MMapURI("/some/test?hello=1234");
    assertEquals("/some/test?hello=1234", uri.asString(false, true));
    assertFalse(uri.isAbsolute());
    assertEquals(1, uri.getParameters().size());
  }

  @Test
  public void testCreate_NotWindows_Uri_NoProps() throws Exception {
    assumeNotWindows();

    MMapURI uri = new MMapURI(new URI("file:///K%C3%B5ik/v%C3%B5i/mitte/midagi.txt"));
    assertEquals("file:///K%C3%B5ik/v%C3%B5i/mitte/midagi.txt", uri.asString(false, false));
    assertEquals(new File("/Kõik/või/mitte/midagi.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());

    uri = new MMapURI(new URI("/some/test"));
    assertEquals("/some/test", uri.asString(false, false));
    assertFalse(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());

    uri = new MMapURI(new URI("/some/test?hello=1234"));
    assertEquals("/some/test?hello=1234", uri.asString(false, true));
    assertFalse(uri.isAbsolute());
    assertEquals(1, uri.getParameters().size());
  }

  @Test
  public void testCreate_NotWindows_Uri_Props() throws Exception {
    assumeNotWindows();
    MMapURI uri = new MMapURI(new URI("file:///K%C3%B5ik/v%C3%B5i/mitte/midagi.txt?one=two"));
    assertEquals("two", uri.getParameters().getProperty("one"));
    assertEquals("file:///K%C3%B5ik/v%C3%B5i/mitte/midagi.txt?one=two", uri.asString(false, true));
    assertEquals(new File("/Kõik/või/mitte/midagi.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertFalse(uri.getParameters().isEmpty());

    uri = new MMapURI(new URI("/some/test"));
    assertEquals("/some/test", uri.asString(false, false));
    assertFalse(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());

    uri = new MMapURI(new URI("/some/test?hello=1234"));
    assertEquals("/some/test?hello=1234", uri.asString(false, true));
    assertFalse(uri.isAbsolute());
    assertEquals(1, uri.getParameters().size());
  }

  @Test
  public void testCreate_Windows_Uri_NoProps() throws Exception {
    assumeWindows();
    MMapURI uri = new MMapURI(new URI("file://C:/K%C3%B5ik/v%C3%B5i/mitte/midagi.txt"));
    assertEquals("file://C:/K%C3%B5ik/v%C3%B5i/mitte/midagi.txt", uri.asString(false, false));
    assertEquals(new File("C:\\Kõik\\või\\mitte\\midagi.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());

    uri = new MMapURI(new URI("/some/test"));
    assertEquals("/some/test", uri.asString(false, false));
    assertFalse(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());

    uri = new MMapURI(new URI("/some/test?hello=1234"));
    assertEquals("/some/test?hello=1234", uri.asString(false, true));
    assertFalse(uri.isAbsolute());
    assertEquals(1, uri.getParameters().size());
  }

  @Test
  public void testCreate_Windows_Uri_Props() throws Exception {
    assumeWindows();
    MMapURI uri = new MMapURI(new URI("file://C:/K%C3%B5ik/v%C3%B5i/mitte/midagi.txt?one=two"));
    assertEquals("file://C:/K%C3%B5ik/v%C3%B5i/mitte/midagi.txt?one=two", uri.asString(false, true));
    assertEquals(new File("C:\\Kõik\\või\\mitte\\midagi.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertFalse(uri.getParameters().isEmpty());
    assertEquals("two", uri.getParameters().getProperty("one"));

    uri = new MMapURI(new URI("/some/test"));
    assertEquals("/some/test", uri.asString(false, false));
    assertFalse(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());

    uri = new MMapURI(new URI("/some/test?hello=1234"));
    assertEquals("/some/test?hello=1234", uri.asString(false, true));
    assertFalse(uri.isAbsolute());
    assertEquals(1, uri.getParameters().size());
  }

  @Test
  public void testCreate_AbsFile_NotWindows_NoBase_NoProps() {
    assumeNotWindows();
    MMapURI uri = new MMapURI(null, new File("/folder/hello world.txt"), null);
    assertEquals("file:///folder/hello%20world.txt", uri.asString(false, true));
    assertEquals("/folder/hello world.txt", uri.asFile(null).getAbsolutePath());
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_Windows_NoBase_NoProps() {
    assumeWindows();
    MMapURI uri = new MMapURI(null, new File("C:\\folder\\hello world.txt"), null);
    assertEquals("file://C:/folder/hello%20world.txt", uri.asString(false, true));
    assertEquals("C:\\folder\\hello world.txt", uri.asFile(null).getAbsolutePath());
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_NotWindows_InsideBase_NoProps() {
    assumeNotWindows();
    MMapURI uri = new MMapURI(new File("/folder"), new File("/folder/folder2/hello world.txt"), null);
    assertEquals("folder2/hello%20world.txt", uri.asString(false, true));
    assertEquals(new File((File) null, "folder2/hello world.txt"), uri.asFile(null));
    assertFalse(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_Windows_InsideBase_NoProps() {
    assumeWindows();
    MMapURI uri = new MMapURI(new File("C:\\folder"), new File("C:\\folder\\folder2\\hello world.txt"), null);
    assertEquals("folder2/hello%20world.txt", uri.asString(false, true));
    assertEquals(new File((File) null, "folder2\\hello world.txt"), uri.asFile(null));
    assertFalse(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_NotWindows_OutsideBase_NoProps() {
    assumeNotWindows();
    MMapURI uri = new MMapURI(new File("/folder1"), new File("/folder/folder2/hello world.txt"), null);
    assertEquals("file:///folder/folder2/hello%20world.txt", uri.asString(false, true));
    assertEquals(new File("/folder/folder2/hello world.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_Windows_OutsideBase_NoProps() {
    assumeWindows();
    MMapURI uri = new MMapURI(new File("C:\\folder1"), new File("C:\\folder\\folder2\\hello world.txt"), null);
    assertEquals("file://C:/folder/folder2/hello%20world.txt", uri.asString(false, true));
    assertEquals(new File("C:\\folder\\folder2\\hello world.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_NotWindows_RelativeBase_NoProps() {
    assumeNotWindows();
    MMapURI uri = new MMapURI(new File("folder1"), new File("/folder1/folder2/hello world.txt"), null);
    assertEquals("file:///folder1/folder2/hello%20world.txt", uri.asString(false, true));
    assertEquals(new File("/folder1/folder2/hello world.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_Windows_RelativeBase_NoProps() {
    assumeWindows();
    MMapURI uri = new MMapURI(new File("folder1"), new File("C:\\folder1\\folder2\\hello world.txt"), null);
    assertEquals("file://C:/folder1/folder2/hello%20world.txt", uri.asString(false, true));
    assertEquals(new File("C:\\folder1\\folder2\\hello world.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_NotWindows_OutsideBase_Props() {
    assumeNotWindows();
    final Properties props = new Properties();
    props.put("привет", "от игоря");
    props.put("hello", "world");

    MMapURI uri = new MMapURI(new File("/folder1"), new File("/folder/folder2/hello world.txt"), props);
    assertEquals("file:///folder/folder2/hello%20world.txt?hello=world&%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82=%D0%BE%D1%82+%D0%B8%D0%B3%D0%BE%D1%80%D1%8F", uri.asString(false, true));
    assertEquals(new File("/folder/folder2/hello world.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertEquals(2, uri.getParameters().size());
    assertEquals("от игоря", uri.getParameters().getProperty("привет"));
    assertEquals("world", uri.getParameters().getProperty("hello"));
  }

  @Test
  public void testCreate_AbsFile_Windows_OutsideBase_Props() {
    assumeWindows();
    final Properties props = new Properties();
    props.put("привет", "от игоря");
    props.put("hello", "world");

    MMapURI uri = new MMapURI(new File("C:\\folder1"), new File("C:\\folder\\folder2\\hello world.txt"), props);
    assertEquals("file://C:/folder/folder2/hello%20world.txt?hello=world&%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82=%D0%BE%D1%82+%D0%B8%D0%B3%D0%BE%D1%80%D1%8F", uri.asString(false, true));
    assertEquals(new File("C:\\folder\\folder2\\hello world.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertEquals(2, uri.getParameters().size());
    assertEquals("от игоря", uri.getParameters().getProperty("привет"));
    assertEquals("world", uri.getParameters().getProperty("hello"));
  }

  @Test
  public void testMakeFromFilePath_NotWindows_NoBase_NoProps() throws Exception {
    assumeNotWindows();
    final MMapURI uri = MMapURI.makeFromFilePath(null, "/hello/igor and larisa.txt", null);
    assertEquals(new URI("file:///hello/igor%20and%20larisa.txt"), uri.asURI());
  }

  @Test
  public void testMakeFromFilePath_Windows_NoBase_NoProps() throws Exception {
    assumeWindows();
    final MMapURI uri = MMapURI.makeFromFilePath(null, "C:\\hello\\igor and larisa.txt", null);
    assertEquals(new URI("file://C:/hello/igor%20and%20larisa.txt"), uri.asURI());
  }

  @Test
  public void testMakeFromFilePath_NotWindows_NoBase_Props() throws Exception {
    assumeNotWindows();
    final Properties props = new Properties();
    props.put("привет", "от игоря");
    props.put("hello", "world");

    final MMapURI uri = MMapURI.makeFromFilePath(null, "/hello/igor and larisa.txt", props);
    assertEquals(new URI("file:///hello/igor%20and%20larisa.txt?hello=world&%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82=%D0%BE%D1%82+%D0%B8%D0%B3%D0%BE%D1%80%D1%8F"), uri.asURI());
  }

  @Test
  public void testMakeFromFilePath_Windows_NoBase_Props() throws Exception {
    assumeWindows();
    final Properties props = new Properties();
    props.put("привет", "от игоря");
    props.put("hello", "world");

    final MMapURI uri = MMapURI.makeFromFilePath(null, "C:\\hello\\igor and larisa.txt", props);
    assertEquals(new URI("file://C:/hello/igor%20and%20larisa.txt?hello=world&%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82=%D0%BE%D1%82+%D0%B8%D0%B3%D0%BE%D1%80%D1%8F"), uri.asURI());
  }

  @Test
  public void testMakeFromFilePath_NotWindows_Base_Props() throws Exception {
    assumeNotWindows();
    final Properties props = new Properties();
    props.put("привет", "от игоря");
    props.put("hello", "world");

    final MMapURI uri = MMapURI.makeFromFilePath(new File("/hello"), "/hello/igor and larisa.txt", props);
    assertEquals(new URI("igor%20and%20larisa.txt?hello=world&%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82=%D0%BE%D1%82+%D0%B8%D0%B3%D0%BE%D1%80%D1%8F"), uri.asURI());
    assertFalse(uri.isAbsolute());
  }

  @Test
  public void testMakeFromFilePath_Windows_Base_Props() throws Exception {
    assumeWindows();
    final Properties props = new Properties();
    props.put("привет", "от игоря");
    props.put("hello", "world");

    final MMapURI uri = MMapURI.makeFromFilePath(new File("C:\\hello"), "C:\\hello\\igor and larisa.txt", props);
    assertEquals(new URI("igor%20and%20larisa.txt?hello=world&%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82=%D0%BE%D1%82+%D0%B8%D0%B3%D0%BE%D1%80%D1%8F"), uri.asURI());
    assertFalse(uri.isAbsolute());
  }

  @Test
  public void testAsURI_CreatedAsURI() throws Exception {
    final URI baseUri = new URI("http://www.igormaznitsa.com?test=one");
    final MMapURI uri = new MMapURI(baseUri);
    assertSame(baseUri, uri.asURI());
    assertEquals("one", uri.getParameters().getProperty("test"));
  }

  @Test
  public void testAsURI_Linux_CreatedAsFile() throws Exception {
    if (SystemUtils.IS_OS_LINUX) {
      final Properties props = new Properties();
      props.put("Kõik või", "tere");

      final MMapURI uri = new MMapURI(null, new File("/Kõik/või/mitte/midagi.txt"), props);
      assertEquals(new URI("file:///K%C3%B5ik/v%C3%B5i/mitte/midagi.txt?K%C3%B5ik+v%C3%B5i=tere"), uri.asURI());
      assertEquals("tere", uri.getParameters().getProperty("Kõik või"));
      assertEquals(new File("/Kõik/või/mitte/midagi.txt"), uri.asFile(null));
    }
  }

  @Test
  public void testAsURI_Windows_CreatedAsFile() throws Exception {
    assumeWindows();
    final Properties props = new Properties();
    props.put("Kõik või", "tere");

    final MMapURI uri = new MMapURI(null, new File("C:\\Kõik\\või\\mitte\\midagi.txt"), props);
    assertEquals(new URI("file://C:/K%C3%B5ik/v%C3%B5i/mitte/midagi.txt?K%C3%B5ik+v%C3%B5i=tere"), uri.asURI());
    assertEquals("tere", uri.getParameters().getProperty("Kõik või"));
    assertEquals(new File("C:\\Kõik\\või\\mitte\\midagi.txt"), uri.asFile(null));
  }

  @Test
  public void testGetExtension() throws Exception {
    assertEquals("", new MMapURI("http://wwww.hello.world/").getExtension());
    assertEquals("", new MMapURI("http://wwww.hello.world/test").getExtension());
    assertEquals("abc", new MMapURI("http://wwww.hello.world/test.abc").getExtension());
    assertEquals("ABC", new MMapURI("http://wwww.hello.world/test.ABC").getExtension());
    assertEquals("ABC", new MMapURI("http://wwww.hello.world/test.ABC?dot=eer.txt").getExtension());
    assertEquals("ABC", new MMapURI("http://wwww.hello.world/test.ABC?dot=eer.txt#rwwewe").getExtension());
  }

}
