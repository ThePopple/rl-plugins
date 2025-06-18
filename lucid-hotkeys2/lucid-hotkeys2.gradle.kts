version = "1.0.8"

project.extra["PluginName"] = "Lucid Hotkeys 2"
project.extra["PluginDescription"] = "Like Lucid Hotkeys 1, but much better"

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