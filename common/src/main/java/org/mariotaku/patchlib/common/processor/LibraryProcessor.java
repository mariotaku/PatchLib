package org.mariotaku.patchlib.common.processor;

import org.mariotaku.patchlib.common.model.ProcessingRules;
import org.mariotaku.patchlib.common.processor.impl.AarLibraryProcessor;
import org.mariotaku.patchlib.common.processor.impl.JarLibraryProcessor;
import org.mariotaku.patchlib.common.util.Utils;
import org.xeustechnologies.jcl.JarClassLoader;

import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
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
    public final ProcessingRules rules;
    public final CommandLineOptions opts;

    public LibraryProcessor(InputStream source, OutputStream target, ProcessingRules rules) {
        this(source, target, rules, new CommandLineOptions());
    }

    public LibraryProcessor(InputStream source, OutputStream target, ProcessingRules rules, CommandLineOptions opts) {
        this.source = source;
        this.target = target;
        this.rules = rules;
        this.opts = opts;
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

    public static LibraryProcessor get(InputStream source, OutputStream target, String name, ProcessingRules conf, CommandLineOptions opts) {
        if (name.endsWith(".jar")) {
            return new JarLibraryProcessor(source, target, conf, opts);
        } else if (name.endsWith(".aar")) {
            return new AarLibraryProcessor(source, target, conf, opts);
        }
        return null;
    }

    public static class CommandLineOptions {
        public Set<File> extraClasspath;
        private ClassLoader cachedClassLoader;
        private boolean verbose;

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

        public void setVerbose(boolean verbose) {
            this.verbose = verbose;
        }

        public boolean isVerbose() {
            return verbose;
        }
    }
}
