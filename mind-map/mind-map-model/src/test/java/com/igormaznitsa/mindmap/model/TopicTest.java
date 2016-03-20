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

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import org.junit.Test;

import static org.junit.Assert.*;

import com.igormaznitsa.mindmap.model.parser.MindMapLexer;

public class TopicTest {

  private final MindMapLexer makeLexer(final String text) {
    final MindMapLexer lexer = new MindMapLexer();
    lexer.start(text, 0, text.length(), MindMapLexer.TokenType.WHITESPACE);
    return lexer;
  }

  @Test
  public void testParse_OnlyTopic() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic topic = Topic.parse(mm, makeLexer("# Topic"));
    assertEquals("Topic",topic.getText());
    assertTrue(topic.getChildren().isEmpty());
  }
  
  @Test
  public void testParse_OnlyTopicWithExtras() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic topic = Topic.parse(mm, makeLexer("# Topic\n- NOTE\n<pre>Some\ntext</pre>\n- LINK\n<pre>http://www.google.com</pre>\n## Topic2"));
    assertEquals("Topic",topic.getText());
    assertEquals(1,topic.getChildren().size());
    assertEquals(2,topic.getExtras().size());
    assertEquals("Some\ntext",(String)topic.getExtras().get(Extra.ExtraType.NOTE).getValue());
    assertEquals(new URI("http://www.google.com"),((MMapURI)topic.getExtras().get(Extra.ExtraType.LINK).getValue()).asURI());
  }
  
  @Test
  public void testParse_PairTopicsFirstContainsMultilineTextNoteAndMiscEOL() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic topic = Topic.parse(mm, makeLexer("# Topic\r\n- NOTE\r\n<pre>   Some   \r\n    text     \n    line  \r\n  end \r\n   </pre>\r\n- LINK\n<pre>http://www.google.com</pre>\n## Topic2"));
    assertEquals("Topic",topic.getText());
    assertEquals(1,topic.getChildren().size());
    assertEquals(2,topic.getExtras().size());
    assertEquals("   Some   \r\n    text     \n    line  \r\n  end \r\n   ",(String)topic.getExtras().get(Extra.ExtraType.NOTE).getValue());
    assertEquals(new URI("http://www.google.com"),((MMapURI)topic.getExtras().get(Extra.ExtraType.LINK).getValue()).asURI());
    final Topic second = topic.getFirst();
    assertEquals("Topic2", second.getText());
  }
  
  @Test
  public void testParse_TopicWithURLContainingSpaces() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic topic = Topic.parse(mm, makeLexer("# Topic\n- NOTE\n<pre>Some\ntext</pre>\n- LINK\n<pre>  http://www.google.com </pre>\n## Topic2"));
    assertEquals("Topic",topic.getText());
    assertEquals(1,topic.getChildren().size());
    assertEquals(2,topic.getExtras().size());
    assertEquals("Some\ntext",(String)topic.getExtras().get(Extra.ExtraType.NOTE).getValue());
    assertEquals(new URI("http://www.google.com"),((MMapURI)topic.getExtras().get(Extra.ExtraType.LINK).getValue()).asURI());
  }
  
  @Test
  public void testParse_OnlyTopicWithExtrasAndAttributes() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic topic = Topic.parse(mm, makeLexer("# Topic\n- NOTE\n<pre>Some\ntext</pre>\n- LINK\n<pre>http://www.google.com</pre>\n> attr1=`hello`,attr2=``wor`ld``"));
    assertEquals("Topic",topic.getText());
    assertTrue(topic.getChildren().isEmpty());
    assertEquals(2,topic.getExtras().size());
    assertEquals("Some\ntext",(String)topic.getExtras().get(Extra.ExtraType.NOTE).getValue());
    assertEquals(new URI("http://www.google.com"),((MMapURI)topic.getExtras().get(Extra.ExtraType.LINK).getValue()).asURI());
    assertEquals(2,topic.getAttributes().size());
    assertEquals("hello",topic.getAttribute("attr1"));
    assertEquals("wor`ld",topic.getAttribute("attr2"));
  }
  
  @Test
  public void testParse_TopicAndChild() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic topic = Topic.parse(mm, makeLexer("# Topic<br>Root\n ## Child<br/>Topic"));
    assertEquals("Topic\nRoot",topic.getText());
    assertEquals(1,topic.getChildren().size());
    
    final Topic child = topic.getChildren().get(0);
    assertEquals("Child\nTopic", child.getText());
    assertTrue(child.getChildren().isEmpty());
  }
  
  @Test
  public void testParse_TopicAndTwoChildren() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic topic = Topic.parse(mm, makeLexer("# Topic\n ## Child1\n ## Child2\n"));
    assertEquals("Topic",topic.getText());
    assertEquals(2,topic.getChildren().size());
    
    final Topic child1 = topic.getChildren().get(0);
    assertEquals("Child1", child1.getText());
    assertTrue(child1.getChildren().isEmpty());

    final Topic child2 = topic.getChildren().get(1);
    assertEquals("Child2", child2.getText());
    assertTrue(child2.getChildren().isEmpty());
  }

  @Test
  public void testParse_MultiLevels() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic root = Topic.parse(mm, makeLexer("# Level1\n## Level2.1\n### Level3.1\n## Level2.2\n### Level3.2\n#### Level4.2\n## Level2.3"));
    assertEquals("Level1",root.getText());
    assertEquals(3, root.getChildren().size());
    assertEquals("Level2.1", root.getChildren().get(0).getText());
    assertEquals("Level2.2", root.getChildren().get(1).getText());
    assertEquals("Level2.3", root.getChildren().get(2).getText());
    
    final Topic level32 = root.getChildren().get(1).getChildren().get(0);
    
    assertEquals("Level3.2", level32.getText());
    assertEquals("Level4.2", level32.getChildren().get(0).getText());
  }

  @Test
  public void testParse_WriteOneLevel() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic root = new Topic(mm,null,"Level1");
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# Level1\n",writer.toString());
  }
  
  @Test
  public void testParse_WriteOneLevelWithExtra() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic root = new Topic(mm,null,"Level1");
    root.setExtra(new ExtraLink("http://wwww.igormaznitsa.com"));
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# Level1\n- LINK\n<pre>http://wwww.igormaznitsa.com</pre>\n",writer.toString());
  }
  
  @Test
  public void testParse_WriteOneLevelWithExtraAndAttribute() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic root = new Topic(mm,null,"Level1");
    root.setAttribute("hello", "wor`ld");
    root.setExtra(new ExtraLink("http://wwww.igormaznitsa.com"));
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# Level1\n> hello=``wor`ld``\n\n- LINK\n<pre>http://wwww.igormaznitsa.com</pre>\n",writer.toString());
  }
  
  @Test
  public void testParse_WriteOneLevelWithSpecialChars() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic root = new Topic(mm,null,"<Level1>\nNextText");
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# \\<Level1\\><br/>NextText\n",writer.toString());
  }
  
  @Test
  public void testParse_WriteTwoLevel() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic root = new Topic(mm,null,"Level1");
    new Topic(mm,root, "Level2");
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# Level1\n\n## Level2\n",writer.toString());
  }
  
  @Test
  public void testParse_WriteThreeLevel() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic root = new Topic(mm,null,"Level1");
    final Topic level2 = new Topic(mm,root, "Level2");
    new Topic(mm,level2, "Level3");
    new Topic(mm,root, "Level2.1");
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("\n# Level1\n\n## Level2\n\n### Level3\n\n## Level2\\.1\n",writer.toString());
  }
  
  @Test
  public void testParse_EmptyTextAtMiddleLevel() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic topic = Topic.parse(mm, makeLexer("# \n## Child\n"));
    assertEquals("", topic.getText());
    assertEquals(1, topic.getChildren().size());

    final Topic child2 = topic.getChildren().get(0);
    assertEquals("Child", child2.getText());
    assertTrue(child2.getChildren().isEmpty());
  }

  @Test
  public void testParse_topicWithTextStartsWithHash() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic root = mm.getRoot();
    new Topic(mm, root, "#NewTopic");
    final String packedMap = mm.packToString();
    final MindMap parsed = new MindMap(null,new StringReader(packedMap));
    final Topic rootParsed = parsed.getRoot();
    
    assertEquals(1,rootParsed.getChildren().size());
    final Topic theTopic = rootParsed.getFirst();
    assertEquals("#NewTopic",theTopic.getText());
    assertTrue(theTopic.getExtras().isEmpty());
  }
  
  @Test
  public void testParse_noteContainsTicks() throws Exception {
    final MindMap mm = new MindMap(null);
    final Topic topic = mm.getRoot();
    topic.setText("`Root\ntopic`");
    topic.setExtra(new ExtraNote("Hello world \n <br>```Some```"));
    
    final StringWriter writer = new StringWriter();
    mm.write(writer);
    final String text = writer.toString();
    
    assertEquals("Mind Map generated by NB MindMap plugin   \n"
            + "> __version__=`1.0`\n"
            + "---\n"
            + "\n# \\`Root<br/>topic\\`\n"
            + "- NOTE\n"
            + "<pre>Hello world \n"
            + " &lt;br&gt;```Some```</pre>\n", text);
  
    final MindMap parsed = new MindMap(null,new StringReader(text));
    
    assertEquals("`Root\ntopic`",parsed.getRoot().getText());
    assertEquals("Hello world \n <br>```Some```",((ExtraNote)parsed.getRoot().getExtras().get(Extra.ExtraType.NOTE)).getValue());
  }
  
}
