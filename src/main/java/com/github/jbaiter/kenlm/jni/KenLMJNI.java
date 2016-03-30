/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.github.jbaiter.kenlm.jni;

public class KenLMJNI {
  public final static native void FullScoreReturn_prob_set(long jarg1, FullScoreReturn jarg1_, float jarg2);
  public final static native float FullScoreReturn_prob_get(long jarg1, FullScoreReturn jarg1_);
  public final static native void FullScoreReturn_ngram_length_set(long jarg1, FullScoreReturn jarg1_, short jarg2);
  public final static native short FullScoreReturn_ngram_length_get(long jarg1, FullScoreReturn jarg1_);
  public final static native long new_FullScoreReturn();
  public final static native void delete_FullScoreReturn(long jarg1);
  public final static native long Vocabulary_BeginSentence(long jarg1, Vocabulary jarg1_);
  public final static native long Vocabulary_EndSentence(long jarg1, Vocabulary jarg1_);
  public final static native long Vocabulary_NotFound(long jarg1, Vocabulary jarg1_);
  public final static native long Vocabulary_Index(long jarg1, Vocabulary jarg1_, String jarg2);
  public final static native void delete_Vocabulary(long jarg1);
  public final static native void Model_BeginSentenceWrite(long jarg1, Model jarg1_, long jarg2);
  public final static native void Model_NullContextWrite(long jarg1, Model jarg1_, long jarg2);
  public final static native long Model_Order(long jarg1, Model jarg1_);
  public final static native long Model_BaseVocabulary(long jarg1, Model jarg1_);
  public final static native float Model_BaseScore(long jarg1, Model jarg1_, long jarg2, long jarg3, long jarg4);
  public final static native long Model_BaseFullScore(long jarg1, Model jarg1_, long jarg2, long jarg3, long jarg4);
  public final static native void delete_Model(long jarg1);
  public final static native int State_Compare(long jarg1, State jarg1_, long jarg2, State jarg2_);
  public final static native long new_State();
  public final static native void delete_State(long jarg1);
  public final static native java.math.BigInteger hash_value(long jarg1, State jarg1_);
  public final static native void Config_show_progress_set(long jarg1, Config jarg1_, boolean jarg2);
  public final static native boolean Config_show_progress_get(long jarg1, Config jarg1_);
  public final static native void Config_unknown_missing_logprob_set(long jarg1, Config jarg1_, float jarg2);
  public final static native float Config_unknown_missing_logprob_get(long jarg1, Config jarg1_);
  public final static native void Config_probing_multiplier_set(long jarg1, Config jarg1_, float jarg2);
  public final static native float Config_probing_multiplier_get(long jarg1, Config jarg1_);
  public final static native void Config_building_memory_set(long jarg1, Config jarg1_, long jarg2);
  public final static native long Config_building_memory_get(long jarg1, Config jarg1_);
  public final static native void Config_temporary_directory_prefix_set(long jarg1, Config jarg1_, String jarg2);
  public final static native String Config_temporary_directory_prefix_get(long jarg1, Config jarg1_);
  public final static native void Config_load_method_set(long jarg1, Config jarg1_, int jarg2);
  public final static native int Config_load_method_get(long jarg1, Config jarg1_);
  public final static native long new_Config();
  public final static native void delete_Config(long jarg1);
  public final static native long LoadVirtual__SWIG_0(String jarg1, long jarg2, Config jarg2_) throws com.github.jbaiter.kenlm.ModelException;
  public final static native long LoadVirtual__SWIG_1(String jarg1) throws com.github.jbaiter.kenlm.ModelException;
}
