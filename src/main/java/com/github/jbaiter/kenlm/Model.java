package com.github.jbaiter.kenlm;

import com.github.jbaiter.kenlm.jni.KenLM;
import com.github.jbaiter.kenlm.jni.SWIGTYPE_p_void;
import com.github.jbaiter.kenlm.jni.State;
import com.github.jbaiter.kenlm.jni.Vocabulary;
import com.github.jbaiter.kenlm.util.KenLMLoader;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Model {
    static {
        try {
            KenLMLoader.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private com.github.jbaiter.kenlm.jni.Model cModel;
    private Vocabulary vocabulary;
    private Config config;

    public Model(String path) throws ModelException {
        this(path, new Config());
    }

    public Model(String path, Config config) throws ModelException {
        this.config = config;
        this.cModel = KenLM.LoadVirtual(path, this.config.getCConfig());
        this.vocabulary = this.cModel.BaseVocabulary();
    }

    public boolean knows(final String word) {
        return vocabulary.Index(word) > 0;
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

    public double score(final Collection<String> sentence) {
        return score(sentence, true, true);
    }

    public double score(final Collection<String> sentence, final boolean useBOS, final boolean useEOS) {
        return score(sentence.stream(), useBOS, useEOS);
    }

    public double score(String[] sentence) {
        return score(sentence, true, true);
    }

    public double score(String[] sentence, boolean useBOS, boolean useEOS) {
        return score(Arrays.stream(sentence), useBOS, useEOS);
    }

    public Collection<FullScoreReturn> fullScores(String sentence) {
        return this.fullScores(sentence.split(" "));
    }

    public Collection<FullScoreReturn> fullScores(String sentence, boolean useBOS, boolean useEOS) {
        return this.fullScores(sentence.split(" "), useBOS, useEOS);
    }

    public Collection<FullScoreReturn> fullScores(String[] sentence) {
        return this.fullScores(sentence, true, true);
    }

    public Collection<FullScoreReturn> fullScores(String[] sentence, boolean useBOS, boolean useEOS) {
        return fullScores(Arrays.stream(sentence), useBOS, useEOS)
            .collect(Collectors.toList());
    }

    public Collection<FullScoreReturn> fullScores(final Collection<String> sentence) {
        return fullScores(sentence, true, true);
    }

    public Collection<FullScoreReturn> fullScores(final Collection<String> sentence, boolean useBOS, boolean useEOS) {
        return fullScores(sentence.stream(), useBOS, useEOS)
            .collect(Collectors.toList());
    }

    private class ScoreMapper implements Function<String, FullScoreReturn> {
        private State state;

        ScoreMapper(boolean useBOS) {
            state = new State();

            if (useBOS) {
                cModel.BeginSentenceWrite(new SWIGTYPE_p_void(State.getCPtr(state), true));
            } else {
                cModel.NullContextWrite(new SWIGTYPE_p_void(State.getCPtr(state), true));
            }
        }

        State getState() {
            return state;
        }

        @Override
        public FullScoreReturn apply(final String word) {
            final long wordIdx = vocabulary.Index(word);
            final State outState = new State();
            final com.github.jbaiter.kenlm.jni.FullScoreReturn ret = cModel.BaseFullScore(new SWIGTYPE_p_void(State.getCPtr(state), false),
                    wordIdx,
                    new SWIGTYPE_p_void(State.getCPtr(outState), true));

            state = outState;

            return new FullScoreReturn(ret.getProb(), ret.getNgram_length(), wordIdx == vocabulary.NotFound());
        }
    }

    /*
        can NOT be used parallel
     */
    private Stream<FullScoreReturn> fullScores(final Stream<String> sentence, boolean useBOS, boolean useEOS) {
        final ScoreMapper mapper = new ScoreMapper(useBOS);

        final Stream<FullScoreReturn> scored = sentence
            .sequential()
            .map(mapper);

        if(!useEOS) {
            return scored;
        }

        // the supplier will be called after the first stream was iterated
        // this means that mapper.state is correct accumulated
        return Stream.concat(
            scored,
            Stream.generate(() -> {
                State outState = new State();
                final com.github.jbaiter.kenlm.jni.FullScoreReturn ret = this.cModel.BaseFullScore(
                        new SWIGTYPE_p_void(State.getCPtr(mapper.getState()), false),
                        this.vocabulary.EndSentence(),
                        new SWIGTYPE_p_void(State.getCPtr(outState), true)
                );

                return new FullScoreReturn(ret.getProb(), ret.getNgram_length(), false);
            })
            .limit(1)
        );
    }

    /*
        can NOT be used parallel
     */
    private double score(final Stream<String> sentence, boolean useBOS, boolean useEOS) {
        return fullScores(sentence, useBOS, useEOS)
            .mapToDouble(FullScoreReturn::getLogProbability)
            .sum();
    }
}
