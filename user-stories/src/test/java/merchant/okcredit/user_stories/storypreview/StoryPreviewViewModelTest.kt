package merchant.okcredit.user_stories.storypreview

import com.camera.models.models.Picture
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import merchant.okcredit.user_stories.usecase.AddUserStory
import merchant.okcredit.user_stories.usecase.GetCapturedImages
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.extensions.toArrayList
import tech.okcredit.camera_contract.CapturedImage
import java.io.File
import java.util.concurrent.TimeUnit

class StoryPreviewViewModelTest {

    private lateinit var testObserver: TestObserver<StoryPreviewContract.State>
    private lateinit var viewModel: StoryPreviewViewModel
    private val getCapturedImage: GetCapturedImages = mockk()
    private val addUserStory: AddUserStory = mockk()
    private val activeMerchantId: String = "10"
    private var initialState = StoryPreviewContract.State()
    private lateinit var testScheduler: TestScheduler

    companion object {
        private val photoList = listOf(
            Picture(
                "/storage/emulated/0/Android/data/" +
                    "in.okcredit.merchant.debug/files/1614162890181pic.jpg"
            ),
            Picture(
                "/storage/emulated/0/Android/data/" +
                    "in.okcredit.merchant.debug/files/16141628901481pic.jpg"
            ),
            Picture(
                "/storage/emulated/0/Android/data/" +
                    "in.okcredit.merchant.debug/files/16141628930181pic.jpg"
            ),
        ).toArrayList()
        private val captureMap = HashMap<CapturedImage?, String>()

        private val captureList = listOf(
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
    }

    @Before
    fun setup() {
        testScheduler = TestScheduler()

        mockkStatic(Schedulers::class)
        every { Schedulers.newThread() } returns Schedulers.trampoline()
        every { Schedulers.computation() } returns testScheduler
        mockkStatic(FirebaseCrashlytics::class)
        val firebaseCrashlytics: FirebaseCrashlytics = mockk()
        every { FirebaseCrashlytics.getInstance() } returns firebaseCrashlytics

        viewModel =
            StoryPreviewViewModel(
                { initialState },
                photoList,
                captureMap,
                activeMerchantId,
                { getCapturedImage },
                { addUserStory }
            )

        testObserver = viewModel.state().test()
    }

    @Test
    fun `OnLoad get captured Images and capture Map`() {
        val response = captureList
        every { (getCapturedImage.execute(photoList)) } returns (Observable.just((response)))

        viewModel.attachIntents(Observable.just(StoryPreviewContract.Intent.Load))
        testScheduler.advanceTimeBy(35, TimeUnit.MILLISECONDS)

        assertEquals(testObserver.values().first(), initialState)

        assertEquals(
            testObserver.values().last(),
            initialState.copy(
                isLoading = false,
                imageList = captureList,
                imageCaptionMap = captureMap,
                selectedImage = captureList[0],
                activeMerchantId = activeMerchantId
            )
        )
    }
}
