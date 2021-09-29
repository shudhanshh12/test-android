package `in`.okcredit.merchant.customer_ui.usecase

import `in`.okcredit.backend._offline.database.CustomerRepo
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetCanShowBuyerTxnAlertTest {

    private val customerRepo: CustomerRepo = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getCanShowBuyerTxnAlert = GetCanShowBuyerTxnAlert({ customerRepo }, { getActiveBusinessId })
    private val request = GetCanShowBuyerTxnAlert.Request("abc")

    @Test
    fun `return true when  conditions are met`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.allCustomersBuyerTxnAlertFeatureList(businessId)).thenReturn(Observable.just(getDummyMap()))
        val testObserver = getCanShowBuyerTxnAlert.execute(request).test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(true)
        )
    }

    @Test
    fun `return  false when account id is present`() {
        val businessId = "business-id"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(customerRepo.allCustomersBuyerTxnAlertFeatureList(businessId)).thenReturn(
            Observable.just(
                getNullDummyMap()
            )
        )
        val testObserver = getCanShowBuyerTxnAlert.execute(request).test()
        testObserver.assertValues(
            `in`.okcredit.shared.usecase.Result.Progress(),
            `in`.okcredit.shared.usecase.Result.Success(false)
        )
    }

    private fun getNullDummyMap(): MutableMap<String, Boolean>? {
        val map = mutableMapOf<String, Boolean>()
        map.put("abcd", true)
        return map
    }

    private fun getDummyMap(): MutableMap<String, Boolean>? {
        val map = mutableMapOf<String, Boolean>()
        map.put("abc", true)
        return map
    }
}
