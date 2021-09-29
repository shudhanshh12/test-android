package `in`.okcredit.merchant.customer_ui.ui.subscription.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.customer_ui.data.CustomerRepositoryImpl
import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class AddSubscription @Inject constructor(
    private val repositoryImpl: Lazy<CustomerRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(
        customerId: String,
        amount: Long,
        name: String,
        frequency: SubscriptionFrequency,
        startDate: Long? = null, // only needed if frequency is Monthly
        days: List<DayOfWeek>? = null, // only needed if frequency is Weekly
    ) = UseCase.wrapSingle(
        rxSingle {
            val businessId = getActiveBusinessId.get().execute().await()
            repositoryImpl.get().addSubscription(
                customerId = customerId,
                amount = amount,
                name = name,
                frequency = frequency,
                startDate = startDate,
                days = days?.map { it.value },
                businessId = businessId,
            )
        }
    )
}
