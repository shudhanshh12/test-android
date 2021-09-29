package `in`.okcredit.business_health_dashboard.contract.model.usecases

import io.reactivex.Completable

interface SubmitFeedbackForTrend {
    val POSITIVE_FEEDBACK_RESPONSE_STRING: String
    val NEGATIVE_FEEDBACK_RESPONSE_STRING: String
    fun execute(trendId: String, response: String): Completable
}
