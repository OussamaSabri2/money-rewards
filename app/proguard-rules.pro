-dontusemixedcaseclassnames
-verbose
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizations !method/inlining/*
-optimizationpasses 5
-allowaccessmodification
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** i(...);
    public static *** v(...);
}
-keepnames class **.sdkoffers.*
-keep class **.GlobalAds{
   public static void hLoad(...);
}

-keep class com.tapjoy.** { *; }
-keep class com.moat.** { *; }
-keepattributes JavascriptInterface
-keepattributes *Annotation*
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
@com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}
-keep class com.google.android.gms.ads.identifier.** { *; }
-dontwarn com.tapjoy.**


-keep public class com.ayetstudios.publishersdk.AyetSdk
-keepclassmembers class com.ayetstudios.publishersdk.AyetSdk {
   public *;
}
-keep public interface com.ayetstudios.publishersdk.interfaces.UserBalanceCallback { *; }
-keep public interface com.ayetstudios.publishersdk.interfaces.DeductUserBalanceCallback { *; }

-keep class com.ayetstudios.publishersdk.models.VastTagReqData { *; }

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep class com.ironsource.adapters.** { *;
}
-dontwarn com.ironsource.mediationsdk.**
-dontwarn com.ironsource.adapters.**
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.chartboost.** { *; }


-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep class com.ironsource.adapters.** { *;
}
-dontwarn com.ironsource.mediationsdk.**
-dontwarn com.ironsource.adapters.**
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.ayetstudios.publishersdk.messages.** {*;}
-keep public class com.ayetstudios.publishersdk.AyetSdk
-keepclassmembers class com.ayetstudios.publishersdk.AyetSdk {
   public *;
}
-keep public interface com.ayetstudios.publishersdk.interfaces.UserBalanceCallback { *; }
-keep public interface com.ayetstudios.publishersdk.interfaces.DeductUserBalanceCallback { *; }

-keep class com.ayetstudios.publishersdk.models.VastTagReqData { *; }

#since API 34 start
-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe
#since API 34 end