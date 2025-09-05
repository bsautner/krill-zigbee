plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(libs.kotlinxCoroutines)
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.zigbeeBundle)
    implementation(libs.xstream)
    implementation(libs.log4j)
    implementation(libs.slf4jApi)
    implementation(libs.log4jSlf4jImpl)
    testImplementation(kotlin("test"))
}