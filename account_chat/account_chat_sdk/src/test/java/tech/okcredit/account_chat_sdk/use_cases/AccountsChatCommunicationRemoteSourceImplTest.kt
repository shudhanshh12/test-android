package tech.okcredit.account_chat_sdk.use_cases

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import tech.okcredit.account_chat_sdk.AccountsApiClient
import tech.okcredit.account_chat_sdk.AccountsChatRemoteSourceImpl
import tech.okcredit.account_chat_sdk.models.FireBaseToken
import tech.okcredit.android.base.utils.ThreadUtils

class AccountsChatCommunicationRemoteSourceImplTest {

    private val accountsApiClient: AccountsApiClient = mock()
    private val serverImpl: AccountsChatRemoteSourceImpl = AccountsChatRemoteSourceImpl { accountsApiClient }

    @Before
    fun setup() {
        mockkStatic(Error::class)
        mockkObject(ThreadUtils)
        every { ThreadUtils.api() } returns Schedulers.trampoline()
        every { ThreadUtils.worker() } returns Schedulers.trampoline()
    }

    @Test
    fun `getToken() when api call successful then return response body`() {

        val fireToken: FireBaseToken = mock()
        val res = Response.success(fireToken)
        val businessId = "business-id"
        whenever(accountsApiClient.getToken(businessId)).thenReturn(Single.just(res))

        val testObserver = serverImpl.getToken(businessId).test()

        testObserver.assertValue(res.body())
        verify(accountsApiClient, times(1)).getToken(businessId)
    }

    @Test
    fun `getToken()  return IllegalStateException when body is null`() {

        val fireToken: FireBaseToken? = null
        val res = Response.success(fireToken)
        val businessId = "business-id"
        whenever(accountsApiClient.getToken(businessId)).thenReturn(Single.just(res))

        val testObserver = serverImpl.getToken(businessId).test()

        testObserver.assertError(IllegalStateException::class.java)
        verify(accountsApiClient, times(1)).getToken(businessId)
    }
}
