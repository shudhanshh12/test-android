package `in`.okcredit.merchant.usecase

import `in`.okcredit.frontend.contract.LoginDataSyncer
import `in`.okcredit.merchant.BusinessRepositoryImpl
import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.GetBusinessIdList
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import tech.okcredit.android.auth.usecases.InvalidateAccessToken

class CreateBusinessTest {
    private val repository: BusinessRepositoryImpl = mock()
    private val getActiveBusinessId: GetActiveBusinessId = mock()
    private val loginDataSyncer: LoginDataSyncer = mock()
    private val switchBusiness: SwitchBusiness = mock()
    private val invalidateAccessToken: InvalidateAccessToken = mock()
    private val getBusinessIdList: GetBusinessIdList = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()
    private val createBusiness = CreateBusiness(
        { repository },
        { getActiveBusinessId },
        { loginDataSyncer },
        { switchBusiness },
        { invalidateAccessToken },
        { getBusinessIdList },
        { firebaseRemoteConfig },
    )
    private val businessId = "businessId"
    private val businessId2 = "businessId2"
    private val businessName = "businessName"
    private val business = mock<Business>().apply {
        whenever(id).thenReturn(businessId2)
    }

    @Before
    fun setup() {
        whenever(repository.saveBusiness(business)).thenReturn(Completable.complete())
        whenever(getActiveBusinessId.execute()).thenReturn(Single.just(businessId))
        whenever(getBusinessIdList.execute()).thenReturn(flowOf(listOf(businessId, businessId2)))
        whenever(firebaseRemoteConfig.getLong("multi_acc_max_number_of_businesses")).thenReturn(50)
    }

    @Test
    fun `given error in loginDataSyncer should complete other steps`() {
        runBlocking {
            whenever(loginDataSyncer.syncDataForBusinessId(anyOrNull(), anyOrNull(), anyOrNull()))
                .thenReturn(Completable.error(RuntimeException()))
            whenever(repository.createBusiness(businessName, businessId)).thenReturn(business)

            createBusiness.execute(businessName)

            verify(getBusinessIdList).execute()
            verify(firebaseRemoteConfig).getLong("multi_acc_max_number_of_businesses")
            verify(getActiveBusinessId).execute()
            verify(invalidateAccessToken).execute()
            verify(repository).createBusiness(businessName, businessId)
            verify(repository).saveBusiness(business)
            verify(switchBusiness).execute(businessId2, businessName, null)
        }
    }

    @Test
    fun `given no error in loginDataSyncer should all steps`() {
        runBlocking {
            whenever(loginDataSyncer.syncDataForBusinessId(anyOrNull(), anyOrNull(), anyOrNull()))
                .thenReturn(Completable.complete())
            whenever(repository.createBusiness(businessName, businessId)).thenReturn(business)

            createBusiness.execute(businessName)

            verify(getBusinessIdList).execute()
            verify(firebaseRemoteConfig).getLong("multi_acc_max_number_of_businesses")
            verify(getActiveBusinessId).execute()
            verify(invalidateAccessToken).execute()
            verify(repository).createBusiness(businessName, businessId)
            verify(repository).saveBusiness(business)
            verify(switchBusiness).execute(businessId2, businessName, null)
        }
    }

    @Test(expected = CreateBusiness.BusinessCountLimitExceededException::class)
    fun `given max businesses present should throw BusinessCountLimitExceededException`() {
        runBlocking {
            whenever(firebaseRemoteConfig.getLong("multi_acc_max_number_of_businesses")).thenReturn(2)

            createBusiness.execute(businessName)

            verify(getBusinessIdList).execute()
            verify(firebaseRemoteConfig).getLong("multi_acc_max_number_of_businesses")
            verify(getActiveBusinessId, times(0)).execute()
            verify(invalidateAccessToken, times(0)).execute()
            verify(repository, times(0)).createBusiness(businessName, businessId)
            verify(repository, times(0)).saveBusiness(business)
            verify(switchBusiness, times(0)).execute(businessId2, businessName, null)
        }
    }
}
