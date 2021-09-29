package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetCanShowChatNewSticker @Inject constructor(
    private val ab: Lazy<AbRepository>
) : UseCase<Unit, Boolean> {

    companion object {
        private const val EXPERIMENT_NAME = "ui_experiment-all-chat_toolbar_new_label"
        private const val DEFAULT_VARIANT_VALUE = false
    }

    override fun execute(req: Unit): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            ab.get().isExperimentEnabled(EXPERIMENT_NAME).flatMap { enabled ->
                if (enabled) {
                    ab.get().getExperimentVariant(EXPERIMENT_NAME).flatMap {
                        when (it) {
                            "show" -> {
                                Observable.just(true)
                            }
                            "dont_show" -> {
                                Observable.just(false)
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
