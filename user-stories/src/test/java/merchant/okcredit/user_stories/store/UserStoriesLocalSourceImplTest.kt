package merchant.okcredit.user_stories.store

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.user_stories.TestData
import merchant.okcredit.user_stories.store.database.UserStoriesDao
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils

class UserStoriesLocalSourceImplTest {
    private val userStoriesDao: UserStoriesDao = mockk()
    private lateinit var userStoriesLocalSource: UserStoriesLocalSourceImpl

    @Before
    fun setUp() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.database() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        userStoriesLocalSource = UserStoriesLocalSourceImpl(userStoriesDao)
    }

    @Test
    fun `saveMyStatus success`() {

        every { userStoriesDao.insertMyStory(TestData.myStatus) }.returns(Completable.complete())

        userStoriesLocalSource.saveMyStory(TestData.myStatus).test().assertComplete()
        verify { userStoriesDao.insertMyStory(TestData.myStatus) }
    }

    @Test
    fun `saveOthersStatus success`() {
        every { userStoriesDao.insertOtherStory(TestData.othersStatus) }.returns(Completable.complete())

        userStoriesLocalSource.saveOthersStory(TestData.othersStatus).test().assertComplete()
        verify { userStoriesDao.insertOtherStory(TestData.othersStatus) }
    }

    @Test
    fun `getLastMyStatus success`() {
        every { userStoriesDao.getLastSyncMyStoryTimestamp(TestData.BUSINESS_ID) }.returns(Single.just(TestData.timestamp))
        val testSingle = userStoriesLocalSource.getLastSyncTimeMyStory(TestData.BUSINESS_ID).test()
        testSingle.assertValue(TestData.timestamp)
        verify { userStoriesDao.getLastSyncMyStoryTimestamp(TestData.BUSINESS_ID) }
    }

    @Test
    fun `getLastOthersStatus success`() {
        every { userStoriesDao.getLastSyncOtherStoryTimestamp(TestData.BUSINESS_ID) }.returns(Single.just(TestData.timestamp))
        val testSingle = userStoriesLocalSource.getLastSyncTimeOthersStory(TestData.BUSINESS_ID).test()
        testSingle.assertValue(TestData.timestamp)
        verify { userStoriesDao.getLastSyncOtherStoryTimestamp(TestData.BUSINESS_ID) }
    }

    @Test
    fun `getHomeUserStoryGroup success`() {
        every { userStoriesDao.getDistinctOtherStoryGroup(TestData.BUSINESS_ID) }.returns(Observable.just(TestData.userStoryList))

        val testObservable = userStoriesLocalSource.getHomeUserStoryGroup(TestData.BUSINESS_ID).test()
        testObservable.assertValue(TestData.userStoryList)
        verify { userStoriesDao.getDistinctOtherStoryGroup(TestData.BUSINESS_ID) }
    }

    @Test
    fun `getHomeMyStory success`() {

        every { userStoriesDao.getDistinctMyStoryHome(TestData.BUSINESS_ID) }.returns(Observable.just(listOf(TestData.myStory)))

        val testObservable = userStoriesLocalSource.getMyStoryHome(TestData.BUSINESS_ID).test()
        testObservable.assertValue(listOf(TestData.myStory))
        verify { userStoriesDao.getDistinctMyStoryHome(TestData.BUSINESS_ID) }
    }

    @Test
    fun `getOthersStoryByRelation success`() {

        every {
            userStoriesDao.getOthersStoryByRelationShip(
                any(),
                any(),
                eq(TestData.BUSINESS_ID)
            )
        }.returns(Single.just(TestData.othersStatus))
        val testSingle = userStoriesLocalSource.getOthersStoryByRelationShip(TestData.BUSINESS_ID).test()

        testSingle.assertValue(TestData.othersStatus)
        verify { userStoriesDao.getOthersStoryByRelationShip(any(), any(), eq(TestData.BUSINESS_ID)) }
    }

    @Test
    fun `getUnSyncedStory success`() {
        every { userStoriesDao.getUnSyncedMyStory(TestData.BUSINESS_ID) }.returns(Single.just(TestData.myStatus))

        val testSingle = userStoriesLocalSource.getUnSyncedStory(TestData.BUSINESS_ID).test()
        testSingle.assertValue(TestData.myStatus)

        verify { userStoriesDao.getUnSyncedMyStory(TestData.BUSINESS_ID) }
        testSingle.dispose()
    }

    @Test
    fun `getActiveCountMyStory success`() {
        val response = 5
        every { userStoriesDao.getActiveCountMyStory(5, TestData.BUSINESS_ID) }.returns(Single.just(response))

        val testSingle = userStoriesLocalSource.getActiveCountMyStory(5, TestData.BUSINESS_ID).test()
        testSingle.assertValue(response)

        verify { userStoriesDao.getActiveCountMyStory(5, TestData.BUSINESS_ID) }
        testSingle.dispose()
    }
}
