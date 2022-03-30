package org.eu.exodus_privacy.exodusprivacy.manager.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.eu.exodus_privacy.exodusprivacy.manager.database.tracker.TrackerData
import java.io.ByteArrayOutputStream

class ExodusDatabaseConverters {

    @TypeConverter
    fun toStringList(string: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(string, listType)
    }

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toTrackerList(string: String): List<TrackerData> {
        val listType = object : TypeToken<List<TrackerData>>() {}.type
        return Gson().fromJson(string, listType)
    }

    @TypeConverter
    fun fromTrackerList(list: List<TrackerData>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}
