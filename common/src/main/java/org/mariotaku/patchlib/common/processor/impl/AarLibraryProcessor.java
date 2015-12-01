package org.mariotaku.patchlib.common.processor.impl;

import org.mariotaku.patchlib.common.model.PatchClassInfo;
import org.mariotaku.patchlib.common.processor.LibraryProcessor;
import org.mariotaku.patchlib.common.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * Created by mariotaku on 15/11/30.
 */
public class AarLibraryProcessor extends LibraryProcessor {

    public AarLibraryProcessor(InputStream source, OutputStream target, Map<String, PatchClassInfo> rules) {
        super(source, target, rules);
    }

    public AarLibraryProcessor(InputStream source, OutputStream target, Map<String, PatchClassInfo> rules, Configuration conf) {
        super(source, target, rules, conf);
    }

    @Override
    protected boolean processEntry(JarInputStream inputStream, JarOutputStream outputStream, JarEntry entry) throws IOException {
        boolean processed = false;
        final String entryName = entry.getName();
        if (entry.isDirectory()) {
            JarLibraryProcessor.processDirectory(outputStream, entry);
        } else if (entryName.endsWith(".class")) {
            final String className = entryName.substring(0, entryName.length() - ".class".length());
            final PatchClassInfo classInfo = rules.get(className);
            processed = Utils.processMatchedClass(inputStream, outputStream, entry, classInfo, conf);
        } else if (entryName.endsWith(".jar")) {
            final JarEntry newEntry = new JarEntry(entry.getName());
            outputStream.putNextEntry(newEntry);
            JarLibraryProcessor processor = new JarLibraryProcessor(inputStream, outputStream, rules, conf);
            processed = processor.process();
            outputStream.closeEntry();
        } else {
            JarLibraryProcessor.processDirectFile(inputStream, outputStream, entry);
        }
        return processed;
    }
}
