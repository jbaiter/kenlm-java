package com.github.jbaiter.kenlm;

import com.github.jbaiter.kenlm.jni.LoadMethod;

public class Config {
  private com.github.jbaiter.kenlm.jni.Config cConfig;

  public Config() {
    this.cConfig = new com.github.jbaiter.kenlm.jni.Config();
  }

  protected Config(com.github.jbaiter.kenlm.jni.Config cConfig) {
    this.cConfig = cConfig;
  }

  protected com.github.jbaiter.kenlm.jni.Config getCConfig() {
    return cConfig;
  }

  public void setShowProgress(boolean value) {
    cConfig.setShow_progress(value);
  }

  public boolean getShowProgress() {
    return cConfig.getShow_progress();
  }

  public void setUnknownMissingLogprob(float value) {
    cConfig.setUnknown_missing_logprob(value);
  }

  public float getUnknownMissingLogprob() {
    return cConfig.getUnknown_missing_logprob();
  }

  public void setProbingMultiplier(float value) {
    cConfig.setProbing_multiplier(value);
  }

  public float getProbingMultiplier() {
    return cConfig.getProbing_multiplier();
  }

  public void setBuildingMemory(long value) {
    cConfig.setBuilding_memory(value);
  }

  public long getBuildingMemory() {
    return cConfig.getBuilding_memory();
  }

  public void setTemporaryDirectoryPrefix(String value) {
    cConfig.setTemporary_directory_prefix(value);
  }

  public String getTemporaryDirectoryPrefix() {
    return cConfig.getTemporary_directory_prefix();
  }

  public void setLoadMethod(LoadMethod value) {
    cConfig.setLoad_method(value);
  }

  public LoadMethod getLoadMethod() {
    return cConfig.getLoad_method();
  }
}
