package usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.workmanager.OkcWorkManager
import tech.okcredit.userSupport.SupportRemoteSource
import tech.okcredit.userSupport.usecses.SubmitFeedback

class SubmitFeedbackTest {
    private lateinit var submitFeedbackTest: SubmitFeedback
    private var remoteSource: SupportRemoteSource = mock()
    private var serverLazy = mockk<Lazy<SupportRemoteSource>>()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private var workManager: Lazy<OkcWorkManager> = mock()

    @Before
    fun setUp() {
        submitFeedbackTest = SubmitFeedback(serverLazy, workManager, { getActiveBusinessId })
        every { serverLazy.get() } returns remoteSource
    }

    @Test
    fun `execute test`() {
        // given
        val testRequestFeedback = "feedbackMessage"
        val testRequestIssueType = "issueType"
        val testRequestBusinessId = "businessId"
        whenever(getActiveBusinessId.thisOrActiveBusinessId(testRequestBusinessId))
            .thenReturn(Single.just(testRequestBusinessId))
        whenever(
            remoteSource.submitFeedback(
                testRequestFeedback,
                testRequestIssueType,
                testRequestBusinessId
            )
        ).thenReturn(
            Completable.complete()
        )

        // when
        submitFeedbackTest.execute(testRequestFeedback, testRequestIssueType, testRequestBusinessId).test()

        // then
        verify(getActiveBusinessId).thisOrActiveBusinessId(eq(testRequestBusinessId))
    }
}
