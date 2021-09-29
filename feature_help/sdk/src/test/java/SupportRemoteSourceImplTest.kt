import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.userSupport.SupportRemoteSource
import tech.okcredit.userSupport.model.UserSuccessFeedBackRequest
import tech.okcredit.userSupport.server.ApiClient
import tech.okcredit.userSupport.server.SupportRemoteSourceImpl

class SupportRemoteSourceImplTest {
    private val apiClient: ApiClient = mock()
    private val remoteSource: SupportRemoteSource = SupportRemoteSourceImpl { apiClient }

    @Before
    fun setup() {
        mockkStatic(Response::class)
        mockkStatic(SupportRemoteSourceImpl::class)
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

//    @Test
//    fun `get help on success`() {
//        val testMerchantId = "merchant_id"
//        val testLanguage = "language"
//        val testResponse: HelpApiResponse = mock()
//        whenever(apiClient.getHelp(testMerchantId, testLanguage)).thenReturn(Single.just(testResponse))
//
//        val testObserver = server.getHelp(testMerchantId, testLanguage).test()
//
//        testObserver.assertValue(testResponse)
//        verify(apiClient, times(1)).getHelp(testMerchantId, testLanguage)
//    }

    @Test
    fun `submitfeedback function`() {
        // given
        val businessId = "business-id"
        val testRequest = UserSuccessFeedBackRequest(
            businessId, "Message", "feedback_type"
        )

        whenever(apiClient.submitFeedback(testRequest, businessId)).thenReturn(Completable.complete())

        // when
        val testObserver =
            remoteSource.submitFeedback(testRequest.message, testRequest.feedback_type, businessId).test()

        // then
        testObserver.assertComplete()
    }
}
