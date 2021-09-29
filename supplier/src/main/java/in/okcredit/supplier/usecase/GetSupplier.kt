package `in`.okcredit.supplier.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.GetSupplier
import `in`.okcredit.merchant.suppliercredit.Supplier
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import javax.inject.Inject

/**
 *  This class is used to fetch supplier
 */

class GetSupplier @Inject constructor(
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<String, Supplier>, GetSupplier {

    override fun execute(req: String): Observable<Result<Supplier>> {
        return UseCase.wrapObservable(
            executeObservable(req)
        )
    }

    override fun executeObservable(req: String): Observable<Supplier> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            supplierCreditRepository.get().getSupplier(req, businessId)
        }
    }
}
