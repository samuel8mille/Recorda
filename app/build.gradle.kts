import java.util.Properties
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics.plugin)
    alias(libs.plugins.dependency.guard)
    jacoco
}

val jacocoExcludes = listOf(
    "**/di/**",
    "**/*Module*",
    "**/*Activity*",
    "**/*Application*",
    "**/composables/**",
    "**/navigation/**",
    "**/work/**",
    "**/analytics/Firebase*",
    "**/logging/Crashlytics*",
    "**/data/source/local/**",
    "**/data/source/remote/api/**",
    "**/data/source/remote/dto/**",
    "**/data/speech/**",
    "**/ui/theme/**",
    "**/*_Impl*",
    "**/*_Factory*",
    "**/*_MembersInjector*",
    "**/*Hilt*",
    "**/presentation/utils/Compose*"
)

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    namespace = "com.samuelribeiro.recorda"
    // Dynamic features — módulos adicionados sob demanda:
    dynamicFeatures.add(":feature:review_session")
    dynamicFeatures.add(":feature:mind_map")
    dynamicFeatures.add(":feature:study")
    compileSdk = 37

    defaultConfig {
        applicationId = "com.samuelribeiro.recorda"
        minSdk = 26
        targetSdk = 37
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "1.0"

        testInstrumentationRunner = "com.samuelribeiro.recorda.HiltTestRunner"

        // Lido de local.properties (não commitado) — gere a sua em https://aistudio.google.com/apikey
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${localProps.getProperty("gemini.api.key", "")}\""
        )
        buildConfigField("String", "GEMINI_BASE_URL", "\"https://generativelanguage.googleapis.com/\"")
    }

    androidResources {
        localeFilters += listOf("en", "pt-rBR")
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (!keystorePath.isNullOrBlank()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            val releaseConfig = signingConfigs.getByName("release")
            if (releaseConfig.storeFile != null) signingConfig = releaseConfig
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(project(":core:mvi"))
    implementation(project(":core:network"))
    implementation(project(":core:ui"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
    implementation(libs.timber)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    lintChecks(project(":lint"))

    // Chamadas REST para a API do Gemini (geração de conteúdo) — mesma
    // stack de rede usada no UrlShortener, atrás de uma abstração própria
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Hilt Testing
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)

    // Compose UI Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(kotlin("test"))

    androidTestImplementation(libs.androidx.junit.ext)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.uiautomator)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.leakcanary)
}

dependencyGuard {
    configuration("releaseRuntimeClasspath")
}

tasks.register<JacocoReport>("jacocoUnitTestReport") {
    group = "verification"
    description = "Generates Jacoco coverage report with excluded infrastructure classes."
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(
            layout.buildDirectory.file("reports/jacoco/jacocoUnitTestReport/report.xml")
        )
        html.outputLocation.set(
            layout.buildDirectory.dir("reports/jacoco/jacocoUnitTestReport/html")
        )
    }

    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/classes/debug/transformDebugClassesWithAsm/dirs")) {
            exclude(jacocoExcludes)
        }
    )
    executionData.setFrom(
        layout.buildDirectory.file(
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
        )
    )
}

/**
 * Opens the Jacoco HTML coverage report in the system browser after generating it.
 *
 * Usage: ./gradlew :app:openCoverageReport
 */
tasks.register("openCoverageReport") {
    group = "verification"
    description = "Generates and opens the unit test coverage report in the default browser."
    dependsOn("jacocoUnitTestReport")
    doLast {
        val report = layout.buildDirectory.file("reports/jacoco/jacocoUnitTestReport/html/index.html").get().asFile
        if (!report.exists()) {
            println("Report not found. Run ./gradlew jacocoUnitTestReport first.")
            return@doLast
        }
        val os = System.getProperty("os.name").lowercase()
        val cmd = when {
            os.contains("mac") -> arrayOf("open", report.absolutePath)
            os.contains("linux") -> arrayOf("xdg-open", report.absolutePath)
            else -> arrayOf("cmd", "/c", "start", report.absolutePath)
        }
        Runtime.getRuntime().exec(cmd)
        println("Coverage report: ${report.absolutePath}")
    }
}

/**
 * Runs the instrumented tests on a connected device with the dynamic feature
 * modules installed.
 *
 * `connectedDebugAndroidTest` installs only the base APK + the test APK, never
 * the dynamic-feature splits — the ContentProviders declared by the feature
 * modules (ReviewSessionInitProvider, MindMapSessionInitProvider) then crash the
 * process with ClassNotFoundException before any test runs. This task automates
 * the workaround documented in the README: `adb install-multiple` with the base
 * APK + every feature split, install of the test APK and `am instrument`.
 *
 * Usage: ./gradlew :app:connectedTestWithFeatures
 */
tasks.register("connectedTestWithFeatures") {
    group = "verification"
    description = "Installs base + dynamic feature APKs and runs instrumented tests via am instrument."
    dependsOn("assembleDebug", "assembleDebugAndroidTest")

    val featurePaths = android.dynamicFeatures.toList()
    featurePaths.forEach { dependsOn("$it:assembleDebug") }

    val adbProvider = androidComponents.sdkComponents.adb
    val rootDirFile = rootDir
    val buildDir = layout.buildDirectory
    val instrumentationTarget =
        "${android.defaultConfig.applicationId}.test/${android.defaultConfig.testInstrumentationRunner}"

    doLast {
        val adb = adbProvider.get().asFile.absolutePath
        val baseApk = buildDir.file("outputs/apk/debug/app-debug.apk").get().asFile
        val testApk = buildDir.file("outputs/apk/androidTest/debug/app-debug-androidTest.apk").get().asFile
        val featureApks = featurePaths.map { path ->
            val moduleName = path.substringAfterLast(":")
            val moduleDir = path.removePrefix(":").replace(":", "/")
            rootDirFile.resolve("$moduleDir/build/outputs/apk/debug/$moduleName-debug.apk")
        }

        fun run(vararg command: String): String {
            val process = ProcessBuilder(*command).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            if (process.waitFor() != 0) {
                throw GradleException("Comando falhou: ${command.joinToString(" ")}\n$output")
            }
            return output
        }

        println("Instalando base + ${featureApks.size} módulo(s) dinâmico(s)…")
        val splitPaths = featureApks.map { it.absolutePath }.toTypedArray()
        run(adb, "install-multiple", "-r", "-t", baseApk.absolutePath, *splitPaths)
        run(adb, "install", "-r", "-t", testApk.absolutePath)

        println("Rodando testes instrumentados ($instrumentationTarget)…")
        val output = run(adb, "shell", "am", "instrument", "-w", "-r", instrumentationTarget)
        val summary = output.lineSequence().lastOrNull { it.startsWith("OK (") || it.startsWith("Tests run:") }

        if (!output.contains("OK (")) {
            throw GradleException("Testes instrumentados falharam: ${summary ?: "veja o log acima"}\n$output")
        }
        println("Testes instrumentados: $summary")
    }
}
