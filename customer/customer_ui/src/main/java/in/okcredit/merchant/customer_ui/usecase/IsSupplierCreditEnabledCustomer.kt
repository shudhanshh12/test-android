package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.service.keyval.KeyValService
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_SC_ENABLED_CUSTOMERS
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class IsSupplierCreditEnabledCustomer @Inject constructor(
    private val keyValService: Lazy<KeyValService>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(req: String): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            keyValService.get().contains(PREF_BUSINESS_SC_ENABLED_CUSTOMERS, Scope.Business(businessId))
                .flatMapObservable { it ->
                    if (it) {
                        keyValService.get()[PREF_BUSINESS_SC_ENABLED_CUSTOMERS, Scope.Business(businessId)].flatMap {
                            return@flatMap Observable.just(it.contains(req))
                        }
                    } else {
                        return@flatMapObservable Observable.just(false)
                    }
                }
        }
    }
}
