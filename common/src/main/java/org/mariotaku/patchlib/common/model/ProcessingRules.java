package org.mariotaku.patchlib.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mariotaku.patchlib.common.processor.LibraryProcessor;

import java.io.IOException;
import java.util.Map;

/**
 * Created by mariotaku on 15/12/2.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessingRules {

    Map<String, PatchClassInfo> rules;

    @JsonProperty("rules")
    public Map<String, PatchClassInfo> getRules() {
        return rules;
    }

    public void setRules(Map<String, PatchClassInfo> rules) {
        this.rules = rules;
    }

    public PatchClassInfo getClassInfo(LibraryProcessor.CommandLineOptions opts, String name, String signature,
                                       String superName, String[] interfaces) {
        if (rules == null) return null;
        for (Map.Entry<String, PatchClassInfo> entry : rules.entrySet()) {
            if (matches(opts, entry.getKey(), name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ConfigurationFile{" +
                "rules=" + rules +
                '}';
    }

    private boolean matches(LibraryProcessor.CommandLineOptions opts, String ruleName, String className) {
        if (ruleName.equals(className)) return true;
        if (ruleName.startsWith("/")) {
            try {
                final ClassLoader loader = opts.createClassLoader();
                final Class<?> ruleCls = loader.loadClass(ruleName.substring(1).replace('/', '.'));
                return ruleCls.isAssignableFrom(loader.loadClass(className.replace('/', '.')));
            } catch (ClassNotFoundException e) {
                return false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }
}
