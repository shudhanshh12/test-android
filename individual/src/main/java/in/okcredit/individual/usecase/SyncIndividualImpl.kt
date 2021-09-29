package `in`.okcredit.individual.usecase

import `in`.okcredit.frontend.contract.LoginDataSyncer
import `in`.okcredit.individual.IndividualRepositoryImpl
import `in`.okcredit.individual.contract.PreferenceKey.*
import `in`.okcredit.individual.contract.SyncIndividual
import `in`.okcredit.individual.contract.SyncIndividual.Response
import `in`.okcredit.individual.contract.model.Individual
import `in`.okcredit.individual.data.remote.IndividualUser
import `in`.okcredit.merchant.contract.BusinessRepository
import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.contract.GetBusinessIdList
import dagger.Lazy
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.rx2.await
import tech.okcredit.android.auth.usecases.InvalidateAccessToken
import javax.inject.Inject
import `in`.okcredit.individual.data.remote.Individual as ApiIndividual

class SyncIndividualImpl @Inject constructor(
    private val repository: Lazy<IndividualRepositoryImpl>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
    private val getBusinessIdList: Lazy<GetBusinessIdList>,
    private val businessRepository: Lazy<BusinessRepository>,
    private val loginDataSyncer: Lazy<LoginDataSyncer>,
    private val invalidateAccessToken: Lazy<InvalidateAccessToken>,
) : SyncIndividual {

    /**
     * Returns pair of Individual ID, List of Business IDs
     */
    override suspend fun execute(): Response {
        val businessId = getActiveBusinessId.get().execute().await()
        val getIndividualResponse = repository.get().getIndividualFromRemoteSource(businessId)
        val individual = buildIndividualObject(getIndividualResponse.individual_user.user)
        repository.get().setIndividual(individual)
        putPreferences(getIndividualResponse.individual_user)
        return Response(getIndividualResponse.individual_user.user.id, getIndividualResponse.business_ids)
    }

    private fun buildIndividualObject(individualUser: IndividualUser) = individualUser.run {
        Individual(
            id = id,
            createTime = create_time,
            mobile = mobile,
            email = email,
            registerTime = register_time,
            lang = lang,
            displayName = display_name,
            profileImage = profile_image,
            addressText = address?.text,
            longitude = address?.longitude,
            latitude = address?.latitude,
            about = about
        )
    }

    private suspend fun putPreferences(individualUser: ApiIndividual) {
        individualUser.app_lock_opt_in?.let { putPreference(APP_LOCK.key, it.toString()) }
        individualUser.whatsapp_opt_in?.let { putPreference(WHATSAPP.key, it.toString()) }
        individualUser.payment_password_enabled?.let { putPreference(PAYMENT_PASSWORD.key, it.toString()) }
        individualUser.fingerprint_lock_opt_in?.let { putPreference(FINGER_PRINT_LOCK.key, it.toString()) }
        individualUser.four_digit_pin_in?.let { putPreference(FOUR_DIGIT_PIN.key, it.toString()) }
    }

    private suspend fun putPreference(key: String, value: String) {
        repository.get().setPreference(key, value)
    }

    override suspend fun syncIndividualAndNewBusinessesIfPresent() {
        val response = execute()
        val existingBusinessIdList = getBusinessIdList.get().execute().first()

        val newBusinessIdList = response.businessIdList - existingBusinessIdList
        if (newBusinessIdList.isNotEmpty()) {
            invalidateAccessToken.get().execute()
        }
        newBusinessIdList.forEach { businessId ->
            syncBusiness(businessId).await()
            loginDataSyncer.get().syncDataForBusinessId(BehaviorSubject.create(), -1, businessId)
                .onErrorComplete().await()
        }
    }

    private fun syncBusiness(businessId: String) = businessRepository.get().executeSyncBusiness(businessId)
}
