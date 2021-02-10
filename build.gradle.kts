import org.jlleitschuh.gradle.ktlint.KtlintExtension

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://kotlin.bintray.com/kotlinx")
        maven(url = "http://dl.bintray.com/piasy/maven")
        maven(url = "https://mobile-sdk.jumio.com")
        maven(url = "https://plugins.gradle.org/m2/")
    }

    dependencies {
        classpath(BuildPlugins.androidGradlePlugin)
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath(BuildPlugins.kotlinXSerializerPlugin)
        classpath(BuildPlugins.navigationSafeArgsPlugin)
        classpath(BuildPlugins.firebaseCrashlyticsPlugin)
        classpath(BuildPlugins.googleServicesPlugin)
        classpath(BuildPlugins.buildKonfigPlugin)
        classpath(BuildPlugins.klintPlugin)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://kotlin.bintray.com/kotlinx")
        maven(url = "http://dl.bintray.com/piasy/maven")
        maven(url = "https://mobile-sdk.jumio.com")
        maven(url = "https://plugins.gradle.org/m2/")
    }
    plugins.apply("org.jlleitschuh.gradle.ktlint")
    configure<KtlintExtension> {
        filter {
            exclude { it.file.path.contains("buildkonfig/") }
            exclude { element -> element.file.path.contains("generated/") }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
