package `in`.okcredit.merchant.store.sharedprefs

import `in`.okcredit.merchant.store.BusinessLocalSource
import `in`.okcredit.merchant.usecase.GetActiveBusinessIdImpl.Companion.DEFAULT_BUSINESS_ID
import android.content.Context
import androidx.core.content.edit
import dagger.Lazy
import dagger.Reusable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class BusinessPreferences @Inject constructor(
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
        const val SHARED_PREF_NAME = "Merchant"
        const val VERSION = 1
    }

    object Keys {
        const val PREF_DEPRECATED_MERCHANT_ID = "deprecated_merchant_id"
    }

    class Migrations @Inject constructor(
        // local source is used directly in usecase to avoid exposing deprecated method via repository
        private val localSource: Lazy<BusinessLocalSource>,
    ) {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration(0, 1) { pref ->
            val deprecatedMerchantId = localSource.get().getBusinessIdForMultipleAccountsMigration()
            if (deprecatedMerchantId != null) {
                pref.edit(commit = true) {
                    putString(Keys.PREF_DEPRECATED_MERCHANT_ID, deprecatedMerchantId)
                    putString(DEFAULT_BUSINESS_ID, deprecatedMerchantId)
                }
            }
        }
    }
}
