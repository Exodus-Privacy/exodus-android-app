package org.eu.exodus_privacy.exodusprivacy.manager.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ExodusDatabaseConverters {

    @TypeConverter
    fun toList(string: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(string, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
    }
}
