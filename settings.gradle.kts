pluginManagement {
    repositories {
        google() // ✅ bắt buộc cho plugin com.android.application
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") } // ✅ cho MPAndroidChart
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // ✅ cho MPAndroidChart
    }
}

rootProject.name = "BVBankingApp"
include(":app")
