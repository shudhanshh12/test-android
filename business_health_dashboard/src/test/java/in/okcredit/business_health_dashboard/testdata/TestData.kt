package `in`.okcredit.business_health_dashboard.testdata

import `in`.okcredit.business_health_dashboard.contract.model.BusinessHealthDashboardModel
import `in`.okcredit.business_health_dashboard.contract.model.Feedback
import `in`.okcredit.business_health_dashboard.contract.model.Metric
import `in`.okcredit.business_health_dashboard.contract.model.TimeCadence
import `in`.okcredit.business_health_dashboard.contract.model.Trend

object TestData {
    private val trend1 = Trend(
        id = "1",
        iconUrl = "abc.com",
        title = "Outstanding - 103% low",
        description = "12 Jul: ₹175989\n13 Jul: ₹6080",
        feedback = Feedback(isVisible = true, description = "Was this useful?", response = "abc")
    )
    private val trend2 = Trend(
        id = "2",
        iconUrl = "abc.com",
        title = "Collected - 93% low",
        description = "12 Jul: ₹178993\n13 Jul: ₹13000",
        feedback = Feedback(isVisible = true, description = "Was this useful?", response = "abc")
    )
    private val trendList = listOf<Trend>(trend1, trend2)
    val cadence1 = TimeCadence(
        title = "Yesterday : 12 Jul",
        totalBalanceMetric = Metric("TOTAL LEDGER BALANCE", 15000),
        paymentMetric = Metric("PAYMENT", 200000),
        creditMetric = Metric("CREDIT", 215000),
        trendsSectionTitle = "Comparison",
        trendList = trendList
    )
    val cadence2 = TimeCadence(
        title = "Last 7 Days : 06 Jul - 13 Jul",
        totalBalanceMetric = Metric("Total Ledger Balance", 120000),
        paymentMetric = Metric("Payment", 300000),
        creditMetric = Metric("Credit", 575000),
        trendsSectionTitle = "Comparison",
        trendList = trendList
    )
    private val timeCadenceList = listOf<TimeCadence>(cadence1, cadence2)

    val BUSINESS_HEALTH_DASHBOARD_MODEL = BusinessHealthDashboardModel(
        lastUpdatedAtText = "Details last updated on 13 Jul, 11:59PM.",
        timeCadenceList = timeCadenceList,
        selectedTimeCadence = cadence1
    )
}
