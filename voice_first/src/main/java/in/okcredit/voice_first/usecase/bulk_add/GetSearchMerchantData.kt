package `in`.okcredit.voice_first.usecase.bulk_add

import `in`.okcredit.backend.contract.GetFlyweightActiveCustomers
import `in`.okcredit.voice_first.data.bulk_add.entities.DraftMerchant
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_CUSTOMER
import `in`.okcredit.voice_first.data.bulk_add.entities.MERCHANT_TYPE_SUPPLIER
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import merchant.okcredit.suppliercredit.contract.GetFlyweightActiveSuppliers
import javax.inject.Inject

class GetSearchMerchantData @Inject constructor(
    private val getFlyweightActiveCustomers: Lazy<GetFlyweightActiveCustomers>,
    private val getFlyweightActiveSuppliers: Lazy<GetFlyweightActiveSuppliers>,
) {

    fun execute(searchQuery: String): Flow<Response> = combine(
        getFlyweightActiveCustomers.get().execute(),
        getFlyweightActiveSuppliers.get().execute(),
    ) { customers, suppliers ->

        val customerDrafts = customers
            .filter { searchQuery.isEmpty() || it.customerName.contains(searchQuery, ignoreCase = true) }
            .map {
                DraftMerchant(
                    merchantId = it.customerId,
                    merchantName = it.customerName,
                    merchantType = MERCHANT_TYPE_CUSTOMER,
                )
            }

        val supplierDrafts = suppliers
            .filter { searchQuery.isBlank() || it.supplierName.contains(searchQuery, ignoreCase = true) }
            .map {
                DraftMerchant(
                    merchantId = it.supplierId,
                    merchantName = it.supplierName,
                    merchantType = MERCHANT_TYPE_SUPPLIER,
                )
            }

        Response(customers = customerDrafts, suppliers = supplierDrafts)
    }

    data class Response(
        val customers: List<DraftMerchant>,
        val suppliers: List<DraftMerchant>,
    )
}
