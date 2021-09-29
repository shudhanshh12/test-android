package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend.contract.Features
import `in`.okcredit.shared.usecase.IsInternetAvailable
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import java.util.*
import javax.inject.Inject

class IsDiscountUiEnabled @Inject constructor(
    private val ab: AbRepository,
    private val isInternetAvailable: IsInternetAvailable
) {
    fun execute(): Observable<Boolean> {
        val observables = listOf(
            ab.isFeatureEnabled(Features.DISCOUNT_V2),
            isInternetAvailable.execute()
        )

        return Observable.combineLatest(observables) {
            val isDiscountEnabled = it[0] as Boolean
            val isInternetAvailable = it[1] as Boolean

            return@combineLatest (isDiscountEnabled && isInternetAvailable)
        }
    }
}
