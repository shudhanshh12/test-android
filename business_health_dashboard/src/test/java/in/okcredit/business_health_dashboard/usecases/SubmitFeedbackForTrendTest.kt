package `in`.okcredit.business_health_dashboard.usecases

import `in`.okcredit.business_health_dashboard.repository.BusinessHealthDashboardRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.After
import org.junit.Before
import org.junit.Test

class SubmitFeedbackForTrendTest {

    private val businessHealthDashboardRepository: BusinessHealthDashboardRepository = mockk()
    private val getActiveBusinessId: GetActiveBusinessId = mockk()

    lateinit var submitFeedbackForTrend: SubmitFeedbackForTrendImpl

    @Before
    fun setUp() {

        submitFeedbackForTrend = SubmitFeedbackForTrendImpl(
            { businessHealthDashboardRepository },
            { getActiveBusinessId }
        )
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun execute() {
        val trendId = "dummy_trend_id"
        val feedbackResponse = "dummy_feedback"
        val businessId = "dummy_business_id"

        every { getActiveBusinessId.execute() } returns Single.just(businessId)
        every {
            businessHealthDashboardRepository.submitFeedbackForTrend(
                trendId, feedbackResponse, businessId
            )
        } returns Completable.complete()

        every { businessHealthDashboardRepository.fetchFromRemoteAndSaveToLocal(businessId) } returns Completable.complete()

        submitFeedbackForTrend.execute(trendId, feedbackResponse).test().apply {
            assertComplete()
            dispose()
        }
        verify(exactly = 1) {
            businessHealthDashboardRepository.submitFeedbackForTrend(
                trendId,
                feedbackResponse,
                businessId
            )
        }
        verify(exactly = 1) { businessHealthDashboardRepository.fetchFromRemoteAndSaveToLocal(businessId) }
    }
}
