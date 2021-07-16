# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

### Reactive Network
-dontwarn com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
-dontwarn io.reactivex.functions.Function
-dontwarn rx.internal.util.**
-dontwarn sun.misc.Unsafe

#### RxJava, RxAndroid (https://gist.github.com/kosiara/487868792fbd3214f9c9)
#-keep class rx.schedulers.Schedulers {
#    public static <methods>;
#}
#-keep class rx.schedulers.ImmediateScheduler {
#    public <methods>;
#}
#-keep class rx.schedulers.TestScheduler {
#    public <methods>;
#}
#-keep class rx.schedulers.Schedulers {
#    public static ** test();
#}
#-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
#    long producerIndex;
#    long consumerIndex;
#}
#-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
#    long producerNode;
#    long consumerNode;
#}
#-dontwarn sun.misc.Unsafe
#
#### Stetho, Stetho Realm plugin
#-keep class com.facebook.stetho.** {
#  *;
#}
#-dontwarn com.facebook.stetho.**
#
#-keep class com.uphyca.** { *; }


### Support v7, Design
# http://stackoverflow.com/questions/29679177/cardview-shadow-not-appearing-in-lollipop-after-obfuscate-with-proguard/29698051
#-keep class android.support.v7.widget.RoundRectDrawable { *; }
#
#-keep public class android.support.v7.widget.** { *; }
#-keep public class android.support.v7.internal.widget.** { *; }
#-keep public class android.support.v7.internal.view.menu.** { *; }
#
#-keep public class * extends android.support.v4.view.ActionProvider {
#    public <init>(android.content.Context);
#}
#
#-dontwarn android.support.**
#-dontwarn android.support.design.**
#-keep class android.support.design.** { *; }
#-keep interface android.support.design.** { *; }
#-keep public class android.support.design.R$* { *; }
#
## https://github.com/Gericop/Android-Support-Preference-V7-Fix/blob/master/preference-v7/proguard-rules.pro
#-keepclassmembers class android.support.v7.preference.PreferenceGroupAdapter {
#    private ** mPreferenceLayouts;
#}
#-keepclassmembers class android.support.v7.preference.PreferenceGroupAdapter$PreferenceLayout {
#    private int resId;
#    private int widgetResId;
#}

# https://github.com/dandar3/android-support-animated-vector-drawable/blob/master/proguard-project.txt
#-keepclassmembers class android.support.graphics.drawable.VectorDrawableCompat$* {
#   void set*(***);
#   *** get*();
#}

#### Viewpager indicator
#-dontwarn com.viewpagerindicator.**

### Retrofit 2
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions


### OkHttp3
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform


#### Fabric
## In order to provide the most meaningful crash reports
#-keepattributes SourceFile,LineNumberTable
## If you're using custom Eception
#-keep public class * extends java.lang.Exception
#
#-keep class com.crashlytics.** { *; }
#-dontwarn com.crashlytics.**


#### Android Architecture Components
## Ref: https://issuetracker.google.com/issues/62113696
## LifecycleObserver's empty constructor is considered to be unused by proguard
##-keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver {
##    <init>(...);
##}
#-keep class * implements android.arch.lifecycle.LifecycleObserver {
#    <init>(...);
#}
#
## ViewModel's empty constructor is considered to be unused by proguard
#-keepclassmembers class * extends android.arch.lifecycle.ViewModel {
#    <init>(...);
#}
#
## keep Lifecycle State and Event enums values
#-keepclassmembers class android.arch.lifecycle.Lifecycle$State { *; }
#-keepclassmembers class android.arch.lifecycle.Lifecycle$Event { *; }
## keep methods annotated with @OnLifecycleEvent even if they seem to be unused
## (Mostly for LiveData.LifecycleBoundObserver.onStateChange(), but who knows)
#-keepclassmembers class * {
#    @android.arch.lifecycle.OnLifecycleEvent *;
#}

###KotlinX
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.unionbankph.corporate.**$$serializer { *; }
-keepclassmembers class com.unionbankph.corporate.** {
    *** Companion;
}
-keepclasseswithmembers class com.unionbankph.corporate.** {
    kotlinx.serialization.KSerializer serializer(...);
}

### Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule


### SmartCropper
-keep class me.pqpo.smartcropperlib.**{*;}


### Jumio
-keep class com.jumio.** { *; }
-keep class jumio.** { *; }
-keep class com.microblink.** { *; }
-keep class com.microblink.**$* { *; }
-keep class com.facetec.zoom.** { *; }

-keep class net.sf.scuba.smartcards.IsoDepCardService {*;}
-keep class org.jmrtd.** { *; }
-keep class net.sf.scuba.** {*;}
-keep class org.bouncycastle.** {*;}
-keep class org.ejbca.** {*;}

-dontwarn java.nio.**
-dontwarn org.codehaus.**
-dontwarn org.ejbca.**
-dontwarn org.bouncycastle.**
-dontwarn module-info
-dontwarn com.microblink.**
-dontwarn javax.annotation.Nullable
-dontwarn com.facetec.zoom.sdk.**
### To be test Jumio
-dontwarn net.sf.scuba.smartcards.CardService
-dontwarn net.sf.scuba.smartcards.APDUEvent
-dontwarn net.sf.scuba.smartcards.FileSystemStructured
-dontwarn net.sf.scuba.smartcards.APDUWrapper
-dontwarn net.sf.scuba.data.Country
-dontwarn net.sf.scuba.smartcards.CardServiceException
-dontwarn net.sf.scuba.smartcards.FileInfo

### Resources
-keepattributes InnerClasses
-keep class **.R
-keep class **.R$* {
    <fields>;
}

-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
 *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
 kotlinx.serialization.KSerializer serializer(...);
}