package com.arcseason.app.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Only converter we need so far: List<String> for the 5-entry journals. */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(list: List<String>): String =
        json.encodeToString(ListSerializer(String.serializer()), list)

    @TypeConverter
    fun toStringList(data: String): List<String> =
        if (data.isBlank()) emptyList()
        else json.decodeFromString(ListSerializer(String.serializer()), data)
}
