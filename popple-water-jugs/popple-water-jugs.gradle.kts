version = "0.0.1"

project.extra["PluginName"] = "Popple's jug buyer"
project.extra["PluginDescription"] = "What it says on the tin."
project.extra["PluginId"] = "popple-jug"

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                "Plugin-Id" to project.extra["PluginId"],
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}