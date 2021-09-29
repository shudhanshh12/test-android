package `in`.okcredit.frontend.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.backend._offline.usecase._sync_usecases.SyncCustomer
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import org.joda.time.DateTime
import javax.inject.Inject

class GetLiveSalesStatement @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val syncCustomer: SyncCustomer,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) :
    UseCase<String, MutableList<GetLiveSalesStatement.LiveSaleItemView>> {
    override fun execute(req: String): Observable<Result<MutableList<LiveSaleItemView>>> =
        UseCase.wrapObservable(
            getActiveBusinessId.get().execute().flatMapObservable { businessId ->
                syncCustomer.schedule(req, businessId).andThen(
                    transactionRepo.listTransactions(req, businessId)
                        .map { transactions ->
                            val sortedTransactions = transactions.sortedWith(nullsLast(compareBy { it.lastActivity() }))
                            var currentStartOfTheDate = DateTime(0)
                            val returnItemList = mutableListOf<LiveSaleItemView>()

                            sortedTransactions.map {
                                if (currentStartOfTheDate.withTimeAtStartOfDay() != it.createdAt.withTimeAtStartOfDay()) {
                                    currentStartOfTheDate = it.createdAt
                                    returnItemList.add(LiveSaleItemView(LiveSaleItemViewType.DATE_ITEM, null, it.createdAt))
                                }
                                returnItemList.add(LiveSaleItemView(LiveSaleItemViewType.TXN_ITEM, it, null))
                            }

                            returnItemList
                        }
                )
            }
        )

    data class LiveSaleItemView(
        val type: LiveSaleItemViewType,
        val transaction: merchant.okcredit.accounting.model.Transaction?,
        val date: DateTime?,
    )

    enum class LiveSaleItemViewType(val value: String) {
        TXN_ITEM("Txn"),
        DATE_ITEM("Date")
    }
}
