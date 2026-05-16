plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.badrqaba.kbooster.core"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation("com.squareup:kotlinpoet:1.18.1")

    compileOnly("com.google.android:android:4.1.1.4")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.github.badrqaba"
                artifactId = "Kbooster-core"
                version = project.version.toString()
            }
        }
    }
}