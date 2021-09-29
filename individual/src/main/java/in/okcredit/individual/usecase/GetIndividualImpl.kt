package `in`.okcredit.individual.usecase

import `in`.okcredit.individual.IndividualRepositoryImpl
import `in`.okcredit.individual.contract.GetIndividual
import `in`.okcredit.individual.contract.model.Individual
import dagger.Lazy
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetIndividualImpl @Inject constructor(
    private val repository: Lazy<IndividualRepositoryImpl>,
) : GetIndividual {
    override fun execute(): Flow<Individual> {
        return repository.get().getIndividualFromLocalSource()
    }
}
