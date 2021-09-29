package `in`.okcredit.individual.data.local

import `in`.okcredit.individual.contract.model.Individual
import dagger.Lazy
import dagger.Reusable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import `in`.okcredit.individual.data.local.Individual as DbIndividual

@Reusable
class IndividualLocalSource @Inject constructor(
    private val dao: Lazy<IndividualDao>,
) {

    suspend fun setIndividual(individual: Individual) {
        dao.get().setIndividual(individual.toDbIndividual())
    }

    suspend fun deleteIndividual() {
        dao.get().deleteIndividual()
    }

    private fun Individual.toDbIndividual() = DbIndividual(
        id = id,
        createTime = createTime,
        mobile = mobile,
        email = email,
        registerTime = registerTime,
        lang = lang,
        displayName = displayName,
        profileImage = profileImage,
        addressText = addressText,
        longitude = longitude,
        latitude = latitude,
        about = about
    )

    fun getIndividual(): Flow<Individual> {
        return dao.get().getIndividual()
            .filterNotNull()
            .map { it.toIndividual() }
    }

    private fun DbIndividual.toIndividual() = Individual(
        id = id,
        createTime = createTime,
        mobile = mobile,
        email = email,
        registerTime = registerTime,
        lang = lang,
        displayName = displayName,
        profileImage = profileImage,
        addressText = addressText,
        longitude = longitude,
        latitude = latitude,
        about = about
    )
}
