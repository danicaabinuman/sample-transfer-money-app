import com.android.build.gradle.internal.dsl.ProductFlavor
import java.io.FileInputStream
import java.util.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val keystorePropertiesFile = rootProject.file("keystore.properties")
val clientPropertiesFile = rootProject.file("client.properties")
val versionPropertiesFile = rootProject.file("version.properties")
val properties = Properties()
properties.load(FileInputStream(keystorePropertiesFile))
properties.load(FileInputStream(clientPropertiesFile))
properties.load(FileInputStream(versionPropertiesFile))

plugins {
    id(BuildPlugins.androidApplication)
    id(BuildPlugins.kotlinAndroid)
    id(BuildPlugins.kotlinKapt)
    id(BuildPlugins.kotlinxSerialization)
    id(BuildPlugins.kotlinAndroidParcelize)
    id(BuildPlugins.navigationSafeArgs)
    id(BuildPlugins.googleServices)
    id(BuildPlugins.firebaseCrashlytics)
    id(BuildPlugins.ktLint)
}


android {
    compileSdkVersion(AndroidSdk.compile)
    flavorDimensions(AndroidSdk.dimension)
    buildFeatures {
        dataBinding = true
    }
    defaultConfig {
        minSdkVersion(AndroidSdk.min)
        targetSdkVersion(AndroidSdk.target)
        applicationId = AndroidSdk.applicationId
        versionName = AndroidSdk.versionName
        versionCode = AndroidSdk.versionCode
        testInstrumentationRunner = AndroidSdk.testRunner
        multiDexEnabled = true
        resConfigs("en")
    }
    sourceSets {
        getByName("main").res.srcDirs(Resources.dirs)
        getByName("test").res.srcDirs(Resources.dirsTest)
    }
    kapt {
        correctErrorTypes = true
    }
    kotlinOptions {
        val options = this
        options.jvmTarget = "1.8"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        animationsDisabled = true
        unitTests.isIncludeAndroidResources = true
    }
    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }
    signingConfigs {
        create("release") {
            storeFile = file(properties["storeFile"].toString())
            storePassword = properties["keyPassword"].toString()
            keyAlias = properties["keyAlias"].toString()
            keyPassword = properties["storePassword"].toString()
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            isDebuggable = true
        }
    }
    productFlavors {
        create(AppId.SME.value + Environment.DEV.getDisplayName()) {
            val env = Environment.DEV
            val appId = AppId.SME
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.SME.value + Environment.QAT.getDisplayName()) {
            val env = Environment.QAT
            val appId = AppId.SME
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.SME.value + Environment.QAT2.getDisplayName()) {
            val env = Environment.QAT2
            val appId = AppId.SME
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.SME.value + Environment.QAT3.getDisplayName()) {
            val env = Environment.QAT3
            val appId = AppId.SME
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.SME.value + Environment.STAGING.getDisplayName()) {
            val env = Environment.STAGING
            val appId = AppId.SME
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.SME.value + Environment.PREPRODUCTION.getDisplayName()) {
            val env = Environment.PREPRODUCTION
            val appId = AppId.SME
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.SME.value + Environment.PRODUCTION.getDisplayName()) {
            val env = Environment.PRODUCTION
            val appId = AppId.SME
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.PORTAL.value + Environment.DEV.getDisplayName()) {
            val env = Environment.DEV
            val appId = AppId.PORTAL
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.PORTAL.value + Environment.QAT.getDisplayName()) {
            val env = Environment.QAT
            val appId = AppId.PORTAL
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.PORTAL.value + Environment.QAT2.getDisplayName()) {
            val env = Environment.QAT2
            val appId = AppId.PORTAL
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.PORTAL.value + Environment.QAT3.getDisplayName()) {
            val env = Environment.QAT3
            val appId = AppId.PORTAL
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.PORTAL.value + Environment.STAGING.getDisplayName()) {
            val env = Environment.STAGING
            val appId = AppId.PORTAL
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.PORTAL.value + Environment.PREPRODUCTION.getDisplayName()) {
            val env = Environment.PREPRODUCTION
            val appId = AppId.PORTAL
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
        create(AppId.PORTAL.value + Environment.PRODUCTION.getDisplayName()) {
            val env = Environment.PRODUCTION
            val appId = AppId.PORTAL
            setupProductFlavors(this@Build_gradle, env, this, appId)
        }
    }
}

ktlint {
    android.set(true)
    outputColorName.set("RED")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(Libraries.kotlinStdLib)
    implementation(Libraries.multiDex)
    /* View */
    implementation(Libraries.appCompat)
    implementation(Libraries.activityAndroidX)
    implementation(Libraries.fragmentAndroidX)
    implementation(Libraries.navigationFragment)
    implementation(Libraries.navigationKTX)
    implementation(Libraries.navigationFeatures)
    implementation(Libraries.constraintlayout)
    implementation(Libraries.material)
    implementation(Libraries.recyclerView)
    implementation(Libraries.cardView)
    /* Core */
    implementation(Libraries.rxRelay)
    implementation(Libraries.rxBinding2)
    implementation(Libraries.rxJava)
    implementation(Libraries.rxJava2Adapter)
    implementation(Libraries.rxAndroid)
    implementation(Libraries.rxKotlin)
    implementation(Libraries.rxSharedPreference)
    implementation(Libraries.rxFingerprint)
    implementation(Libraries.rxPermission)
    implementation(Libraries.okhttp)
    implementation(Libraries.retrofit)
    implementation(Libraries.loggingInterceptor)
    implementation(Libraries.dagger)
    implementation(Libraries.daggerSupport)
    implementation(Libraries.coreKtx)
    implementation(Libraries.kotlinXCore)
    implementation(Libraries.kotlinX)
    implementation(Libraries.kotlinXConverter)
    implementation(Libraries.extensionLifeCycle)
    implementation(Libraries.glide)
    implementation(Libraries.timber)
    kapt(Libraries.glideCompiler)
    kapt(Libraries.daggerCompiler)
    kapt(Libraries.daggerProcessor)
    /* Debug */
    implementation(Libraries.stetho)
    implementation(Libraries.stethoOkhttp3)
    implementation(Libraries.fireBaseMessaging)
    implementation(Libraries.googlePlayCore)
    implementation(Libraries.fireBaseCrashlytics)
    implementation(Libraries.fireBaseAnalytics)
    /* Various */
    implementation(Libraries.circleIndicator)
    implementation(Libraries.zoomLayout)
    implementation(Libraries.spotLight)
    implementation(Libraries.securePreferences)
    implementation(Libraries.materialDialog)
    implementation(Libraries.materialDialogLifeCycle)
    implementation(Libraries.shimmer)
    implementation(Libraries.epoxy)
    implementation(Libraries.ahbottomnavigation)
    implementation(Libraries.shortcutBadger)
    implementation(Libraries.phoneNumber)
    implementation(Libraries.countryCodePicker)
    implementation(Libraries.inputMask)
    implementation(Libraries.autofitTextView)
    implementation(Libraries.materialDatePicker)
    implementation(Libraries.fab)
    implementation(Libraries.circularProgress)
    implementation(Libraries.aeroGear)
    implementation(Libraries.zxingCore)
    implementation(Libraries.cameraView)
    implementation(Libraries.compressor)
    implementation(Libraries.smartCropper)
    implementation(Libraries.stv)
    kapt(Libraries.epoxyProcessor)

    // Third party SDK
    implementation(Libraries.jumioCore)
    implementation(Libraries.jumioBam)
    implementation(Libraries.jumioNv)
    implementation(Libraries.jumioMRZ)
    implementation(Libraries.jumioNFC)
    implementation(Libraries.jumioOCR)
    implementation(Libraries.jumioIProove)
    implementation(Libraries.roomRuntime)

    implementation(Libraries.iProovSDK) {
        exclude("org.json", "json")
    }

    /* Testing */
    testImplementation(TestLibraries.junit)
    testImplementation(TestLibraries.mockK)
    testImplementation(TestLibraries.archTesting)
    testImplementation(TestLibraries.mockWebServer)

    androidTestImplementation(TestLibraries.mockKAndroid)
    androidTestImplementation(TestLibraries.runner)
    androidTestImplementation(TestLibraries.espressoCore)

    androidTestImplementation(Libraries.dagger)
    androidTestImplementation(TestLibraries.archTesting)
    kaptTest(Libraries.daggerCompiler)
    kaptAndroidTest(Libraries.daggerCompiler)
}

fun getPatchVersion(env: Environment, patchType: String): Int {
    return Integer.parseInt(properties["${env.getLowerCaseName()}Version$patchType"].toString())
}

fun setupProductFlavors(
    buildGradle: Build_gradle,
    env: Environment,
    productFlavor: ProductFlavor,
    appId: AppId
) {
    val versionMajor = buildGradle.getPatchVersion(env, VersionPatch.MAJOR.getDisplayName())
    val versionMinor = buildGradle.getPatchVersion(env, VersionPatch.MINOR.getDisplayName())
    val versionPatch = buildGradle.getPatchVersion(env, VersionPatch.PATCH.getDisplayName())
    val newVersionCode =
        getNewVersionCode(versionMajor, versionMinor, versionPatch)
    val newVersionName = "$versionMajor.$versionMinor.$versionPatch"
    productFlavor.versionName = newVersionName
    productFlavor.versionCode = newVersionCode
    productFlavor.applicationIdSuffix = getApplicationSuffixId(appId, env)
    productFlavor.versionNameSuffix = getVersionNameSuffix(env)
    productFlavor.dimension(AndroidSdk.dimension)
    var host = buildGradle.properties["host${env.getDisplayName()}"].toString()
    productFlavor.manifestPlaceholders["appTheme"] = when (appId == AppId.SME) {
        true -> "@style/AppThemeSME"
        else -> "@style/AppThemePortal"
    }
    productFlavor.resValue("string", "app_name", getAppName(appId, env))
    productFlavor.resValue(
        "string",
        "host_name",
        host.replace("https://", "")
            .replace("/", "")
    )
    productFlavor.resValue(
        "string",
        "app_version_number",
        "v${productFlavor.versionName}${productFlavor.versionNameSuffix}"
    )
    productFlavor.resValue(
        "string",
        "app_version",
        "${productFlavor.versionName}${productFlavor.versionNameSuffix}"
    )
    productFlavor.buildConfigField(
        "String",
        "HOST",
        host
    )
    productFlavor.buildConfigField(
        "String",
        "CLIENT_ID",
        buildGradle.properties["clientId${env.getDisplayName()}"].toString()
    )
    productFlavor.buildConfigField(
        "String",
        "CLIENT_SECRET",
        buildGradle.properties["clientSecret${env.getDisplayName()}"].toString()
    )
    productFlavor.buildConfigField(
        "String",
        "CLIENT_API_VERSION",
        buildGradle.properties["clientApiVersion"].toString()
    )

    productFlavor.buildConfigField(
        "String",
        "MSME_CLIENT_API_VERSION",
        buildGradle.properties["msmeClientApiVersion"].toString()
    )

    productFlavor.buildConfigField(
        "String",
        "MSME_CLIENT_ID",
        buildGradle.properties["msmeClientId${env.getDisplayName()}"].toString()
    )

    productFlavor.buildConfigField(
        "String",
        "MSME_CLIENT_SECRET",
        buildGradle.properties["msmeClientSecret${env.getDisplayName()}"].toString()
    )
}

//val compileKotlin: KotlinCompile by tasks
//compileKotlin.kotlinOptions {
//    languageVersion = "1.4"
//}