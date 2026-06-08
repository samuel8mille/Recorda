import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.dynamic.feature) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics.plugin) apply false
    alias(libs.plugins.owasp.dependency.check)
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.sonarqube)
}

sonar {
    properties {
        property("sonar.projectKey", "samuel8mille_Recorda")
        property("sonar.organization", "samuel8mille")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.sources", "app/src/main/java")
        property("sonar.tests", "app/src/test/java,app/src/androidTest/java")
        property("sonar.java.binaries", "app/build/tmp/kotlin-classes/debug,app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes")
        property("sonar.exclusions", "**/R.class,**/R\$*.class,**/BuildConfig.*,**/Manifest*.*,android/**/*.*")
        property("sonar.androidLint.reportPaths", "app/build/reports/lint-results-debug.xml")
        property("sonar.kotlin.detekt.reportPaths", "app/build/reports/detekt/detekt.xml")
        property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/coverage/test/debug/report.xml")
    }
}

// Valida que módulos :core não dependem de :app — evita dependências circulares
tasks.register("validateModuleGraph") {
    notCompatibleWithConfigurationCache("Reads build files of subprojects at execution time")
    doLast {
        val violations = mutableListOf<String>()
        subprojects
            .filter { it.path.startsWith(":core") }
            .forEach { project ->
                val buildFile = project.buildFile
                if (buildFile.exists()) {
                    val content = buildFile.readText()
                    if (content.contains("project(\":app\")")) {
                        violations += "${project.path} references :app — core modules must not depend on app"
                    }
                }
            }
        if (violations.isNotEmpty()) {
            throw GradleException("Module graph violations:\n${violations.joinToString("\n")}")
        }
        println("Module graph OK")
    }
}

// Aplica ktlint e detekt em todos os módulos do projeto
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.file("detekt.yml"))
    }
}

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

dependencyCheck {
    // CI passa a chave via -PnvdApiKey; localmente lê do local.properties
    nvd.apiKey = (project.findProperty("nvdApiKey") as String?)
        ?: localProps.getProperty("nvd.apiKey", "").trim('"')
    formats = listOf("HTML", "SARIF")
    suppressionFile = "owasp-suppressions.xml"
    scanConfigurations = listOf("releaseRuntimeClasspath", "debugRuntimeClasspath")
    analyzers.ossIndex.enabled = false
}
