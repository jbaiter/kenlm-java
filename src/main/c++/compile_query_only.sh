#!/bin/bash
rm {lm,util}/*.o 2>/dev/null
set -e

CXX=${CXX:-g++}

CXXFLAGS+=" -I. -O3 -DNDEBUG -DKENLM_MAX_ORDER=6 -DHAVE_ZLIB -DHAVE_BZLIB"

echo 'Compiling with '$CXX $CXXFLAGS

#Grab all cc files in these directories except those ending in test.cc or main.cc
objects=""
for i in util/double-conversion/*.cc util/*.cc lm/*.cc; do
    $CXX $CXXFLAGS -c $i -o ${i%.cc}.o
    objects="$objects ${i%.cc}.o"
done

$CXX $CXXFLAGS -c kenlm_wrap.cc -I$JAVA_HOME/include -I$JAVA_HOME/include/linux -I.
$CXX $CXXFLAGS $objects kenlm_wrap.o -shared -Wl,-soname,libken.so -o libken.so -lz -Wno-deprecated -pthread
