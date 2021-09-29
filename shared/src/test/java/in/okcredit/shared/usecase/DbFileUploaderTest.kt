package `in`.okcredit.shared.usecase

import `in`.okcredit.shared.data.SharedRepo
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.utils.ThreadUtils
import java.io.File

class DbFileUploaderTest {
    private val sharedRepo: SharedRepo = mock()

    private val dbFileUploader: DbFileUploader = DbFileUploader(
        sharedRepo = { sharedRepo }
    )

    @Before
    fun setup() {
        mockkObject(ThreadUtils)
        every { ThreadUtils.newThread() } returns Schedulers.trampoline()
    }

    @Test
    fun shouldBeTrueAlways() {
        Truth.assertThat(true).isTrue()
    }

    @Test
    fun execute() {
        val testDbFile = File("path")

        whenever(sharedRepo.uploadDbFile(testDbFile)).thenReturn(Completable.complete())

        val testObserver = dbFileUploader.execute(testDbFile).test()
        testObserver.assertComplete()
        verify(sharedRepo).uploadDbFile(testDbFile)
    }
}
