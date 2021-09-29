package `in`.okcredit.merchant.server

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.server.internal.ApiMessages
import `in`.okcredit.merchant.server.internal.IdentityApiClient
import `in`.okcredit.merchant.server.internal.MerchantApiClient
import `in`.okcredit.merchant.server.internal.MerchantAuiClient
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.every
import io.mockk.mockkStatic
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.base.crashlytics.RecordException

class BusinessRemoteServerImplTest {
    private val merchantApiClient: MerchantApiClient = mock()
    private val identityApiClient: IdentityApiClient = mock()
    private val merchantAuiClient: MerchantAuiClient = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()

    private val serverImpl =
        BusinessRemoteServerImpl(
            { merchantApiClient },
            { identityApiClient },
            { merchantAuiClient },
            { getActiveBusinessId }
        )

    @Before
    fun setup() {
        mockkStatic(RecordException::class)
        every { RecordException.recordException(any()) } returns Unit
    }

    @Test
    fun `check the Server Response of should return true if Merchant is Activated `() {
        runBlocking {
            val businessId = "businessId"
            val request = ApiMessages.IsMerchantActivatedApiRequest(merchantId = businessId)
            val response = ApiMessages.IsMerchantActivated(true)
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(merchantAuiClient.isMerchantActivated(request, businessId)).thenReturn(response)

            val result = serverImpl.isMerchantActivated()

            Truth.assertThat(result).isEqualTo(response)
        }
    }

    @Test
    fun `check the Server Response of should return false if Merchant is Activated `() {
        runBlocking {
            val businessId = "businessId"
            val request = ApiMessages.IsMerchantActivatedApiRequest(merchantId = businessId)
            val response = ApiMessages.IsMerchantActivated(false)
            whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
            whenever(merchantAuiClient.isMerchantActivated(request, businessId)).thenReturn(response)

            val result = serverImpl.isMerchantActivated()

            Truth.assertThat(result).isEqualTo(response)
        }
    }
}
