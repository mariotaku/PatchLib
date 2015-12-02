package org.mariotaku.patchlib.common.processor;

import org.mariotaku.patchlib.common.model.PatchClassInfo;
import org.mariotaku.patchlib.common.processor.impl.AarLibraryProcessor;
import org.mariotaku.patchlib.common.processor.impl.JarLibraryProcessor;
import org.mariotaku.patchlib.common.util.Utils;
import org.xeustechnologies.jcl.JarClassLoader;

import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * Created by mariotaku on 15/11/30.
 */
public abstract class LibraryProcessor {

    public final InputStream source;
    public final OutputStream target;
    public final Map<String, PatchClassInfo> rules;
    public final Configuration conf;

    public LibraryProcessor(InputStream source, OutputStream target, Map<String, PatchClassInfo> rules) {
        this(source, target, rules, new Configuration());
    }

    public LibraryProcessor(InputStream source, OutputStream target, Map<String, PatchClassInfo> rules, Configuration conf) {
        this.source = source;
        this.target = target;
        this.rules = rules;
        this.conf = conf;
    }

    public boolean process() throws IOException {
        boolean processed = false;
        final JarInputStream inputStream = new JarInputStream(source);
        final JarOutputStream outputStream = Utils.openJarOutputStreamForCopy(inputStream, target);
        JarEntry entry;
        while ((entry = inputStream.getNextJarEntry()) != null) {
            // Only process .class file
            processed |= processEntry(inputStream, outputStream, entry);
        }
        outputStream.finish();
        return processed;
    }

    protected abstract boolean processEntry(JarInputStream inputArchive, JarOutputStream outputArchive, JarEntry entry) throws IOException;

    public static LibraryProcessor get(InputStream source, OutputStream target, Map<String, PatchClassInfo> rules, String name) {
        if (name.endsWith(".jar")) {
            return new JarLibraryProcessor(source, target, rules);
        } else if (name.endsWith(".aar")) {
            return new AarLibraryProcessor(source, target, rules);
        }
        return null;
    }

    public void setExtraClasspath(Set<File> classpath) {
        conf.setExtraClasspath(classpath);
    }

    public static class Configuration {
        public Set<File> extraClasspath;
        private ClassLoader cachedClassLoader;

        public Set<File> getExtraClasspath() {
            return extraClasspath;
        }

        public void setExtraClasspath(Set<File> extraClasspath) {
            this.extraClasspath = extraClasspath;
            cachedClassLoader = null;
        }

        public ClassLoader createClassLoader() throws IOException {
            if (cachedClassLoader != null) return cachedClassLoader;
            if (extraClasspath == null) {
                return cachedClassLoader = LibraryProcessor.class.getClassLoader();
            }
            return cachedClassLoader = createClassLoader(extraClasspath);
        }

        private static ClassLoader createClassLoader(Collection<File> classpath) throws IOException {
            JarClassLoader jcl = new JarClassLoader();
            for (File item : classpath) {
                if (item == null) continue;
                final String name = item.getName();
                if (name.endsWith(".jar")) {
                    jcl.add(new FileInputStream(item));
                } else if (name.endsWith(".aar")) {
                    try (JarFile zip = new JarFile(item)) {
                        final Enumeration<? extends JarEntry> entries = zip.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            if (entry.getName().endsWith(".jar")) {
                                jcl.add(zip.getInputStream(entry));
                            }
                        }
                    }
                }
            }
            return jcl;
        }
    }
}
