@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.spotless)
  alias(libs.plugins.kotlin.android) apply false
  // https://youtrack.jetbrains.com/issue/KT-46200
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
}

spotless {
  kotlin {
    target("**/*.kt")
    targetExclude("**/build/**/*.kt")
    ktfmt("0.43")
  }

  format("kts") {
    target("**/*.gradle.kts")
    targetExclude("**/build/**/*.kts")
  }
}
