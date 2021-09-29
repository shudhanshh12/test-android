package tech.okcredit.use_case

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.bills.BillRepository
import tech.okcredit.bills.IGetAccountsTotalBills
import javax.inject.Inject

class GetAccountsTotalBills @Inject constructor(
    private val billRepository: Lazy<BillRepository>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : IGetAccountsTotalBills {

    override fun execute(accountId: String): Observable<IGetAccountsTotalBills.Response> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            billRepository.get().getUnreadBillCount(accountId, businessId).flatMap { unread ->
                billRepository.get().getTotalBillCount(accountId, businessId).map { total ->
                    IGetAccountsTotalBills.Response(total, unread)
                }
            }
        }
    }
}
