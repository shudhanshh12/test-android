package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend.contract.GetTotalTxnCount
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.asObservable
import tech.okcredit.android.base.preferences.DefaultPreferences
import tech.okcredit.android.base.preferences.DefaultPreferences.Keys.PREF_BUSINESS_IS_COLLECTION_DATE_SHOWN
import tech.okcredit.android.base.preferences.Scope
import javax.inject.Inject

class GetCanShowCollectionDate @Inject constructor(
    private val getTotalTxnCount: Lazy<GetTotalTxnCount>,
    private val pref: Lazy<DefaultPreferences>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<Unit, Boolean> {

    companion object {
        private const val DEFAULT_THRESHOLD = 5
    }

    override fun execute(req: Unit): Observable<Result<Boolean>> {
        return UseCase.wrapSingle(
            getActiveBusinessId.get().execute().flatMap { businessId ->
                pref.get().getBoolean(PREF_BUSINESS_IS_COLLECTION_DATE_SHOWN, Scope.Business(businessId)).asObservable()
                    .flatMap { collectionDateShown ->
                        if (collectionDateShown.not()) {
                            getTotalTxnCount.get().execute().map {
                                it > DEFAULT_THRESHOLD
                            }.toObservable()
                        } else {
                            Observable.just(false)
                        }
                    }.firstOrError()
            }
        )
    }
}
