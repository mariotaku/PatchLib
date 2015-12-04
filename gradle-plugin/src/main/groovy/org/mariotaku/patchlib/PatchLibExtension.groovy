package org.mariotaku.patchlib

import org.gradle.api.file.FileCollection

/**
 * Created by mariotaku on 15/11/29.
 */
class PatchLibExtension {
    FileCollection rules;
    boolean verbose = false;

    FileCollection getRules() {
        return rules
    }

    void setRules(FileCollection rules) {
        this.rules = rules
    }

    boolean getVerbose() {
        return verbose
    }

    void setVerbose(boolean verbose) {
        this.verbose = verbose
    }
}
