package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import dagger.Lazy
import javax.inject.Inject

class HideAddBillToolTip @Inject constructor(
    private val customerRepositoryImpl: Lazy<CustomerRepositoryImpl>,
) {
    fun execute() {
        customerRepositoryImpl.get().setAddBillToolTipShowed()
    }
}
