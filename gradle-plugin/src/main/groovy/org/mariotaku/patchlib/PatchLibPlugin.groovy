package org.mariotaku.patchlib

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.compile.JavaCompile
import org.mariotaku.patchlib.task.PatchLibProcessTask

import java.util.jar.JarFile
import java.util.regex.Pattern

/**
 * Created by mariotaku on 15/11/29.
 */
class PatchLibPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        project.extensions.create('patchLib', PatchLibExtension)
        project.configurations.create('patchCompile')

        def patchLibDir = new File(project.buildDir, 'patchLib')

        project.tasks.create('patchLibProcess', PatchLibProcessTask) << { PatchLibProcessTask process ->
            PatchLibExtension props = project.extensions.findByType(PatchLibExtension)
            if (!props) return;
            Set execClasspath = []
            // Add compile classpath and build script classpath to java exec task
            project.tasks.withType(JavaCompile) { compile ->

                def bootClasspath = compile.options.bootClasspath
                if (bootClasspath != null) {
                    execClasspath += project.files(splitBootClassPath(bootClasspath))
                }
            }

            execClasspath += project.rootProject.buildscript.configurations.classpath
            execClasspath += project.buildscript.configurations.classpath

            Set alreadyPatched = []
            addToAlreadyPatched(project, alreadyPatched)

            def dependencyLibs = [:]
            def librariesToRemove = []
            project.configurations.patchCompile.each { File libFile ->
                def destinationArchive = new File(patchLibDir, libFile.name)
                if (alreadyPatched.contains(libFile)) {
                    printf('Ignoring processed library %s\n', libFile)
                    librariesToRemove.add(destinationArchive)
                    return
                }
                project.javaexec {
                    it.main = Main.class.name
                    it.args = ['-i', libFile.absolutePath,
                               '-o', destinationArchive.absolutePath,
                               '-r', props.rules.getAsPath(),
                               // In order to avoid library clash, all dependency libraries are loaded separately in
                               // custom ClassLoader
                               '-c', project.configurations.patchCompile.getAsPath(),
                               '-v', String.valueOf(props.verbose)
                    ]
                    //
                    it.classpath += project.files(execClasspath)
                }

                dependencyLibs.put(libFile, destinationArchive)
            }

            project.configurations.patchCompile.files.each { File libFile ->
                if (!dependencyLibs.containsKey(libFile)) {
                    dependencyLibs.put(libFile, libFile)
                } else {
                    librariesToRemove.add(libFile)
                }
            }

            project.tasks.withType(JavaCompile) { compile ->
                Set finalLibraries = [], addedLibs = [];
                dependencyLibs.each {
                    def jar = new JarFile(it.value as File)

                    if (jar.manifest?.getAttributes("PatchLib")?.containsKey("Source-File-Path")) {
                        def sourceFile = new File(jar.manifest.getAttributes("PatchLib").get("Source-File-Path") as String)
                        if (sourceFile.exists()) {
                            // Remove deps in comment, and mark this library added
                            if (!addedLibs.contains(sourceFile)) {
                                finalLibraries.add(it.value)
                                addedLibs.add(sourceFile)
                            } else {
                                librariesToRemove.add(it.value)
                            }
                            librariesToRemove.add(sourceFile)
                        } else {
                            finalLibraries.add(it.value)
                        }
                    } else {
                        finalLibraries.add(it.value)
                    }

                    jar.close()
                }
                compile.classpath += project.files(finalLibraries)
                compile.classpath -= project.files(librariesToRemove)
                compile.classpath -= project.files(alreadyPatched)
            }
        }

        project.tasks.withType(JavaCompile) { compile ->
            compile.dependsOn += project.tasks.withType(PatchLibProcessTask)
        }
        project.dependencies.add('compile', project.fileTree(dir: patchLibDir, include: ['*.jar']))

    }

    static def addToAlreadyPatched(Project project, Set set) {
        project.configurations.compile.dependencies.each { dependency ->
            if (dependency instanceof ProjectDependency) {
                def dependencyProject = dependency.dependencyProject
                if (dependencyProject.configurations.hasProperty('patchCompile')) {
                    dependencyProject.configurations.patchCompile.each { libFile ->
                        set.add(libFile)
                    }
                }
            }
        }
    }

    static def splitBootClassPath(String paths) {
        def result = []
        paths.split(Pattern.quote(paths)).each {
            if (!it.empty) result.add(it)
        }
        return result.toArray()
    }

    static def getAarDependency(String name) {
        def lastDot = name.lastIndexOf('.'), lastDash = name.lastIndexOf('-')
        return name.substring(0, lastDash) + ':' + name.substring(lastDash + 1, lastDot) + '@' +
                name.substring(name.lastIndexOf('.') + 1)
    }

    static def getLibraryExtension(String name) {
        return name.substring(name.lastIndexOf('.') + 1)
    }

    static def isJavaProject(Project project) {
        project.plugins.findPlugin('java')
    }

    static def isAndroidProject(Project project) {
        project.plugins.findPlugin('com.android.application') || project.plugins.findPlugin('com.android.library')
    }

}
