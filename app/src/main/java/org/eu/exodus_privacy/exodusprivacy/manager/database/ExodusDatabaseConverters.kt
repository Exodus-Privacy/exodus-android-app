package org.eu.exodus_privacy.exodusprivacy.manager.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.eu.exodus_privacy.exodusprivacy.objects.Permission
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
    fun toIntList(string: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(string, listType)
    }

    @TypeConverter
    fun fromIntList(list: List<Int>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toPermissionList(string: String): List<Permission> {
        val listType = object : TypeToken<List<Permission>>() {}.type
        return Gson().fromJson(string, listType)
    }

    @TypeConverter
    fun fromPermissionList(list: List<Permission>): String {
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
