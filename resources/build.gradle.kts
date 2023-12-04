plugins {
    alias(libs.plugins.moko.resources)
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
            baseName = "resources"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
        }
        val iosMain by getting {
            dependsOn(commonMain)
        }
        val iosX64Main by getting {
            resources.srcDirs("build/generated/moko/iosX64Main/src")
        }
        val iosArm64Main by getting {
            resources.srcDirs("build/generated/moko/iosArm64Main/src")
        }
        val iosSimulatorArm64Main by getting {
            resources.srcDirs("build/generated/moko/iosSimulatorArm64Main/src")
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "com.github.diegoberaldin.raccoonforlemmy.resources" // required
    iosBaseLocalizationRegion = "en"
}

android {
    namespace = "com.github.diegoberaldin.raccoonforlemmy.resources"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    sourceSets {
        getByName("main").java.srcDirs("build/generated/moko/androidMain/src")
    }
}
