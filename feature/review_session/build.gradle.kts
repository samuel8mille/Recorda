// Sem plugin Hilt/KSP de propósito: a versão do plugin Hilt não reconhece a
// variant API de com.android.dynamic-feature do AGP atual. Para módulos
// dynamic-feature, mantemos DI/ViewModels em :app e este módulo expõe apenas
// Composables sem estado — um padrão comum e mais simples para feature delivery.
plugins {
    alias(libs.plugins.android.dynamic.feature)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.samuelribeiro.recorda.feature.reviewsession"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":app"))
    implementation(project(":core:mvi"))
    implementation(project(":core:ui"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
}
