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
            baseName = "core-commonui"
        }
    }

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.coil)
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)

                implementation(libs.koin.core)
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenmodel)
                implementation(libs.voyager.bottomsheet)
                implementation(libs.voyager.tab)

                implementation(projects.coreUtils)
                implementation(projects.coreAppearance)
                implementation(projects.coreArchitecture)
                implementation(projects.corePreferences)
                implementation(projects.corePersistence)
                implementation(projects.coreMd)
                implementation(projects.coreNotifications)
                implementation(projects.coreNavigation)
                implementation(projects.coreCommonui.components)
                implementation(projects.coreCommonui.lemmyui)
                implementation(projects.coreCommonui.modals)
                implementation(projects.coreCommonui.unitBan)
                implementation(projects.coreCommonui.unitChat)
                implementation(projects.coreCommonui.unitZoomableimage)
                implementation(projects.coreCommonui.unitWeb)

                implementation(projects.domainLemmy.data)
                implementation(projects.domainLemmy.repository)
                implementation(projects.domainIdentity)

                implementation(projects.resources)
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
    namespace = "com.github.diegoberaldin.raccoonforlemmy.core.commonui"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
