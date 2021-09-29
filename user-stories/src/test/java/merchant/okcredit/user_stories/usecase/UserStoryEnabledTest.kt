package merchant.okcredit.user_stories.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.user_stories.utils.UserStoriesFeature
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.ab.AbRepository
import tech.okcredit.android.base.utils.ThreadUtils

class UserStoryEnabledTest {

    private val abRepository: AbRepository = mockk()
    private val userStoryEnabled = UserStoryEnabled { abRepository }

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        every { ThreadUtils.database() } returns Schedulers.trampoline()
    }

    @Test
    fun `execute when user story feature enable `() {
        val response = true
        every { (abRepository.isFeatureEnabled(UserStoriesFeature.FEATURE_USER_STORIES)) } returns (
            Observable.just(
                response
            )
            )

        val testObservable = userStoryEnabled.execute().test()
        testObservable.assertValue(response)

        verify(exactly = 1) { abRepository.isFeatureEnabled(UserStoriesFeature.FEATURE_USER_STORIES) }
    }

    @Test
    fun `execute when user story feature disable `() {
        val response = false
        every { (abRepository.isFeatureEnabled(UserStoriesFeature.FEATURE_USER_STORIES)) } returns (
            Observable.just(
                response
            )
            )

        val testObservable = userStoryEnabled.execute().test()
        testObservable.assertValue(response)

        verify(exactly = 1) { abRepository.isFeatureEnabled(UserStoriesFeature.FEATURE_USER_STORIES) }
    }
}
