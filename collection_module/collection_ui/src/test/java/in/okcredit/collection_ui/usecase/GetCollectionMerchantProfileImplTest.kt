package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.collection.contract.CollectionMerchantProfile
import `in`.okcredit.collection.contract.CollectionRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test

class GetCollectionMerchantProfileImplTest {
    private val collectionRepository: CollectionRepository = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val getCollectionMerchantProfile =
        GetCollectionMerchantProfileImpl({ collectionRepository }, { getActiveBusinessId })
    private val businessId = "businessId"

    @Test
    fun `should return CollectionMerchantProfile if present`() {
        val merchantCollectionProfile = Gson().fromJson(
            "{\"merchant_id\":\"1234\", \"payment_address\":\"8882946897@ybl\"}",
            CollectionMerchantProfile::class.java
        )
        whenever(collectionRepository.getCollectionMerchantProfile(businessId))
            .thenReturn(Observable.just(merchantCollectionProfile))
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))

        val testObserver = getCollectionMerchantProfile.execute().test()

        testObserver.assertValue(merchantCollectionProfile)
    }
}
