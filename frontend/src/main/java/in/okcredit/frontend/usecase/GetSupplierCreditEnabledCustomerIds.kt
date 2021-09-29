package `in`.okcredit.frontend.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.service.keyval.KeyValService
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_SC_ENABLED_CUSTOMERS
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class GetSupplierCreditEnabledCustomerIds @Inject constructor(
    private val keyValService: KeyValService,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, String> {
    override fun execute(req: Unit): Observable<Result<String>> {
        return UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                keyValService.contains(PREF_BUSINESS_SC_ENABLED_CUSTOMERS, Scope.Business(businessId))
                    .flatMapObservable { it ->
                        if (it) {
                            keyValService[PREF_BUSINESS_SC_ENABLED_CUSTOMERS, Scope.Business(businessId)].flatMap {
                                return@flatMap Observable.just(it)
                            }
                        } else {
                            return@flatMapObservable Observable.just("")
                        }
                    }
            }
        )
    }
}
