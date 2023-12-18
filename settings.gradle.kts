pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Raccoon_for_Lemmy"
include(":androidApp")
include(":shared")
include(":resources")
include(":core-utils")
include(":core-appearance")
include(":core-preferences")
include(":core-architecture")
include(":core-api")
include(":core-md")
include(":core-commonui")
include(":core-commonui:components")
include(":core-commonui:lemmyui")
include(":core-commonui:modals")
include(":core-notifications")
include(":core-persistence")
include(":core-navigation")
include(":domain-lemmy")
include(":domain-lemmy:repository")
include(":domain-lemmy:data")
include(":domain-identity")
include(":feature-home")
include(":feature-inbox")
include(":feature-search")
include(":feature-profile")
include(":feature-settings")
include(":core-commonui:unit-ban")
