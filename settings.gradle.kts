enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "tv-launcher"

include(":app")

pluginManagement {
	repositories {
		gradlePluginPortal()
		mavenCentral()
		google()
	}
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

	repositories {
		mavenCentral()
		google()
	}
}
