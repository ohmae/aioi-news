import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
    alias(libs.plugins.gradleVersions)
    alias(libs.plugins.dependencyGuard)

    // for release
}

val applicationName = "AIOI"
val versionMajor = 0
val versionMinor = 0
val versionPatch = 1

android {
    namespace = "net.mm2d.news.aioi"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.mm2d.news.aioi"
        minSdk = 28
        targetSdk = 35
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        base.archivesName.set("$applicationName-$versionName")
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugarJdkLibs)

    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxCoroutinesAndroid)
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxDatetime)
    implementation(libs.hiltAndroid)
    ksp(libs.hiltAndroidCompiler)

    implementation(libs.androidxCore)
    implementation(libs.androidxLifecycleRuntime)
    implementation(libs.androidxLifecycleProcess)
    implementation(libs.androidxLifecycleViewModelCompose)
    implementation(libs.androidxBrowser)
    implementation(libs.androidxRoom)
    ksp(libs.androidxRoomCompiler)

    implementation(libs.androidxActivityCompose)
    implementation(libs.androidxNavigationCompose)
    implementation(libs.androidxHiltCompose)
    implementation(platform(libs.androidxComposeBom))
    implementation(libs.androidxComposeUi)
    implementation(libs.androidxComposeUiGraphics)
    implementation(libs.androidxComposeUiToolingPreview)
    implementation(libs.androidxComposeMaterial3)
    implementation(libs.accompanistDrawablePainter)

    implementation(libs.material)
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientOkhttp)

    testImplementation(libs.junit)

    // for release
}

ksp {
    arg("room.schemaLocation", File(projectDir, "schemas").path)
}

dependencyGuard {
    configuration("releaseRuntimeClasspath")
}

fun isStable(version: String): Boolean {
    val versionUpperCase = version.uppercase()
    val hasStableKeyword = listOf("RELEASE", "FINAL", "GA").any { versionUpperCase.contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    return hasStableKeyword || regex.matches(version)
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    rejectVersionIf { !isStable(candidate.version) && isStable(currentVersion) }
}
