import ProjectVersions.unethicaliteVersion
import java.nio.file.Files
import java.nio.file.StandardCopyOption

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("kapt") version "1.6.21"
}

apply<JavaLibraryPlugin>()
apply<BootstrapPlugin>()
//apply<CheckstylePlugin>()

allprojects {
    group = "com.poppleplugins"

    project.extra["PluginProvider"] = "popple-plugins"
    project.extra["PluginLicense"] = "3-Clause BSD License"

    apply<JavaPlugin>()
    apply(plugin = "java-library")
    apply(plugin = "checkstyle")
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
//        maven {
//            url = uri("https://repo.runelite.net")
//        }
    }


    dependencies {
        annotationProcessor(Libraries.lombok)
        annotationProcessor(Libraries.pf4j)

//        compileOnly(files("C:/Users/Jack/.vulcan/repository2/runelite-api-1.10.44.jar"))
//        compileOnly(files("C:/Users/Jack/.vulcan/repository2/client-1.10.44.jar"))

        compileOnly("net.unethicalite:http-api:$unethicaliteVersion+")
        compileOnly("net.unethicalite:runelite-api:$unethicaliteVersion+")
        compileOnly("net.unethicalite:runelite-client:$unethicaliteVersion+")
        compileOnly("net.unethicalite.rs:runescape-api:$unethicaliteVersion+")

        compileOnly(Libraries.okhttp3)
        compileOnly(Libraries.gson)
        compileOnly(Libraries.guice)
        compileOnly(Libraries.javax)
        compileOnly(Libraries.lombok)
        compileOnly(Libraries.pf4j)
        compileOnly(Libraries.apacheCommonsText)
//        compileOnly(files("client.jar"))
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {

        compileKotlin {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = listOf("-Xjvm-default=all-compatibility")
            }
            sourceCompatibility = "11"
        }

        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }

        register("release") {
            // Define a task dependency on the clean and build tasks of each subproject
            subprojects.forEach { project ->
                dependsOn("${project.path}:build")
//                finalizedBy("${project.path}:clean")
            }

            doLast {
                // Create the release directory in the root project directory if it doesn't exist
                val releaseDir = rootProject.projectDir.resolve("release")
                releaseDir.mkdirs()

                // Move the JARs of each child project to the release directory
                subprojects.forEach { project ->
                    project.tasks.withType(Jar::class.java).forEach { jarTask ->
                        val jarFile = jarTask.archiveFile.get().asFile
                        if (jarFile.exists()) {
                            val targetFile = releaseDir.resolve(jarFile.name)
                            Files.move(jarFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
            }
        }
    }
}
