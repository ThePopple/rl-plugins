version = "0.0.2"

project.extra["PluginName"] = "Popple's gem buyer"
project.extra["PluginDescription"] = "What it says on the tin."
project.extra["PluginId"] = "popple-gb"

tasks {
    jar {
        manifest {
            attributes(
                mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to project.extra["PluginId"],
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
                )
            )
        }
    }
}