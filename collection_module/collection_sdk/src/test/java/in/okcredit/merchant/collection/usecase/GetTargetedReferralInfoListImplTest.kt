package `in`.okcredit.merchant.collection.usecase

import `in`.okcredit.backend.contract.GetSpecificCustomerList
import `in`.okcredit.collection.contract.GetTargetedReferralList
import `in`.okcredit.merchant.collection.CollectionTestData.CUSTOMER
import `in`.okcredit.merchant.collection.CollectionTestData.CUSTOMER_ADDITIONAL_INFO
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Test

class GetTargetedReferralInfoListImplTest {
    private val getTargetedReferralList: GetTargetedReferralList = mock()
    private val getSpecificCustomerList: GetSpecificCustomerList = mock()
    private val getTargetedReferralInfoListImpl =
        GetTargetedReferralInfoListImpl({ getTargetedReferralList }, { getSpecificCustomerList })

    @Test
    fun `execute TargetedCustomerReferralInfo list successfully`() {
        val customerAdditionalInfoList = listOf(CUSTOMER_ADDITIONAL_INFO)

        whenever(getTargetedReferralList.execute()).thenReturn(
            Observable.create {
                it.onNext(customerAdditionalInfoList)
            }
        )
        val customerIdList = customerAdditionalInfoList.map { it.id }

        whenever(getSpecificCustomerList.execute(customerIdList)).thenReturn(
            Observable.create {
                it.onNext(listOf(CUSTOMER))
            }
        )

        val testObserver = getTargetedReferralInfoListImpl.execute().test()
        assert(
            testObserver.valueCount() == 1 &&
                testObserver.values().last()[0].id == customerAdditionalInfoList[0].id &&
                testObserver.values().last()[0].id == CUSTOMER.id &&
                testObserver.values().last()[0].link == customerAdditionalInfoList[0].link &&
                testObserver.values().last()[0].description == CUSTOMER.description
        )
        verify(getTargetedReferralList).execute()
        verify(getSpecificCustomerList).execute(customerIdList)
    }

    @Test
    fun `execute customerAdditionalInfoList list is empty returns empty list `() {
        val customerAdditionalInfoList = listOf(CUSTOMER_ADDITIONAL_INFO)

        whenever(getTargetedReferralList.execute()).thenReturn(
            Observable.create {
                it.onNext(customerAdditionalInfoList)
            }
        )
        val customerIdList = customerAdditionalInfoList.map { it.id }

        whenever(getSpecificCustomerList.execute(customerIdList)).thenReturn(
            Observable.create {
                it.onNext(listOf())
            }
        )

        val testObserver = getTargetedReferralInfoListImpl.execute().test()
        assert(
            testObserver.valueCount() == 1 && testObserver.values().first().isEmpty()
        )
        verify(getTargetedReferralList).execute()
        verify(getSpecificCustomerList).execute(customerIdList)
    }
}
