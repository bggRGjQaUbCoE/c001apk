// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("dev.rikka.tools.materialthemebuilder") version "1.4.1"
    id("com.google.devtools.ksp") version "1.9.23-1.0.19" apply false
}
buildscript {
    dependencies {
        classpath("com.github.megatronking.stringfog:gradle-plugin:5.1.0")
        classpath("com.github.megatronking.stringfog:xor:5.0.0")
    }
}