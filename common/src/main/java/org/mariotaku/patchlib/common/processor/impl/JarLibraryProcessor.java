package org.mariotaku.patchlib.common.processor.impl;

import org.mariotaku.patchlib.common.model.ProcessingRules;
import org.mariotaku.patchlib.common.processor.LibraryProcessor;
import org.mariotaku.patchlib.common.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * Created by mariotaku on 15/11/29.
 */
public class JarLibraryProcessor extends LibraryProcessor {

    public JarLibraryProcessor(InputStream source, OutputStream target, ProcessingRules conf, CommandLineOptions opts) {
        super(source, target, conf, opts);
    }

    public JarLibraryProcessor(InputStream source, OutputStream target, ProcessingRules conf) {
        super(source, target, conf);
    }

    protected boolean processEntry(JarInputStream inputArchive, JarOutputStream outputArchive, JarEntry entry) throws IOException {
        boolean processed = false;
        final String entryName = entry.getName();
        if (entry.isDirectory()) {
            processDirectory(outputArchive, entry);
        } else if (entryName.endsWith(".class")) {
            processed = Utils.processMatchedClass(inputArchive, outputArchive, entry, rules, opts);
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
