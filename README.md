To build this project you need the following prerequisites:
* jdk
* swig v3+
* g++
* kenlm

the order to build this project is:
1. run ./build.sh. This will generate the required Java JNI classes and the C++ interface using swig.
2. put the output libkenlm-jni into your java library path (optinal for running the tests in phase 3)
3. run mvn package

The following system environment variables need to be set when running build:
* LIBRARY_PATH to have libkenlm and libkenlm_util
* CPATH to point to kenlm source, JAVA_HOME/include and JAVA_HOME/include/<arch>

The library can be renamed. If that is the case com-github-jbaiter-kenlm.properties needs to be provided pointing directly to the file. An example can be found in test resources. If the properties are not set the library is loaded from java.library.path.
