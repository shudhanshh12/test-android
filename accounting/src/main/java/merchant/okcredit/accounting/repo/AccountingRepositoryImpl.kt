package merchant.okcredit.accounting.repo

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import merchant.okcredit.accounting.contract.AccountingRepository
import merchant.okcredit.accounting.contract.model.FRC_HElP_CHAT_NUMBER_PAYMENT
import merchant.okcredit.accounting.contract.model.FRC_HElP_NUMBER_PAYMENT
import merchant.okcredit.accounting.contract.model.FRC_PAYMENT_24X7
import merchant.okcredit.accounting.data.local.AccountingPreference
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class AccountingRepositoryImpl @Inject constructor(
    private val accountingPreference: Lazy<AccountingPreference>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) : AccountingRepository {

    fun setCustomerSupportExitPreference(businessId: String): Completable {
        return rxCompletable {
            accountingPreference.get()
                .increment(AccountingPreference.PREF_EXIT_DIALOG_SHOWN_COUNT, Scope.Business(businessId))
        }
    }

    fun shouldShowCustomerSupportExitDialog(businessId: String): Single<Boolean> {
        return accountingPreference.get()
            .getInt(AccountingPreference.PREF_EXIT_DIALOG_SHOWN_COUNT, Scope.Business(businessId))
            .asObservable().firstOrError()
            .map { it < AccountingPreference.MAX_LIMIT }
    }

    override fun clearAccountingData(): Completable {
        return accountingPreference.get().clearAccountingPref()
    }

    override fun get24x7String(): String = firebaseRemoteConfig.get().getString(FRC_PAYMENT_24X7)

    override fun getCustomerCareCallNumber() = firebaseRemoteConfig.get().getString(FRC_HElP_NUMBER_PAYMENT)

    override fun getCustomerCareChatNumber() = firebaseRemoteConfig.get().getString(FRC_HElP_CHAT_NUMBER_PAYMENT)

    suspend fun isContactPermissionAskedOnce() = accountingPreference.get().isContactPermissionAskedOnce()

    suspend fun setContactPermissionAskedOnce(value: Boolean) =
        accountingPreference.get().setContactPermissionAskedOnce(value)
}
