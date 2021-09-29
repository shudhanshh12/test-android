package `in`.okcredit.merchant.customer_ui.usecase

import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class ShowEditAmountABExperiment @Inject constructor(
    private val ab: AbRepository
) {

    companion object {
        const val EXPERIMENT_NAME = "postlogin_android-all-show_edit_amount_education"
        const val VARIANT_V2 = "v2"
    }

    fun execute(): Observable<Boolean> {

        return ab.isExperimentEnabled(EXPERIMENT_NAME).flatMap { enabled ->
            if (enabled) {
                return@flatMap getExperimentVariantObservable()
                    .map { variant ->
                        return@map variant == VARIANT_V2
                    }
            } else {
                Observable.just(false)
            }
        }
    }

    private fun getExperimentVariantObservable() = ab.getExperimentVariant(EXPERIMENT_NAME)
}
