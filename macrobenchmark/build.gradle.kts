plugins {
    alias(libs.plugins.android.test)
}

android {
    namespace = "com.samuelribeiro.recorda.macrobenchmark"
    compileSdk = 37

    defaultConfig {
        minSdk = 29
        targetSdk = 37
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] =
            "EMULATOR,LOW-BATTERY,UNLOCKED"
    }

    buildTypes {
        create("benchmark") {
            isDebuggable = false
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.junit.ext)
    implementation(libs.androidx.espresso.core)
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}

/**
 * Reads the JSON files produced by the last benchmark run and prints a formatted table.
 *
 * Usage:
 *   1. ./gradlew :app:installBenchmark
 *   2. ./gradlew :macrobenchmark:connectedBenchmarkAndroidTest
 *   3. ./gradlew :macrobenchmark:printBenchmarkResults
 */
@Suppress("UNCHECKED_CAST")
tasks.register("printBenchmarkResults") {
    group = "benchmark"
    description = "Prints formatted results from the last benchmark run."
    doLast {
        val outputDir = file("build/outputs/connected_android_test_additional_output")
        if (!outputDir.exists() || outputDir.walk().filter { it.extension == "json" }.none()) {
            println("\n⚠  No benchmark results found.")
            println(
                "   Run: ./gradlew :app:installBenchmark && ./gradlew :macrobenchmark:connectedBenchmarkAndroidTest\n"
            )
            return@doLast
        }

        val slurper = groovy.json.JsonSlurper()
        outputDir.walk().filter { it.extension == "json" }.forEach { file ->
            val data = slurper.parse(file) as? Map<*, *> ?: return@forEach
            val benchmarks = data["benchmarks"] as? List<Map<*, *>> ?: return@forEach

            println("\n═══════════════════════════════════════════════════")
            println("  BENCHMARK RESULTS")
            println("═══════════════════════════════════════════════════\n")

            benchmarks.forEach { benchmark ->
                val name = benchmark["name"] as? String ?: return@forEach
                println("▶ $name")
                val metrics = (benchmark["metrics"] as? Map<*, *>).orEmpty()
                val sampled = (benchmark["sampledMetrics"] as? Map<*, *>).orEmpty()
                (metrics + sampled).forEach { (metricName, values) ->
                    val v = values as? Map<*, *> ?: return@forEach
                    println(
                        "   %-34s  min=%-9s  median=%-9s  max=%s"
                            .format(metricName, v["minimum"], v["median"], v["maximum"])
                    )
                }
                println()
            }
        }
    }
}
