package `in`.okcredit.merchant.customer_ui.usecase

import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class ReportFromBalanceWidgetExpt @Inject constructor(
    private val ab: Lazy<AbRepository>,
) {

    companion object {
        const val EXPT = "postlogin_android-all-report_from_balance_widget"

        const val TEST = "TEST"
        const val CONTROL = "CONTROL"
    }

    fun execute(): Observable<Boolean> {
        return isExperimentEnabled().flatMap {
            getVariant().flatMap {
                when (it) {
                    TEST -> Observable.just(true)
                    else -> Observable.just(false)
                }
            }
        }
    }

    private fun isExperimentEnabled() = ab.get().isExperimentEnabled(EXPT).filter { it }

    private fun getVariant() = ab.get().getExperimentVariant(EXPT)
}
