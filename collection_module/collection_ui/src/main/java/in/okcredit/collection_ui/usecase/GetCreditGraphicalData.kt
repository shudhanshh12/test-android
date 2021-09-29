package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.backend._offline.database.TransactionRepo
import `in`.okcredit.collection.contract.CreditGraphicalDataProvider
import `in`.okcredit.collection.contract.CreditGraphicalDataProvider.GraphDuration
import `in`.okcredit.collection.contract.CreditGraphicalDataProvider.GraphResponse
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.accounting.model.Transaction
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetCreditGraphicalData @Inject constructor(
    private val transactionRepo: TransactionRepo,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : CreditGraphicalDataProvider {

    override fun execute(graphDuration: GraphDuration): Observable<GraphResponse> {
        val startTimeInSeconds: Long
        val endTimeInSeconds: Long

        when (graphDuration) {
            GraphDuration.MONTH -> {
                startTimeInSeconds =
                    TimeUnit.MILLISECONDS.toSeconds(DateTime().withTimeAtStartOfDay().minusDays(27).millis)
                endTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(DateTime().millis)
            }
            GraphDuration.WEEK -> {
                startTimeInSeconds =
                    TimeUnit.MILLISECONDS.toSeconds(DateTime().withTimeAtStartOfDay().minusDays(6).millis)
                endTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(DateTime().millis)
            }
            GraphDuration.YESTERDAY -> {

                startTimeInSeconds =
                    TimeUnit.MILLISECONDS.toSeconds(DateTime().withTimeAtStartOfDay().minusDays(1).millis)
                endTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(DateTime().withTimeAtStartOfDay().millis)
            }
            else -> {

                startTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(DateTime().withTimeAtStartOfDay().millis)
                endTimeInSeconds = TimeUnit.MILLISECONDS.toSeconds(DateTime().plusDays(1).withTimeAtStartOfDay().millis)
            }
        }

        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            transactionRepo.listTransactionsBetweenBillDate(startTimeInSeconds, endTimeInSeconds, businessId)
                .map { transactions ->
                    when (graphDuration) {
                        GraphDuration.MONTH -> {
                            return@map getInsightForMonth(transactions)
                        }
                        GraphDuration.WEEK -> {
                            return@map getInsightForWeek(transactions)
                        }
                        GraphDuration.YESTERDAY -> {
                            return@map getInsightForSelectedDay(transactions, false)
                        }
                        GraphDuration.TODAY -> {
                            return@map getInsightForSelectedDay(transactions, true)
                        }
                    }
                }
        }
    }

    private fun getInsightForSelectedDay(transactionList: List<Transaction>, today: Boolean): GraphResponse {
        val barGroup: MutableList<BarEntry> = mutableListOf()

        val (onlineCollection, offlineCollection, givenCreditAmount) = getInsightsData(transactionList)
        barGroup.add(BarEntry(0F, (givenCreditAmount / 100).toFloat()))

        if (today) {
            val year = DateTime().year()
            val day = DateTime().dayOfWeek()
            val month = DateTime().monthOfYear()
            val date = DateTime().dayOfMonth()
            val selectedDateRange = StringBuilder(month.asText.substring(0..2))
                .append(" ${date.asText}")
                .append(", ${year.asText}")
                .toString()

            return GraphResponse(
                BarDataSet(barGroup, ""), mutableListOf(day.asText.substring(0..2)),
                offlineCollection, givenCreditAmount, onlineCollection, selectedDateRange, GraphDuration.TODAY
            )
        } else {
            val year = DateTime().minusDays(1).year()
            val day = DateTime().minusDays(1).dayOfWeek()
            val month = DateTime().minusDays(1).monthOfYear()
            val date = DateTime().minusDays(1).dayOfMonth()
            val selectedDateRange = StringBuilder(month.asText.substring(0..2))
                .append(" ${date.asText}")
                .append(", ${year.asText}")
                .toString()

            return GraphResponse(
                BarDataSet(barGroup, ""), mutableListOf(day.asText.substring(0..2)),
                offlineCollection, givenCreditAmount, onlineCollection, selectedDateRange, GraphDuration.YESTERDAY
            )
        }
    }

    private fun getInsightForWeek(transactionList: List<Transaction>): GraphResponse {
        val barGroup: MutableList<BarEntry> = mutableListOf()
        val labelNames = mutableListOf<String>()

        var barIndex = 0f
        for (i in 7 downTo 1) {
            val start = DateTime().minusDays(i).millis / 1000
            val end = DateTime().minusDays(i - 1).millis / 1000

            labelNames.add(DateTime().minusDays(i - 1).dayOfWeek().asText.substring(0..2))

            val creditTxnsList = transactionList.filter { transaction -> transaction.type == Transaction.CREDIT }
            val filteredList = creditTxnsList.filter { it.billDate.millis / 1000 in (start + 1)..end }

            var creditPerDay = 0F
            for (txnPerDay in filteredList) {
                creditPerDay += txnPerDay.amountV2 / 100
            }

            barGroup.add(BarEntry(barIndex++, creditPerDay))
        }

        val (onlineCollection, offlineCollection, givenCreditAmount) = getInsightsData(transactionList)

        val selectedDateRange = getSelectedDateRange(6)

        return GraphResponse(
            BarDataSet(barGroup, ""),
            labelNames,
            offlineCollection,
            givenCreditAmount,
            onlineCollection,
            selectedDateRange.toString(),
            GraphDuration.WEEK
        )
    }

    private fun getInsightForMonth(transactionList: List<Transaction>): GraphResponse {
        val barGroup: MutableList<BarEntry> = mutableListOf()
        val labelNames = mutableListOf<String>()

        var barIndex = 0f
        for (i in 4 downTo 1) {

            val startDate = 7 * i
            val endDate = 7 * (i - 1)

            val startDatelabel = DateTime().withTimeAtStartOfDay().minusDays(startDate - 1).dayOfMonth().asText
            val endDatelabel = DateTime().withTimeAtStartOfDay().minusDays(endDate).dayOfMonth().asText
            labelNames.add(StringBuilder(startDatelabel).append("-").append(endDatelabel).toString())

            val start = DateTime().minusDays(startDate).millis / 1000
            val end = DateTime().minusDays(endDate).millis / 1000

            val creditTxnsList =
                transactionList.filter { transaction -> transaction.isDeleted.not() && transaction.type == Transaction.CREDIT }
            val filteredList = creditTxnsList.filter { it.billDate.millis / 1000 in (start + 1)..end }

            var creditsPerWeek = 0F
            for (txnsPerWeek in filteredList) {
                creditsPerWeek += txnsPerWeek.amountV2 / 100
            }

            barGroup.add(BarEntry(barIndex++, creditsPerWeek))
        }

        val (onlineCollection, offlineCollection, givenCreditAmount) = getInsightsData(transactionList)
        val selectedDateRange = getSelectedDateRange(27)

        return GraphResponse(
            BarDataSet(barGroup, ""), labelNames,
            offlineCollection, givenCreditAmount, onlineCollection, selectedDateRange.toString(), GraphDuration.MONTH
        )
    }

    private fun getSelectedDateRange(minusDays: Int): StringBuilder {
        val month1 = DateTime().minusDays(minusDays).monthOfYear()
        val month2 = DateTime().monthOfYear()
        val date1 = DateTime().minusDays(minusDays).dayOfMonth()
        val date2 = DateTime().dayOfMonth()
        val selectedDateRange = StringBuilder()
        if (month1 != month2) {
            selectedDateRange.append(month1.asShortText).append(" ").append(date1.asShortText).append(" - ")
                .append(month2.asShortText).append(" ").append(date2.asShortText)
        } else {
            selectedDateRange.append(month1.asShortText).append(" ")
                .append(date1.asShortText).append(" - ").append(date2.asShortText)
        }
        return selectedDateRange
    }

    private fun getInsightsData(transactionList: List<Transaction>): Triple<Long, Long, Long> {
        var onlineCollection = 0L
        var offlineCollection = 0L
        var givenCreditAmount = 0L

        transactionList.forEach { transaction ->
            if (transaction.isOnlinePaymentTransaction) {
                onlineCollection += transaction.amountV2
            } else if (transaction.type == Transaction.CREDIT) {
                givenCreditAmount += transaction.amountV2
            } else if ((transaction.type == Transaction.PAYMENT || transaction.type == Transaction.RETURN) && transaction.transactionCategory != Transaction.DISCOUNT) {
                offlineCollection += transaction.amountV2
            }
        }
        return Triple(onlineCollection, offlineCollection, givenCreditAmount)
    }
}
