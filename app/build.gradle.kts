import java.util.Properties

plugins {
    // PLUGINS POR DEFECTO
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
// PLUGINS EXTERNOS
    // Kotlin Parcelize
    id("kotlin-parcelize")
    // Safe Args incluye classpath a nivel de proyecto
    id("androidx.navigation.safeargs")
    // Hilt incluye un puglin a nivel de proyecto
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    // Secrets
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    // Google Services
    id("com.google.gms.google-services")
}

// Leer el valor desde local.properties
val apiKeyValue: String by lazy {
    val localProperties = File(rootDir, "local.properties")
    if (localProperties.exists()) {
        val properties = Properties()
        properties.load(localProperties.inputStream())
        properties.getProperty("apiKey", "")
    } else {
        ""
    }
}


android {
    namespace = "com.xrandolphx.elegantcommerce"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xrandolphx.elegantcommerce"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        // Pasar la apiKey a BuildConfig
        buildConfigField("String", "API_KEY", "\"$apiKeyValue\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // CONFIGURACIONES ADICIONALES
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // DEPENDENCIAS BASE
    implementation(libs.androidx.core.ktx)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.material3.android)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.ui.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    // Firebase
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")

    // Coroutines with firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Dependency for the Google AI client SDK for Android
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Navigation/Views/Fragments integration
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // DEPENDENCIAS EXTERNAS

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Loading Button
    implementation("com.github.leandroborgesferreira:loading-button-android:2.3.0")

    // Circular Image
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Pager Dots Indicator
    implementation("com.tbuonomo:dotsindicator:5.0")

    // StepView
    implementation("com.github.shuhart:stepview:1.5.1")

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

    // lifecycle-viewmodel-compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.4.0")

}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}