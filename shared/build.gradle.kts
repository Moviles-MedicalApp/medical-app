import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {

    /*
     * =====================================================
     * iOS
     * =====================================================
     */
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->

        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }


    /*
     * =====================================================
     * ANDROID
     * =====================================================
     */
    android {

        namespace = "pe.com.smart.shared"

        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()

        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        compilerOptions {

            jvmTarget =
                JvmTarget.JVM_11
        }

        androidResources {
            enable = true
        }

        withHostTest {

            isIncludeAndroidResources =
                true
        }

        withDeviceTestBuilder {

            sourceSetTreeName =
                "test"

        }.configure {

            instrumentationRunner =
                "androidx.test.runner.AndroidJUnitRunner"
        }
    }


    /*
     * =====================================================
     * SOURCE SETS
     * =====================================================
     */
    sourceSets {

        /*
         * =================================================
         * ANDROID
         * =================================================
         */
        androidMain.dependencies {

            /*
             * Compose tooling
             */
            implementation(
                libs.compose.uiToolingPreview
            )

            implementation(
                libs.compose.uiTooling
            )

            /*
             * Ktor Engine Android
             */
            implementation(
                "io.ktor:ktor-client-okhttp:3.5.1"
            )
        }


        /*
         * =================================================
         * iOS
         * =================================================
         */
        iosMain.dependencies {

            /*
             * Ktor Engine iOS
             */
            implementation(
                "io.ktor:ktor-client-darwin:3.5.1"
            )
        }


        /*
         * =================================================
         * COMMON MAIN
         * =================================================
         */
        commonMain.dependencies {

            /*
             * -------------------------------------------------
             * COMPOSE MULTIPLATFORM
             * -------------------------------------------------
             */
            implementation(
                libs.compose.runtime
            )

            implementation(
                libs.compose.foundation
            )

            implementation(
                libs.compose.material3
            )

            implementation(
                libs.compose.ui
            )

            implementation(
                libs.compose.components.resources
            )

            implementation(
                libs.compose.uiToolingPreview
            )


            /*
             * -------------------------------------------------
             * LIFECYCLE / VIEWMODEL
             * -------------------------------------------------
             */
            implementation(
                libs.androidx.lifecycle.viewmodelCompose
            )

            implementation(
                libs.androidx.lifecycle.runtimeCompose
            )


            /*
             * -------------------------------------------------
             * NAVIGATION
             * -------------------------------------------------
             */
            implementation(
                "org.jetbrains.androidx.navigation:navigation-compose:2.9.2"
            )


            /*
             * -------------------------------------------------
             * MATERIAL ICONS
             * -------------------------------------------------
             */
            implementation(
                "org.jetbrains.compose.material:material-icons-extended:1.7.3"
            )


            /*
             * -------------------------------------------------
             * KTOR
             * -------------------------------------------------
             */
            implementation(
                "io.ktor:ktor-client-core:3.5.1"
            )

            implementation(
                "io.ktor:ktor-client-content-negotiation:3.5.1"
            )

            implementation(
                "io.ktor:ktor-serialization-kotlinx-json:3.5.1"
            )

            implementation(
                "io.ktor:ktor-client-logging:3.5.1"
            )


            /*
             * -------------------------------------------------
             * LOCAL STORAGE
             * -------------------------------------------------
             */
            implementation(
                "com.russhwolf:multiplatform-settings-no-arg:1.3.0"
            )
        }


        /*
         * =================================================
         * TEST
         * =================================================
         */
        commonTest.dependencies {

            implementation(
                libs.kotlin.test
            )
        }
    }
}


/*
 * =====================================================
 * ANDROID TOOLING
 * =====================================================
 */
dependencies {

    androidRuntimeClasspath(
        libs.compose.uiTooling
    )
}