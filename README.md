requires:
    jdk
    swig
    g++
    kenlm

required to set:
LIBRARY_PATH to have libkenlm and libkenlm_util
CPATH to point to kenlm source, JAVA_HOME/include and JAVA_HOME/include/<arch>

run build.sh to create libkenlm-jni.so/libkenlm-jni.dylib
put this into you java.library.path

library can be renamed by creating and moved if you dont have access to java.library.path using com-github-jbaiter-kenlm.properties
example can be found in test resources
