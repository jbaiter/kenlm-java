package com.github.jbaiter.kenlm.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * <b>Internal only - Do not use this class.</b> This class loads a native
 * library of libkenlm (libkenlm.dll, libkenlm.so, etc.) according to the
 * user platform (<i>os.name</i> and <i>os.arch</i>). The natively compiled
 * libraries bundled to libkenlm contain the codes of the original libkenlm and
 * JNI programs to access libkenlm.
 * <p>
 * In default, no configuration is required to use libkenlm, but you can load
 * your own native library created by 'make native' command.
 * <p>
 * This KenLMLoader searches for native libraries (libkenlm.dll,
 * libkenlm.so, etc.) in the following order:
 * <ol>
 * <li>If system property <i>com.github.jbaiter.kenlm.use.systemlib</i> is set to true,
 * lookup folders specified by <i>java.lib.path</i> system property (This is the
 * default path that JVM searches for native libraries)
 * <li>(System property: <i>com.github.jbaiter.kenlm.lib.path</i>)/(System property:
 * <i>com.github.jbaiter.kenlm.lib.name</i>)
 * <li>One of the libraries embedded in kenlm-(version).jar extracted into
 * (System property: <i>java.io.tempdir</i>). If
 * <i>com.github.jbaiter.kenlm.tempdir</i> is set, use this folder instead of
 * <i>java.io.tempdir</i>.
 * </ol>
 * <p>
 * <p>
 * If you do not want to use folder <i>java.io.tempdir</i>, set the System
 * property <i>com.github.jbaiter.kenlm.tempdir</i>. For example, to use
 * <i>/tmp/vinhkhuc</i> as a temporary folder to copy native libraries, use -D option
 * of JVM:
 * <p>
 * <pre>
 * <code>
 * java -Dcom.github.jbaiter.kenlm.tempdir="/tmp/vinhkhuc" ...
 * </code>
 * </pre>
 * <p>
 * </p>
 * <p>
 * NOTE: Adapted from CrfSuiteLoaderLoader.java (jcrfsuite 0.6)
 *
 * @author jbaiter
 * @author leo
 * @author Vinh Khuc
 */
public class KenLMLoader {

  public static final String KENLM_SYSTEM_PROPERTIES_FILE = "com-github-jbaiter-kenlm.properties";
  public static final String KEY_KENLM_LIB_PATH = "com.github.jbaiter.kenlm.lib.path";
  public static final String KEY_KENLM_LIB_NAME = "com.github.jbaiter.kenlm.lib.name";
  public static final String KEY_KENLM_TEMPDIR = "com.github.jbaiter.kenlm.tempdir";
  public static final String KEY_KENLM_USE_SYSTEMLIB = "com.github.jbaiter.kenlm.use.systemlib";
  // Depreciated, but preserved for backward compatibility
  public static final String KEY_KENLM_DISABLE_BUNDLED_LIBS = "com.github.jbaiter.kenlm.disable.bundled.libs";

  private static volatile boolean isLoaded = false;

  /**
   * load system properties when configuration file of the name
   * {@link #KENLM_SYSTEM_PROPERTIES_FILE} is found
   */
  private static void loadCrfSuiteSystemProperties() {
    try {
      InputStream is = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(KENLM_SYSTEM_PROPERTIES_FILE);

      if (is == null)
        return; // no configuration file is found

      // Load property file
      Properties props = new Properties();
      props.load(is);
      is.close();
      Enumeration<?> names = props.propertyNames();
      while (names.hasMoreElements()) {
        String name = (String) names.nextElement();
        if (name.startsWith("com.github.jbaiter.kenlm.")) {
          if (System.getProperty(name) == null) {
            System.setProperty(name, props.getProperty(name));
          }
        }
      }
    } catch (Throwable ex) {
      System.err.println("Could not load '" + KENLM_SYSTEM_PROPERTIES_FILE +
          "' from classpath: " + ex.toString());
    }
  }

  static {
    loadCrfSuiteSystemProperties();
  }

  private static ClassLoader getRootClassLoader() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    while (cl.getParent() != null) {
      cl = cl.getParent();
    }
    return cl;
  }

  private static byte[] getByteCode(String resourcePath) throws IOException {

    InputStream in = KenLMLoader.class.getResourceAsStream(resourcePath);
    if (in == null)
      throw new IOException(resourcePath + " is not found");
    byte[] buf = new byte[1024];
    ByteArrayOutputStream byteCodeBuf = new ByteArrayOutputStream();
    for (int readLength; (readLength = in.read(buf)) != -1; ) {
      byteCodeBuf.write(buf, 0, readLength);
    }
    in.close();

    return byteCodeBuf.toByteArray();
  }

  public static boolean isNativeLibraryLoaded() {
    return isLoaded;
  }

  private static boolean hasInjectedNativeLoader() {
    try {
      final String nativeLoaderClassName = "native_loader.CrfSuiteNativeLoader";
      Class.forName(nativeLoaderClassName);
      // If this native loader class is already defined, it means that another class loader already loaded the native library of kenlm
      return true;

    } catch (ClassNotFoundException e) {
      // do loading
      return false;
    }
  }

  /**
   * Load CrfSuiteNative and its JNI native implementation using the root class
   * loader. This hack is for avoiding the JNI multi-loading issue when the
   * same JNI library is loaded by different class loaders.
   * <p>
   * In order to load native code in the root class loader, this method first
   * inject CrfsuiteNativeLoader class into the root class loader, because
   * {@link System#load(String)} method uses the class loader of the caller
   * class when loading native libraries.
   * <p>
   * <pre>
   * (root class loader) -> [CrfSuiteNativeLoader (load JNI code), CrfSuiteNative (has native methods), CrfSuiteNativeAPI, CrfSuiteErrorCode]  (injected by this method)
   *    |
   *    |
   * (child class loader) -> Sees the above classes loaded by the root class loader.
   *   Then creates CrfSuiteNativeAPI implementation by instantiating CrfSuiteNative class.
   * </pre>
   * <p>
   * <p>
   * <pre>
   * (root class loader) -> [CrfSuiteNativeLoader, CrfSuiteNative ...]  -> native code is loaded by once in this class loader
   *   |   \
   *   |    (child2 class loader)
   * (child1 class loader)
   *
   * child1 and child2 share the same CrfSuiteNative code loaded by the root class loader.
   * </pre>
   * <p>
   * Note that Java's class loader first delegates the class lookup to its
   * parent class loader. So once CrfSuiteNativeLoader is loaded by the root
   * class loader, no child class loader initialize CrfSuiteNativeLoader again.
   *
   * @throws Exception
   */
  public static synchronized void load() throws Exception {

    if (!isLoaded) {
      try {
        if (!hasInjectedNativeLoader()) {
          // Inject CrfSuiteNativeLoader (native_loader.KenLMLoader.bytecode) to the root class loader
          Class<?> nativeLoader = injectKenLMNativeLoader();
          // Load the JNI code using the injected loader
          loadNativeLibrary(nativeLoader);
        }

        // Look up CrfSuiteNative, injected to the root classloader, using reflection in order
        // to avoid the initialization of CrfSuiteNative class in this context class loader.
        Class.forName("com.github.jbaiter.kenlm.util.KenLMLoader");

        isLoaded = true;
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  /**
   * Inject CrfSuiteNativeLoader class to the root class loader
   *
   * @return native code loader class initialized in the root class loader
   * @throws Exception
   */
  private static Class<?> injectKenLMNativeLoader() throws Exception {

    try {
      // Use parent class loader to load CrfSuiteNative, since Tomcat, which
      // uses different class loaders for each webapps, cannot load JNI interface twice
      final String nativeLoaderClassName = "native_loader.KenLMNativeLoader";
      ClassLoader rootClassLoader = getRootClassLoader();
      // Load a byte code
      byte[] byteCode = getByteCode("/native_loader/KenLMNativeLoader.bytecode");
      // In addition, we need to load the other dependent classes (e.g., CrfSuiteNative) using the system class loader
      final String[] classesToPreload = new String[]{
          "com.github.jbaiter.kenlm.jni.Config",
          "com.github.jbaiter.kenlm.jni.FullScoreReturn",
          "com.github.jbaiter.kenlm.jni.KenLM",
          "com.github.jbaiter.kenlm.jni.KenLMJNI",
          "com.github.jbaiter.kenlm.jni.LoadMethod",
          "com.github.jbaiter.kenlm.jni.Model",
          "com.github.jbaiter.kenlm.jni.State",
          "com.github.jbaiter.kenlm.jni.SWIGTYPE_p_void",
          "com.github.jbaiter.kenlm.jni.Vocabulary",
          "com.github.jbaiter.kenlm.ModelException",
          "com.github.jbaiter.kenlm.ConfigException",
          "com.github.jbaiter.kenlm.LoadException",
          "com.github.jbaiter.kenlm.FormatLoadException",
          "com.github.jbaiter.kenlm.VocabLoadException",
      };
      List<byte[]> preloadClassByteCode = new ArrayList<byte[]>(classesToPreload.length);
      for (String each : classesToPreload) {
        preloadClassByteCode.add(getByteCode(String.format("/%s.class", each.replaceAll("\\.", "/"))));
      }

      // Create CrfSuiteNativeLoader class from a byte code
      Class<?> classLoader = Class.forName("java.lang.ClassLoader");
      Method defineClass = classLoader.getDeclaredMethod("defineClass", new Class<?>[]{String.class, byte[].class,
          int.class, int.class, ProtectionDomain.class});

      ProtectionDomain pd = System.class.getProtectionDomain();

      // ClassLoader.defineClass is a protected method, so we have to make it accessible
      defineClass.setAccessible(true);
      try {
        // Create a new class using a ClassLoader#defineClass
        defineClass.invoke(rootClassLoader, nativeLoaderClassName, byteCode, 0, byteCode.length, pd);

        // And also define dependent classes in the root class loader
        for (int i = 0; i < classesToPreload.length; ++i) {
          byte[] b = preloadClassByteCode.get(i);
          defineClass.invoke(rootClassLoader, classesToPreload[i], b, 0, b.length, pd);
        }
      } finally {
        // Reset the accessibility to defineClass method
        defineClass.setAccessible(false);
      }

      // Load the CrfSuiteNativeLoader class
      return rootClassLoader.loadClass(nativeLoaderClassName);

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }

  }

  /**
   * Load kenlm's native code using load method of the
   * KenMLNativeLoader class injected to the root class loader.
   *
   * @param loaderClass
   * @throws Exception
   */
  private static void loadNativeLibrary(Class<?> loaderClass) throws Exception {
    if (loaderClass == null)
      throw new Exception("missing kenlm native loader class");

    File nativeLib = findNativeLibrary();
    if (nativeLib != null) {
      // Load extracted or specified kenlm native library.
      Method loadMethod = loaderClass.getDeclaredMethod("loadLibByFile", new Class<?>[]{String.class});
      loadMethod.invoke(null, nativeLib.getAbsolutePath());
    } else {
      // Load preinstalled kenlm (in the path -Djava.library.path)
      Method loadMethod = loaderClass.getDeclaredMethod("loadLibrary", new Class<?>[]{String.class});
      loadMethod.invoke(null, "kenlm");
    }
  }

  /**
   * Computes the MD5 value of the input stream
   *
   * @param input
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  static String md5sum(InputStream input) throws IOException {
    BufferedInputStream in = new BufferedInputStream(input);
    try {
      MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
      DigestInputStream digestInputStream = new DigestInputStream(in, digest);
      for (; digestInputStream.read() >= 0; ) {

      }
      ByteArrayOutputStream md5out = new ByteArrayOutputStream();
      md5out.write(digest.digest());
      return md5out.toString();

    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 algorithm is not available: " + e);

    } finally {
      in.close();
    }
  }

  /**
   * Extract the specified library file to the target folder
   *
   * @param libFolderForCurrentOS
   * @param libraryFileName
   * @param targetFolder
   * @return library file object
   * @throws Exception
   */
  private static File extractLibraryFile(String libFolderForCurrentOS, String libraryFileName, String targetFolder) throws Exception {
    String nativeLibraryFilePath = libFolderForCurrentOS + "/" + libraryFileName;
    File extractedLibFile = new File(targetFolder, libraryFileName);

    try {
      if (extractedLibFile.exists()) {
        // test md5sum value
        String md5sum1 = md5sum(KenLMLoader.class.getResourceAsStream(nativeLibraryFilePath));
        String md5sum2 = md5sum(new FileInputStream(extractedLibFile));

        if (md5sum1.equals(md5sum2)) {
          return extractedLibFile;

        } else {
          // remove old native library file
          boolean deletionSucceeded = extractedLibFile.delete();
          if (!deletionSucceeded) {
            throw new IOException("failed to remove existing native library file: "
                + extractedLibFile.getAbsolutePath());
          }
        }
      }

      // Extract a native library file into the target directory
      InputStream reader = KenLMLoader.class.getResourceAsStream(nativeLibraryFilePath);
      FileOutputStream writer = new FileOutputStream(extractedLibFile);
      byte[] buffer = new byte[8192];
      int bytesRead = 0;
      while ((bytesRead = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, bytesRead);
      }

      writer.close();
      reader.close();

      // Set executable (x) flag to enable Java to load the native library
      if (!System.getProperty("os.name").contains("Windows")) {
        try {
          Runtime.getRuntime().exec(new String[]{"chmod", "755", extractedLibFile.getAbsolutePath()})
              .waitFor();
        } catch (Throwable e) {
        }
      }

      return extractedLibFile;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

  }

  static File findNativeLibrary() throws Exception {

    boolean useSystemLib = Boolean.parseBoolean(System.getProperty(KEY_KENLM_USE_SYSTEMLIB, "false"));
    if (useSystemLib)
      return null;

    boolean disabledBundledLibs = Boolean
        .parseBoolean(System.getProperty(KEY_KENLM_DISABLE_BUNDLED_LIBS, "false"));
    if (disabledBundledLibs)
      return null;

    // Try to load the library in com.github.jbaiter.kenlm.lib.path  */
    String kenlmNativeLibraryPath = System.getProperty(KEY_KENLM_LIB_PATH);
    String kenlmNativeLibraryName = System.getProperty(KEY_KENLM_LIB_NAME);

    // Resolve the library file name with a suffix (e.g., dll, .so, etc.)
    if (kenlmNativeLibraryName == null)
      kenlmNativeLibraryName = System.mapLibraryName("kenlm");

    if (kenlmNativeLibraryPath != null) {
      File nativeLib = new File(kenlmNativeLibraryPath, kenlmNativeLibraryName);
      if (nativeLib.exists())
        return nativeLib;
      nativeLib = new File(kenlmNativeLibraryPath, System.mapLibraryName(kenlmNativeLibraryName));
      if (nativeLib.exists())
        return nativeLib;
    }

    // Load an OS-dependent native library inside a jar file
    kenlmNativeLibraryPath = "/lib/" + OSInfo.getNativeLibFolderPathForCurrentOS();

    if (KenLMLoader.class.getResource(kenlmNativeLibraryPath + "/" + kenlmNativeLibraryName) != null) {
      // Temporary library folder. Use the value of com.github.jbaiter.kenlm.tempdir or java.io.tmpdir
      String tempFolder = new File(System.getProperty(KEY_KENLM_TEMPDIR,
          System.getProperty("java.io.tmpdir"))).getAbsolutePath();

      // Extract and load a native library inside the jar file
      return extractLibraryFile(kenlmNativeLibraryPath, kenlmNativeLibraryName, tempFolder);
    }

    return null; // Use a pre-installed libkenlm
  }
}
