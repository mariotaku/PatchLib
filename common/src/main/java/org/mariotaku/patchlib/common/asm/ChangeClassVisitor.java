package org.mariotaku.patchlib.common.asm;

import org.mariotaku.patchlib.common.model.PatchClassInfo;
import org.mariotaku.patchlib.common.model.ProcessingRules;
import org.mariotaku.patchlib.common.processor.LibraryProcessor;
import org.objectweb.asm.*;

public class ChangeClassVisitor extends ClassVisitor {

    private final ProcessingRules conf;
    private final LibraryProcessor.CommandLineOptions opts;
    private PatchClassInfo classInfo;

    public ChangeClassVisitor(ClassWriter cw, ProcessingRules conf, LibraryProcessor.CommandLineOptions opts) {
        super(Opcodes.ASM5, cw);
        this.conf = conf;
        this.opts = opts;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classInfo = conf.getClassInfo(opts, name, signature, superName, interfaces);
        if (classInfo != null) {
            if (opts.isVerbose()) {
                System.out.printf("Processing class %s\n", name);
            }
            access = classInfo.processModifiers(access);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (classInfo != null) {
            PatchClassInfo.PatchMemberInfo fieldInfo = classInfo.getFieldInfo(name);
            if (fieldInfo != null) {
                if (opts.isVerbose()) {
                    System.out.printf("Processing field %s\n", name);
                }
                access = fieldInfo.processModifiers(access);
            }
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (classInfo != null) {
            PatchClassInfo.PatchMethodInfo fieldInfo = classInfo.getMethodInfo(name);
            if (fieldInfo != null) {
                if (opts.isVerbose()) {
                    System.out.printf("Processing method %s\n", name);
                }
                access = fieldInfo.processModifiers(access);
                exceptions = fieldInfo.processExceptions(exceptions);
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    public boolean found() {
        return classInfo != null;
    }
}