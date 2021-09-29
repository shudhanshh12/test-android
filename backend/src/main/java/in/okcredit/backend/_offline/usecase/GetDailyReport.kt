package `in`.okcredit.backend._offline.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetDailyReport @Inject constructor(
    private val transactionRepo: Lazy<TransactionRepo>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) {
    fun execute(): Observable<Response> {

        val startTimeInMilliSec =
            TimeUnit.MILLISECONDS.toSeconds(DateTimeUtils.currentDateTime().withTimeAtStartOfDay().millis)
        val endTimeInMilliSec =
            TimeUnit.MILLISECONDS.toSeconds(DateTimeUtils.currentDateTime().withTimeAtStartOfDay().plusDays(1).millis)

        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            transactionRepo.get().listTransactionsBetweenBillDate(startTimeInMilliSec, endTimeInMilliSec, businessId)
                .flatMap {
                    var netCreditAmount: Long = 0
                    var netPaymentAmount: Long = 0

                    it.forEach { transaction ->
                        if (transaction.type == Transaction.CREDIT) {
                            netCreditAmount += transaction.amountV2
                        } else if (transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN) {
                            netPaymentAmount += transaction.amountV2
                        }
                    }
                    val netBalance = netPaymentAmount - netCreditAmount

                    return@flatMap Observable.just(Response(netBalance, netCreditAmount, netPaymentAmount))
                }
        }
    }

    data class Response(
        val netBalance: Long,
        val netCreditAmount: Long,
        val netPaymentAmount: Long,
    )
}
