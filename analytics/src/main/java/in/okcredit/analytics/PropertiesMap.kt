package `in`.okcredit.analytics

import org.jetbrains.annotations.NonNls

class PropertiesMap private constructor() {

    private val mutableMap = mutableMapOf<String, Any>()

    @NonNls
    fun add(key: String, value: Any): PropertiesMap {
        mutableMap.put(key, value)
        return this
    }

    companion object {
        fun create(): PropertiesMap {
            return PropertiesMap()
        }
    }

    fun map(): MutableMap<String, Any> {
        return mutableMap
    }
}
