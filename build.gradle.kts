// remove this on upgrading android gradle plugin to 8
@Suppress("DSL_SCOPE_VIOLATION") plugins { alias(libs.plugins.spotless) }

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
