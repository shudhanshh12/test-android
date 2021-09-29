package `in`.okcredit.frontend.usecase.supplier

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import dagger.Lazy
import io.reactivex.Completable
import tech.okcredit.android.auth.usecases.VerifyPassword
import javax.inject.Inject

/**
 *  This class is used to fetch transaction details of supplier
 */

class DeleteSupplierTransaction @Inject constructor(
    private val verifyPassword: Lazy<VerifyPassword>,
    private val supplierCreditRepository: Lazy<SupplierCreditRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {

    fun execute(txnId: String): Completable {
        return getActiveBusinessId.get().execute().flatMapCompletable { businessId ->
            supplierCreditRepository.get().deleteTransaction(txnId, businessId)
        }
    }
}
