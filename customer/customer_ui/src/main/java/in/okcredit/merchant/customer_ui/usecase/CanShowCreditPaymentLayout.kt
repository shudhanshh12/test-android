package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.service.keyval.KeyValService
import `in`.okcredit.shared.utils.AbFeatures
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class CanShowCreditPaymentLayout @Inject constructor(
    private val keyValService: Lazy<KeyValService>,
    private val ab: Lazy<AbRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(req: String): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            ab.get().isFeatureEnabled(AbFeatures.SINGLE_LIST, businessId = businessId).flatMap {
                if (it) {
                    keyValService.get().contains(PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS, Scope.Business(businessId))
                        .flatMapObservable { it ->
                            if (it) {
                                keyValService.get()[
                                    PREF_BUSINESS_ADD_TXN_RESTRICTED_CUSTOMERS,
                                    Scope.Business(businessId)
                                ]
                                    .flatMap { return@flatMap Observable.just(it.contains(req).not()) }
                            } else {
                                return@flatMapObservable Observable.just(true)
                            }
                        }
                } else {
                    Observable.just(true)
                }
            }
        }
    }
}
