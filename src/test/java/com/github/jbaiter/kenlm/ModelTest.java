package com.github.jbaiter.kenlm;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class ModelTest {
  private URL toy0Url = getClass().getResource("/toy0.arpa");
  private URL toy1Url = getClass().getResource("/toy0.arpa");

  @Test
  public void getOrder() throws Exception {
    Model model = new Model(toy0Url.getPath());
    assertEquals(model.getOrder(), 3);
  }

  @Test
  public void score() throws Exception {

  }

  @Test
  public void score1() throws Exception {

  }

  @Test
  public void fullScores() throws Exception {

  }

  @Test
  public void fullScores1() throws Exception {

  }
}