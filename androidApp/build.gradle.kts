import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.gms)
}

android {
    namespace = "com.github.diegoberaldin.raccoonforlemmy.android"
    compileSdk = libs.versions.android.targetSdk.get().toInt()
    defaultConfig {
        applicationId = "com.github.diegoberaldin.raccoonforlemmy.android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 52
        versionName = "1.0.0-RC21"
        archivesName.set("RaccoonForLemmy")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            resValue("string", "app_name", "Kijetesantakalu for Lemmy")
            applicationIdSuffix = ".dev"
            extra["enableCrashlytics"] = false
        }
        getByName("release") {
            resValue("string", "app_name", "Raccoon for Lemmy")
            isMinifyEnabled = true
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.coil)
    implementation(libs.coil.gif)
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.tab)

    implementation(projects.shared)
    implementation(projects.core.utils)
    implementation(projects.core.navigation)
}
