import ProjectVersions.unethicaliteVersion
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.Date

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


    }
}

tasks {
    register<DefaultTask>("release") {
        dependsOn(subprojects.map { it.tasks.named("build") })

        doLast {
            // Create the release directory
            val releaseDir = rootProject.projectDir.resolve("release")
            println("Release directory: ${releaseDir.absolutePath}")

            if (releaseDir.exists()) {
                releaseDir.listFiles()?.forEach { it.delete() }
                println("Cleared existing release directory")
            } else {
                releaseDir.mkdirs()
                println("Created new release directory")
            }

            // Move the JARs
            subprojects.forEach { project ->
                println("-".repeat(20))
                println("Processing project: ${project.name}")
                project.tasks.withType(Jar::class.java).forEach { jarTask ->
                    val jarFile = jarTask.archiveFile.get().asFile
                    println("Found jar: ${jarFile.absolutePath} (exists: ${jarFile.exists()})")
                    if (jarFile.exists()) {
                        val targetFile = releaseDir.resolve(jarFile.name)
                        println("Copying to: ${targetFile.absolutePath}")
                        Files.copy(jarFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }

            // Create plugins.json
            val pluginsFile = rootProject.projectDir.resolve("plugins.json")
            val plugins = mutableListOf<Map<String, Any>>()

            subprojects.forEach { project ->
                if (project.properties.containsKey("PluginName") && project.properties.containsKey("PluginDescription")) {
                    val plugin: Map<String, Any> = mapOf(
                        "name" to (project.extra["PluginName"] as String),
                        "id" to project.name.lowercase(),
                        "description" to (project.extra["PluginDescription"] as String),
                        "provider" to (project.extra["PluginProvider"] as String),
                        "projectUrl" to "",
                        "releases" to listOf(
                            mapOf(
                                "version" to project.version.toString(),
                                "url" to "https://github.com/ThePopple/rl-plugins/blob/master/release/${project.name}-${project.version}.jar?raw=true",
                                "date" to SimpleDateFormat("dd-MM-yyyy").format(Date())
                            )
                        )
                    )
                    plugins.add(plugin)
                }
            }

            pluginsFile.writeText(GsonBuilder().setPrettyPrinting().create().toJson(plugins))
        }
    }
}