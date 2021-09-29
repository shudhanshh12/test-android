package `in`.okcredit.business_health_dashboard.contract.model

data class BusinessHealthDashboardModel(
    val lastUpdatedAtText: String,
    val timeCadenceList: List<TimeCadence>,
    val selectedTimeCadence: TimeCadence,
)

data class TimeCadence(
    val title: String,
    val totalBalanceMetric: Metric,
    val paymentMetric: Metric,
    val creditMetric: Metric,
    val trendsSectionTitle: String,
    val trendList: List<Trend>,
)

data class Metric(
    val title: String,
    val value: Long,
)

data class Trend(
    val id: String,
    val iconUrl: String,
    val title: String,
    val description: String,
    val feedback: Feedback
)

data class Feedback(
    val isVisible: Boolean,
    val description: String,
    val response: String // change this
)
