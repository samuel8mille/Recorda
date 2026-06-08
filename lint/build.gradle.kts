plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    compileOnly(libs.lint.api)
    testImplementation(libs.lint.tests)
    testImplementation(libs.junit)
}
