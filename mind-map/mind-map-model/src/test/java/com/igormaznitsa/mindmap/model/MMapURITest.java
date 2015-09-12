/*
 * Copyright 2015 Igor Maznitsa.
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

import java.io.File;
import java.net.URI;
import java.util.Properties;
import org.junit.Test;
import static org.junit.Assert.*;

public class MMapURITest {

  @Test(expected = NullPointerException.class)
  public void testCreate_StrNull() throws Exception {
    new MMapURI((String) null);
  }

  @Test(expected = NullPointerException.class)
  public void testCreate_URINull() throws Exception {
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
    assertEquals("/some/test?hello=1234", uri.asString(false, false));
    assertFalse(uri.isAbsolute());
    assertEquals(1, uri.getParameters().size());
  }

  @Test
  public void testCreate_Uri() throws Exception {
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
    assertEquals("/some/test?hello=1234", uri.asString(false, false));
    assertFalse(uri.isAbsolute());
    assertEquals(1, uri.getParameters().size());
  }

  @Test
  public void testCreate_AbsFile_Linux_NoBase_NoProps() throws Exception {
    MMapURI uri = new MMapURI(null, new File("/folder/hello world.txt"), null);
    assertEquals("file:///folder/hello%20world.txt", uri.asString(false, true));
    assertEquals("/folder/hello world.txt", uri.asFile(null).getAbsolutePath());
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_Linux_InsideBase_NoProps() throws Exception {
    MMapURI uri = new MMapURI(new File("/folder"), new File("/folder/folder2/hello world.txt"), null);
    assertEquals("folder2/hello%20world.txt", uri.asString(false, true));
    assertEquals(new File((File) null, "folder2/hello world.txt"), uri.asFile(null));
    assertFalse(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_Linux_OutsideBase_NoProps() throws Exception {
    MMapURI uri = new MMapURI(new File("/folder1"), new File("/folder/folder2/hello world.txt"), null);
    assertEquals("file:///folder/folder2/hello%20world.txt", uri.asString(false, true));
    assertEquals(new File("/folder/folder2/hello world.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_Linux_RelativeBase_NoProps() throws Exception {
    MMapURI uri = new MMapURI(new File("folder1"), new File("/folder1/folder2/hello world.txt"), null);
    assertEquals("file:///folder1/folder2/hello%20world.txt", uri.asString(false, true));
    assertEquals(new File("/folder1/folder2/hello world.txt"), uri.asFile(null));
    assertTrue(uri.isAbsolute());
    assertTrue(uri.getParameters().isEmpty());
  }

  @Test
  public void testCreate_AbsFile_Linux_OutsideBase_Props() throws Exception {
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
  public void testMakeFromFilePath_NoBase_NoProps() throws Exception {
    final MMapURI uri = MMapURI.makeFromFilePath(null, "/hello/igor and larisa.txt", null);
    assertEquals(new URI("file:///hello/igor%20and%20larisa.txt"),uri.asURI());
  }
  
  @Test
  public void testMakeFromFilePath_NoBase_Props() throws Exception {
    final Properties props = new Properties();
    props.put("привет", "от игоря");
    props.put("hello", "world");

    final MMapURI uri = MMapURI.makeFromFilePath(null, "/hello/igor and larisa.txt", props);
    assertEquals(new URI("file:///hello/igor%20and%20larisa.txt?hello=world&%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82=%D0%BE%D1%82+%D0%B8%D0%B3%D0%BE%D1%80%D1%8F"),uri.asURI());
  }
  
  @Test
  public void testMakeFromFilePath_Base_Props() throws Exception {
    final Properties props = new Properties();
    props.put("привет", "от игоря");
    props.put("hello", "world");

    final MMapURI uri = MMapURI.makeFromFilePath(new File("/hello"), "/hello/igor and larisa.txt", props);
    assertEquals(new URI("igor%20and%20larisa.txt?hello=world&%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82=%D0%BE%D1%82+%D0%B8%D0%B3%D0%BE%D1%80%D1%8F"),uri.asURI());
    assertFalse(uri.isAbsolute());
  }
  
  @Test
  public void testAsURI_CreatedAsURI() throws Exception {
    final URI baseUri = new URI("http://www.igormaznitsa.com?test=one");
    final MMapURI uri = new MMapURI(baseUri);
    assertSame(baseUri, uri.asURI());
    assertEquals("one",uri.getParameters().getProperty("test"));
  }

  @Test
  public void testAsURI_CreatedAsFile() throws Exception {
    final Properties props = new Properties();
    props.put("Kõik või", "tere");
    
    final MMapURI uri = new MMapURI(null,new File("/Kõik/või/mitte/midagi.txt"),props);
    assertEquals(new URI("file:///K%C3%B5ik/v%C3%B5i/mitte/midagi.txt?K%C3%B5ik+v%C3%B5i=tere"), uri.asURI());
    assertEquals("tere", uri.getParameters().getProperty("Kõik või"));
    assertEquals(new File("/Kõik/või/mitte/midagi.txt"),uri.asFile(null));
  }

  @Test
  public void testGetExtension() throws Exception {
    assertEquals("",new MMapURI("http://wwww.hello.world/").getExtension());
    assertEquals("",new MMapURI("http://wwww.hello.world/test").getExtension());
    assertEquals("abc",new MMapURI("http://wwww.hello.world/test.abc").getExtension());
    assertEquals("ABC",new MMapURI("http://wwww.hello.world/test.ABC").getExtension());
    assertEquals("ABC",new MMapURI("http://wwww.hello.world/test.ABC?dot=eer.txt").getExtension());
    assertEquals("ABC",new MMapURI("http://wwww.hello.world/test.ABC?dot=eer.txt#rwwewe").getExtension());
  }

}
