// BUILDSCRIPT DE SAFE ARGS
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val nav_version = "2.7.7"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
// PLUGINS ADICIONALES
    // Hilt
    id("com.google.dagger.hilt.android") version "2.52" apply false
    // Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.2" apply false
}