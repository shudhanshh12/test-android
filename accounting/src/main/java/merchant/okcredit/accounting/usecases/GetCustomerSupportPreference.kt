package merchant.okcredit.accounting.usecases

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Completable
import merchant.okcredit.accounting.repo.AccountingRepositoryImpl
import javax.inject.Inject

class GetCustomerSupportPreference @Inject constructor(
    private val accountingRepositoryImpl: Lazy<AccountingRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun setCustomerSupportExitPreference(): Completable =
        getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            accountingRepositoryImpl.get().setCustomerSupportExitPreference(businessId)
        }

    fun shouldShowCustomerSupportExitDialog() = getActiveBusinessId.get().execute().flatMap { businessId ->
        accountingRepositoryImpl.get().shouldShowCustomerSupportExitDialog(businessId)
    }
}
