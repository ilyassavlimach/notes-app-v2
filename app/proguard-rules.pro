# Project R8 rules (HARD-03). Libraries ship their own consumer rules; add app-specific keeps here.

# Readable crash stack traces without exposing source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Navigation 3 keys are @Serializable NavKeys saved/restored through their serializers.
-keep @kotlinx.serialization.Serializable class com.machinarium.notesv2.**.navigation.** { *; }

# Strip any stray logging from release builds (SEC-06).
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
