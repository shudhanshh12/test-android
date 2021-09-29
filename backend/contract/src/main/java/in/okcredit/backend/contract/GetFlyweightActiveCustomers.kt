package `in`.okcredit.backend.contract

import androidx.annotation.Keep
import kotlinx.coroutines.flow.Flow

interface GetFlyweightActiveCustomers {

    fun execute(): Flow<List<FlyweightCustomer>>
}

@Keep
data class FlyweightCustomer(
    val customerId: String,
    val customerName: String,
)
