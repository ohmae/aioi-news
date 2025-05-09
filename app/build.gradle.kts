import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
    alias(libs.plugins.gradleVersions)
    alias(libs.plugins.dependencyGuard)

    alias(libs.plugins.baselineProfile)

    // for release
}

val applicationName = "AIOI-News"
val versionMajor = 0
val versionMinor = 0
val versionPatch = 5

android {
    namespace = "net.mm2d.news.aioi"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.mm2d.news.aioi"
        minSdk = 28
        targetSdk = 36
        versionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
        versionName = "$versionMajor.$versionMinor.$versionPatch"
        base.archivesName.set("$applicationName-$versionName")
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
    }
    room {
        schemaDirectory("$projectDir/schemas")
        generateKotlin = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugarJdkLibs)

    implementation(libs.kotlinxCoroutinesCore)
    implementation(libs.kotlinxCoroutinesAndroid)
    implementation(libs.kotlinxSerializationCore)
    implementation(libs.kotlinxSerializationJson)
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
    implementation(libs.okhttp)
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientOkhttp)

    debugImplementation(libs.okhttpLoggingInterceptor)
    debugImplementation(libs.leakcanary)
    debugImplementation(libs.bundles.flipper)

    testImplementation(libs.junit)

    implementation(libs.profileInstaller)
    baselineProfile(projects.baselineProfile)

    // for release
}

dependencyGuard {
    configuration("releaseRuntimeClasspath")
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
