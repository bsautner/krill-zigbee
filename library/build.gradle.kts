plugins {
    // publish & signing
    id("com.vanniktech.maven.publish") version "0.34.0"
    // your existing plugins:
    `maven-publish`
    alias(libs.plugins.shadow)
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

// Use a group that starts with your registered namespace.
// Keep it consistent across the project.
group = "io.github.bsautner"              // or "io.github.bsautner.krill"
version = "0.0.1"                         // remove -SNAPSHOT for releases




mavenPublishing {
    // Publish & auto-release via Central Portal
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    // Library coordinates
    coordinates(
        group.toString(),
        artifactId = "krill-zigbee",
        version = version.toString()
    )

    // POM metadata
    pom {
        name.set("krill-zigbee")
        description.set("A Ktor Plugin for Zigbee")
        url.set("https://github.com/bsautner/krill-zigbee")
        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                distribution.set("repo")
            }
        }
        scm {
            url.set("https://github.com/bsautner/krill-zigbee")
            connection.set("scm:git:git://github.com/bsautner/krill-zigbee.git")
            developerConnection.set("scm:git:ssh://git@github.com/bsautner/krill-zigbee.git")
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


dependencies {
    implementation(libs.kotlinxCoroutines)
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.zigbeeBundle)
    implementation(libs.xstream)
    testImplementation(kotlin("test"))
}
