package org.eu.exodus_privacy.exodusprivacy.manager.storage

import kotlinx.coroutines.flow.Flow

interface ExodusStorage<T> {
    suspend fun insert(data: Map<String, T>)

    suspend fun insertAppSetup(data: T)

    fun get(key: String): Flow<T>

    fun getAll(): Flow<Map<String,T>>

    fun clearAll(): Flow<Int>
}