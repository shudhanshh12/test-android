package `in`.okcredit.shared.data

import `in`.okcredit.shared.data.server.SharedRemoteSource
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class SharedRepoImplTest {

    private val sharedRemoteSource: SharedRemoteSource = mock()

    private val sharedRepo = SharedRepoImpl { sharedRemoteSource }

    @Test
    fun uploadDbFile() {
        val testFile = File("path")
        whenever(sharedRemoteSource.uploadDbFile(testFile)).thenReturn(Completable.complete())

        val testObserver = sharedRepo.uploadDbFile(testFile).test()

        testObserver.assertComplete()
        verify(sharedRemoteSource).uploadDbFile(testFile)
    }
}
