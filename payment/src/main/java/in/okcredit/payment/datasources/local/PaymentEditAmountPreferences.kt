package `in`.okcredit.payment.datasources.local

import android.content.Context
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.preferences.OkcSharedPreferences
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.preferences.SharedPreferencesMigration
import tech.okcredit.android.base.preferences.SharedPreferencesMigrationHandler
import javax.inject.Inject

@Reusable
class PaymentEditAmountPreferences @Inject constructor(
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
        private const val SHARED_PREF_NAME = "payment_edit_amount"
        private const val SHARED_PREF_VERSION = 1

        private const val SHOULD_SHOW_KYC_BANNER_ON_PAYMENT_EDIT_AMOUNT_PAGE =
            "should_show_kyc_banner_on_payment_edit_amount_page"
    }

    class Migrations @Inject constructor() {
        fun getList() = listOf(migration0To1())

        private fun migration0To1() = SharedPreferencesMigration.emptyMigration(0, 1)
    }

    fun setShouldShowKycBannerOnPaymentEditAmountPage(value: Boolean) = rxCompletable {
        set(SHOULD_SHOW_KYC_BANNER_ON_PAYMENT_EDIT_AMOUNT_PAGE, value, Scope.Individual)
    }

    fun getShouldShowKycBannerOnPaymentEditAmountPage(defaultValue: Boolean = true) =
        getBoolean(SHOULD_SHOW_KYC_BANNER_ON_PAYMENT_EDIT_AMOUNT_PAGE, Scope.Individual, defaultValue).asObservable()
}
