#!/usr/bin/env bash
set -e

JNIPATH="./src/main/java/com/github/jbaiter/kenlm/jni"
SWIGPATH="./src/main/swig/KenLM.swg"

swig -c++ -java -Wall -package com.github.jbaiter.kenlm.jni \
     -outdir $JNIPATH -o kenlm_wrap.cc $SWIGPATH

CXXFLAGS="-I. -O3 -DNDEBUG -DHAVE_BZLIB -DKENLM_MAX_ORDER=6 -fPIC $CXXFLAGS"

g++ $CXXFLAGS ./kenlm_wrap.cc \
    $objects -shared -lkenlm -lkenlm_util \
    -o ./libkenlm-jni -Wno-deprecated -pthread
