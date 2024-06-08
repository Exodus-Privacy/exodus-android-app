package org.eu.exodus_privacy.exodusprivacy.utils

inline fun <T> List<T>.updateAndGet(block: MutableList<T>.() -> Unit): List<T> {
    return toMutableList().apply(block)
}