package `in`.okcredit.individual.contract

import `in`.okcredit.individual.contract.model.Individual
import kotlinx.coroutines.flow.Flow

interface GetIndividual {
    fun execute(): Flow<Individual>
}
