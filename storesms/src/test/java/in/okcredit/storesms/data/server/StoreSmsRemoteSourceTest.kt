package `in`.okcredit.storesms.data.server

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class StoreSmsRemoteSourceTest {
    private val apiClient: StoreSmsApiClient = mock()

    private val storeSmsServer = StoreSmsServer(Lazy { apiClient })

    @Test
    fun `sendRawSmsToServer is successful`() {
        val businessId = "dummy123"
        runBlocking {
            whenever(
                apiClient.sendRawSmsToServer(
                    RawSmsRequestBody(
                        texts = listOf()
                    ),
                    businessId
                )
            ).thenReturn(Unit)

            val result = storeSmsServer.sendRawSmsToServer(listOf(), businessId)

            assertThat(result).isEqualTo(Unit)
        }
    }

    @Test(expected = java.lang.Exception::class)
    fun `sendRawSmsToServer is throwing error`() {
        val businessId = "dummy123"
        runBlocking {
            whenever(
                apiClient.sendRawSmsToServer(
                    RawSmsRequestBody(
                        texts = listOf()
                    ),
                    businessId
                )
            ).thenThrow(java.lang.Exception())

            storeSmsServer.sendRawSmsToServer(listOf(), businessId)

            verify(apiClient).sendRawSmsToServer(
                RawSmsRequestBody(
                    texts = listOf()
                ),
                businessId
            )
        }
    }

    @Test
    fun `getLasRawSmsSyncedTime when sync time is not empty return value greater than 0L`() {
        val businessId = "dummy123"
        runBlocking {
            whenever(
                apiClient.getLasRawSmsSyncedTime(businessId)
            ).thenReturn(RawSmsSyncLastTimeResponse(time = 12345678L))

            val result = storeSmsServer.getLastRawSmsSyncedTime(businessId)

            assertThat(result).isEqualTo(RawSmsSyncLastTimeResponse(time = 12345678L))
        }
    }

    @Test
    fun `getLasRawSmsSyncedTime when sync time is empty return value  0L`() {
        val businessId = "dummy123"
        runBlocking {
            whenever(
                apiClient.getLasRawSmsSyncedTime(businessId)
            ).thenReturn(RawSmsSyncLastTimeResponse(time = 0L))

            val result = storeSmsServer.getLastRawSmsSyncedTime(businessId)

            assertThat(result).isEqualTo(RawSmsSyncLastTimeResponse(time = 0L))
        }
    }

    @Test(expected = java.lang.Exception::class)
    fun `getLasRawSmsSyncedTime is throwing error`() {
        val businessId = "dummy123"
        runBlocking {
            whenever(
                apiClient.getLasRawSmsSyncedTime(businessId)
            ).thenThrow(java.lang.Exception())

            storeSmsServer.getLastRawSmsSyncedTime(businessId)

            verify(apiClient).getLasRawSmsSyncedTime(businessId)
        }
    }
}
