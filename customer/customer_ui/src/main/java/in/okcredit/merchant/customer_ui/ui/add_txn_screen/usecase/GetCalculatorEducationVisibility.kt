package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase

import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.usecase.GetAllTransactionCount
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class GetCalculatorEducationVisibility @Inject constructor(
    private val abRepository: Lazy<AbRepository>,
    private val customerRepository: Lazy<CustomerRepositoryImpl>,
    private val getAllTransactionCount: Lazy<GetAllTransactionCount>,
) {

    companion object {
        private const val EXPERIMENT_CALCULATOR_EDUCATION = "postlogin_android-all-calculator_education"
        private const val VARIANT_CONSECUTIVE_TXN = "consecutive_txns"
        private const val VARIANT_EVERY_THIRD_TXN = "every_third_txn"

        private const val TRANSACTION_THRESHOLD = 3
    }

    fun execute(): Observable<Boolean> = Observable.combineLatest(
        customerRepository.get().canShowCalculatorEducation(),
        checkForEducationVariant(),
        getAllTransactionCount.get().execute(Unit).filter { it is Result.Success },
        ::checkResult
    )

    private fun checkResult(
        showEducation: Boolean,
        experimentVariant: String,
        countResult: Result<Int>
    ): Boolean {
        if (!showEducation || experimentVariant.isEmpty()) return false

        val totalTxnCount = (countResult as? Result.Success)?.value ?: 0
        val txnCountOnCalculatorEducation = customerRepository.get().getTxnCntForCalculatorEducation()
        if (txnCountOnCalculatorEducation == -1) {
            customerRepository.get().setTxnCountForCalculatorEducation(totalTxnCount)
            return true
        }

        return when (experimentVariant) {
            VARIANT_CONSECUTIVE_TXN -> {
                totalTxnCount - txnCountOnCalculatorEducation < TRANSACTION_THRESHOLD
            }
            VARIANT_EVERY_THIRD_TXN -> {
                (totalTxnCount - txnCountOnCalculatorEducation <= (2 * TRANSACTION_THRESHOLD)) && (totalTxnCount - txnCountOnCalculatorEducation) % 3 == 0
            }
            else -> {
                false
            }
        }
    }

    private fun checkForEducationVariant() = abRepository.get().isExperimentEnabled(
        EXPERIMENT_CALCULATOR_EDUCATION
    ).switchMap { enabled ->
        if (!enabled) {
            return@switchMap Observable.just("")
        }

        abRepository.get().getExperimentVariant(EXPERIMENT_CALCULATOR_EDUCATION)
    }
}
