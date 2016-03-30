/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.github.jbaiter.kenlm.jni;

public class Model {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  public Model(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  public static long getCPtr(Model obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        KenLMJNI.delete_Model(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void BeginSentenceWrite(SWIGTYPE_p_void to) {
    KenLMJNI.Model_BeginSentenceWrite(swigCPtr, this, SWIGTYPE_p_void.getCPtr(to));
  }

  public void NullContextWrite(SWIGTYPE_p_void to) {
    KenLMJNI.Model_NullContextWrite(swigCPtr, this, SWIGTYPE_p_void.getCPtr(to));
  }

  public long Order() {
    return KenLMJNI.Model_Order(swigCPtr, this);
  }

  public Vocabulary BaseVocabulary() {
    return new Vocabulary(KenLMJNI.Model_BaseVocabulary(swigCPtr, this), false);
  }

  public float BaseScore(SWIGTYPE_p_void in_state, long new_word, SWIGTYPE_p_void out_state) {
    return KenLMJNI.Model_BaseScore(swigCPtr, this, SWIGTYPE_p_void.getCPtr(in_state), new_word, SWIGTYPE_p_void.getCPtr(out_state));
  }

  public FullScoreReturn BaseFullScore(SWIGTYPE_p_void in_state, long new_word, SWIGTYPE_p_void out_state) {
    return new FullScoreReturn(KenLMJNI.Model_BaseFullScore(swigCPtr, this, SWIGTYPE_p_void.getCPtr(in_state), new_word, SWIGTYPE_p_void.getCPtr(out_state)), true);
  }

}
