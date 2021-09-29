package tech.okcredit.android.auth.store

import android.content.Context
import dagger.Lazy
import dagger.Reusable
import org.joda.time.DateTime
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class AuthPreferences @Inject constructor(
    context: Lazy<Context>,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context.get(),
    prefName = SHARED_PREF_NAME,
    version = VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {
    companion object {
        const val VERSION = 2
        private const val SHARED_PREF_NAME = "auth.prefs"
    }

    class Migrations @Inject constructor(
        private val context: Lazy<Context>,
    ) {
        fun getList() = listOf(migration0To1(), migration1To2())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { prefs ->
            val defaultPrefs = context.get().getSharedPreferences("in.okcredit.default", Context.MODE_PRIVATE)
            val mobile = defaultPrefs.getString("auth.username", null) ?: return@SharedPreferencesMigration
            val accessToken = defaultPrefs.getString("auth.access_token", null) ?: return@SharedPreferencesMigration
            val refreshToken = defaultPrefs.getString("auth.refresh_token", null) ?: return@SharedPreferencesMigration
            val expireTime = try {
                DateTime(defaultPrefs.getString("auth.access_token_expiry", null)!!.toLong() * 1000L)
            } catch (e: Exception) {
                DateTime.now()
            }

            prefs.edit().putString(AuthLocalSourceImpl.KEY_MOBILE, mobile).commit()
            prefs.edit().putString(AuthLocalSourceImpl.KEY_ACCESS_TOKEN, accessToken).commit()
            prefs.edit().putString(AuthLocalSourceImpl.KEY_REFRESH_TOKEN, refreshToken).commit()
            prefs.edit().putLong(AuthLocalSourceImpl.KEY_EXPIRE_TIME, expireTime.millis).commit()
        }

        private fun migration1To2() = SharedPreferencesMigration(1, 2) { prefs ->
            prefs.edit().putLong(AuthLocalSourceImpl.KEY_EXPIRE_TIME, 0).commit() // invalidate deprecated V1 auth token
        }
    }
}
