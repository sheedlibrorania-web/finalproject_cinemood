import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val movieProperties = Properties().apply {
    val file = rootProject.file("tmdb.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val movieToken = (System.getenv("TMDB_READ_ACCESS_TOKEN")
    ?: movieProperties.getProperty("TMDB_READ_ACCESS_TOKEN", "")).trim().removePrefix("Bearer ")
val quotedMovieToken = "\"" + movieToken.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.example.cinemood"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.cinemood"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "TMDB_READ_ACCESS_TOKEN", quotedMovieToken)
    }
    buildFeatures { buildConfig = true }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.configureEach {
    if (name == "packageRelease" || name == "bundleRelease") {
        val tokenConfigured = movieToken.isNotBlank()
        doFirst {
            check(tokenConfigured) {
                "Configure TMDB_READ_ACCESS_TOKEN in ignored tmdb.properties or the build environment before packaging a release."
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
