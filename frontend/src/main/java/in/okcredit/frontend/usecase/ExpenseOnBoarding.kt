package `in`.okcredit.frontend.usecase

import `in`.okcredit.frontend.ui.expense_manager.ExpenseManagerContract
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class ExpenseOnBoarding @Inject constructor(
    private val ab: AbRepository
) : UseCase<Unit, ExpenseManagerContract.OnBoardingVariant> {

    private val experimentName = "postlogin_android-all-expense_onboarding_infographics"
    private val v1 = "v1"
    private val v2 = "v2"
    private val v3 = "v3"

    override fun execute(req: Unit): Observable<Result<ExpenseManagerContract.OnBoardingVariant>> {
        return UseCase.wrapObservable(
            isExpenseOnBoardingExperimentEnabled().flatMap {
                if (it) {
                    return@flatMap getExperimentVariant()
                }
                return@flatMap Observable.just(ExpenseManagerContract.OnBoardingVariant.v1)
            }
        )
    }

    private fun isExpenseOnBoardingExperimentEnabled() = ab.isExperimentEnabled(experimentName)
    private fun getExperimentVariant() = ab.getExperimentVariant(experimentName).map {
        return@map when (it) {
            v1 -> ExpenseManagerContract.OnBoardingVariant.v1
            v2 -> ExpenseManagerContract.OnBoardingVariant.v2
            v3 -> ExpenseManagerContract.OnBoardingVariant.v3
            else -> ExpenseManagerContract.OnBoardingVariant.v1
        }
    }
}
