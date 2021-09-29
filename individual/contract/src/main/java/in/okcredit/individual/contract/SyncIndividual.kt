package `in`.okcredit.individual.contract

interface SyncIndividual {
    suspend fun execute(): Response
    suspend fun syncIndividualAndNewBusinessesIfPresent()

    data class Response(val individualId: String, val businessIdList: List<String>)
}
