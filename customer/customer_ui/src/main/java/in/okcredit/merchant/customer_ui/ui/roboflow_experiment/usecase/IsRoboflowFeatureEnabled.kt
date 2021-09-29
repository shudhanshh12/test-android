package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class IsRoboflowFeatureEnabled @Inject constructor(
    private val ab: Lazy<AbRepository>
) {
    companion object {
        const val FEATURE_NAME = "image_onboarding"
    }

    fun execute(): Observable<Boolean> = ab.get().isFeatureEnabled(FEATURE_NAME)
}
