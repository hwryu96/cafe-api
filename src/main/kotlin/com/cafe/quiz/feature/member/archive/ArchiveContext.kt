package com.cafe.quiz.feature.member.archive

class ArchiveContext {
    private val store = HashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String): T? = store[key] as? T

    fun <T : Any> set(
        key: String,
        value: T,
    ) {
        store[key] = value
    }

    fun remove(key: String) {
        store.remove(key)
    }
}
