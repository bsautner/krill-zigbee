plugins {
    `maven-publish`
    alias(libs.plugins.shadow)
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

group = "io.github.bsautner.krill-zigbee"
version = "0.0.1-SNAPSHOT"

// Configure shadow jar
tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
}

publishing {
    repositories {
        mavenLocal()
    }

    publications {
        create<MavenPublication>("shadow") {
            artifact(tasks.shadowJar)

            artifactId = "krill-zigbee"
            groupId = "io.github.bsautner.krill"
            version = "0.0.1-SNAPSHOT"

            pom {
                name.set("krill-zigbee")
                description.set("A Ktor Plugin for Zigbee")
                url.set("https://github.com/bsautner/krill-zigbee")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/bsautner/krill-zigbee.git")
                    developerConnection.set("scm:git:ssh://github.com/bsautner/krill-zigbee.git")
                    url.set("https://github.com/bsautner/krill-zigbee")
                }
                developers {
                    developer {
                        id.set("bsautner")
                        name.set("Benjamin Sautner")
                        email.set("bsautner@gmail.com")
                    }
                }
            }
        }
    }
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