package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen.usecase

import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import dagger.Lazy
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class UpdateCalculatorEducationVisibility @Inject constructor(
    private val customerRepository: Lazy<CustomerRepositoryImpl>,
) {

    fun execute() = rxCompletable {
        customerRepository.get().setTxnCountForCalculatorEducation(-1)
        customerRepository.get().setShowCalculatorEducation(false)
    }
}
