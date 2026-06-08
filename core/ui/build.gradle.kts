plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.samuelribeiro.recorda.core.ui"
    compileSdk = 37

    defaultConfig { minSdk = 26 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                // Repassa a flag de record para a JVM dos testes
                test.jvmArgs(
                    "-Droborazzi.test.record=${System.getProperty("roborazzi.test.record", "false")}"
                )
            }
        }
    }
}

dependencies {
    api(project(":core:mvi"))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)

    // Screenshot tests (Roborazzi + Robolectric — sem plugin Gradle, sem AGP 9.x issues)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi.core)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.androidx.compose.ui.test.junit4)

    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)
}
