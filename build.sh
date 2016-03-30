#!/usr/bin/env bash
ARCH=$(uname -m |sed -e 's/x86_64/amd64/g')
OS=$(uname -o |sed -e 's/GNU\/Linux/Linux/g')
JNIPATH="./src/main/java/com/github/jbaiter/kenlm/jni"
SRCPATH="./src/main/c++"
LIBPATH="./src/main/resources/lib/$OS/$ARCH"
SWIGPATH="./src/main/swig/KenLM.swg"
LIBNAME="libkenlm.so"

echo Generating source files with SWIG
rm -f $JNIPATH/*.java $SRCPATH/kenlm_wrap.cc
swig -c++ -java -Wall -package com.github.jbaiter.kenlm.jni \
     -outdir $JNIPATH -o $SRCPATH/kenlm_wrap.cc -I$SRCPATH $SWIGPATH


echo Compiling source files

rm $SRCPATH/{lm,util}/*.o 2>/dev/null

CXXFLAGS="-I. -O3 -DNDEBUG -DHAVE_ZLIB -DHAVE_BZLIB -DHAVE_XZLIB -DKENLM_MAX_ORDER=6 -fPIC $CXXFLAGS"

#Grab all cc files in these directories except those ending in test.cc or main.cc
objects=""
for i in $SRCPATH/util/double-conversion/*.cc $SRCPATH/util/*.cc $SRCPATH/lm/*.cc; do
    g++ -I$SRCPATH $CXXFLAGS -c $i -o ${i%.cc}.o
    objects="$objects ${i%.cc}.o"
done

echo Compiling JNI library to $LIBPATH/$LIBNAME
g++ $CXXFLAGS $SRCPATH/kenlm_wrap.cc \
    -I$JAVA_HOME/include \
    -I$JAVA_HOME/include/linux \
    -I$SRCPATH \
    $objects -shared -Wl,-soname,$LIBNAME \
    -o $LIBPATH/$LIBNAME -lz -Wno-deprecated -pthread
