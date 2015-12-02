package org.mariotaku.patchlib.common.processor.impl;

import org.mariotaku.patchlib.common.model.ConfigurationFile;
import org.mariotaku.patchlib.common.processor.LibraryProcessor;
import org.mariotaku.patchlib.common.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

/**
 * Created by mariotaku on 15/11/30.
 */
public class AarLibraryProcessor extends LibraryProcessor {

    public AarLibraryProcessor(InputStream source, OutputStream target, ConfigurationFile conf) {
        super(source, target, conf);
    }

    public AarLibraryProcessor(InputStream source, OutputStream target, ConfigurationFile conf, Options opts) {
        super(source, target, conf, opts);
    }

    @Override
    protected boolean processEntry(JarInputStream inputStream, JarOutputStream outputStream, JarEntry entry) throws IOException {
        boolean processed = false;
        final String entryName = entry.getName();
        if (entry.isDirectory()) {
            JarLibraryProcessor.processDirectory(outputStream, entry);
        } else if (entryName.endsWith(".class")) {
            processed = Utils.processMatchedClass(inputStream, outputStream, entry, conf, opts);
        } else if (entryName.endsWith(".jar")) {
            final JarEntry newEntry = new JarEntry(entry.getName());
            outputStream.putNextEntry(newEntry);
            JarLibraryProcessor processor = new JarLibraryProcessor(inputStream, outputStream, conf, opts);
            processed = processor.process();
            outputStream.closeEntry();
        } else {
            JarLibraryProcessor.processDirectFile(inputStream, outputStream, entry);
        }
        return processed;
    }
}
