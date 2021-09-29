package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Single
import merchant.okcredit.supplier.contract.IsNetworkReminderEnabled
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class IsNetworkReminderEnabledImpl @Inject constructor(
    private val ab: Lazy<AbRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : IsNetworkReminderEnabled {
    companion object {
        const val FEATURE_NETWORK_REMINDER = "network_reminder"
    }

    override fun execute(): Single<Boolean> {
        return getActiveBusinessId.get().execute().flatMap {
            ab.get().isFeatureEnabled(FEATURE_NETWORK_REMINDER, businessId = it).firstOrError()
        }
    }
}
