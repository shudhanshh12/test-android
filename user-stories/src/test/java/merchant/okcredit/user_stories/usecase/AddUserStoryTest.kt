package merchant.okcredit.user_stories.usecase

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import merchant.okcredit.user_stories.contract.UserStoryRepository
import merchant.okcredit.user_stories.store.UserStoriesLocalSource
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.extensions.toArrayList
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.camera_contract.CapturedImage
import java.io.File

class AddUserStoryTest {
    private val userStoriesLocalSources: UserStoriesLocalSource = mockk()
    private val userStoryRepository: UserStoryRepository = mockk()
    private val getActiveBusinessId: GetActiveBusinessId = mockk()

    private val addUserStory = AddUserStory(
        { userStoriesLocalSources },
        { userStoryRepository },
        { getActiveBusinessId }
    )

    companion object {
        private val photoList = listOf(
            CapturedImage(
                File(
                    "/storage/emulated/0/Android/data/" +
                        "in.okcredit.merchant.debug/files/1614162890181pic.jpg"
                )
            ),
            CapturedImage(
                File(
                    "/storage/emulated/0/Android/data/" +
                        "in.okcredit.merchant.debug/files/1614162890182pic.jpg"
                )
            ),
            CapturedImage(
                File(
                    "/storage/emulated/0/Android/data/" +
                        "in.okcredit.merchant.debug/files/1614162890183pic.jpg"
                )
            ),
        ).toArrayList()
        private val captureMap = HashMap<CapturedImage?, String>()

        val request = AddUserStory.Request(photoList, captionMap = captureMap)
    }

    @Before
    fun setup() {

        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
        every { ThreadUtils.database() } returns Schedulers.trampoline()
    }

    @Test
    fun `addUserStory success`() {
        val businessId = "business-id"
        every { userStoriesLocalSources.saveMyStory(any()) }.returns(Completable.complete())
        every { userStoryRepository.syncAddStory(businessId) }.returns(Completable.complete())
        every { getActiveBusinessId.execute() }.returns(Single.just(businessId))
        val testCompletable = addUserStory.execute(request).test()
        testCompletable.assertComplete()

        verify { userStoriesLocalSources.saveMyStory(any()) }
        verify { userStoryRepository.syncAddStory(businessId) }

        testCompletable.dispose()
    }
}
