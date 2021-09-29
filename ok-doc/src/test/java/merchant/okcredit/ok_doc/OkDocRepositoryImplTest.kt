package merchant.okcredit.ok_doc

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import merchant.okcredit.ok_doc.contract.model.ImageDoc
import merchant.okcredit.ok_doc.server.OkDocRemoteSource
import org.junit.Before
import org.junit.Test

class OkDocRepositoryImplTest {
    private val okDocRemoteSource: OkDocRemoteSource = mock()
    private lateinit var okDocRepositoryImpl: OkDocRepositoryImpl

    @Before
    fun setUp() {
        okDocRepositoryImpl = OkDocRepositoryImpl { okDocRemoteSource }
    }

    @Test
    fun `getImageDoc with media_id returns success`() {
        val expected: ImageDoc = mock()

        runBlocking {
            whenever(okDocRemoteSource.getImageDoc(TestData.TEST_MEDIA_ID, TestData.TEST_BUSINESS_ID)).thenReturn(expected)

            val response = okDocRepositoryImpl.getImageDoc(TestData.TEST_MEDIA_ID, TestData.TEST_BUSINESS_ID)
            assert(expected == response)

            verify(okDocRemoteSource, times(1)).getImageDoc(TestData.TEST_MEDIA_ID, TestData.TEST_BUSINESS_ID)
        }
    }

    @Test(expected = Exception::class)
    fun `getImageDoc with invalid media_id throws exception`() {

        runBlocking {
            whenever(okDocRemoteSource.getImageDoc(TestData.TEST_MEDIA_ID, TestData.TEST_BUSINESS_ID)).thenThrow(Exception(TestData.TEST_SERVER_EXCEPTION_MESSAGE))
            okDocRepositoryImpl.getImageDoc(TestData.TEST_MEDIA_ID, TestData.TEST_BUSINESS_ID)
        }
    }
}
