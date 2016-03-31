%module KenLM
%{
#include <iostream>
#include <string>
#include "lm/word_index.hh"
#include "lm/return.hh"
#include "lm/state.hh"
#include "lm/virtual_interface.hh"
#include "util/mmap.hh"
#include "lm/config.hh"
#include "lm/model.hh"
#include "lm/binary_format.hh"
#include "util/exception.hh"
%}

%include "std_string.i"

SWIG_JAVABODY_PROXY(public, public, SWIGTYPE)
SWIG_JAVABODY_TYPEWRAPPER(public, public, public, SWIGTYPE)

typedef unsigned long long uint64_t;
typedef unsigned int size_t;

namespace lm {
    typedef unsigned int WordIndex;

    struct FullScoreReturn {
        float prob;
        unsigned char ngram_length;
    };
};

%nodefaultctor lm::base::Vocabulary;
%nodefaultctor lm::base::Model;

namespace lm::base {
    class Vocabulary {
    public:
        lm::WordIndex BeginSentence() const;
        lm::WordIndex EndSentence() const;
        lm::WordIndex NotFound() const;
        lm::WordIndex Index(const char *str) const;
    };

    class Model {
    public:
        void BeginSentenceWrite(void *to) const;
        void NullContextWrite(void *to) const;
        unsigned int Order() const;
        const lm::base::Vocabulary &BaseVocabulary() const;
        float BaseScore(const void *in_state, const lm::WordIndex new_word, void *out_state) const;
        lm::FullScoreReturn BaseFullScore(const void *in_state, const lm::WordIndex new_word, void *out_state) const;
    };
};

namespace util {
    enum LoadMethod {
        LAZY,
        POPULATE_OR_LAZY,
        POPULATE_OR_READ,
        READ,
        PARALLEL_READ,
    };
};

namespace lm::ngram {
    class State {
    public:
        int Compare(const lm::ngram::State &other) const;
    };

    uint64_t hash_value(const lm::ngram::State &state);

    class Config {
    public:
        bool show_progress;
        float unknown_missing_logprob;
        float probing_multiplier;
        size_t  building_memory;
        std::ostream *messages;
        std::string temporary_directory_prefix;
        util::LoadMethod load_method;
    };

    %javaexception("com.github.jbaiter.kenlm.ModelException") LoadVirtual {
        try {
            $action
        } catch (util::Exception &e) {
            jclass clazz = jenv->FindClass("com/github/jbaiter/kenlm/ModelException");
            jenv->ThrowNew(clazz, e.what());
            return $null;
        }
    }
    %newobject LoadVirtual;
    lm::base::Model* LoadVirtual(const char *file_name,
                                 const lm::ngram::Config &config = lm::ngram::Config());
};
