plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
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
            baseName = "api"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)
                api(libs.ktorfit.lib)
                implementation(libs.ktor.serialization)
                implementation(libs.ktor.contentnegotiation)
                implementation(libs.ktor.json)
                implementation(libs.ktor.logging)
                implementation(projects.core.utils)
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
    namespace = "com.github.diegoberaldin.raccoonforlemmy.core.api"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

dependencies {
    val ktorfitVersion = libs.versions.ktorfit.lib.get()
    add(
        "kspCommonMainMetadata",
        "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion",
    )
    add(
        "kspAndroid",
        "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion",
    )
    add(
        "kspIosX64",
        "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion",
    )
    add(
        "kspIosSimulatorArm64",
        "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion",
    )
}
