package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.service.keyval.KeyValService
import `in`.okcredit.shared.utils.AbFeatures
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class IsAddTransactionRestricted @Inject constructor(
    private val keyValService: KeyValService,
    private val isSupplierCreditEnabledCustomer: IsSupplierCreditEnabledCustomer,
    private val ab: AbRepository,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(req: String): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            ab.isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = businessId).flatMap { enabled ->
                if (enabled) {
                    keyValService.contains(PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS, Scope.Business(businessId))
                        .flatMapObservable { it ->
                            if (it) {
                                keyValService[PREF_BUSINESS_TXN_RESTRICTED_CUSTOMERS, Scope.Business(businessId)]
                                    .flatMap {
                                        return@flatMap Observable.just(it.contains(req))
                                    }
                            } else {
                                return@flatMapObservable Observable.just(false)
                            }
                        }
                } else {
                    isSupplierCreditEnabledCustomer.execute(req)
                }
            }
        }
    }
}
