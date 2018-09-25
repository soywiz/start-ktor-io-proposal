/*
 * Copyright 2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.ktor.start.project

import io.ktor.start.*
import io.ktor.start.util.*

internal object BuildFilesGradle : BuildInfoBlock() {
    override fun BlockBuilder.render(info: BuildInfo) {
        fileText("gradle.properties") {
            for ((key, value) in Properties.getProperties(info)) {
                +"$key=$value"
            }
        }
        fileText("build.gradle") {
            "buildscript" {
                "repositories" {
                    +"jcenter()"
                }
                +""
                "dependencies" {
                    +"classpath \"org.jetbrains.kotlin:kotlin-gradle-plugin:\$kotlin_version\""
                }
            }
            +""
            +"apply plugin: 'kotlin'"
            +"apply plugin: 'application'"
            +""
            +"group '${info.artifactName}'"
            +"version '${info.artifactVersion}'"

            +"mainClassName = \"${info.developmentEngineFQ}\""
            +""
            "sourceSets" {
                +"main.kotlin.srcDirs = main.java.srcDirs = ['src']"
                +"test.kotlin.srcDirs = test.java.srcDirs = ['test']"
                +"main.resources.srcDirs = ['resources']"
                +"test.resources.srcDirs = ['testresources']"
            }
            +""
            "repositories" {
                for (repo in reposToInclude) {
                    when (repo) {
                        "local" -> +"mavenLocal()"
                        "jcenter" -> +"jcenter()"
                        else -> +"maven { url '$repo' }"
                    }
                }
            }
            +""
            "dependencies" {
                linedeferred {
                    println("compileDependencies: $compileDependencies")
                    for (dep in compileDependencies) {
                        +"compile \"${dep.dependency}\""
                    }
                    for (dep in testDependencies - compileDependencies) {
                        +"testCompile \"${dep.dependency}\""
                    }
                }
            }
            +""
            +"kotlin.experimental.coroutines = 'enable'"
        }
        fileText("settings.gradle") {
            +"rootProject.name = '${info.artifactName}'"
        }
        if (info.includeWrapper) {
            fileBinary("gradlew", mode = "755".toInt(8)) { info.fetch("gradle/gradlew") }
            fileBinary("gradlew.bat") { info.fetch("gradle/gradlew.bat") }
            fileBinary("gradle/wrapper/gradle-wrapper.jar") {
                info.fetch("gradle/gradle/wrapper/gradle-wrapper.jar")
            }
            fileBinary("gradle/wrapper/gradle-wrapper.properties") {
                info.fetch("gradle/gradle/wrapper/gradle-wrapper.properties")
            }
        }
    }
}