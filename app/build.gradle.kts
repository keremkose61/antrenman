import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pplantreman.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pplantreman.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Read signing config from local.properties
    val props = Properties()
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) props.load(propsFile.inputStream())

    signingConfigs {
        create("release") {
            val rawPath = props.getProperty("KEYSTORE_PATH")?.trim() ?: ""
            val keystorePath = rawPath.replace("C: /", "C:/")
            if (keystorePath.isNotEmpty()) {
                val kFile = file(keystorePath)
                if (kFile.exists()) {
                    storeFile = kFile
                    storePassword = props.getProperty("KEYSTORE_PASSWORD") ?: ""
                    keyAlias = props.getProperty("KEY_ALIAS") ?: ""
                    keyPassword = props.getProperty("KEY_PASSWORD") ?: ""
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null && releaseSigning.storeFile!!.exists()) {
                signingConfig = releaseSigning
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // New features dependencies
    implementation(libs.mpandroidchart)
    

    
    // WorkManager for notifications
    implementation(libs.work.runtime.ktx)
}