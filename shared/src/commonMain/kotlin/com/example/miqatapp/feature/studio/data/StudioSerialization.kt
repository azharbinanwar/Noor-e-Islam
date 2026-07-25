package com.example.miqatapp.feature.studio.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

// Compose Color <-> ARGB int (library type, so a contextual serializer instead of an annotation).
private object ColorArgbSerializer : KSerializer<Color> {
    override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: Color) = encoder.encodeInt(value.toArgb())
    override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeInt())
}

// TextAlign <-> name (studio only uses Start/Center/End).
private object TextAlignSerializer : KSerializer<TextAlign> {
    override val descriptor = PrimitiveSerialDescriptor("TextAlign", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: TextAlign) = encoder.encodeString(
        when (value) {
            TextAlign.Start -> "start"
            TextAlign.End -> "end"
            TextAlign.Justify -> "justify"
            else -> "center"
        }
    )
    override fun deserialize(decoder: Decoder): TextAlign = when (decoder.decodeString()) {
        "start" -> TextAlign.Start
        "end" -> TextAlign.End
        "justify" -> TextAlign.Justify
        else -> TextAlign.Center
    }
}

private val studioJson = Json {
    serializersModule = SerializersModule {
        contextual(Color::class, ColorArgbSerializer)
        contextual(TextAlign::class, TextAlignSerializer)
    }
    ignoreUnknownKeys = true   // tolerate config fields added/removed across versions
    encodeDefaults = true
}

// Flutter-style: the model serializes itself.
fun StudioConfig.toJson(): String = studioJson.encodeToString(StudioConfig.serializer(), this)
fun StudioConfig.Companion.fromJson(json: String): StudioConfig = studioJson.decodeFromString(StudioConfig.serializer(), json)
