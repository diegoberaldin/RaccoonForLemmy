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
            baseName = "core-md"
        }
    }

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.markwon.core)
                implementation(libs.markwon.strikethrough)
                implementation(libs.markwon.tables)
                implementation(libs.markwon.html)
                implementation(libs.markwon.linkify)
                implementation(libs.markwon.image.coil)
                implementation(libs.coil)
                implementation(libs.coil.gif)
                implementation(libs.android.gif.drawable)
            }
        }
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)

                api(libs.markdown)
                implementation(projects.core.commonui.components)
                implementation(projects.core.utils)
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
    namespace = "com.github.diegoberaldin.raccoonforlemmy.core.markdown"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
