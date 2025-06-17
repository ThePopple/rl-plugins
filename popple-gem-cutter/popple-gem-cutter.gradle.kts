version = "0.0.2"

project.extra["PluginName"] = "Popple's gem cutter"
project.extra["PluginDescription"] = "What it says on the tin."

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to "popple-gem-cutter",
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}