package `in`.okcredit.individual

import `in`.okcredit.individual.contract.IndividualRepository
import `in`.okcredit.individual.contract.PreferenceKey
import `in`.okcredit.individual.contract.model.Individual
import `in`.okcredit.individual.data.local.IndividualLocalSource
import `in`.okcredit.individual.data.local.IndividualPreferences
import `in`.okcredit.individual.data.remote.GetIndividualResponse
import `in`.okcredit.individual.data.remote.IndividualRemoteServer
import `in`.okcredit.individual.data.remote.UpdateIndividualRequest
import dagger.Lazy
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.rxCompletable
import javax.inject.Inject

class IndividualRepositoryImpl @Inject constructor(
    private val preferences: Lazy<IndividualPreferences>,
    private val remoteSource: Lazy<IndividualRemoteServer>,
    private val localSource: Lazy<IndividualLocalSource>,
) : IndividualRepository {

    override fun getPreference(preferenceKey: PreferenceKey): Flow<String> {
        return preferences.get().getString(preferenceKey.key, preferenceKey.defaultValue)
    }

    override suspend fun setPreference(key: String, value: String) {
        return preferences.get().setString(key, value)
    }

    override suspend fun isPreferenceAvailable(key: String): Boolean {
        return preferences.get().isPreferenceAvailable(key)
    }

    override fun clearLocalData(): Completable {
        return rxCompletable {
            preferences.get().clear()
            localSource.get().deleteIndividual()
        }
    }

    suspend fun getIndividualFromRemoteSource(businessId: String): GetIndividualResponse {
        return remoteSource.get().getIndividual(businessId)
    }

    fun getIndividualFromLocalSource(): Flow<Individual> {
        return localSource.get().getIndividual()
    }

    suspend fun setIndividual(individual: Individual) {
        return localSource.get().setIndividual(individual)
    }

    suspend fun setIndividualPreference(request: UpdateIndividualRequest, businessId: String) {
        remoteSource.get().updateIndividual(request, businessId)
    }

    suspend fun updateBusinessMobile(
        mobile: String,
        currentMobileOtpToken: String,
        newMobileOtpToken: String,
        individualId: String,
        businessId: String,
    ) {
        remoteSource.get()
            .updateBusinessMobile(mobile, currentMobileOtpToken, newMobileOtpToken, individualId, businessId)
    }
}
