package `in`.okcredit.merchant.customer_ui.usecase

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class CanShowCollectWithGPay @Inject constructor(
    private val ab: Lazy<AbRepository>,
) {

    companion object {
        const val EXPT = "postlogin_android-all-gpay_experiment"

        const val TEST = "test"
        const val CONTROL = "control"
    }

    fun execute(): Observable<Boolean> {
        return isExperimentEnabled().flatMap { enabled ->
            if (enabled) {
                getVariant().flatMap { variant ->
                    when (variant) {
                        TEST -> Observable.just(true)
                        else -> Observable.just(false)
                    }
                }
            } else {
                Observable.just(false)
            }
        }
    }

    private fun isExperimentEnabled() = ab.get().isExperimentEnabled(EXPT)

    private fun getVariant() = ab.get().getExperimentVariant(EXPT)
}
