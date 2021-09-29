package merchant.okcredit.accounting.data.local

import android.content.Context
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class AccountingPreference @Inject constructor(
    context: Context,
    sharedPreferencesMigrationLogger: Lazy<SharedPreferencesMigrationHandler.Logger>,
    migrations: Lazy<Migrations>,
) : OkcSharedPreferences(
    context = context,
    prefName = SHARED_PREF_NAME,
    version = SHARED_PREF_VERSION,
    migrationList = migrations.get().getList(),
    sharedPreferencesMigrationLogger = sharedPreferencesMigrationLogger,
) {

    companion object {
        private const val SHARED_PREF_NAME = "Accounting"
        private const val SHARED_PREF_VERSION = 1

        const val PREF_EXIT_DIALOG_SHOWN_COUNT = "customer_support_exit_dialog_shown_count"
        const val PREF_CONTACT_PERMISSION_ASKED_ONCE = "contact_permission_asked_once"
        const val MAX_LIMIT = 3
    }

    class Migrations @Inject constructor() {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration.emptyMigration(0, 1)
    }

    suspend fun isContactPermissionAskedOnce() =
        getBoolean(PREF_CONTACT_PERMISSION_ASKED_ONCE, Scope.Individual).first()

    suspend fun setContactPermissionAskedOnce(value: Boolean) = withContext(Dispatchers.IO) {
        set(PREF_CONTACT_PERMISSION_ASKED_ONCE, value, Scope.Individual)
    }

    fun clearAccountingPref(): Completable {
        return rxCompletable {
            clear()
        }.subscribeOn(Schedulers.io())
    }
}
