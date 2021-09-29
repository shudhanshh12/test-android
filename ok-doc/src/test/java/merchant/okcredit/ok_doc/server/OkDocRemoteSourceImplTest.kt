package merchant.okcredit.ok_doc.server

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.runBlocking
import merchant.okcredit.ok_doc.TestData
import merchant.okcredit.ok_doc.contract.model.ImageDoc
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class OkDocRemoteSourceImplTest {

    private val apiDocService: OkDocService = mock()
    private lateinit var okDocRemoteSource: OkDocRemoteSource

    @Before
    fun setUp() {
        okDocRemoteSource = OkDocRemoteSourceImpl { apiDocService }
    }

    @Test
    fun `getImageDoc with media_id returns success`() {
        val expected: ImageDoc = mock()

        runBlocking {
            whenever(
                apiDocService.getImageUrl(
                    TestData.TEST_MEDIA_ID,
                    TestData.TEST_BUSINESS_ID
                )
            ).thenReturn(Response.success(expected))

            val response = okDocRemoteSource.getImageDoc(TestData.TEST_MEDIA_ID, TestData.TEST_BUSINESS_ID)
            assert(expected == response)

            verify(apiDocService, times(1)).getImageUrl(TestData.TEST_MEDIA_ID, TestData.TEST_BUSINESS_ID)
        }
    }

    @Test(expected = Exception::class)
    fun `getImageDoc with invalid media_id throws exception`() {

        runBlocking {
            whenever(apiDocService.getImageUrl(TestData.TEST_MEDIA_ID, TestData.TEST_BUSINESS_ID)).thenThrow(
                Exception(
                    TestData.TEST_SERVER_EXCEPTION_MESSAGE
                )
            )

            okDocRemoteSource.getImageDoc(TestData.TEST_MEDIA_ID, TestData.TEST_BUSINESS_ID)
        }
    }
}
