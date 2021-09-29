package `in`.okcredit.merchant.suppliercredit.use_case

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.rx2.await
import merchant.okcredit.suppliercredit.contract.FlyweightSupplier
import merchant.okcredit.suppliercredit.contract.GetFlyweightActiveSuppliers
import javax.inject.Inject

class GetFlyweightActiveSuppliersImpl @Inject constructor(
    private val getBusinessId: Lazy<GetActiveBusinessId>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
) : GetFlyweightActiveSuppliers {

    @ExperimentalCoroutinesApi
    override fun execute(): Flow<List<FlyweightSupplier>> {
        return flow { emit(getBusinessId.get().execute().await()) }
            .take(1)
            .flatMapLatest { supplierCreditRepository.get().listActiveSuppliersByFlyweight(it) }
            .map { list -> list.filter { it.supplierName.isNotBlank() } }
    }
}
