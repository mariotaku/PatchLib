package org.mariotaku.patchlib.common.util;

import org.mariotaku.patchlib.common.asm.ChangeClassVisitor;
import org.mariotaku.patchlib.common.asm.ExtendedClassWriter;
import org.mariotaku.patchlib.common.model.ConfigurationFile;
import org.mariotaku.patchlib.common.processor.LibraryProcessor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Created by mariotaku on 15/12/1.
 */
public class Utils {

    public static JarOutputStream openJarOutputStreamForCopy(JarInputStream sourceStream, OutputStream targetStream) throws IOException {
        final Manifest manifest = sourceStream.getManifest();
        if (manifest != null) return new JarOutputStream(targetStream, manifest);
        return new JarOutputStream(targetStream);
    }

    public static boolean processMatchedClass(JarInputStream inputArchive, JarOutputStream outputArchive, JarEntry entry,
                                              ConfigurationFile conf, LibraryProcessor.Options opts) throws IOException {
        final ClassReader cr = new ClassReader(inputArchive);
        final ClassWriter cw = new ExtendedClassWriter(ClassWriter.COMPUTE_FRAMES, opts.createClassLoader());

        final ChangeClassVisitor cv = new ChangeClassVisitor(cw, conf, opts);
        cr.accept(cv, 0);

        final JarEntry newEntry = new JarEntry(entry.getName());
        outputArchive.putNextEntry(newEntry);
        outputArchive.write(cw.toByteArray());
        outputArchive.closeEntry();
        return cv.found();
    }

    public static void copy(JarInputStream is, JarOutputStream os) throws IOException {
        final byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
    }
}
