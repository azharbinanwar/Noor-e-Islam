import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Same upload key as the Quran app. keystore.properties and the .jks are gitignored.
val keystoreProperties = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    // the splash animation is drawn here rather than by the system — see NoorSplash.kt
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.kodeelite.nooreislam"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.kodeelite.nooreislam"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 6 // kept in step with the Quran app so one number covers both
        versionName = "1.0.0"
    }
    buildFeatures {
        buildConfig = true // BuildConfig.DEBUG drives the debug ribbon in MainActivity
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            // details live in the gitignored keystore.properties; falls back to unsigned if absent
            keystoreProperties.getProperty("storeFile")?.let {
                storeFile = rootProject.file(it)
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    sourceSets {
        // dev-build resources (icon, "(dev)" label) live in src/dev, not the default src/debug
        getByName("debug") { res.srcDirs("src/dev/res") }
    }
    buildTypes {
        getByName("debug") {
            // installs alongside the store build: own icon, own data, "(dev)" label
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}