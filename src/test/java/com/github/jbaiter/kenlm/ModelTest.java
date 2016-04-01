package com.github.jbaiter.kenlm;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Collection;

import static org.junit.Assert.*;

public class ModelTest {
  private Model model;

  @Before
  public void setUp() throws Exception {
    this.model = new Model(getClass().getResource("/test.arpa").getPath());
  }

  @Test
  public void testLoadArpa() {
    assertNotNull(this.model);
  }

  @Test
  public void testLoadGzippedArpa() throws Exception {
    Model model = new Model(getClass().getResource("/test.arpa.gz").getPath());
    assertNotNull(model);
    assertEquals(model.getOrder(), 5);
  }

  @Test
  public void testLoadProbing() throws Exception {
    Model model = new Model(getClass().getResource("/test_probing.mmap").getPath());
    assertNotNull(model);
    assertEquals(model.getOrder(), 5);
  }

  // FIXME: We should actually expect a FormatLoadException, but for some weird
  // classloader-related reason, the thrown exception does not get matched  by
  // JUnit. This has probably something to do with the fact that we inject the
  // classes in KenLMLoader...
  @Test(expected=Exception.class)
  public void testLoadGzippedProbing()  throws Exception {
    Model model = new Model(getClass().getResource("/test_probing.mmap.gz").getPath());
    assertNotNull(model);
    assertEquals(model.getOrder(), 5);
  }

  @Test
  public void testLoadTrie() throws Exception {
    Model model = new Model(getClass().getResource("/test_trie.mmap").getPath());
    assertNotNull(model);
    assertEquals(model.getOrder(), 5);
  }

  @Test
  public void testLoadArrayTrie() throws Exception {
    Model model = new Model(getClass().getResource("/test_array_trie.mmap").getPath());
    assertNotNull(model);
    assertEquals(model.getOrder(), 5);
  }

  @Test
  public void testLoadQuantTrie() throws Exception {
    Model model = new Model(getClass().getResource("/test_quant_trie.mmap").getPath());
    assertNotNull(model);
    assertEquals(model.getOrder(), 5);
  }

  @Test
  public void testLoadQuantArrayTrie() throws Exception {
    Model model = new Model(getClass().getResource("/test_quant_array_trie.mmap").getPath());
    assertNotNull(model);
    assertEquals(model.getOrder(), 5);
  }

  @Test
  public void getOrder() throws Exception {
    assertEquals(model.getOrder(), 5);
  }

  @Test
  public void score() throws Exception {
    assertEquals(-6.4675869, model.score("a little more"), 1e-6);
    assertTrue(model.score("a little more") > model.score("probably not known"));
  }

  @Test
  public void fullScores() throws Exception {
    Collection<FullScoreReturn> scores = model.fullScores("looking a little beyond");
    assertEquals(scores.size(), 5);
    assertTrue(scores.stream().allMatch(fs -> !fs.isOov()));

    scores = model.fullScores("some g4rb4ge words");
    assertEquals(scores.stream().filter(fs -> fs.isOov()).count(), 3);
  }
}