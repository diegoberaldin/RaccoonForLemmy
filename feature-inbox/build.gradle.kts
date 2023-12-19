plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "feature-inbox"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenmodel)
                implementation(libs.voyager.tab)
                implementation(libs.voyager.bottomsheet)

                implementation(projects.resources)
                implementation(projects.coreArchitecture)
                implementation(projects.coreAppearance)
                implementation(projects.coreNavigation)
                implementation(projects.coreCommonui.components)
                implementation(projects.coreCommonui.lemmyui)
                implementation(projects.coreCommonui.modals)
                implementation(projects.unitZoomableimage)
                implementation(projects.unitChat)
                implementation(projects.coreCommonui.detailopenerApi)
                implementation(projects.coreUtils)
                implementation(projects.corePreferences)
                implementation(projects.corePersistence)
                implementation(projects.coreNotifications)

                implementation(projects.domainLemmy.data)
                implementation(projects.domainLemmy.repository)
                implementation(projects.domainIdentity)

            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

android {
    namespace = "com.github.diegoberaldin.raccoonforlemmy.feature.inbox"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
