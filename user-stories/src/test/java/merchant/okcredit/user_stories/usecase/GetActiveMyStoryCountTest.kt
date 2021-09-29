package merchant.okcredit.user_stories.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Single
import merchant.okcredit.user_stories.TestData
import merchant.okcredit.user_stories.store.UserStoriesLocalSource
import org.junit.Test

class GetActiveMyStoryCountTest {

    private val userStoriesLocalSources: UserStoriesLocalSource = mockk()
    private val getActiveBusinessId: GetActiveBusinessId = mockk()
    private val getActiveMyStoryCount = GetActiveMyStoryCount(
        { userStoriesLocalSources },
        { getActiveBusinessId }
    )

    @Test
    fun `getActiveMyStoryCount Success`() {
        val response: Int = 0
        every {
            (
                userStoriesLocalSources.getActiveCountMyStory(
                    TestData.timestampLong,
                    TestData.BUSINESS_ID
                )
                )
        } returns (Single.just(response))
        every { getActiveBusinessId.execute() } returns (Single.just(TestData.BUSINESS_ID))
        val testObservable = getActiveMyStoryCount.execute(TestData.timestampLong).test()
        testObservable.assertValue(response)
        verify { userStoriesLocalSources.getActiveCountMyStory(TestData.timestampLong, TestData.BUSINESS_ID) }
        testObservable.dispose()
    }
}
