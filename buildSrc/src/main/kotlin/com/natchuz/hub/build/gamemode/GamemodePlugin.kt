package com.natchuz.hub.build.gamemode

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.JavaExec
import java.io.File

const val LOCAL_SERVER_CONFIGURATION = "localServer"

open class GamemodePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("gamemode", GamemodeExtension::class.java, project)

        project.tasks.register("prepareEnvironment", Copy::class.java) { task ->
            task.from(extension.baseProject.configurations.getByName(LOCAL_SERVER_CONFIGURATION))
            task.from(extension.testMapFile) { scope ->
                scope.into("maps")
            }
            task.from(project.configurations.getByName("shadow").artifacts.files) { scope ->
                scope.into("mods")
            }
            task.into(extension.workDirectory)
            task.dependsOn("build")
            task.group = Companion.TASKS_GROUP
        }

        project.tasks.register("cleanRuntime", Delete::class.java) { task ->
            task.delete(project.files(extension.workDirectory))
            task.group = Companion.TASKS_GROUP
        }

        project.tasks.register("run", JavaExec::class.java) { task ->
            task.dependsOn("prepareEnvironment")
            task.standardInput = System.`in`
            task.workingDir = File(extension.workDirectory)
            task.classpath = project.files("${extension.workDirectory}/sponge.jar")
            task.systemProperties["server.context"] = "standalone"
            task.environment["SERVERID"] = "testing-1"
            task.environment["SERVERTYPE"] = "testing"
            task.group = Companion.TASKS_GROUP
        }

        project.dependencies.add("implementation", extension.baseProject)
    }

    companion object {
        const val TASKS_GROUP = "gamemode"
    }
}