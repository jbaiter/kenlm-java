package com.github.jbaiter.kenlm;

import com.github.jbaiter.kenlm.jni.KenLM;
import com.github.jbaiter.kenlm.jni.SWIGTYPE_p_void;
import com.github.jbaiter.kenlm.jni.State;
import com.github.jbaiter.kenlm.jni.Vocabulary;
import com.github.jbaiter.kenlm.util.KenMLLoader;

import java.util.ArrayList;
import java.util.Collection;

public class Model {
  static {
    try {
      KenMLLoader.load();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private com.github.jbaiter.kenlm.jni.Model cModel;
  private String path;
  private Vocabulary vocabulary;
  private Config config;

  public Model(String path) throws ModelException {
    this(path, new Config());
  }

  public Model(String path, Config config) throws ModelException {
    this.config = config;
    this.path = path;
    this.cModel = KenLM.LoadVirtual(path, this.config.getCConfig());
    this.vocabulary = this.cModel.BaseVocabulary();
  }

  public long getOrder() {
    return this.cModel.Order();
  }

  public double score(String sentence) {
    return score(sentence, true, true);
  }

  public double score(String sentence, boolean useBOS, boolean useEOS) {
    return score(sentence.split(" "), useBOS, useEOS);
  }

  public double score(String[] sentence) {
    return score(sentence, true, true);
  }

  public double score(String[] sentence, boolean useBOS, boolean useEOS) {
    State state = new State();
    if (useBOS) {
      this.cModel.BeginSentenceWrite(new SWIGTYPE_p_void(State.getCPtr(state), true));
    } else {
      this.cModel.NullContextWrite(new SWIGTYPE_p_void(State.getCPtr(state), true));
    }
    State outState = new State();
    double total = 0.0;
    for (String word : sentence) {
      total += this.cModel.BaseScore(new SWIGTYPE_p_void(State.getCPtr(state), true),
                                     this.vocabulary.Index(word),
                                     new SWIGTYPE_p_void(State.getCPtr(outState), true));
    }
    if (useEOS) {
      total += this.cModel.BaseScore(new SWIGTYPE_p_void(State.getCPtr(state), true),
                                     this.vocabulary.EndSentence(),
                                     new SWIGTYPE_p_void(State.getCPtr(outState), true));
    }
    return total;
  }

  public Collection<FullScoreReturn> fullScores(String sentence) {
    return this.fullScores(sentence.split(" "));
  }

  public Collection<FullScoreReturn> fullScores(String sentence, boolean useBOS, boolean useEOS) {
    return this.fullScores(sentence.split(" "),  useBOS, useEOS);
  }

  public Collection<FullScoreReturn> fullScores(String[] sentence) {
    return this.fullScores(sentence, true, true);
  }

  public Collection<FullScoreReturn> fullScores(String[] sentence, boolean useBOS, boolean useEOS) {
    ArrayList<FullScoreReturn> scores = new ArrayList<>();
    State state = new State();
    if (useBOS) {
      this.cModel.BeginSentenceWrite(new SWIGTYPE_p_void(State.getCPtr(state), true));
    } else {
      this.cModel.NullContextWrite(new SWIGTYPE_p_void(State.getCPtr(state), true));
    }
    State outState = new State();
    com.github.jbaiter.kenlm.jni.FullScoreReturn ret;
    long wordIdx;
    for (String word : sentence) {
      wordIdx = this.vocabulary.Index(word);
      ret = this.cModel.BaseFullScore(new SWIGTYPE_p_void(State.getCPtr(state), true),
                                      wordIdx,
                                      new SWIGTYPE_p_void(State.getCPtr(outState), true));
      scores.add(new FullScoreReturn(ret.getProb(), ret.getNgram_length(), wordIdx == 0));
    }
    if (useEOS) {
      ret = this.cModel.BaseFullScore(new SWIGTYPE_p_void(State.getCPtr(state), true),
                                      this.vocabulary.EndSentence(),
                                      new SWIGTYPE_p_void(State.getCPtr(outState), true));
      scores.add(new FullScoreReturn(ret.getProb(), ret.getNgram_length(), false));
    }
    return scores;
  }
}
