package merchant.okcredit.user_stories

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.user_stories.store.UserStoriesLocalSource
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.android.base.workmanager.OkcWorkManager

class UserStoryRepositoryImplTest {

    private val workManager: OkcWorkManager = mockk()
    private val userStoryLocalSource: UserStoriesLocalSource = mockk()
    private val userStoriesRepository = UserStoryRepositoryImpl({ workManager }, { userStoryLocalSource })

    @Before
    fun setUp() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        every { ThreadUtils.database() } returns Schedulers.trampoline()
    }

    @Test
    fun `getGroupUserStories success`() {

        every { (userStoryLocalSource.getHomeUserStoryGroup(TestData.BUSINESS_ID)) } returns (Observable.just(TestData.userStoryList))

        val testObservable = userStoriesRepository.getGroupUserStories(TestData.BUSINESS_ID).test()
        testObservable.assertValue(TestData.userStoryList)

        verify { userStoryLocalSource.getHomeUserStoryGroup(TestData.BUSINESS_ID) }
    }

    @Test
    fun `getMyStoryHome success`() {

        every { (userStoryLocalSource.getMyStoryHome(TestData.BUSINESS_ID)) } returns (Observable.just(listOf(TestData.myStory)))

        val testObservable = userStoriesRepository.getMyStoryHome(TestData.BUSINESS_ID).test()
        testObservable.assertValue(listOf(TestData.myStory))

        verify { userStoryLocalSource.getMyStoryHome(TestData.BUSINESS_ID) }
    }
}
