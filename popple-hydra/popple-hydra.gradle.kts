version = "0.0.1"

project.extra["PluginName"] = "Popple hydra"
project.extra["PluginDescription"] = ""

plugins{
    kotlin("kapt")
}

dependencies {
    kapt(Libraries.pf4j)
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}
