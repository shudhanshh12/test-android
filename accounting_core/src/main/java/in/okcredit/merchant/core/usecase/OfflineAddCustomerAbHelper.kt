package `in`.okcredit.merchant.core.usecase

import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class OfflineAddCustomerAbHelper @Inject constructor(
    private val ab: Lazy<AbRepository>,
) {

    companion object {
        const val FEATURE_NAME = "disable_offline_add_customer"
        const val EXPERIMENT_NAME = "activation_android-all-add_customer_offline"
        const val VARIANT_OFFLINE = "offline"
    }

    fun isDisableOfflineAddCustomerFeature(): Single<Boolean> = ab.get()
        .isFeatureEnabled(FEATURE_NAME)
        .firstOrError()

    fun isEligibleForOfflineAddCustomer(): Single<Boolean> = ab.get()
        .isExperimentEnabled(EXPERIMENT_NAME)
        .flatMap { enabled ->
            if (enabled) {
                ab.get().getExperimentVariant(EXPERIMENT_NAME)
                    .map { it.equals(VARIANT_OFFLINE, ignoreCase = true) }
            } else {
                Observable.just(false)
            }
        }
        .firstOrError()
}
