# Add project specific ProGuard rules here.
-dontwarn android.webkit.**
-keep class android.webkit.** { *; }
-keep class com.shadowxfb.** { *; }
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider