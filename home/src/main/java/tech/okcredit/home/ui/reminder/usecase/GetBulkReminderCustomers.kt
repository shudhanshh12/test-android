package tech.okcredit.home.ui.reminder.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import javax.inject.Inject

class GetBulkReminderCustomers @Inject constructor(
    private val repository: Lazy<CustomerRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute() = getActiveBusinessId.get().execute().flatMapObservable { businessId ->
        repository.get().getDefaultersList(businessId)
    }
}
