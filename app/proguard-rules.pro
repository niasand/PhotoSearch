# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Room entities
-keep class com.photosearch.app.data.local.** { *; }

# Keep model classes
-keep class com.photosearch.app.data.model.** { *; }

# ONNX Runtime
-keep class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**

# Coil
-keep class coil.** { *; }

# Hilt
-keep class * extends dagger.hilt.** { *; }
