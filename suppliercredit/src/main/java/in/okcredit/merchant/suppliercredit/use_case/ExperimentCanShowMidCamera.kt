package `in`.okcredit.merchant.suppliercredit.use_case

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class ExperimentCanShowMidCamera @Inject constructor(
    private val ab: AbRepository
) : UseCase<Unit, Boolean> {

    companion object {
        private const val EXPERIMENT_NAME = "postlogin_android-all-add_image_icon_tx_screen"
        private const val DEFAULT_VARIANT_VALUE = false
    }

    override fun execute(req: Unit): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            ab.isExperimentEnabled(EXPERIMENT_NAME).flatMap { enabled ->
                if (enabled) {
                    ab.getExperimentVariant(EXPERIMENT_NAME).flatMap {
                        when (it) {
                            "defaultCamera" -> {
                                Observable.just(false)
                            }
                            "midCamera" -> {
                                Observable.just(true)
                            }
                            else -> {
                                Observable.just(DEFAULT_VARIANT_VALUE)
                            }
                        }
                    }
                } else {
                    Observable.just(DEFAULT_VARIANT_VALUE)
                }
            }
        )
    }
}
