package org.mariotaku.patchlib

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.mariotaku.patchlib.task.PatchLibProcessTask

/**
 * Created by mariotaku on 15/11/29.
 */
class PatchLibPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        project.extensions.create("patchLib", PatchLibExtension, project)
        project.configurations.create('patchCompile')

        project.tasks.create('patchCompile', PatchLibProcessTask) << { PatchLibProcessTask process ->
            def rulePath = project.patchLib?.rule?.absolutePath
            def patchLibDir = new File(project.buildDir, 'patchLib')
            Set execClasspath = []
            // Add compile classpath and build script classpath to java exec task
            project.tasks.withType(JavaCompile) { compile ->

                def bootClasspath = compile.options.bootClasspath
                if (bootClasspath != null) {
                    execClasspath += project.files(bootClasspath.split(":"))
                }
            }
            execClasspath += project.buildscript.configurations.classpath

            // In order to avoid library clash, all dependency libraries are loaded separately in custom ClassLoader
            project.configurations.patchCompile.files.each { File libFile ->
                def destinationArchive = createLibsFile(patchLibDir, libFile.name)
                project.javaexec {
                    it.main = Main.class.name
                    it.args = ['-i', libFile.absolutePath, '-o', destinationArchive.absolutePath, '-r', rulePath, '-c',
                               project.configurations.patchCompile.files.join(":")]
                    //
                    it.classpath += project.files(execClasspath)
                }
                project.tasks.withType(JavaCompile) { compile ->
                    compile.classpath += project.files(destinationArchive)
                }
            }
        }

        // Setup process task by assigning configs
//        project.configurations.patchCompile.files.each { File libFile ->
//            project.tasks.withType(PatchLibProcessTask) { PatchLibProcessTask process ->
//                process.main = Main.class.name
//                process.args = ['-i', libFile.absolutePath, '-o', destinationArchive.absolutePath, '-r', rulePath]
//
//                // Add classpaths
//                project.tasks.withType(JavaCompile) { compile ->
//
//                    def bootClasspath = compile.options.bootClasspath
//                    if (bootClasspath != null) {
//                        process.classpath += project.files(bootClasspath.split(":"))
//                    }
//                    process.classpath += project.configurations.patchCompile
//                }
//            }
//        }

//        project.tasks.withType(PatchLibProcessTask) { process ->
//            process.dependsOn += patchLibSetupTask
//        }

        project.tasks.withType(JavaCompile) { compile ->
            compile.dependsOn += project.tasks.withType(PatchLibProcessTask)
        }

        project.dependencies.add('compile', project.fileTree(dir: 'build/patchLib/jar', include: ['*.jar']))
//        project.repositories.add(project.repositories.flatDir('dirs': 'build/patchLib/aar'))
    }

    static def getTaskName(def file) {
        def suffix = file.name.replaceAll(/[^\w\d]/, '_')
        return "patchLib${suffix}"
    }

    static def getAarDependency(String name) {
        def lastDot = name.lastIndexOf('.'), lastDash = name.lastIndexOf('-')
        return name.substring(0, lastDash) + ':' + name.substring(lastDash + 1, lastDot) + '@' +
                name.substring(name.lastIndexOf('.') + 1)
    }

    static def createLibsFile(File dir, String name) {
        File libDir = new File(dir, getLibraryExtension(name))
        if (!libDir.exists()) {
            libDir.mkdirs()
        }
        return new File(libDir, name)
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
