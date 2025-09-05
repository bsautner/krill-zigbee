plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    implementation(libs.log4j)
    implementation(libs.slf4jApi)
    implementation(libs.log4jSlf4jImpl)
    implementation(libs.bundles.zigbeeBundle)
    implementation(libs.kotlinxCoroutines)
    implementation(project(":library"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "com.github.bsautner.app.AppKt"
}
