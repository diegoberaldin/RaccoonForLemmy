plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.moko.resources)
}

kotlin {
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
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenmodel)
                implementation(libs.voyager.transition)
                implementation(libs.voyager.tab)
                implementation(libs.voyager.bottomsheet)

                implementation(projects.coreUtils)
                implementation(projects.coreArchitecture)
                implementation(projects.coreAppearance)
                implementation(projects.corePreferences)
                implementation(projects.coreApi)
                implementation(projects.coreMd)
                implementation(projects.coreNavigation)
                implementation(projects.coreCommonui.lemmyui)
                implementation(projects.coreCommonui.detailopenerApi)
                implementation(projects.coreCommonui.detailopenerImpl)
                implementation(projects.coreNotifications)
                implementation(projects.corePersistence)

                implementation(projects.domainIdentity)
                implementation(projects.domainLemmy.data)
                implementation(projects.domainLemmy.repository)

                implementation(projects.unitBan)
                implementation(projects.unitChat)
                implementation(projects.unitCommunityinfo)
                implementation(projects.unitCreatecomment)
                implementation(projects.unitCreatereport)
                implementation(projects.unitCreatepost)
                implementation(projects.unitDrawer)
                implementation(projects.unitInstanceinfo)
                implementation(projects.unitRemove)
                implementation(projects.unitReportlist)
                implementation(projects.unitSaveditems)
                implementation(projects.unitSelectcommunity)
                implementation(projects.unitZoomableimage)
                implementation(projects.unitPostdetail)
                implementation(projects.unitCommunitydetail)
                implementation(projects.unitUserdetail)
                implementation(projects.unitManagesubscriptions)
                implementation(projects.unitMulticommunity)

                api(projects.resources)
                api(projects.featureHome)
                api(projects.featureInbox)
                api(projects.featureSearch)
                api(projects.featureProfile)
                api(projects.featureSettings)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
        }
        val androidUnitTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "com.github.diegoberaldin.raccoonforlemmy"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
