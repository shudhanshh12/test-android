package `in`.okcredit.payment.usecases

import `in`.okcredit.payment.contract.usecase.IsPspUpiFeatureEnabled
import dagger.Lazy
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class IsPspUpiFeatureEnabledImpl @Inject constructor(
    private val ab: Lazy<AbRepository>
) : IsPspUpiFeatureEnabled {
    override fun execute() = ab.get().isFeatureEnabled("upi_psp")
}
