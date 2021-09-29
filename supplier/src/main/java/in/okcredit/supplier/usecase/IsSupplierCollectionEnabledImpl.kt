package `in`.okcredit.supplier.usecase

import dagger.Lazy
import merchant.okcredit.supplier.contract.IsSupplierCollectionEnabled
import tech.okcredit.android.ab.AbRepository
import javax.inject.Inject

class IsSupplierCollectionEnabledImpl @Inject constructor(
    private val ab: Lazy<AbRepository>
) : IsSupplierCollectionEnabled {

    override fun execute() = ab.get().isFeatureEnabled("supplier_collection")
}
