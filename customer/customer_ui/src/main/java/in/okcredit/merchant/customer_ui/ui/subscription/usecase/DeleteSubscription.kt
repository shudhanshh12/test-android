package `in`.okcredit.merchant.customer_ui.ui.subscription.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.data.server.model.response.Subscription
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class DeleteSubscription @Inject constructor(
    private val repositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(
        updatedSubscription: Subscription,
    ) = UseCase.wrapCompletable(
        rxCompletable {
            val businessId = getActiveBusinessId.get().execute().await()
            repositoryImpl.get().deleteSubscription(updatedSubscription, businessId)
        }
    )
}
