package merchant.okcredit.suppliercredit.contract

import androidx.annotation.Keep
import kotlinx.coroutines.flow.Flow

interface GetFlyweightActiveSuppliers {

    fun execute(): Flow<List<FlyweightSupplier>>
}

@Keep
data class FlyweightSupplier(
    val supplierId: String,
    val supplierName: String,
)
