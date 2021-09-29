package tech.okcredit.home.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class ShowFeedback @Inject constructor(
    private val ab: Lazy<AbRepository>
) {

    companion object {
        private const val EXPERIMENT_NAME = "ui_experiment-all-feedback"
        private const val FEEDBACK_VERIENT = "feedback"
    }

    fun execute(): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(
            ab.get().getExperimentVariant(EXPERIMENT_NAME).map { variant ->
                return@map when (variant) {
                    FEEDBACK_VERIENT -> true
                    else -> false
                }
            }
        )
    }
}
