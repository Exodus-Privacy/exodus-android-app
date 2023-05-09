package org.eu.exodus_privacy.exodusprivacy.manager.storage

import kotlinx.coroutines.flow.Flow

interface Storage<T> {
    fun insert(data: Map<String, T>): Flow<Int>

    fun get(key: String): Flow<T>

    fun getAll(): Flow<Map<String,T>>

    fun clearAll(): Flow<Int>
}