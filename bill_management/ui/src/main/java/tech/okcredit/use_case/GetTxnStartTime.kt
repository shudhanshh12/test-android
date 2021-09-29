package tech.okcredit.use_case

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.SupplierCreditRepository
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import javax.inject.Inject

class GetTxnStartTime @Inject constructor(
    private val supplierCreditRepository: SupplierCreditRepository,
    private val customerRepo: CustomerRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : UseCase<GetTxnStartTime.Request, Long> {
    override fun execute(req: Request): Observable<Result<Long>> {

        return UseCase.wrapObservable(

            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                if (req.role == BILL_INTENT_EXTRAS.CUSTOMER) {
                    customerRepo.getCustomer(req.accountId, businessId).map {
                        return@map it.txnStartTime?.times(1000) ?: 0
                    }
                } else {
                    supplierCreditRepository.getSupplier(req.accountId, businessId).map {
                        return@map it.txnStartTime.times(1000)
                    }
                }
            }
        )
    }

    data class Request(val accountId: String, val role: String)
}
