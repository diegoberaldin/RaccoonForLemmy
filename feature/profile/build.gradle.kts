import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
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
            baseName = "feature.profile"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.material)

                implementation(libs.koin.core)
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenmodel)
                implementation(libs.voyager.koin)
                implementation(libs.voyager.tab)
                implementation(libs.voyager.bottomsheet)
                implementation(libs.ktor.cio)

                implementation(projects.core.appearance)
                implementation(projects.core.architecture)
                implementation(projects.core.commonui.components)
                implementation(projects.core.commonui.detailopenerApi)
                implementation(projects.core.commonui.lemmyui)
                implementation(projects.core.commonui.modals)
                implementation(projects.core.l10n)
                implementation(projects.core.navigation)
                implementation(projects.core.notifications)
                implementation(projects.core.persistence)
                implementation(projects.core.preferences)
                implementation(projects.core.utils)

                implementation(projects.domain.identity)
                implementation(projects.domain.lemmy.data)
                implementation(projects.domain.lemmy.repository)

                implementation(projects.unit.createcomment)
                implementation(projects.unit.createpost)
                implementation(projects.unit.drafts)
                implementation(projects.unit.filteredcontents)
                implementation(projects.unit.login)
                implementation(projects.unit.manageaccounts)
                implementation(projects.unit.managesubscriptions)
                implementation(projects.unit.modlog)
                implementation(projects.unit.myaccount)
                implementation(projects.unit.reportlist)
                implementation(projects.unit.web)
                implementation(projects.unit.zoomableimage)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.test)
                implementation(kotlin("test-junit"))
                implementation(libs.mockk)
                implementation(libs.turbine)
                implementation(projects.core.testutils)
                implementation(projects.core.architecture.testutils)
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
