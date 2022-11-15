package com.igormaznitsa.mindmap.annoit;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class LinkingTest extends AbstractMmdTest {
  @Test
  public void testCreatedMaps() throws Exception {
    assertNotNull(this.loadMindMap("com/igormaznitsa/mindmap/annoit/linking/Root.mmd"));
    assertNotNull(
        this.loadMindMap("com/igormaznitsa/mindmap/annoit/linking/sub1/RootSuccessor.mmd"));
    assertNotNull(this.loadMindMap("com/igormaznitsa/mindmap/annoit/linking/sub3/alpha.mmd"));
    assertNotNull(this.loadMindMap("com/igormaznitsa/mindmap/annoit/linking/sub3/gamma.mmd"));
    assertNotNull(this.loadMindMap("com/igormaznitsa/mindmap/annoit/linking/sub3/beta/alpha.mmd"));
  }
}
