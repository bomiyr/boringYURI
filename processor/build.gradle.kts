/*
 * Copyright 2020 Anton Novikau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import groovy.util.Node
import groovy.util.NodeList
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.dokka")
    id("maven-publish")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":api"))
    implementation(project(":processor-common-apt"))
    implementation(project(":processor-steps"))

    //noinspection AnnotationProcessorOnCompilePath
    compileOnly(libs.google.auto.service)
    kapt(libs.google.auto.service)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11" // in order to compile Kotlin to java 11 bytecode
    }
}

tasks.jar {
    from(
        configurations
            .runtimeClasspath
            .get()
            .filter {
                it.absolutePath.contains("boringYURI", ignoreCase = true)
                        && !it.absolutePath.contains("boringYURI/api", ignoreCase = true)
            }
            .mapNotNull {
                println("added into fat jar: $it")
                if (it.isDirectory) it else zipTree(it)
            })
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.anton-novikau"
            artifactId = "boringyuri-processor"
            version = "2.0"

            from(components["java"])

            pom.withXml {
                val root = asNode()

                val collectedDeps = collectDeps().distinctBy { "${it.group}:${it.name}" }

                (root["dependencies"] as NodeList).forEach { depsNode ->
                    require(depsNode is Node)
                    root.remove(depsNode)
                    val generatedDepsNode = Node(root, "dependencies")
                    collectedDeps.forEach {
                        Node(generatedDepsNode, "dependency").apply {
                            appendNode("groupId", it.group)
                            appendNode("artifactId", it.name)
                            appendNode("version", it.version)
                            if (it.group == "org.jetbrains.kotlin") {
                                appendNode("scope", "runtime")
                            } else {
                                appendNode("scope", "compile")
                            }
                        }
                    }
                }
            }
        }
    }
}


// Task just to test output from CLI
tasks.register("deps") {
    doLast {
        val deps = mutableSetOf<Dependency>()
        collectDependencies(project, deps)
        println(deps)
    }
}

fun collectDeps(): Set<Dependency> {
    val set = mutableSetOf<Dependency>()
    collectDependencies(project, set)
    return set
}

fun collectDependencies(project: Project, deps: MutableSet<Dependency>) {
    project.configurations.filter { it.name == "api" || it.name == "implementation" }
        .forEach { config ->
            config.dependencies.forEach {
                if (it.group == "BoringYURI") {
                    val importedProject = rootProject.findProject(it.name)
                    collectDependencies(importedProject!!, deps)
                } else if (it.group != null) {
                    deps += it
                }
            }
        }

}
