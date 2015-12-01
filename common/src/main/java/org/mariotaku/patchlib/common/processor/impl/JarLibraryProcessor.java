package org.mariotaku.patchlib.common.processor.impl;

import org.mariotaku.patchlib.common.model.PatchClassInfo;
import org.mariotaku.patchlib.common.processor.LibraryProcessor;
import org.mariotaku.patchlib.common.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * Created by mariotaku on 15/11/29.
 */
public class JarLibraryProcessor extends LibraryProcessor {

    public JarLibraryProcessor(InputStream source, OutputStream target, Map<String, PatchClassInfo> rules, Configuration conf) {
        super(source, target, rules, conf);
    }

    public JarLibraryProcessor(InputStream source, OutputStream target, Map<String, PatchClassInfo> rules) {
        super(source, target, rules);
    }

    protected boolean processEntry(JarInputStream inputArchive, JarOutputStream outputArchive, JarEntry entry) throws IOException {
        boolean processed = false;
        final String entryName = entry.getName();
        if (entry.isDirectory()) {
            processDirectory(outputArchive, entry);
        } else if (entryName.endsWith(".class")) {
            final String className = entryName.substring(0, entryName.length() - ".class".length());
            final PatchClassInfo classInfo = rules.get(className);
            processed = Utils.processMatchedClass(inputArchive, outputArchive, entry, classInfo, conf);
        } else if (!entryName.equals(JarFile.MANIFEST_NAME)) {
            processDirectFile(inputArchive, outputArchive, entry);
        }
        return processed;
    }

    static void processDirectFile(JarInputStream inputArchive, JarOutputStream outputArchive, JarEntry entry) throws IOException {
        // Pass through
        final JarEntry newEntry = new JarEntry(entry.getName());
        outputArchive.putNextEntry(newEntry);
        Utils.copy(inputArchive, outputArchive);
        outputArchive.closeEntry();
    }

    static void processDirectory(JarOutputStream outputArchive, JarEntry entry) throws IOException {
        outputArchive.putNextEntry(new JarEntry(entry.getName()));
        outputArchive.closeEntry();
    }


}
