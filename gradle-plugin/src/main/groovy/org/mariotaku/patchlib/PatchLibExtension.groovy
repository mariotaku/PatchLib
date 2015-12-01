package org.mariotaku.patchlib

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency

/**
 * Created by mariotaku on 15/11/29.
 */
class PatchLibExtension {
    File rule;
    Project project

    PatchLibExtension(Project project) {
        this.project = project
    }

}
