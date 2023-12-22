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
            baseName = "login"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                implementation(compose.material)

                implementation(libs.koin.core)
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenmodel)
                implementation(libs.voyager.bottomsheet)

                implementation(projects.core.architecture)
                implementation(projects.core.appearance)
                implementation(projects.core.utils)
                implementation(projects.core.navigation)
                implementation(projects.core.commonui.components)
                implementation(projects.core.commonui.lemmyui)
                implementation(projects.core.preferences)
                implementation(projects.core.notifications)
                implementation(projects.core.persistence)
                implementation(projects.domain.identity)
                implementation(projects.domain.lemmy.data)
                implementation(projects.domain.lemmy.repository)
                implementation(projects.unit.web)
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
    namespace = "com.github.diegoberaldin.raccoonforlemmy.unit.login"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
