# MoneyBol ProGuard Rules

# Keep Hilt-generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Room entities
-keep class com.moneybol.app.database.** { *; }

# Keep payment models (used in reflection-free serialization but good to preserve)
-keep class com.moneybol.app.core.model.** { *; }

# Keep provider parsers and registry
-keep class com.moneybol.app.providers.** { *; }

# Keep settings and preferences
-keep class com.moneybol.app.settings.** { *; }

# Keep NotificationListenerService
-keep class com.moneybol.app.notification.MoneyBolNotificationService { *; }

# Standard Android optimizations
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
