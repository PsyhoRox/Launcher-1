package ru.gravit.utils.helper;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import ru.gravit.launcher.LauncherAPI;

public final class JVMHelper {
    @SuppressWarnings("unused")
    @LauncherAPI
    public enum OS {
        MUSTDIE("mustdie"), LINUX("linux"), MACOSX("macosx");

        public static OS byName(String name) {
            if (name.startsWith("Windows"))
                return MUSTDIE;
            if (name.startsWith("Linux"))
                return LINUX;
            if (name.startsWith("Mac OS X"))
                return MACOSX;
            throw new RuntimeException(String.format("This shit is not yet supported: '%s'", name));
        }

        public final String name;

        OS(String name) {
            this.name = name;
        }
    }

    // MXBeans exports
    @LauncherAPI
    public static final RuntimeMXBean RUNTIME_MXBEAN = ManagementFactory.getRuntimeMXBean();

    @LauncherAPI
    public static final OperatingSystemMXBean OPERATING_SYSTEM_MXBEAN =
            ManagementFactory.getOperatingSystemMXBean();
    // System properties
    @LauncherAPI
    public static final OS OS_TYPE = OS.byName(OPERATING_SYSTEM_MXBEAN.getName());
    @LauncherAPI
    public static final String OS_VERSION = OPERATING_SYSTEM_MXBEAN.getVersion();
    @LauncherAPI
    public static final int OS_BITS = getCorrectOSArch();
    @LauncherAPI
    public static final int JVM_BITS = Integer.parseInt(System.getProperty("sun.arch.data.model"));
    @LauncherAPI
    public static final int RAM = getRAMAmount();

    @LauncherAPI
    public static final SecurityManager SECURITY_MANAGER = System.getSecurityManager();
    // Public static fields
    @LauncherAPI
    public static final Runtime RUNTIME = Runtime.getRuntime();

    @LauncherAPI
    public static final ClassLoader LOADER = ClassLoader.getSystemClassLoader();

    static {
        try {
            MethodHandles.publicLookup(); // Just to initialize class
        } catch (Throwable exc) {
            LogHelper.error("Unsafe field is not initialized");
            throw new InternalError(exc);
        }
    }

    @Deprecated
    @LauncherAPI
    public static void addClassPath(URL url) {
        throw new IllegalArgumentException("Method Deprecated");
    }

    @Deprecated
    @LauncherAPI
    public static void addNativePath(Path path) {
        throw new IllegalArgumentException("Method Deprecated");
    }

    @LauncherAPI
    public static void appendVars(ProcessBuilder builder, Map<String, String> vars) {
        builder.environment().putAll(vars);
    }

    public static Class<?> firstClass(String... names) throws ClassNotFoundException {
        for (String name : names)
            try {
                return Class.forName(name, false, LOADER);
            } catch (ClassNotFoundException ignored) {
                // Expected
            }
        throw new ClassNotFoundException(Arrays.toString(names));
    }

    @LauncherAPI
    @SuppressWarnings("CallToSystemGC")
    public static void fullGC() {
        RUNTIME.gc();
        RUNTIME.runFinalization();
        LogHelper.debug("Used heap: %d MiB", RUNTIME.totalMemory() - RUNTIME.freeMemory() >> 20);
    }

    @Deprecated
    @LauncherAPI
    public static Certificate[] getCertificates(String resource) {
        throw new IllegalArgumentException("Method Deprecated");
    }

    @LauncherAPI
    public static String[] getClassPath() {
        return System.getProperty("java.class.path").split(File.pathSeparator);
    }

    @LauncherAPI
    public static URL[] getClassPathURL() {
        String[] cp = System.getProperty("java.class.path").split(File.pathSeparator);
        URL[] list = new URL[cp.length];
        int it = 0;
        for (String s : cp) {
            URL url = null;
            try {
                url = new URL(s);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            list[it] = url;
            it++;
        }
        return list;
    }

    @SuppressWarnings("CallToSystemGetenv")
    private static int getCorrectOSArch() {
        // As always, mustdie must die
        if (OS_TYPE == OS.MUSTDIE)
            return System.getenv("ProgramFiles(x86)") == null ? 32 : 64;

        // Or trust system property (maybe incorrect)
        return System.getProperty("os.arch").contains("64") ? 64 : 32;
    }

    @LauncherAPI
    public static String getEnvPropertyCaseSensitive(String name) {
        return System.getenv().get(name);
    }

    private static int getRAMAmount() {
        // TODO Normal fix.
        int physicalRam = (int) (((com.sun.management.OperatingSystemMXBean) OPERATING_SYSTEM_MXBEAN).getTotalPhysicalMemorySize() >> 20);
        return Math.min(physicalRam, OS_BITS == 32 ? 1536 : 32768); // Limit 32-bit OS to 1536 MiB, and 64-bit OS to 32768 MiB (because it's enough)
    }

    @LauncherAPI
    public static boolean isJVMMatchesSystemArch() {
        return JVM_BITS == OS_BITS;
    }

    @LauncherAPI
    public static String jvmProperty(String name, String value) {
        return String.format("-D%s=%s", name, value);
    }

    @LauncherAPI
    public static String systemToJvmProperty(String name) {
        return String.format("-D%s=%s", name, System.getProperties().getProperty(name));
    }

    @LauncherAPI
    public static void verifySystemProperties(Class<?> mainClass, boolean requireSystem) {
        Locale.setDefault(Locale.US);
        // Verify class loader
        LogHelper.debug("Verifying class loader");
        if (requireSystem && !mainClass.getClassLoader().equals(LOADER))
            throw new SecurityException("ClassLoader should be system");

        // Verify system and java architecture
        LogHelper.debug("Verifying JVM architecture");
        if (!isJVMMatchesSystemArch()) {
            LogHelper.warning("Java and OS architecture mismatch");
            LogHelper.warning("It's recommended to download %d-bit JRE", OS_BITS);
        }
    }

    private JVMHelper() {
    }
}
