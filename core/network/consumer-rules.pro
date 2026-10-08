# DTOs are (de)serialized by kotlinx.serialization; keep their generated serializers.
-keep,includedescriptorclasses class com.machinarium.notesv2.core.network.model.**$$serializer { *; }
-keepclassmembers class com.machinarium.notesv2.core.network.model.** { *** Companion; }
