plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.moko.resources)
}

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
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                implementation(libs.koin.core)
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenmodel)
                implementation(libs.voyager.koin)
                implementation(libs.voyager.transition)
                implementation(libs.voyager.tab)
                implementation(libs.voyager.bottomsheet)

                implementation(projects.core.utils)
                implementation(projects.core.architecture)
                implementation(projects.core.appearance)
                implementation(projects.core.preferences)
                implementation(projects.core.api)
                implementation(projects.core.md)
                implementation(projects.core.navigation)
                implementation(projects.core.commonui.lemmyui)
                implementation(projects.core.commonui.detailopenerApi)
                implementation(projects.core.commonui.detailopenerImpl)
                implementation(projects.core.notifications)
                implementation(projects.core.persistence)

                implementation(projects.domain.identity)
                implementation(projects.domain.inbox)
                implementation(projects.domain.lemmy.data)
                implementation(projects.domain.lemmy.repository)

                implementation(projects.unit.ban)
                implementation(projects.unit.chat)
                implementation(projects.unit.communityinfo)
                implementation(projects.unit.createcomment)
                implementation(projects.unit.createreport)
                implementation(projects.unit.createpost)
                implementation(projects.unit.drawer)
                implementation(projects.unit.instanceinfo)
                implementation(projects.unit.remove)
                implementation(projects.unit.reportlist)
                implementation(projects.unit.saveditems)
                implementation(projects.unit.selectcommunity)
                implementation(projects.unit.zoomableimage)
                implementation(projects.unit.postdetail)
                implementation(projects.unit.communitydetail)
                implementation(projects.unit.userdetail)
                implementation(projects.unit.userinfo)
                implementation(projects.unit.managesubscriptions)
                implementation(projects.unit.multicommunity)
                implementation(projects.unit.modlog)
                implementation(projects.unit.accountsettings)
                implementation(projects.unit.manageban)
                implementation(projects.unit.selectinstance)

                api(projects.resources)
                api(projects.feature.home)
                api(projects.feature.inbox)
                api(projects.feature.search)
                api(projects.feature.profile)
                api(projects.feature.settings)
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
        val iosMain by getting {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by getting {
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
