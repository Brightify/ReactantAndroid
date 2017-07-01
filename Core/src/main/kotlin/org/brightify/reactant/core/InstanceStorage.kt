package org.brightify.reactant.core

/**
 *  @author <a href="mailto:filip.dolnik.96@gmail.com">Filip Dolnik</a>
 */
internal object InstanceStorage {

    private val data = HashMap<String, Any>()

    fun store(key: String, value: Any) {
        data[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> retrieve(key: String): T {
        return data.remove(key) as T
    }
}