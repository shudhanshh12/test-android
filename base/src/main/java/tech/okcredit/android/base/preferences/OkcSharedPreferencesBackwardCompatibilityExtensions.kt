package tech.okcredit.android.base.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use getBoolean()", replaceWith = ReplaceWith("getBoolean()"))
fun OkcSharedPreferences.blockingGetBoolean(key: String, scope: Scope, defaultValue: Boolean = false): Boolean {
    return runBlocking { getBoolean(key, scope, defaultValue).first() }
}

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use getString()", replaceWith = ReplaceWith("getString()"))
fun OkcSharedPreferences.blockingGetString(key: String, scope: Scope, defaultValue: String? = ""): String? {
    return runBlocking {
        if (contains(key, scope)) { // added to support nullable defaultValue
            getString(key, scope).first()
        } else {
            defaultValue
        }
    }
}

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use getLong()", replaceWith = ReplaceWith("getLong()"))
fun OkcSharedPreferences.blockingGetLong(key: String, scope: Scope, defaultValue: Long = 0L): Long {
    return runBlocking { getLong(key, scope, defaultValue).first() }
}

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use getInt()", replaceWith = ReplaceWith("getInt()"))
fun OkcSharedPreferences.blockingGetInt(key: String, scope: Scope, defaultValue: Int = 0): Int {
    return runBlocking { getInt(key, scope, defaultValue).first() }
}

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use set()", replaceWith = ReplaceWith("set()"))
fun OkcSharedPreferences.blockingSet(key: String, value: Boolean, scope: Scope) {
    runBlocking { set(key, value, scope) }
}

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use set()", replaceWith = ReplaceWith("set()"))
fun OkcSharedPreferences.blockingSet(key: String, value: String, scope: Scope) {
    runBlocking { set(key, value, scope) }
}

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use set()", replaceWith = ReplaceWith("set()"))
fun OkcSharedPreferences.blockingSet(key: String, value: Long, scope: Scope) {
    runBlocking { set(key, value, scope) }
}

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use set()", replaceWith = ReplaceWith("set()"))
fun OkcSharedPreferences.blockingSet(key: String, value: Int, scope: Scope) {
    runBlocking { set(key, value, scope) }
}

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use contains()", replaceWith = ReplaceWith("contains()"))
fun OkcSharedPreferences.blockingContains(key: String, scope: Scope): Boolean {
    return runBlocking { contains(key, scope) }
}

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use clear()", replaceWith = ReplaceWith("clear()"))
fun OkcSharedPreferences.blockingClear() {
    runBlocking { clear() }
}

// Added to migrate legacy code to use OkcSharedPreferences
@Deprecated("Use remove()", replaceWith = ReplaceWith("remove()"))
fun OkcSharedPreferences.blockingRemove(key: String, scope: Scope) {
    runBlocking { remove(key, scope) }
}

fun getIndividualScope() = Scope.Individual
