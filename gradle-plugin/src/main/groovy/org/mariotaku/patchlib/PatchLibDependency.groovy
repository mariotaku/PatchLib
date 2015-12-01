package org.mariotaku.patchlib

import org.apache.ivy.plugins.resolver.DependencyResolver
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.internal.file.FileSystemSubset
import org.gradle.api.tasks.TaskDependency

/**
 * Created by mariotaku on 15/11/29.
 */
class PatchLibDependency implements FileCollectionDependency {

    Dependency delegate
    Set<File> files

    PatchLibDependency(Dependency delegate, Set<File> files) {
        this.delegate = delegate;
        this.files = files;
    }

    @Override
    void registerWatchPoints(FileSystemSubset.Builder builder) {

    }

    @Override
    Set<File> resolve() {
        return files
    }

    @Override
    Set<File> resolve(boolean transitive) {
        return files
    }

    @Override
    TaskDependency getBuildDependencies() {
        return null
    }

    @Override
    String getGroup() {
        return delegate.group
    }

    @Override
    String getName() {
        return delegate.name
    }

    @Override
    String getVersion() {
        return delegate.version
    }

    @Override
    boolean contentEquals(Dependency dependency) {
        return false
    }

    @Override
    Dependency copy() {
        return new PatchLibDependency(delegate, files)
    }
}
