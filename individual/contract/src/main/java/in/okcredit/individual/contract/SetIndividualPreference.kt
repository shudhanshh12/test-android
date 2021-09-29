package `in`.okcredit.individual.contract

interface SetIndividualPreference {
    suspend fun execute(key: String, value: String, businessId: String)
    suspend fun schedule(key: String, value: String, businessId: String)
}
