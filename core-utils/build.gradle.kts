plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    targetHierarchy.default()

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
            baseName = "core-utils"
        }
    }

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.activity)
                implementation(libs.ktor.android)
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(libs.koin.core)
                implementation(libs.ktor.cio)
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
    namespace = "com.github.diegoberaldin.raccoonforlemmy.core.utils"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
