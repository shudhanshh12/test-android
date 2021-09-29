package `in`.okcredit.merchant.usecase

import `in`.okcredit.frontend.contract.LoginDataSyncer
import `in`.okcredit.merchant.BusinessRepositoryImpl
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.GetBusinessIdList
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Lazy
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.await
import org.jetbrains.annotations.NonNls
import tech.okcredit.android.auth.usecases.InvalidateAccessToken
import javax.inject.Inject

class CreateBusiness @Inject constructor(
    private val repository: Lazy<BusinessRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val loginDataSyncer: Lazy<LoginDataSyncer>,
    private val switchBusiness: Lazy<SwitchBusiness>,
    private val invalidateAccessToken: Lazy<InvalidateAccessToken>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
    private val firebaseRemoteConfig: Lazy<FirebaseRemoteConfig>,
) {

    companion object {
        @NonNls
        const val FRC_MAX_NUMBER_OF_BUSINESSES = "multi_acc_max_number_of_businesses"
    }

    suspend fun execute(businessName: String) {
        if (canCreateBusiness().not()) {
            throw BusinessCountLimitExceededException()
        }
        val activeBusinessId = getActiveBusinessId.get().execute().await()
        val newBusiness = repository.get().createBusiness(businessName, activeBusinessId)
        repository.get().saveBusiness(newBusiness).await()
        switchBusiness.get().execute(newBusiness.id, businessName)
        // V2 access token contains mapping of list of businesses, need to refresh it after adding a new business
        invalidateAccessToken.get().execute()
        syncDataForBusiness(newBusiness.id)
    }

    private suspend fun canCreateBusiness(): Boolean {
        val businessCount = getBusinessIdList.get().execute().first().size
        val maxCount = firebaseRemoteConfig.get().getLong(FRC_MAX_NUMBER_OF_BUSINESSES)
        return businessCount < maxCount
    }

    private suspend fun syncDataForBusiness(businessId: String) {
        loginDataSyncer.get().syncDataForBusinessId(BehaviorSubject.create(), -1, businessId)
            .onErrorComplete().await()
    }

    class BusinessCountLimitExceededException : Exception("Business count limit exhausted")
}
