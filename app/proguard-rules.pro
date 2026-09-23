# Proguard rules for Mobile-Use
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
