package merchant.okcredit.user_stories.server

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.user_stories.TestData
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import tech.okcredit.android.base.utils.ThreadUtils

class UserStoriesRemoteSourceImplTest {
    private val apiClient: UserStoriesApiClient = mockk()
    private val userStoriesRemoteSource = UserStoriesRemoteSourceImpl { apiClient }

    companion object {
        val requestData = UserStoriesApiMessage.AddMyStatus(
            request_id = "asdf-12345",
            caption = "test caption ",
            updatedAt = 324123131231L,
            media_url = "/storage/emulated/0/Android/data/in.okcredit.merchant.debug/files/1614260434008pic.jpg"
        )

        private val myStatus = UserStoriesApiMessage.MyStory(
            request_id = "asdf-12345",
            status_id = "asdd-asdf-12345",
            media_type = "image",
            caption = "test caption ",
            created_at = "324123131231",
            deleted = false,
            urls = UserStoriesApiMessage.Urls(
                "https://ik.imagekit.io/wyvmhe0uzv5/tr:w-256,h-256/6037b3a662a52c7f762e74a4.jpg?ik-t=1614868006&ik-s=af61509e8c3835009afc3d2d6c1546c3c82446db",
                "testurl2.png"
            )
        )

        val apiResponse = UserStoriesApiMessage.UserStatusResponse(myStatus)
    }

    @Before
    fun setUp() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `getMyStatus when api call successful then return response body`() {

        every {
            (
                apiClient.getMyStory(
                    0L,
                    TestData.BUSINESS_ID
                )
                )
        } returns (Single.just(Response.success(TestData.myStoryTestModel)))

        val testObserver = userStoriesRemoteSource.getMyStory(0L, TestData.BUSINESS_ID).test()

        testObserver.assertValue(TestData.myStoryTestModel.response)
        verify { apiClient.getMyStory(0L, TestData.BUSINESS_ID) }
    }

    @Test
    fun `getOthersStatus when api call successful then return response body`() {

        every {
            apiClient.getOtherStory(
                0L,
                TestData.BUSINESS_ID
            )
        } returns (Single.just(Response.success(TestData.otherStoryResponse)))

        val testObserver = userStoriesRemoteSource.getOthersStory(0L, TestData.BUSINESS_ID).test()

        testObserver.assertValue(TestData.otherStoryResponse.response)
        verify { apiClient.getOtherStory(0L, TestData.BUSINESS_ID) }
    }

    @Test
    fun `addUserStory success`() {
        every {
            (
                apiClient.postStory(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
                )
        } returns (Single.just(Response.success(apiResponse)))

        val testSingle = userStoriesRemoteSource.postStory(requestData, TestData.BUSINESS_ID).test()
        testSingle.assertValue(apiResponse.response)

        verify {
            apiClient.postStory(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }
}
