package `in`.okcredit.merchant.server

import `in`.okcredit.merchant.BusinessRepositoryImpl
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.usecase.GetActiveBusinessImpl
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.Test

class GetActiveBusinessImplTest {
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val businessApi: BusinessRepositoryImpl = mock()
    private val getActiveMerchantImpl = GetActiveBusinessImpl({ businessApi }, { getActiveBusinessId })

    companion object {
        var formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")
        var dt: DateTime = formatter.parseDateTime("04/10/2020 20:27:05")

        val merchant = Business(
            id = "id",
            name = "name",
            mobile = "mobile",
            createdAt = dt,
            updateCategory = true,
            updateMobile = true
        )
    }

    @Test
    fun executeTest() {
        val businessId = "businessId"
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(businessApi.getBusiness(businessId)).thenReturn(Observable.just(merchant))
        val result = getActiveMerchantImpl.execute().test()
        result.assertValue(merchant)
    }
}
