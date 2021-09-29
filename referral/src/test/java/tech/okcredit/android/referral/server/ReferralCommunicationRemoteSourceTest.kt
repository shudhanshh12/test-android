package tech.okcredit.android.referral.server

import `in`.okcredit.referral.contract.models.ReferralApiMessages
import `in`.okcredit.referral.contract.models.ReferralInfo
import `in`.okcredit.referral.contract.models.TargetedUser
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import tech.okcredit.android.TestData
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.referral.data.GetReferralLinkResponse
import tech.okcredit.android.referral.data.JourneyQualificationRequest
import tech.okcredit.android.referral.data.JourneyQualificationResponse
import tech.okcredit.android.referral.data.ReferralApiService
import tech.okcredit.android.referral.ui.rewards_on_signup.model.GetReferralTargetsApiRequest
import tech.okcredit.android.referral.ui.rewards_on_signup.model.GetReferralTargetsApiResponse
import tech.okcredit.base.network.ApiError

class ReferralCommunicationRemoteSourceTest {

    private val referralApi: ReferralApiService = mock()
    private val schedulerProvider: SchedulerProvider = mock()
    private val server =
        ReferralServer({ referralApi }, { schedulerProvider })

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        whenever(schedulerProvider.io()).thenReturn(Schedulers.trampoline())
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `should return targetedUsers returned by the API`() {
        runBlocking {
            whenever(referralApi.getTargetedUsers(TestData.BUSINESS_ID)).thenReturn(
                ReferralApiMessages.GetTargetedUsersResponse(TestData.TARGETED_USERS)
            )

            val result = server.getTargetedUsers(TestData.BUSINESS_ID)

            assertThat(result).isEqualTo(TestData.TARGETED_USERS)
        }
    }

    @Test
    fun `should pass "null" and return share content response returned by the API when getShareContent is called without any parameter`() {
        runBlocking {
            val response = ReferralApiMessages.GetShareContentResponse(null, TestData.GENERIC_CONTENT)
            whenever(referralApi.getShareContent("null", TestData.BUSINESS_ID)).thenReturn(response)

            val result = server.getShareContent(businessId = TestData.BUSINESS_ID)

            assertThat(result).isEqualTo(response)
        }
    }

    @Test
    fun `should pass "null" and return share content response returned by the API when getShareContent is called with empty targetedUserId`() {
        runBlocking {
            val response = ReferralApiMessages.GetShareContentResponse(null, TestData.GENERIC_CONTENT)
            whenever(referralApi.getShareContent("null", TestData.BUSINESS_ID)).thenReturn(response)

            val result = server.getShareContent("", TestData.BUSINESS_ID)

            assertThat(result).isEqualTo(response)
        }
    }

    @Test
    fun `should pass targetedUserId and return share content response returned by the API when getShareContent is called with targetedUserId`() {
        runBlocking {
            whenever(
                referralApi.getShareContent(
                    "targetedUserId",
                    TestData.BUSINESS_ID
                )
            ).thenReturn(TestData.SHARE_CONTENT_RESPONSE)

            val result = server.getShareContent("targetedUserId", TestData.BUSINESS_ID)

            assertThat(result).isEqualTo(TestData.SHARE_CONTENT_RESPONSE)
        }
    }

    @Test
    fun `should return 1 when api response has qualified 1`() {
        val businessId = "businessId"
        whenever(referralApi.checkJourneyQualification(JourneyQualificationRequest(businessId), businessId))
            .thenReturn(Single.just(JourneyQualificationResponse(qualified = 1)))

        val testObserver = server.checkJourneyQualification(businessId).test()

        testObserver.assertValue(1)
        testObserver.dispose()
    }

    @Test
    fun `should return referral info returned by the api`() {
        val businessId = "business-id"
        val referralInfo = ReferralInfo(100, 500, 1)
        whenever(referralApi.getProfile(businessId, businessId)).thenReturn(Single.just(referralInfo))

        val testObserver = server.getReferralInfo(businessId).test()

        testObserver.assertValue(referralInfo)
        testObserver.dispose()
    }

    @Test
    fun `should return default referral info returned when api call fails`() {
        val businessId = "business-id"
        whenever(referralApi.getProfile(businessId, businessId)).thenReturn(
            Single.error(
                HttpException(
                    Response.error<ApiError>(
                        501, "Internal Server Error".toResponseBody("text/plain".toMediaType())
                    )
                )
            )
        )

        val testObserver = server.getReferralInfo(businessId).test()

        testObserver.assertValue(ReferralInfo(5000, 25000, 0))
        testObserver.dispose()
    }

    @Test
    fun `should return users return by the api`() {
        runBlocking {
            val response = ReferralApiMessages.GetTargetedUsersResponse(
                listOf(
                    TargetedUser(
                        "id",
                        "Kim Jon Un",
                        "5678900000",
                        "imageUrl",
                        "source"
                    )
                )
            )
            whenever(referralApi.getTargetedUsers(TestData.BUSINESS_ID)).thenReturn(response)

            val result = server.getTargetedUsers(TestData.BUSINESS_ID)

            assertThat(result).isEqualTo(response.targetUsers)
            verify(referralApi, times(1)).getTargetedUsers(TestData.BUSINESS_ID)
        }
    }

    @Test
    fun `should return getReferralTarget returned by the API`() {
        runBlocking {
            val businessId = "business-id"
            val request = GetReferralTargetsApiRequest(merchantId = businessId)
            whenever(referralApi.getReferralTarget(request, businessId)).thenReturn(
                GetReferralTargetsApiResponse(rewards = TestData.listOfReferralTargets)
            )

            val result = server.getReferralTarget(businessId)

            assertThat(result).isEqualTo(TestData.listOfReferralTargets)
        }
    }

    @Test
    fun `should return getReferralLink returned by the api`() {
        val testReferralString = "https://okcredit.in/r/rxdysdd3d"
        val testResponse = Response.success(
            GetReferralLinkResponse(referralLink = testReferralString)
        )
        whenever(referralApi.getReferralLink(TestData.BUSINESS_ID)).thenReturn(
            Single.just(testResponse)
        )
        val testObserver = server.getReferralLink(TestData.BUSINESS_ID).test()

        testObserver.assertValue(testResponse.body()?.referralLink)

        testObserver.dispose()
    }

    @Test
    fun `should return default ReferralLink if api fails`() {

        val fakeError = HttpException(
            Response.error<ApiError>(
                501, "Internal Server Error".toResponseBody("text/plain".toMediaType())
            )
        )
        whenever(referralApi.getReferralLink(TestData.BUSINESS_ID)).thenReturn(
            Single.error(fakeError)
        )
        val testObserver = server.getReferralLink(TestData.BUSINESS_ID).test()

        testObserver.assertError(fakeError)

        testObserver.dispose()
    }
}
