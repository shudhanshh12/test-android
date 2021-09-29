package `in`.okcredit.merchant.contract

import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.preferences.OkcSharedPreferences

interface BusinessScopedPreferenceWithActiveBusinessId {
    fun setBoolean(prefs: OkcSharedPreferences, key: String, value: Boolean): Completable
    fun setString(prefs: OkcSharedPreferences, key: String, value: String): Completable
    fun contains(prefs: OkcSharedPreferences, key: String): Single<Boolean>
    fun delete(prefs: OkcSharedPreferences, key: String): Completable
}
