rootProject.name = "popple-plugins"

val ignoredFolders = arrayOf(
    ".git",
    ".gradle",
    ".idea",
    "build",
    "buildSrc",
    "config",
    "gradle",
    "release",
    "example-plugin-kt",
    "lucid-api",
    );

for (file in rootProject.projectDir.listFiles()?.filter { it.isDirectory && it.name !in ignoredFolders }!!) {
    include(file.toRelativeString(rootDir).replace("/", ":"))
}

for (project in rootProject.children) {
    project.apply {
        projectDir = file(name)
        buildFileName = "$name.gradle.kts"

        require(projectDir.isDirectory) { "Project '${project.path} must have a $projectDir directory" }
        require(buildFile.isFile) { "Project '${project.path} must have a $buildFile build script" }
    }
}