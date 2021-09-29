package `in`.okcredit.cashback.usecase

import `in`.okcredit.cashback.contract.usecase.IsSupplierCashbackFeatureEnabled
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class IsSupplierCashbackFeatureEnabledImpl @Inject constructor(
    private val ab: Lazy<AbRepository>,
) : IsSupplierCashbackFeatureEnabled {

    override fun execute(): Observable<Boolean> {
        return ab.get().isFeatureEnabled(FEATURE_NAME)
    }

    companion object {
        private const val FEATURE_NAME = "supplier_cashback"
    }
}
