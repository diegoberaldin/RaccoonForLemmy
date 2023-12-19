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
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
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
                implementation(libs.voyager.screenmodel)
                implementation(libs.voyager.navigator)

                implementation(projects.coreUtils)
                implementation(projects.coreAppearance)
                implementation(projects.coreArchitecture)
                implementation(projects.coreCommonui.components)
                implementation(projects.coreCommonui.lemmyui)
                implementation(projects.coreCommonui.modals)
                implementation(projects.unitZoomableimage)
                implementation(projects.unitWeb)
                implementation(projects.unitCreatereport)
                implementation(projects.unitCreatecomment)
                implementation(projects.unitCreatepost)
                implementation(projects.unitRemove)
                implementation(projects.unitBan)
                implementation(projects.unitCommunityinfo)
                implementation(projects.unitInstanceinfo)
                implementation(projects.unitCreatereport)
                implementation(projects.unitReportlist)
                implementation(projects.coreCommonui.detailopenerApi)
                implementation(projects.coreNavigation)
                implementation(projects.corePersistence)
                implementation(projects.coreNotifications)

                implementation(projects.domainIdentity)
                implementation(projects.domainLemmy.data)
                implementation(projects.domainLemmy.repository)

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
    namespace = "com.github.diegoberaldin.raccoonforlemmy.unit.multicommunity"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
