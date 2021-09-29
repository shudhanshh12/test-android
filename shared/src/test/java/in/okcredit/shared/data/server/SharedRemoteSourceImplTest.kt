package `in`.okcredit.shared.data.server

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.junit.Assert.*
import retrofit2.Response
import java.io.File

class SharedRemoteSourceImplTest {
    private val okDocsClient: OkDocsClient = mock()

    private val sharedRemoteSource = SharedRemoteSourceImpl { okDocsClient }

//    @Test
    fun uploadDbFile() {
        val testFile = File.createTempFile("test", ".db")
        val contentType = "multipart/form-data".toMediaTypeOrNull()
        val partFile: MultipartBody.Part =
            MultipartBody.Part.createFormData("file", testFile.name, testFile.asRequestBody(contentType))
        mockkObject(MultipartBody.Part)
        every {
            MultipartBody.Part.createFormData(
                "file",
                testFile.name,
                testFile.asRequestBody(contentType)
            )
        } returns partFile
        whenever(okDocsClient.uploadDBFile(partFile)).thenReturn(Single.just(Response.success(Unit)))

        val testObserver = sharedRemoteSource.uploadDbFile(testFile).test()

        testObserver.assertComplete()
        verify(okDocsClient).uploadDBFile(partFile)
    }
}
