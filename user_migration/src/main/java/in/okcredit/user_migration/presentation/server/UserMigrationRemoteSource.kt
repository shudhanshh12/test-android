package `in`.okcredit.user_migration.presentation.server

import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Single
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.network.asError
import tech.okcredit.user_migration.contract.models.GetPredictedDataApiRequest
import tech.okcredit.user_migration.contract.models.ParsedMigrationFileResponse
import tech.okcredit.user_migration.contract.models.SetAmountAmendedApiRequest
import javax.inject.Inject

class UserMigrationRemoteSource @Inject constructor(
    private val apiService: Lazy<UserMigrationApiClient>
) {

    fun getParseFileData(
        urls: List<String?>,
        businessId: String
    ): Single<List<ParsedMigrationFileResponse>> {

        return apiService.get().getParsedMigrationFileData(
            tech.okcredit.user_migration.contract.models.ParsedMigrationRequest(
                urls,
                businessId
            ),
            businessId
        )
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .map {
                if (it.isSuccessful) {
                    return@map it.body()
                } else {
                    throw it.asError()
                }
            }
    }

    fun createCustomerAndTransaction(
        customers: List<tech.okcredit.user_migration.contract.models.create_customer_transaction.Customers>,
        businessId: String
    ): Completable {
        val request = tech.okcredit.user_migration.contract.models.create_customer_transaction.CreateCustomerRequest(
            merchant_id = businessId,
            customers = customers
        )
        return apiService.get().getCustomerAndTransaction(request, businessId)
            .subscribeOn(ThreadUtils.api())
            .observeOn(ThreadUtils.worker())
            .flatMapCompletable {
                if (it.isSuccessful) {
                    Completable.complete()
                } else {
                    Completable.error(it.asError())
                }
            }
    }

    suspend fun getPredictedDataFromRemote(url: String, businessId: String) =
        apiService.get().getPredictedData(GetPredictedDataApiRequest(url, businessId), businessId)

    fun setAmountAmended(request: SetAmountAmendedApiRequest, businessId: String): Completable {
        return apiService.get().setPredictedAmountAmended(request, businessId)
    }
}
