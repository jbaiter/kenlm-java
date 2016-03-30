package com.github.jbaiter.kenlm;

public class FullScoreReturn {
  private float logProbability;
  private int ngramLength;
  private boolean isOov;

  public FullScoreReturn(float logProbability, int ngramLength, boolean isOov) {
    this.logProbability = logProbability;
    this.ngramLength = ngramLength;
    this.isOov = isOov;
  }

  public float getLogProbability() {
    return logProbability;
  }

  public int getNgramLength() {
    return ngramLength;
  }

  public boolean isOov() {
    return isOov;
  }
}
