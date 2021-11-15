object BuildPlugins {

    object Versions {
        const val buildTools = "4.1.2"
        const val navigationSafeArgs = "2.4.0-alpha02"
        const val firebaseCrashlytics = "2.4.1"
        const val googleServices = "4.3.4"
        const val kotlin = "1.4.30"
        const val kotlin_version = "1.5.21"
        const val klint = "9.2.1"
        const val buildKonfig = "0.4.1"
    }

    const val androidApplication = "com.android.application"
    const val kotlinAndroid = "kotlin-android"
    const val kotlinKapt = "kotlin-kapt"
    const val kotlinAndroidParcelize = "kotlin-parcelize"
    const val kotlinxSerialization = "kotlinx-serialization"
    const val firebaseCrashlytics = "com.google.firebase.crashlytics"
    const val navigationSafeArgs = "androidx.navigation.safeargs.kotlin"
    const val googleServices = "com.google.gms.google-services"
    const val ktLint = "org.jlleitschuh.gradle.ktlint"

    const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.buildTools}"
    const val kotlinGradlePlugin =
        "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin_version}"
    const val kotlinXSerializerPlugin =
        "org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}"
    const val navigationSafeArgsPlugin =
        "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigationSafeArgs}"
    const val firebaseCrashlyticsPlugin = "com.google.firebase:firebase-crashlytics-gradle:${Versions.firebaseCrashlytics}"
    const val googleServicesPlugin = "com.google.gms:google-services:${Versions.googleServices}"
    const val klintPlugin = "org.jlleitschuh.gradle:ktlint-gradle:${Versions.klint}"
    const val buildKonfigPlugin = "com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:${Versions.buildKonfig}"

}

object Libraries {
    private object Versions {
        const val kotlin = "1.4.30"

        const val appCompat = "1.1.0"
        const val support = "1.0.0"
        const val constraintLayout = "1.1.3"
        const val material = "1.2.1"
        const val recyclerView = "1.0.0"
        const val cardView = "1.0.0"
        const val activityAndroidX = "1.0.0"
        const val fragmentAndroidX = "1.1.0"
        const val navigation = "2.4.0-alpha02"
        const val gridLayout = "1.0.0-beta01"

        // Core
        const val okhttp = "3.9.0"
        const val retrofit2 = "2.4.0"
        const val loggingInterceptor = "3.11.0"
        const val dagger2 = "2.17"
        const val lifeCycle = "2.1.0"
        const val securePreferences = "0.1.4"
        const val rxJava2 = "2.2.5"
        const val rxKotlin = "2.3.0"
        const val rxAndroid = "2.1.0"
        const val rxSharedPreference = "2.0.0"
        const val rxFingerprint = "2.2.1"
        const val rxRelay = "2.1.0"
        const val rxBinding2 = "2.1.1"
        const val rxPermission = "0.10.2"
        const val kotlinX = "1.2.2"
        const val kotlinXConverter = "0.8.0"
        const val coreKtx = "1.6.0"
        const val kotlinReflect = "1.5.20"
        const val multiDex = "2.0.0"
        const val stetho = "1.5.0"
        const val stethoOkhttp3 = "1.5.0"
        const val biometric = "1.1.0"

        // Google
        const val firebase = "16.0.1"
        const val firebaseMessaging = "20.1.5"
        const val googlePlayCore = "1.7.1"
        const val firebaseCrashlytics = "17.3.0"
        const val firebaseAnalytics = "18.0.0"
        const val zxingCore = "3.3.0"
        const val glide = "4.11.0"

        // Misc
        const val timber = "4.7.1"
        const val circleIndicator = "1.2.2@aar"
        const val materialDialog = "3.3.0"
        const val materialDialogLifeCycle = "3.2.1"
        const val epoxy = "4.6.2"
        const val epoxyViewBinding = "2.6.0"
        const val philJayChart = "v3.1.0"

        //epoxy = "2.19.0"
        const val shimmer = "2.1.0"
        const val ahbottomnavigation = "2.2.0"
        const val shortcutBadger = "1.1.22"
        const val zoomLayout = "1.3.0"
        const val spotLight = "1.5.0"
        const val phoneNumber = "8.8.5"
        const val countryCodePicker = "2.1.8"
        const val inputMask = "3.4.4"
        const val autofitTextView = "0.2.1"
        const val materialDatePicker = "2.3.0"
        const val fab = "1.6.4"
        const val circularProgress = "1.3.0"
        const val aeroGear = "1.0.0"
        const val cameraView = "2.7.1"
        const val compressor = "2.1.1"
        const val smartCropper = "v2.1.3"
        const val paris = "1.5.0"
        const val stv = "1.0.0"

        //Lifecycle
        const val lifecycle_version = "2.4.0"

        //Coroutines
        const val coroutines_version = "1.3.9"

        //Third Party SDK
        const val jumio = "3.9.2@aar"
        const val iProov = "6.4.1"
        const val roomRuntime = "2.2.5"
    }

    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinReflect}"
    const val multiDex = "androidx.multidex:multidex:${Versions.multiDex}"

    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val activityAndroidX =
        "androidx.activity:activity-ktx:${Versions.activityAndroidX}"
    const val fragmentAndroidX =
        "androidx.fragment:fragment-ktx:${Versions.fragmentAndroidX}"
    const val navigationFragment =
        "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    const val navigationKTX = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    const val navigationFeatures =
        "androidx.navigation:navigation-dynamic-features-fragment:${Versions.navigation}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    const val support = "androidx.legacy:legacy-support-v4:${Versions.support}"
    const val cardView = "androidx.cardview:cardview:${Versions.cardView}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    const val gridLayout = "androidx.gridlayout:gridlayout:${Versions.gridLayout}"

    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit2}"
    const val rxJava = "io.reactivex.rxjava2:rxjava:${Versions.rxJava2}"
    const val rxKotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxKotlin}"
    const val rxAndroid = "io.reactivex.rxjava2:rxandroid:${Versions.rxAndroid}"
    const val kotlinXCore = "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinX}"
    const val kotlinX = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinX}"
    const val kotlinXConverter =
        "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:${Versions.kotlinXConverter}"
    const val rxJava2Adapter = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit2}"
    const val rxRelay = "com.jakewharton.rxrelay2:rxrelay:${Versions.rxRelay}"
    const val rxBinding2 = "com.jakewharton.rxbinding2:rxbinding:${Versions.rxBinding2}"
    const val rxSharedPreference =
        "com.f2prateek.rx.preferences2:rx-preferences:${Versions.rxSharedPreference}"
    const val rxFingerprint = "com.mtramin:rxfingerprint:${Versions.rxFingerprint}"
    const val rxPermission = "com.github.tbruyelle:rxpermissions:${Versions.rxPermission}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val loggingInterceptor =
        "com.squareup.okhttp3:logging-interceptor:${Versions.loggingInterceptor}"
    const val dagger = "com.google.dagger:dagger:${Versions.dagger2}"
    const val daggerSupport = "com.google.dagger:dagger-android-support:${Versions.dagger2}"
    const val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.dagger2}"
    const val daggerProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger2}"
    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
    const val stetho = "com.facebook.stetho:stetho:${Versions.stetho}"
    const val stethoOkhttp3 = "com.facebook.stetho:stetho-okhttp3:${Versions.stethoOkhttp3}"
    const val circleIndicator = "me.relex:circleindicator:${Versions.circleIndicator}"
    const val extensionLifeCycle = "androidx.lifecycle:lifecycle-extensions:${Versions.lifeCycle}"
    const val commonJava8LifeCycle = "androidx.lifecycle:lifecycle-common-java8:${Versions.lifeCycle}"
    const val securePreferences =
        "com.scottyab:secure-preferences-lib:${Versions.securePreferences}"
    const val materialDialog = "com.afollestad.material-dialogs:core:${Versions.materialDialog}"
    const val materialDialogLifeCycle = "com.afollestad.material-dialogs:lifecycle:${Versions.materialDialogLifeCycle}"
    const val epoxy = "com.airbnb.android:epoxy:${Versions.epoxy}"
    const val epoxyProcessor = "com.airbnb.android:epoxy-processor:${Versions.epoxy}"
    const val epoxyViewBinding = "com.airbnb.android:epoxy-databinding:${Versions.epoxyViewBinding}"
    const val ahbottomnavigation =
        "com.aurelhubert:ahbottomnavigation:${Versions.ahbottomnavigation}"
    const val shimmer = "io.supercharge:shimmerlayout:${Versions.shimmer}"
    const val shortcutBadger = "me.leolin:ShortcutBadger:${Versions.shortcutBadger}"
    const val zoomLayout = "com.otaliastudios:zoomlayout:${Versions.zoomLayout}"
    const val spotLight = "com.github.takusemba:spotlight:${Versions.spotLight}"
    const val phoneNumber = "io.michaelrocks:libphonenumber-android:${Versions.phoneNumber}"
    const val inputMask = "com.redmadrobot:inputmask:${Versions.inputMask}"
    const val countryCodePicker =
        "com.github.joielechong:countrycodepicker:${Versions.countryCodePicker}"
    const val autofitTextView = "me.grantland:autofittextview:${Versions.autofitTextView}"
    const val materialDatePicker =
        "com.wdullaer:materialdatetimepicker:${Versions.materialDatePicker}"
    const val fab = "com.github.clans:fab:${Versions.fab}"
    const val circularProgress =
        "com.github.antonKozyriatskyi:CircularProgressIndicator:${Versions.circularProgress}"
    const val aeroGear = "org.jboss.aerogear:aerogear-otp-java:${Versions.aeroGear}"
    const val zxingCore = "com.google.zxing:core:${Versions.zxingCore}"
    const val cameraView = "com.otaliastudios:cameraview:${Versions.cameraView}"
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glideCompiler = "com.github.bumptech.glide:compiler:${Versions.glide}"
    const val compressor = "id.zelory:compressor:${Versions.compressor}"
    const val smartCropper = "com.github.pqpo:SmartCropper:${Versions.smartCropper}"
    const val paris = "com.airbnb.android:paris:${Versions.paris}"
    const val stv = "com.milaptank:stv:${Versions.stv}"
    const val playServicesAuth = "com.google.android.gms:play-services-auth:17.0.0"
    const val playServicesAuthPhone = "com.google.android.gms:play-services-auth-api-phone:17.4.0"
    const val biometric = "androidx.biometric:biometric:${Versions.biometric}"

    //Coroutines
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines_version}"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines_version}"

    //Lifecycle
    const val lifecycleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle_version}"
    const val lifecycleViewModelLiveData = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle_version}"
    const val lifecycleViewModelRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle_version}"
    const val lifecycleViewModelSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:${Versions.lifecycle_version}"

    //Lifecycle AnnotationProcessorWithJava8
    /*const val lifecycleKapt = "androidx.lifecycle-compiler:${Versions.lifecycle_version}"
    const val lifecycleCommonJava8 = "androidx.lifecycle-common-java8:${Versions.lifecycle_version}"*/

    // Third party library
    const val philJayChart = "com.github.PhilJay:MPAndroidChart:${Versions.philJayChart}"

    // Jumio
    const val jumioCore = "com.jumio.android:core:${Versions.jumio}"
    const val jumioBam = "com.jumio.android:bam:${Versions.jumio}"
    const val jumioNv = "com.jumio.android:nv:${Versions.jumio}"
    const val jumioMRZ = "com.jumio.android:nv-mrz:${Versions.jumio}"
    const val jumioNFC = "com.jumio.android:nv-nfc:${Versions.jumio}"
    const val jumioOCR = "com.jumio.android:nv-ocr:${Versions.jumio}"
    const val jumioIProove = "com.jumio.android:iproov:${Versions.jumio}"
    const val roomRuntime = "androidx.room:room-runtime:${Versions.roomRuntime}"

    const val iProovSDK = "com.iproov.sdk:iproov:${Versions.iProov}"

    //Firebase
    const val fireBaseCore = "com.google.firebase:firebase-core:${Versions.firebase}"
    const val fireBaseMessaging =
        "com.google.firebase:firebase-messaging:${Versions.firebaseMessaging}"
    const val googlePlayCore = "com.google.android.play:core:${Versions.googlePlayCore}"
    const val fireBaseCrashlytics =
        "com.google.firebase:firebase-crashlytics:${Versions.firebaseCrashlytics}"
    const val fireBaseAnalytics =
        "com.google.firebase:firebase-analytics:${Versions.firebaseAnalytics}"

}

object TestLibraries {
    private object Versions {
        const val leakcanaryAndroid = "1.5.4"
        const val runner = "1.2.0"
        const val testRules = "1.1.0"
        const val espressoCore = "3.2.0"
        const val junit = "4.12"
        const val testRunner = "1.0.1"
        const val archTesting = "2.1.0"
        const val mockK = "1.9.3"
        const val mockWebServer = "4.8.0"
    }

    const val junit = "junit:junit:${Versions.junit}"
    const val mockK = "io.mockk:mockk:${Versions.mockK}"
    const val mockKAndroid = "io.mockk:mockk-android:${Versions.mockK}"
    const val mockWebServer = "com.squareup.okhttp3:mockwebserver:${Versions.mockWebServer}"
    const val archTesting = "androidx.arch.core:core-testing:${Versions.archTesting}"
    const val runner = "androidx.test:runner:${Versions.runner}"
    const val testRunner = "com.android.support.test:runner:${Versions.testRunner}"
    const val testRules = "androidx.test:rules:${Versions.testRules}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"
    const val leakcanaryAndroid =
        "com.squareup.leakcanary:leakcanary-android:${Versions.leakcanaryAndroid}"
}
