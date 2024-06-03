import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "core.architecture.testutils"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting
        val androidMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines)
                implementation(projects.core.architecture)
                implementation(kotlin("test-junit"))
            }
        }
    }
}

android {
    namespace = "com.github.diegoberaldin.raccoonforlemmy.core.architecture.testutils"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
