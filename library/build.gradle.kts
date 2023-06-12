// remove this on upgrading android gradle plugin to 8
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  id("maven-publish")
}

android {
  namespace = "com.instacart.truetime"

  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    aarMetadata { minCompileSdk = libs.versions.compileSdk.get().toInt() }
  }

  buildTypes { getByName("release") { isMinifyEnabled = false } }

  buildFeatures { buildConfig = false }

  compileOptions {
    // after agp 8.1.0-alpha09, this is no longer needed
    // https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

dependencies { api(libs.kotlinx.coroutines.core) }

afterEvaluate {
  publishing {
    publications {
      register<MavenPublication>("release") {
        groupId = "com.instacart"
        artifactId = "truetime"
        version = libs.versions.trueTime.get()
        afterEvaluate { from(components["release"]) }
      }
    }
  }
}
