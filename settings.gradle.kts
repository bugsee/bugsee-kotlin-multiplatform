pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "bugsee-kotlin-multiplatform"
include(":composeApp")
include(":library")
include(":library-protect")
