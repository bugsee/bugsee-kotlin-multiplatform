pluginManagement {
    repositories {
//        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
//        mavenLocal()
        google()
        mavenCentral()
    }
}

rootProject.name = "bugsee-kotlin-multiplatform"
include(":composeApp")
include(":library")
include(":library-protect")
