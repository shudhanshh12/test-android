package `in`.okcredit.cashback

import `in`.okcredit.cashback.repository.CashbackRepository
import `in`.okcredit.cashback.usecase.GetCashbackMessageDetailsImpl
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import org.junit.Test
import tech.okcredit.android.base.string_resource_provider.StringResourceProvider

class GetCashbackMessageDetailsTest {

    private val cashbackRepository: CashbackRepository = mock()
    private val stringResourceProvider: StringResourceProvider = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getCashbackMessageDetails =
        GetCashbackMessageDetailsImpl({ cashbackRepository }, { stringResourceProvider }, { getActiveBusinessId })

    @Test
    fun `test usecase methods`() {
        val cashbackMessageDetails = TestData.cashbackMessageDetails
        val stringTemplate = "Get Rs.%d cashback on your first payment of atleast Rs.%d"
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(cashbackRepository.getCashbackMessageDetails(businessId)).thenReturn(Observable.just(cashbackMessageDetails))
        whenever(stringResourceProvider.getByResourceId(any())).thenReturn(stringTemplate)

        getCashbackMessageDetails.execute().test().apply {
            assertEquals(
                this.values().last(),
                cashbackMessageDetails
            )
            assertComplete()
        }

        assertEquals(
            String.format(
                stringTemplate,
                cashbackMessageDetails.cashbackAmount,
                cashbackMessageDetails.minimumPaymentAmount
            ),
            getCashbackMessageDetails.getHumanReadableStringFromModel(cashbackMessageDetails)
        )
    }
}
