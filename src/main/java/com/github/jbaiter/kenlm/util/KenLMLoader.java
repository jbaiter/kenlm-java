package com.github.jbaiter.kenlm.util;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
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
    private static void loadProperties() {
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
        loadProperties();
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
                loadNativeLibrary();
                isLoaded = true;
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    /**
     * Load kenlm's native code using load method of the
     * KenMLNativeLoader class injected to the root class loader.
     *
     * @throws Exception
     */
    private static void loadNativeLibrary() throws Exception {
        File nativeLib = findNativeLibrary();
        if (nativeLib != null) {
            System.load(nativeLib.getAbsolutePath());
        } else {
            final String libraryName = System.getProperty(KEY_KENLM_LIB_NAME);
            if (libraryName == null || "".equals(libraryName)) {
                System.loadLibrary("kenlm-jni");
            } else {
                try {
                    System.loadLibrary(libraryName);
                } catch (final Exception e) {
                    System.loadLibrary(System.mapLibraryName(libraryName));
                }
            }
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

        return null; // Use a pre-installed libkenlm
    }
}
