package org.mariotaku.patchlib.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.mariotaku.patchlib.common.model.deserializer.ExceptionInfoDeserializer;
import org.mariotaku.patchlib.common.model.deserializer.ModifierInfoDeserializer;

import java.util.Map;

/**
 * Created by mariotaku on 15/11/29.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatchClassInfo {

    Map<String, PatchMemberInfo> fields;
    Map<String, PatchMethodInfo> methods;
    ModifierInfo modifiers;

    @JsonProperty("modifiers")
    public ModifierInfo getModifiers() {
        return modifiers;
    }

    @JsonDeserialize(using = ModifierInfoDeserializer.class)
    public void setModifiers(ModifierInfo modifiers) {
        this.modifiers = modifiers;
    }

    @JsonProperty("fields")
    public Map<String, PatchMemberInfo> getFields() {
        return fields;
    }

    public void setFields(Map<String, PatchMemberInfo> fields) {
        this.fields = fields;
    }

    @JsonProperty("methods")
    public Map<String, PatchMethodInfo> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, PatchMethodInfo> methods) {
        this.methods = methods;
    }

    public int processModifiers(int access) {
        if (modifiers == null) return access;
        return modifiers.process(access);
    }

    public PatchMemberInfo getFieldInfo(String name) {
        if (fields == null) return null;
        return fields.get(name);
    }

    public PatchMethodInfo getMethodInfo(String name) {
        if (methods == null) return null;
        return methods.get(name);
    }

    @Override
    public String toString() {
        return "PatchClassInfo{" +
                "fields=" + fields +
                ", methods=" + methods +
                ", modifiers=" + modifiers +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PatchMemberInfo {
        ModifierInfo modifiers;

        @JsonProperty("modifiers")
        public ModifierInfo getModifiers() {
            return modifiers;
        }

        @JsonDeserialize(using = ModifierInfoDeserializer.class)
        public void setModifiers(ModifierInfo modifiers) {
            this.modifiers = modifiers;
        }

        public int processModifiers(int access) {
            if (modifiers == null) return access;
            return modifiers.process(access);
        }

        @Override
        public String toString() {
            return "PatchMemberInfo{" +
                    "modifiers=" + modifiers +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PatchMethodInfo extends PatchMemberInfo {
        ExceptionInfo exceptions;

        @JsonProperty("exceptions")
        public ExceptionInfo getExceptions() {
            return exceptions;
        }

        @JsonDeserialize(using = ExceptionInfoDeserializer.class)
        public void setExceptions(ExceptionInfo exceptions) {
            this.exceptions = exceptions;
        }

        @Override
        public String toString() {
            return "PatchMethodInfo{" +
                    "exceptions=" + exceptions +
                    "} " + super.toString();
        }

        public String[] processExceptions(String[] array) {
            if (exceptions == null) return array;
            return exceptions.process(array);
        }
    }

}
