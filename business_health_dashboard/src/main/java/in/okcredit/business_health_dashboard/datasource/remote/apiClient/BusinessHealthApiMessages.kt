package `in`.okcredit.business_health_dashboard.datasource.remote.apiClient

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BusinessHealthDashboardModelDto(
    @Json(name = "dashboard")
    val dashboardData: DashboardData,
)

@JsonClass(generateAdapter = true)
data class DashboardData(
    @Json(name = "last_updated_at")
    val lastUpdatedAt: String,

    @Json(name = "default_time_range")
    val defaultTimeCadenceString: String,

    @Json(name = "time_filter_values")
    val timeCadenceList: List<TimeCadence>
)

@JsonClass(generateAdapter = true)
data class TimeCadence(
    @Json(name = "cadence")
    val title: String,

    @Json(name = "metrics")
    val metricsList: List<Metric>,

    @Json(name = "trends")
    val trends: Trends
)

@JsonClass(generateAdapter = true)
data class Metric(
    @Json(name = "title")
    val title: String,

    @Json(name = "value")
    val value: Long,
)

@JsonClass(generateAdapter = true)
data class Trends(
    @Json(name = "title")
    val title: String,

    @Json(name = "trend_list")
    val trendList: List<Trend>,
)

@JsonClass(generateAdapter = true)
data class Trend(
    @Json(name = "id")
    val id: String,

    @Json(name = "icon_url")
    val iconUrl: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "description")
    val description: String,

    @Json(name = "feedback")
    val feedback: Feedback
)

@JsonClass(generateAdapter = true)
data class Feedback(
    @Json(name = "is_visible")
    val isVisible: Boolean,

    @Json(name = "description")
    val description: String,

    @Json(name = "response")
    val response: String
)

// /////////////////////////////

@JsonClass(generateAdapter = true)
data class TrendFeedbackRequest(
    @Json(name = "insight_id")
    val insightId: String,
    @Json(name = "response")
    val response: String
)
