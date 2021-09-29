package merchant.okcredit.user_stories.usecase

import com.camera.models.models.Picture
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.extensions.toArrayList
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.camera_contract.CapturedImage
import java.io.File

class GetCapturedImagesTest {
    private val getCapturedImages = GetCapturedImages()

    companion object {
        private val photoList = listOf(
            Picture("/storage/emulated/0/Android/data/in.okcredit.merchant.debug/files/1614162890181pic.jpg"),
            Picture("/storage/emulated/0/Android/data/in.okcredit.merchant.debug/files/1614162890182pic.jpg"),
            Picture("/storage/emulated/0/Android/data/in.okcredit.merchant.debug/files/1614162890183pic.jpg"),
        ).toArrayList()

        private val captureImageList = photoList.map { CapturedImage(File(it.path)) }.toArrayList()
    }

    @Before
    fun setUp() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.computation() } returns Schedulers.trampoline()
    }

    @Test
    fun `getCapturedImage success`() {

        val testObserver = getCapturedImages.execute(photoList).test()
        testObserver.assertValues(captureImageList)
    }
}
