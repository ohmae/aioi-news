import com.android.build.api.dsl.ManagedVirtualDevice
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.androidTest)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.baselineProfile)
    alias(libs.plugins.gradleVersions)
}

android {
    namespace = "net.mm2d.news.aioi.baseline_profile"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
        targetSdk = 36

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    targetProjectPath = ":app"
    testOptions.managedDevices.allDevices {
        @Suppress("UnstableApiUsage")
        create<ManagedVirtualDevice>("pixel9Api36") {
            device = "Pixel 9"
            apiLevel = 36
            systemImageSource = "google"
        }
    }
}

baselineProfile {
    managedDevices += "pixel9Api36"
    useConnectedDevices = false
}

dependencies {
    coreLibraryDesugaring(libs.desugarJdkLibs)

    implementation(libs.androidxJunit)
    implementation(libs.espressoCore)
    implementation(libs.uiAutomator)
    implementation(libs.benchmarkMacroJunit4)
    implementation(libs.testRule)
}

androidComponents {
    onVariants { v ->
        val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
        @Suppress("UnstableApiUsage")
        v.instrumentationRunnerArguments.put(
            "targetAppId",
            v.testedApks.map { artifactsLoader.load(it)!!.applicationId },
        )
    }
}

fun isStable(
    version: String,
): Boolean {
    val versionUpperCase = version.uppercase()
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf { !isStable(candidate.version) && isStable(currentVersion) }
}
