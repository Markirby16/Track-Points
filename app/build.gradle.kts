plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.diffplug.spotless") version "8.0.0"
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint("1.3.1").editorConfigOverride(
            mapOf(
                "ktlint_standard_function-naming" to "disabled", // allow Composables starting with uppercase
                "ktlint_standard_no-wildcard-imports" to "disabled", // allow wildcard imports
                "indent_size" to 4,
                "max_line_length" to 120,
            ),
        )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
    }
}

android {
    namespace = "com.example.trackpoints"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.trackpoints"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.windowsizeclass)
    implementation(libs.androidx.adaptive.layout)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.navigation3.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.geometry)

    implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.1.0"))
    implementation(libs.koin.core)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.compose.navigation)

    implementation(libs.face.detection)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("io.coil-kt.coil3:coil-compose:3.3.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0")

    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.6"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt:3.2.6")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.2.6")
    implementation("io.ktor:ktor-client-okhttp:3.3.2")
    implementation("io.ktor:ktor-client-android:3.3.2")
    implementation("com.github.yalantis:ucrop:2.2.11")
    implementation("androidx.transition:transition-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.7.1")

    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
}