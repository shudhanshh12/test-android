package `in`.okcredit.merchant.customer_ui.ui.subscription.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class ListSubscriptions @Inject constructor(
    private val repositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(customerId: String?) =
        UseCase.wrapSingle(
            rxSingle {
                val businessId = getActiveBusinessId.get().execute().await()
                repositoryImpl.get().listSubscriptions(customerId, businessId)
            }
        )
}
