@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.instacart.sample"

  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.instacart.truetime"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()
    versionCode = libs.versions.appVersionCode.get().toInt()
    versionName = libs.versions.appVersionName.get()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes { getByName("release") { isMinifyEnabled = false } }

  buildFeatures { compose = true }

  composeOptions { kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get() }

  compileOptions {
    // after agp 8.1.0-alpha09, this is no longer needed
    // https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    // required for Date parsing
    isCoreLibraryDesugaringEnabled = true
  }
}

dependencies {
  implementation(project(":library"))
  //  implementation(libs.truetime)
  implementation(libs.truetime.rx)
  implementation(libs.kotlin.stdlib)

  // Compose Bill Of Materials
  implementation(platform(libs.compose.bom))
  // Android Studio Preview support
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)

  implementation(libs.activity.compose)
  implementation(libs.compose.material3)
  implementation(libs.compose.ui.ui)

  ksp(libs.kotlin.inject.compiler)
  implementation(libs.kotlin.inject.runtime)

  coreLibraryDesugaring(libs.desugar.jdk.lib)
}
