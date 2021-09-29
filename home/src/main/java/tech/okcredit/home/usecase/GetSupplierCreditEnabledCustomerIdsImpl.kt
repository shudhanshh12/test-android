package tech.okcredit.home.usecase

import `in`.okcredit.home.GetSupplierCreditEnabledCustomerIds
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.service.keyval.KeyValService
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_SC_ENABLED_CUSTOMERS
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class GetSupplierCreditEnabledCustomerIdsImpl @Inject constructor(
    private val keyValService: Lazy<KeyValService>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    GetSupplierCreditEnabledCustomerIds {
    override fun execute(businessId: String?): Observable<String> {
        return getActiveBusinessId.get().thisOrActiveBusinessId(businessId).flatMapObservable { _businessId ->
            keyValService.get()[PREF_BUSINESS_SC_ENABLED_CUSTOMERS, Scope.Business(_businessId)].flatMap {
                return@flatMap Observable.just(it)
            }
        }
    }
}
