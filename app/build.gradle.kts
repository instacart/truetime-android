// remove this on upgrading android gradle plugin to 8
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
}

android {
  compileSdk = libs.versions.compileSdk.get().toInt()

  namespace = "com.instacart.sample"

  defaultConfig {
    applicationId = "com.instacart.truetime"
    minSdk = libs.versions.minSdk.get().toInt()
    targetSdk = libs.versions.targetSdk.get().toInt()

    versionCode = libs.versions.trueTimeVersionCode.get().toInt()
    versionName = libs.versions.trueTime.get()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes { getByName("release") { isMinifyEnabled = false } }

  buildFeatures { viewBinding = true }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlinOptions { jvmTarget = JavaVersion.VERSION_11.toString() }
}

dependencies {
  implementation(project(":library"))
  //    implementation(libs.truetime)
  implementation(libs.truetime.rx)
  implementation(libs.kotlin.stdlib)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.rxjava.android)
}
