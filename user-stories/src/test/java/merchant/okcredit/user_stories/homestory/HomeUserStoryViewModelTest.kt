package merchant.okcredit.user_stories.homestory

import `in`.okcredit.merchant.contract.GetActiveBusiness
import `in`.okcredit.shared.usecase.Result
import dagger.Lazy
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import merchant.okcredit.user_stories.TestData
import merchant.okcredit.user_stories.usecase.GetActiveMyStoryCount
import merchant.okcredit.user_stories.usecase.GetHomeUserStories
import merchant.okcredit.user_stories.usecase.GetHomeUserStoriesTest
import org.joda.time.DateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.DateTimeUtils
import java.util.concurrent.TimeUnit

class HomeUserStoryViewModelTest {

    lateinit var testObserver: TestObserver<HomeUserStoryContract.State>

    private var initialState = HomeUserStoryContract.State()
    private val homeUserStoryLazy: Lazy<GetHomeUserStories> = mockk()
    private val getActiveBusinessLazy: Lazy<GetActiveBusiness> = mockk()
    private val getActiveMyStoryCountLazy: Lazy<GetActiveMyStoryCount> = mockk()

    private val homeUserStory: GetHomeUserStories = mockk()
    private val getActiveBusiness: GetActiveBusiness = mockk()
    private val getActiveMyStoryCount: GetActiveMyStoryCount = mockk()

    private lateinit var testScheduler: TestScheduler
    private lateinit var viewModel: HomeUserStoryViewModel

    @Before
    fun setup() {

        initialState = HomeUserStoryContract.State()
        testScheduler = TestScheduler()

        every { (homeUserStoryLazy.get()) } returns (homeUserStory)
        every { (getActiveBusinessLazy.get()) } returns (getActiveBusiness)
        every { (getActiveMyStoryCountLazy.get()) } returns (getActiveMyStoryCount)

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.io() } returns Schedulers.trampoline()
        every { Schedulers.computation() } returns testScheduler

        viewModel =
            HomeUserStoryViewModel(
                initialState, homeUserStoryLazy,
                getActiveBusinessLazy,
                getActiveMyStoryCountLazy
            )

        testObserver = viewModel.state().test()
    }

    @Test
    fun `load user stories success when stores feature disable`() {
        val response: GetHomeUserStories.Response = GetHomeUserStories.Response(false, null)
        val totalActiveMyStoryCount = 0
        val timeInMillis = 6231231234215
        val currentDateTime: DateTime = mockk()

        mockkStatic(DateTimeUtils::class)
        every { DateTimeUtils.currentDateTime() } returns currentDateTime

        every { (DateTimeUtils.currentDateTime().millis) } returns (timeInMillis)

        every { (homeUserStoryLazy.get().execute(Unit)) } returns (
            Observable.just(Result.Success(response))
            )
        every { (getActiveBusinessLazy.get().execute()) } returns (Observable.just(TestData.merchant))

        every { (getActiveMyStoryCountLazy.get().execute(timeInMillis)) } returns (Single.just(totalActiveMyStoryCount))

        viewModel.attachIntents(Observable.just(HomeUserStoryContract.Intent.Load))
        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assertEquals(testObserver.values().first(), initialState)

        assertEquals(
            testObserver.values().last(),
            initialState.copy(
                isLoading = false,
                homeUserStory = response.homeStories,
                isUserStoryEnabled = response.isEnabled,
                activeMerchantId = TestData.merchant.id,
                activeMyStoryCount = totalActiveMyStoryCount
            )
        )
    }

    @Test
    fun `load user stories success when stores feature enable `() {

        val timeInMillis = 1231231234215
        val currentDateTime: DateTime = mockk()

        mockkStatic(DateTimeUtils::class)
        every { DateTimeUtils.currentDateTime() } returns currentDateTime

        every { (DateTimeUtils.currentDateTime().millis) } returns (timeInMillis)

        val response: GetHomeUserStories.Response =
            GetHomeUserStories.Response(true, GetHomeUserStoriesTest.homeStories)

        val totalActiveMyStoryCount = 5
        every { (homeUserStoryLazy.get().execute(Unit)) } returns (
            Observable.just(Result.Success(response))
            )
        every { (getActiveBusinessLazy.get().execute()) } returns (Observable.just(TestData.merchant))

        every { (getActiveMyStoryCountLazy.get().execute(timeInMillis)) } returns (Single.just(totalActiveMyStoryCount))

        viewModel.attachIntents(Observable.just(HomeUserStoryContract.Intent.Load))
        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assertEquals(testObserver.values().first(), initialState)
        assertEquals(
            testObserver.values().last(),
            initialState.copy(
                isLoading = false,
                homeUserStory = response.homeStories,
                isUserStoryEnabled = response.isEnabled,
                activeMerchantId = TestData.merchant.id,
                activeMyStoryCount = totalActiveMyStoryCount
            )
        )
    }

    @After
    fun cleanup() {
        testObserver.dispose()
    }
}
