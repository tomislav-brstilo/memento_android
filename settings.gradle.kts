// This block defines how plugins are resolved for the build (e.g., Android, Kotlin, etc.)
pluginManagement {
    repositories {
        // Google's Maven repository (for Android Gradle Plugin, Jetpack, etc.)
        google {
            content {
                // Limit what's fetched from Google's repo to only relevant groups
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        // Maven Central for general open-source libraries
        mavenCentral()

        // Gradle Plugin Portal for plugin resolution (e.g., Kotlin, KSP, etc.)
        gradlePluginPortal()

        // Mapbox private Maven repository (requires access token via gradle.properties or environment)
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
    }
}

// This block defines how your project resolves dependencies
dependencyResolutionManagement {
    // Fail if any subproject declares its own repositories (enforces consistency)
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // Primary repositories for dependencies
        google()
        mavenCentral()

        // Mapbox Maps SDK repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
    }
}

// Project name
rootProject.name = "Memento"

// Include the app module in the build
include(":app")
