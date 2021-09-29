package `in`.okcredit.merchant.customer_ui.ui.roboflow_experiment.usecase

import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import dagger.Lazy
import io.reactivex.Single
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

class RoboflowCanShowAddBillTooltip @Inject constructor(
    private val customerRepositoryImpl: Lazy<CustomerRepositoryImpl>
) {

    fun execute(): Single<Boolean> {
        return customerRepositoryImpl.get().isAddBillTooltipShowed()
            .asObservable()
            .firstOrError()
            .map { !it }
    }
}
