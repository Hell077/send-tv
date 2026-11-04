plugins {
    id("com.android.application")
}

android {
    namespace = "org.sendtv.receiver"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.sendtv.receiver"
        // Initial supported floor verified on MiTV-MOOQ1 (Android 11/API 30).
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-dev"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

abstract class SenderAssets : Exec() {
    @get:OutputDirectory
    abstract val assetsDirectory: DirectoryProperty
}

val buildSender by tasks.registering(SenderAssets::class) {
    workingDir(rootProject.projectDir)
    commandLine("npm", "run", "build:sender")
    inputs.dir(rootProject.file("sender"))
    inputs.file(rootProject.file("scripts/build-sender.mjs"))
    inputs.file(rootProject.file("package-lock.json"))
    assetsDirectory.set(rootProject.layout.buildDirectory.dir("sender"))
}

androidComponents.onVariants { variant ->
    variant.sources.assets?.addGeneratedSourceDirectory(buildSender, SenderAssets::assetsDirectory)
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.google.zxing:core:3.5.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
}

tasks.register<JavaExec>("senderTestServer") {
    description = "Run the actual HTTP API on loopback with a test-only TV approval/launch adapter."
    dependsOn("compileDebugUnitTestKotlin", "buildSender")
    val tests = tasks.named<Test>("testDebugUnitTest")
    classpath = tests.get().classpath + tests.get().testClassesDirs
    mainClass.set("org.sendtv.receiver.ReceiverBrowserHarness")
    args(rootProject.file("build/sender/sender").absolutePath, rootProject.file("build/test-challenge").absolutePath)
}
