plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}
// iOS reads its version from Config.xcconfig; keep those two lines in step with version.properties
// so one bump covers Android and iOS alike. Runs before any Android build, and by hand: ./gradlew syncIosVersion
tasks.register("syncIosVersion") {
    val props = file("version.properties")
    val xcconfig = file("iosApp/Configuration/Config.xcconfig")
    inputs.file(props)
    outputs.file(xcconfig)
    doLast {
        val v = java.util.Properties().apply { props.inputStream().use(::load) }
        xcconfig.writeText(
            xcconfig.readText()
                .replace(Regex("CURRENT_PROJECT_VERSION=.*"), "CURRENT_PROJECT_VERSION=${v["versionCode"]}")
                .replace(Regex("MARKETING_VERSION=.*"), "MARKETING_VERSION=${v["versionName"]}"),
        )
    }
}
subprojects {
    tasks.matching { it.name == "preBuild" }.configureEach { dependsOn(rootProject.tasks.named("syncIosVersion")) }
}
