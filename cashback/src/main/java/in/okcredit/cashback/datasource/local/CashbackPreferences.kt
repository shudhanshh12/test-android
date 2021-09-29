package `in`.okcredit.cashback.datasource.local

import android.content.Context
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class CashbackPreferences @Inject constructor(
    context: Context,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context,
    prefName = SHARED_PREF_NAME,
    version = VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {

    companion object {
        const val VERSION = 1
        const val SHARED_PREF_NAME = "cashback"

        private const val KEY_CASHBACK_MESSAGE_DETAILS = "cashback_message_details"
        private const val KEY_CASHBACK_MESSAGE_DETAILS_LAST_UPDATED_AT = "cashback_message_details_last_updated_at"

        private const val INVALIDATED_CASHBACK_MESSAGE_DETAILS = ""
        private const val INVALIDATED_CASHBACK_MESSAGE_DETAILS_LAST_UPDATED_AT = -1L
    }

    class Migrations @Inject constructor() {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration.emptyMigration(0, 1)
    }

    fun setCashbackMessageDetails(value: String) =
        rxCompletable { set(KEY_CASHBACK_MESSAGE_DETAILS, value, Scope.Individual) }

    fun getCashbackMessageDetails(defaultValue: String = INVALIDATED_CASHBACK_MESSAGE_DETAILS) =
        getString(KEY_CASHBACK_MESSAGE_DETAILS, Scope.Individual, defaultValue).asObservable()

    fun setCashbackMessageDetailsTimestamp(millis: Long) =
        rxCompletable { set(KEY_CASHBACK_MESSAGE_DETAILS_LAST_UPDATED_AT, millis, Scope.Individual) }

    fun getCashbackMessageDetailsTimestamp(defaultValue: Long = INVALIDATED_CASHBACK_MESSAGE_DETAILS_LAST_UPDATED_AT) =
        getLong(KEY_CASHBACK_MESSAGE_DETAILS_LAST_UPDATED_AT, Scope.Individual, defaultValue).asObservable()

    fun invalidatePreferenceValues(): Completable {
        return setCashbackMessageDetailsTimestamp(INVALIDATED_CASHBACK_MESSAGE_DETAILS_LAST_UPDATED_AT)
            .andThen(setCashbackMessageDetails(INVALIDATED_CASHBACK_MESSAGE_DETAILS))
    }
}
