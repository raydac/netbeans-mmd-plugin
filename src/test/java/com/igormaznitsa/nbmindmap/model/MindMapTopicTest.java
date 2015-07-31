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
package com.igormaznitsa.nbmindmap.model;

import java.io.StringWriter;
import java.net.URI;
import org.junit.Test;
import static org.junit.Assert.*;

public class MindMapTopicTest {

  @Test
  public void testParse_OnlyTopic() throws Exception {
    final MindMap mm = new MindMap();
    final Topic topic = Topic.parse(mm,"# Topic");
    assertEquals("Topic",topic.getText());
    assertTrue(topic.getChildren().isEmpty());
  }
  
  @Test
  public void testParse_OnlyTopicWithExtras() throws Exception {
    final MindMap mm = new MindMap();
    final Topic topic = Topic.parse(mm,"# Topic\n- NOTE\n```Some\ntext```\n- LINK\n![Hello](http://www.google.com)");
    assertEquals("Topic",topic.getText());
    assertTrue(topic.getChildren().isEmpty());
    assertEquals(2,topic.getExtras().size());
    assertEquals("Some\ntext",(String)topic.getExtras().get(Extra.ExtraType.NOTE).getValue());
    assertEquals(new URI("http://www.google.com"),(URI)topic.getExtras().get(Extra.ExtraType.LINK).getValue());
  }
  
  @Test
  public void testParse_OnlyTopicWithExtrasAndAttributes() throws Exception {
    final MindMap mm = new MindMap();
    final Topic topic = Topic.parse(mm,"# Topic\n- NOTE\n```Some\ntext```\n- LINK\n```http://www.google.com```\n> attr1=\"hello\",attr2=\"world\"");
    assertEquals("Topic",topic.getText());
    assertTrue(topic.getChildren().isEmpty());
    assertEquals(2,topic.getExtras().size());
    assertEquals("Some\ntext",(String)topic.getExtras().get(Extra.ExtraType.NOTE).getValue());
    assertEquals(new URI("http://www.google.com"),(URI)topic.getExtras().get(Extra.ExtraType.LINK).getValue());
    assertEquals(2,topic.getAttributes().size());
    assertEquals("hello",topic.getAttribute("attr1"));
    assertEquals("world",topic.getAttribute("attr2"));
  }
  
  @Test
  public void testParse_TopicAndChild() throws Exception {
    final MindMap mm = new MindMap();
    final Topic topic = Topic.parse(mm,"# Topic\n ## Child");
    assertEquals("Topic",topic.getText());
    assertEquals(1,topic.getChildren().size());
    
    final Topic child = topic.getChildren().get(0);
    assertEquals("Child", child.getText());
    assertTrue(child.getChildren().isEmpty());
  }
  
  @Test
  public void testParse_TopicAndTwoChildren() throws Exception {
    final MindMap mm = new MindMap();
    final Topic topic = Topic.parse(mm,"# Topic\n ## Child1\n ## Child2\n");
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
    final MindMap mm = new MindMap();
    final Topic root = Topic.parse(mm,"#Level1\n##Level2.1\n###Level3.1\n##Level2.2\n###Level3.2\n####Level4.2\n##Level2.3");
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
    final MindMap mm = new MindMap();
    final Topic root = new Topic(mm,null,"Level1");
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("# Level1\n",writer.toString());
  }
  
  @Test
  public void testParse_WriteOneLevelWithExtra() throws Exception {
    final MindMap mm = new MindMap();
    final Topic root = new Topic(mm,null,"Level1");
    root.setExtra(new ExtraLink("http://wwww.igormaznitsa.com"));
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("# Level1\n- LINK\n[http://wwww.igormaznitsa.com](http://wwww.igormaznitsa.com)\n",writer.toString());
  }
  
  @Test
  public void testParse_WriteOneLevelWithExtraAndAttribute() throws Exception {
    final MindMap mm = new MindMap();
    final Topic root = new Topic(mm,null,"Level1");
    root.setAttribute("hello", "world");
    root.setExtra(new ExtraLink("http://wwww.igormaznitsa.com"));
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("# Level1\n> hello=\"world\"\n- LINK\n[http://wwww.igormaznitsa.com](http://wwww.igormaznitsa.com)\n",writer.toString());
  }
  
  @Test
  public void testParse_WriteOneLevelWithSpecialChars() throws Exception {
    final MindMap mm = new MindMap();
    final Topic root = new Topic(mm,null,"<Level1>\nNextText");
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("# &lt;Level1&gt;<br>NextText\n",writer.toString());
  }
  
  @Test
  public void testParse_WriteTwoLevel() throws Exception {
    final MindMap mm = new MindMap();
    final Topic root = new Topic(mm,null,"Level1");
    new Topic(mm,root, "Level2");
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("# Level1\n## Level2\n",writer.toString());
  }
  
  @Test
  public void testParse_WriteThreeLevel() throws Exception {
    final MindMap mm = new MindMap();
    final Topic root = new Topic(mm,null,"Level1");
    final Topic level2 = new Topic(mm,root, "Level2");
    new Topic(mm,level2, "Level3");
    new Topic(mm,root, "Level2.1");
    final StringWriter writer = new StringWriter();
    root.write(writer);
    assertEquals("# Level1\n## Level2\n### Level3\n## Level2.1\n",writer.toString());
  }
  
}
