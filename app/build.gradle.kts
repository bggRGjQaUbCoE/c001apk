plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dev.rikka.tools.materialthemebuilder")
    id("com.google.devtools.ksp")
    id("stringfog")
}
apply(plugin = "stringfog")

configure<com.github.megatronking.stringfog.plugin.StringFogExtension> {
    // 必要：加解密库的实现类路径，需和上面配置的加解密算法库一致。
    implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
    // 可选：加密开关，默认开启。
    enable = true
    // 可选：指定需加密的代码包路径，可配置多个，未指定将默认全部加密。
    fogPackages = arrayOf("com.example.c001apk")
    //kg = com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator()
    // base64或者bytes
    mode = com.github.megatronking.stringfog.plugin.StringFogMode.bytes
}

materialThemeBuilder {
    themes {
        for ((name, color) in listOf(
            "Default" to "6750A4",
            "Red" to "F44336",
            "Pink" to "E91E63",
            "Purple" to "9C27B0",
            "DeepPurple" to "673AB7",
            "Indigo" to "3F51B5",
            "Blue" to "2196F3",
            "LightBlue" to "03A9F4",
            "Cyan" to "00BCD4",
            "Teal" to "009688",
            "Green" to "4FAF50",
            "LightGreen" to "8BC3A4",
            "Lime" to "CDDC39",
            "Yellow" to "FFEB3B",
            "Amber" to "FFC107",
            "Orange" to "FF9800",
            "DeepOrange" to "FF5722",
            "Brown" to "795548",
            "BlueGrey" to "607D8F",
            "Sakura" to "FF9CA8"
        )) {
            create("Material$name") {
                lightThemeFormat = "ThemeOverlay.Light.%s"
                darkThemeFormat = "ThemeOverlay.Dark.%s"
                primaryColor = "#$color"
            }
        }
    }
    // Add Material Design 3 color tokens (such as palettePrimary100) in generated theme
    // rikka.material >= 2.0.0 provides such attributes
    generatePalette = true
}

val gitBuildNumber: Int by lazy {
    val stdout = org.apache.commons.io.output.ByteArrayOutputStream()
    rootProject.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        standardOutput = stdout
    }
    stdout.toString().trim().toInt()
}

val gitBuildName: String by lazy {
    val stdout = org.apache.commons.io.output.ByteArrayOutputStream()
    rootProject.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
    }
    stdout.toString().trim()
}

android {
    namespace = "com.example.c001apk"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.c001apk"
        minSdk = 24
        targetSdk = 34
        versionCode = gitBuildNumber
        versionName = gitBuildName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    defaultConfig {
        ndk {
            abiFilters.add("arm64-v8a")
            abiFilters.add("armeabi-v7a")
//            abiFilters.add("armeabi")
//            abiFilters.add("x86")
            abiFilters.add("x86_64")
        }
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    applicationVariants.all {
        outputs.all {
            val versionName = defaultConfig.versionName
            val versionCode = defaultConfig.versionCode
            if (buildType.name == "release")
                (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                    "c001apk_$versionName($versionCode).apk"
        }
    }
}

configurations.configureEach {
    exclude("androidx.appcompat", "appcompat")
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.drakeet.about:about:2.5.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("dev.rikka.rikkax.material:material:2.7.0")
    implementation("dev.rikka.rikkax.material:material-preference:2.0.0")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("androidx.webkit:webkit:1.9.0")
    implementation("org.jsoup:jsoup:1.17.1")
    implementation(project(":mojito"))
    implementation(project(":SketchImageViewLoader"))
    implementation(project(":GlideImageLoader"))
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("com.github.megatronking.stringfog:xor:5.0.0")
    //implementation("androidx.palette:palette:1.0.0")
//    implementation("com.microsoft.appcenter:appcenter-analytics:5.0.4")
//    implementation("com.microsoft.appcenter:appcenter-crashes:5.0.4")
    implementation("com.github.zhaobozhen.libraries:utils:1.1.4")

    // injekt
    // implementation("com.github.inorichi.injekt:injekt-core:65b0440")
}