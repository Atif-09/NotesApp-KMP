import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    id("app.cash.sqldelight") version "2.0.1"
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    js(IR) {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).copy()
            }
        }
        binaries.executable()
    }

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation("app.cash.sqldelight:android-driver:2.0.1")


        }

        iosMain.dependencies {
            implementation("app.cash.sqldelight:native-driver:2.0.1")

        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            //implementation(libs.runtime)
            //implementation(libs.kotlinx.datetime)
            implementation("app.cash.sqldelight:coroutines-extensions:2.0.1")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
        }
        jsMain {
            dependencies {
                implementation("app.cash.sqldelight:web-worker-driver:2.0.1")
                implementation(devNpm("copy-webpack-plugin", "9.1.0"))
                implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.0.1"))
                implementation(npm("sql.js", "1.8.0"))
            }
        }

    }
}

android {
    namespace = "org.notesapp.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "org.notesapp.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.notesapp.project"
            packageVersion = "1.0.0"
        }
    }
}

compose.experimental {
    web.application {}
}

sqldelight {
    databases {
        create("NotesDatabase") {
            packageName.set("org.notesapp.project")
            generateAsync.set(true)
        }
    }
}

