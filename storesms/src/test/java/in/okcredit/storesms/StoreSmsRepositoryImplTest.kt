package `in`.okcredit.storesms

import `in`.okcredit.storesms.data.server.RawSmsSyncLastTimeResponse
import `in`.okcredit.storesms.data.server.StoreSmsServer
import androidx.work.ListenableWorker
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.coroutines.DispatcherProvider

class StoreSmsRepositoryImplTest {
    private val storeSmsServer: StoreSmsServer = mock()
    private val dispatcherProvider: DispatcherProvider = mock()
    private val storeSmsApiImpl =
        StoreSmsRepositoryImpl(
            { storeSmsServer },
            { dispatcherProvider },
        )

    @Before
    fun setUp() {
        whenever(dispatcherProvider.io()).thenReturn(Unconfined)
    }

    @Test
    fun `sendRawSmsToServer is successful`() {
        val businessId = "dummy123"
        runBlocking {
            withContext(dispatcherProvider.io()) {
                whenever(storeSmsServer.sendRawSmsToServer(listOf(), businessId)).thenReturn(Unit)

                storeSmsApiImpl.sendRawSmsToServer(listOf(), businessId)

                assertThat(ListenableWorker.Result.success())

                verify(storeSmsServer).sendRawSmsToServer(listOf(), businessId)
            }
        }
    }

    @Test(expected = java.lang.Exception::class)
    fun `sendRawSmsToServer is fails`() {
        val businessId = "dummy123"
        runBlocking {
            withContext(dispatcherProvider.io()) {

                whenever(storeSmsServer.sendRawSmsToServer(listOf(), businessId)).thenThrow(Exception())

                storeSmsApiImpl.sendRawSmsToServer(listOf(), businessId)

                assertThat(ListenableWorker.Result.failure())

                verify(storeSmsServer).sendRawSmsToServer(listOf(), businessId)
            }
        }
    }

    @Test
    fun `getLastRawSmsSyncedTimerFromServer is successful`() {
        val businessId = "dummy123"
        runBlocking {
            withContext(dispatcherProvider.io()) {
                whenever(storeSmsServer.getLastRawSmsSyncedTime(businessId)).thenReturn(
                    RawSmsSyncLastTimeResponse(
                        12345678L
                    )
                )

                storeSmsApiImpl.getLastRawSmsSyncedTimerFromServer(businessId)

                assertThat(12345678L)

                verify(storeSmsServer).getLastRawSmsSyncedTime(businessId)
            }
        }
    }
}
