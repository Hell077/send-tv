plugins {
    id("com.android.application") version "9.2.0" apply false
}

val ktlint by configurations.creating {
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

dependencies {
    ktlint("com.pinterest.ktlint:ktlint-cli:1.5.0")
}

tasks.register<JavaExec>("ktlintCheck") {
    description = "Check Kotlin source and Gradle script formatting."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args("**/*.kt", "**/*.kts", "!**/build/**", "!**/.gradle/**")
}

tasks.register<JavaExec>("ktlintFormat") {
    description = "Format Kotlin source and Gradle scripts."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args("-F", "**/*.kt", "**/*.kts", "!**/build/**", "!**/.gradle/**")
}
