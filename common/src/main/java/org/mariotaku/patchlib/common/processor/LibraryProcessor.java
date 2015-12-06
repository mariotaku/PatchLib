package org.mariotaku.patchlib.common.processor;

import org.mariotaku.patchlib.common.model.ProcessingRules;
import org.mariotaku.patchlib.common.processor.impl.AarLibraryProcessor;
import org.mariotaku.patchlib.common.processor.impl.JarLibraryProcessor;
import org.xeustechnologies.jcl.JarClassLoader;

import java.io.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.*;

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

        Manifest manifest = inputStream.getManifest();
        if (manifest == null) {
            manifest = new Manifest();
        }
        if (opts.sourceFilePath != null) {
            final Map<String, Attributes> entries = manifest.getEntries();
            Attributes attributes = new Attributes();
            attributes.put(new Attributes.Name("Source-File-Path"), opts.sourceFilePath);
            entries.put("PatchLib", attributes);
        }
        final JarOutputStream outputStream = new JarOutputStream(target, manifest);
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
        private String sourceFilePath;

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

        public void setSourceFilePath(String comment) {
            this.sourceFilePath = comment;
        }

        public String getSourceFilePath() {
            return sourceFilePath;
        }
    }
}
