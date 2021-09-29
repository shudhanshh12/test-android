package merchant.okcredit.user_stories.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.user_stories.contract.UserStoryRepository
import merchant.okcredit.user_stories.contract.model.HomeStories
import merchant.okcredit.user_stories.contract.model.MyStoryHome
import merchant.okcredit.user_stories.contract.model.StoriesConstants
import merchant.okcredit.user_stories.contract.model.UserStories
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils

class GetHomeUserStoriesTest {

    private val userStoryRepository: UserStoryRepository = mockk()
    private val userStoryEnabled: UserStoryEnabled = mockk()
    private val getActiveBusinessId: GetActiveBusinessId = mockk()

    private val getHomeUserStories = GetHomeUserStories({ userStoryRepository }, { userStoryEnabled }, { getActiveBusinessId })

    companion object {

        val myStory = MyStoryHome(
            "https://external-preview.redd.it/sZ9p13maoqlt83X-TK6EgqOexM2rsI1BtUnmnaVTcsw.png?auto=webp&s=23e92e941d1630d02028f6b9d79d7c371b587c01",
            true,
            "7766513131234",
            false
        )

        val userStoryList = listOf<UserStories>(
            UserStories(
                id = "usesada-2231313n3144",
                type = StoriesConstants.RELATIONSHIP_KNOWN,
                leastUnseenImageUrl = "https://avante.biz/wp-content/uploads/Man-u-mobile-wallpaper/Man-u-mobile-wallpaper5.jpg",
                totalSeen = 5,
                totalStories = 10,
                name = "Test Customer",
                recentCreatedAt = "1213131412152",
                storyId = "dkdfl24729471739",
                allViewed = 0,
                relationship = "customer",
                storyType = "Static"
            ),
            UserStories(
                id = "usesada-2231313n3145",
                type = StoriesConstants.RELATIONSHIP_UNKNOWN,
                leastUnseenImageUrl = "https://avante.biz/wp-content/uploads/Man-u-mobile-wallpaper/Man-u-mobile-wallpaper5.jpg",
                totalSeen = 5,
                totalStories = 10,
                name = "Test User",
                recentCreatedAt = "1213131412152",
                storyId = "rkdfl247294717394",
                allViewed = 1,
                relationship = "unknown",
                storyType = "Static"
            ),
            UserStories(
                id = "usesada-2231313n31435",
                type = StoriesConstants.HANDLE_VENDOR,
                leastUnseenImageUrl = "https://avante.biz/wp-content/uploads/Man-u-mobile-wallpaper/Man-u-mobile-wallpaper5.jpg",
                totalSeen = 5,
                totalStories = 10,
                name = "Test User",
                recentCreatedAt = "1213131412152",
                storyId = "tkdfl2472947173394",
                allViewed = 1,
                relationship = "vendor",
                storyType = "Static"
            )
        )

        val homeStories = HomeStories(
            userStories = userStoryList,
            isMyStoryAdded = myStory.isMyStoryAdded,
            isAllSynced = myStory.allSynced,
            lastStoryUrl = myStory.latestImageUrl
        )
        val businessId = "business-id"
    }

    @Before
    fun setUp() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        every { ThreadUtils.database() } returns Schedulers.trampoline()
    }

    @Test
    fun `execute when user story disable `() {
        val response: GetHomeUserStories.Response = GetHomeUserStories.Response(false, null)

        every { (userStoryEnabled.execute()) } returns (Observable.just(false))
        every { (getActiveBusinessId.execute()) } returns (Single.just(businessId))
        val testObservable = getHomeUserStories.execute(Unit).test()

        testObservable.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(response)
        )

        verify(exactly = 1) { userStoryEnabled.execute() }
        verify(exactly = 0) { userStoryRepository.getGroupUserStories(businessId) }
        verify(exactly = 0) { userStoryRepository.getMyStoryHome(businessId) }

        testObservable.dispose()
    }

    @Test
    fun `execute when user story enable `() {
        val response: GetHomeUserStories.Response = GetHomeUserStories.Response(true, homeStories)

        every { (getActiveBusinessId.execute()) } returns (Single.just(businessId))
        every { (userStoryEnabled.execute()) } returns (Observable.just(true))
        every { (userStoryRepository.getMyStoryHome(businessId)) } returns (Observable.just(listOf(myStory)))
        every { (userStoryRepository.getGroupUserStories(businessId)) } returns (Observable.just(userStoryList))

        val testObservable = getHomeUserStories.execute(Unit).test()

        testObservable.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(response)
        )

        verify(exactly = 1) { userStoryEnabled.execute() }
        verify(exactly = 1) { userStoryRepository.getGroupUserStories(businessId) }
        verify(exactly = 1) { userStoryRepository.getMyStoryHome(businessId) }

        testObservable.dispose()
    }
}
