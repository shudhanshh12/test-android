package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test

class GetBusinessListTest {

    private val businessRepository: BusinessRepository = mock()

    private val getBusinessList = GetBusinessList { businessRepository }

    @Test
    fun `execute should return list of business`() {
        val businessList = listOf<Business>()
        whenever(businessRepository.getBusinessList()).thenReturn(Observable.just(businessList))

        val testObserver = getBusinessList.execute().test()

        testObserver.assertValue(businessList)
        verify(businessRepository).getBusinessList()
        testObserver.dispose()
    }
}
