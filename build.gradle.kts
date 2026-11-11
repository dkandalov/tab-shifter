plugins {
    id("org.jetbrains.intellij.platform") version "2.10.4"
    kotlin("jvm") version "2.3.20"
    java
    groovy
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

sourceSets {
    main {
        java.srcDir("src")
        kotlin.srcDir("src")
        resources.srcDir("resources")
    }
}

// To find available IDE versions see https://www.jetbrains.com/intellij-repository/releases
// and https://www.jetbrains.com/intellij-repository/snapshots
val ideVersion = "2026.3"
//val ideVersion = "LATEST-EAP-SNAPSHOT"

dependencies {
    intellijPlatform {
        intellijIdea(ideVersion)
    }
}

intellijPlatform {
    projectName.set(rootProject.name)
    buildSearchableOptions.set(false) // Disable because it takes a long time and the plugin doesn't need it
    pluginConfiguration {
        ideaVersion {
            // Disable patching plugin.xml because:
            // - "since" version can be earlier than current SDK (e.g. to specify major release 252 instead of specific EAP version)
            // - "until" version is better open-ended
            sinceBuild.set(provider { null })
            untilBuild.set(provider { null })
        }
    }
}

tasks {
    publishPlugin {
        token.set(providers.environmentVariable("PUBLISH_TOKEN"))
        hidden.set(true)
    }
}
