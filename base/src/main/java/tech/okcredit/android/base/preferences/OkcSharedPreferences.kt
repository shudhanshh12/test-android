package tech.okcredit.android.base.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Lazy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.preferences.Scope.Companion.DIVIDER
import tech.okcredit.android.base.preferences.Scope.Companion.getScopedKey
import tech.okcredit.android.base.preferences.SharedPreferencesMigration.Companion.convertToIndividualScopedKey
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler.Companion.PREF_VERSION
import tech.okcredit.android.base.utils.debug
import timber.log.Timber

/**
 * [OkcSharedPreferences] is the base class for all SharedPreferences classes for OkCredit app.
 * It implements multiple account key-value support using [Scope].
 *
 * PS: Do not use SharedPreferences or RxSharedPreferences directly anywhere in the app.
 * All shared preferences in OkCredit app should extend [OkcSharedPreferences].
 */

abstract class OkcSharedPreferences protected constructor(
    context: Context,
    private val prefName: String? = null,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
    version: Int,
    migrationList: List<SharedPreferencesMigration>,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
) {

    internal val sharedPreference by lazy {
        if (prefName.isNullOrBlank()) {
            PreferenceManager.getDefaultSharedPreferences(context)
        } else {
            context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        }
    }

    private val rxPreferences by lazy {
        RxSharedPreferences.create(sharedPreference)
    }

    init {
        SharedPreferencesMigrationHandler(
            version = version,
            prefName = prefName,
            sharedPreference = sharedPreference,
            migrationList = migrationList,
            sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
            coroutineDispatcher = coroutineDispatcher
        ).checkForMigrations()
    }

    //region Getters

    fun getString(key: String, scope: Scope, defaultValue: String = ""): Flow<String> {
        val scopedKey = getScopedKey(key, scope)
        return rxPreferences.getString(scopedKey, defaultValue).asObservable().asFlow()
            .checkForIncorrectKeyScope(key, scopedKey, scope)
    }

    fun getBoolean(key: String, scope: Scope, defaultValue: Boolean = false): Flow<Boolean> {
        val scopedKey = getScopedKey(key, scope)
        return rxPreferences.getBoolean(scopedKey, defaultValue).asObservable().asFlow()
            .checkForIncorrectKeyScope(key, scopedKey, scope)
    }

    fun getInt(key: String, scope: Scope, defaultValue: Int = 0): Flow<Int> {
        val scopedKey = getScopedKey(key, scope)
        return rxPreferences.getInteger(scopedKey, defaultValue).asObservable().asFlow()
            .checkForIncorrectKeyScope(key, scopedKey, scope)
    }

    fun getLong(key: String, scope: Scope, defaultValue: Long = 0L): Flow<Long> {
        val scopedKey = getScopedKey(key, scope)
        return rxPreferences.getLong(scopedKey, defaultValue).asObservable().asFlow()
            .checkForIncorrectKeyScope(key, scopedKey, scope)
    }

    fun getFloat(key: String, scope: Scope, defaultValue: Float = 0f): Flow<Float> {
        val scopedKey = getScopedKey(key, scope)
        return rxPreferences.getFloat(scopedKey, defaultValue).asObservable().asFlow()
            .checkForIncorrectKeyScope(key, scopedKey, scope)
    }

    fun <T : Any> getObject(key: String, scope: Scope, defaultValue: T, converter: Preference.Converter<T>): Flow<T> {
        val scopedKey = getScopedKey(key, scope)
        return rxPreferences.getObject(scopedKey, defaultValue, converter).asObservable().asFlow()
            .checkForIncorrectKeyScope(key, scopedKey, scope)
    }

    //endregion

    //region Setters

    suspend fun set(key: String, value: String, scope: Scope) = withContext(coroutineDispatcher) {
        val scopedKey = getScopedKey(key, scope)
        rxPreferences.getString(scopedKey).set(value)
    }

    suspend fun set(key: String, value: Boolean, scope: Scope) = withContext(coroutineDispatcher) {
        val scopedKey = getScopedKey(key, scope)
        rxPreferences.getBoolean(scopedKey).set(value)
    }

    suspend fun set(key: String, value: Int, scope: Scope) = withContext(coroutineDispatcher) {
        val scopedKey = getScopedKey(key, scope)
        rxPreferences.getInteger(scopedKey).set(value)
    }

    suspend fun set(key: String, value: Long, scope: Scope) = withContext(coroutineDispatcher) {
        val scopedKey = getScopedKey(key, scope)
        rxPreferences.getLong(scopedKey).set(value)
    }

    suspend fun set(key: String, value: Float, scope: Scope) = withContext(coroutineDispatcher) {
        val scopedKey = getScopedKey(key, scope)
        rxPreferences.getFloat(scopedKey).set(value)
    }

    suspend fun <T : Any> set(key: String, value: T, scope: Scope, converter: Preference.Converter<T>) =
        withContext(coroutineDispatcher) {
            val scopedKey = getScopedKey(key, scope)
            rxPreferences.getObject(scopedKey, value, converter).set(value)
        }

    suspend fun increment(key: String, scope: Scope) = withContext(coroutineDispatcher) {
        val currentValue = getInt(key, scope).first()
        val scopedKey = getScopedKey(key, scope)
        rxPreferences.getInteger(scopedKey).set(currentValue + 1)
    }

    //endregion

    suspend fun contains(key: String, scope: Scope): Boolean = withContext(coroutineDispatcher) {
        val scopedKey = getScopedKey(key, scope)
        sharedPreference.contains(scopedKey)
    }

    suspend fun remove(key: String, scope: Scope) = withContext(coroutineDispatcher) {
        val scopedKey = getScopedKey(key, scope)
        sharedPreference.edit().remove(scopedKey).commit()
    }

    // preserve shared preference version and clear data
    suspend fun clear() = withContext(coroutineDispatcher) {
        sharedPreference.edit(commit = true) {
            val version = sharedPreference.getInt(PREF_VERSION, 0)
            clear()
            putInt(PREF_VERSION, version)
        }
    }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreference.registerOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Checks if scope passed with key is incorrect to print a log as a warning.
     * Note - for performance reasons, this check only runs in debug app.
     */
    private fun <T> Flow<T>.checkForIncorrectKeyScope(key: String, scopedKey: String, scope: Scope): Flow<T> =
        onEach {
            debug {
                if (sharedPreference.contains(scopedKey).not()) {
                    when (scope) {
                        is Scope.Individual -> {
                            // Check if key is present in Business scope
                            sharedPreference.all.forEach { entry ->
                                if (entry.key.contains(DIVIDER.plus(key))) {
                                    Timber.e(
                                        """OkcSharedPreferences > ${prefName ?: "Default"}:
                                    Key '$key' was not found in Individual scope but was found in Business scope.
                                    Write a SharedPreferencesMigration to migrate key scopes."""
                                    )
                                    return@forEach
                                }
                            }
                        }
                        is Scope.Business -> {
                            // Check if key is present in Individual scope
                            val individualScopedKey = convertToIndividualScopedKey(scopedKey)
                            if (sharedPreference.contains(individualScopedKey)) {
                                Timber.e(
                                    """OkcSharedPreferences > ${prefName ?: "Default"}:
                                    Key '$key' was not found in Business scope but was found in Individual scope.
                                    Write a SharedPreferencesMigration to migrate key scopes."""
                                )
                            }
                        }
                    }
                }
            }
        }
}
