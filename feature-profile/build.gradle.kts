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
            baseName = "feature-profile"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                implementation(compose.material)

                implementation(libs.voyager.navigator)
                implementation(libs.voyager.tab)
                implementation(libs.voyager.bottomsheet)
                implementation(libs.ktor.cio)

                implementation(projects.resources)
                implementation(projects.coreArchitecture)
                implementation(projects.coreAppearance)
                implementation(projects.coreUtils)
                implementation(projects.coreMd)
                implementation(projects.coreNavigation)
                implementation(projects.coreCommonui)
                implementation(projects.coreCommonui.components)
                implementation(projects.corePreferences)
                implementation(projects.coreNotifications)
                implementation(projects.corePersistence)
                implementation(projects.domainIdentity)
                implementation(projects.domainLemmy.data)
                implementation(projects.domainLemmy.repository)
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
    namespace = "com.github.diegoberaldin.raccoonforlemmy.feature.profile"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
