package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.backend.contract.FlyweightCustomer
import `in`.okcredit.backend.contract.GetFlyweightActiveCustomers
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject

class GetFlyweightActiveCustomersImpl @Inject constructor(
    private val getBusinessId: Lazy<GetActiveBusinessId>,
    private val customerRepo: Lazy<CustomerRepo>,
) : GetFlyweightActiveCustomers {

    override fun execute(): Flow<List<FlyweightCustomer>> {
        return getBusinessId.get().execute()
            .flatMapObservable { customerRepo.get().listActiveCustomers(it) }
            .asFlow().map { list ->
                list
                    .filter { it.description.isNotBlank() }
                    .map { FlyweightCustomer(it.id, it.description) }
            }
    }
}
