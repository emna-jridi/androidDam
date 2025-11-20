// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.20" apply false

    id("com.google.dagger.hilt.android") version "2.48" apply false
}
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0") // Add this line for Firebase
    }
}