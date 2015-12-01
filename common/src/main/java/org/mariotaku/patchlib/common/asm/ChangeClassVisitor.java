package org.mariotaku.patchlib.common.asm;

import org.mariotaku.patchlib.common.model.PatchClassInfo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ChangeClassVisitor extends ClassVisitor {

    private final PatchClassInfo classInfo;

    public ChangeClassVisitor(ClassVisitor cv, PatchClassInfo classInfo) {
        super(Opcodes.ASM5, cv);
        this.classInfo = classInfo;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        access = classInfo.processModifiers(access);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        PatchClassInfo.PatchMemberInfo fieldInfo = classInfo.getFieldInfo(name);
        if (fieldInfo != null) {
            access = fieldInfo.processModifiers(access);
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        PatchClassInfo.PatchMemberInfo fieldInfo = classInfo.getMethodInfo(name);
        if (fieldInfo != null) {
            access = fieldInfo.processModifiers(access);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
}