package `in`.okcredit.merchant.customer_ui.ui.subscription.usecase

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class SubscriptionFeatureEnabled @Inject constructor(
    private val ab: Lazy<AbRepository>
) {

    companion object {
        const val SUBSCRIPTION_FEATURE = "subscription"
    }

    fun execute(): Observable<Boolean> {
        return ab.get().isFeatureEnabled(SUBSCRIPTION_FEATURE)
    }
}
