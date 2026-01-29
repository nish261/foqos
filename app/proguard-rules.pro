# Add project specific ProGuard rules here.

# Keep Room entities
-keep class com.foqos.data.local.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }

# Keep serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep QR/Barcode scanning
-keep class com.google.android.gms.** { *; }
-keep class com.google.zxing.** { *; }
