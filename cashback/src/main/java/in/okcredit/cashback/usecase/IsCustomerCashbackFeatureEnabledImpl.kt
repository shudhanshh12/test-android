package `in`.okcredit.cashback.usecase

import `in`.okcredit.backend.contract.Features
import `in`.okcredit.cashback.contract.usecase.IsCustomerCashbackFeatureEnabled
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class IsCustomerCashbackFeatureEnabledImpl @Inject constructor(
    private val ab: Lazy<AbRepository>,
) : IsCustomerCashbackFeatureEnabled {

    override fun execute(): Observable<Boolean> {
        val observables = listOf(
            ab.get().isFeatureEnabled(Features.CUSTOMER_ONLINE_PAYMENT),
            ab.get().isFeatureEnabled(FEATURE_NAME)
        )

        return Observable.combineLatest(observables) {
            val isCustomerCollectionEnabled = it[0] as Boolean
            val isCustomerCashbackFeatureEnabled = it[1] as Boolean

            return@combineLatest (isCustomerCollectionEnabled && isCustomerCashbackFeatureEnabled)
        }
    }

    companion object {
        private const val FEATURE_NAME = "customer_cashback"
    }
}
