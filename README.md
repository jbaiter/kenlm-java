requires:
    jdk
    swig
    g++
    kenlm

required to set:
LIBRARY_PATH to have libkenlm and libkenlm_util
CPATH to point to kenlm source, JAVA_HOME/include and JAVA_HOME/include/<arch>

run build.sh to create libkenlm-jni.so
put this into you java.library.path
