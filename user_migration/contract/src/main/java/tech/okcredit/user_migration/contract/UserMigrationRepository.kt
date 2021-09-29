package tech.okcredit.user_migration.contract

import androidx.fragment.app.Fragment
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.user_migration.contract.models.ParsedMigrationFileResponse
import tech.okcredit.user_migration.contract.models.PredictedData
import tech.okcredit.user_migration.contract.models.SetAmountAmendedApiRequest
import tech.okcredit.user_migration.contract.models.create_customer_transaction.Customers

interface UserMigrationRepository {

    fun navigate(fragment: Fragment)

    fun getParseFileData(urls: List<String?>, businessId: String): Single<List<ParsedMigrationFileResponse>>

    fun createCustomerAndTransaction(customers: List<Customers>, businessId: String): Completable

    suspend fun getPredictedData(url: String, businessId: String): PredictedData

    suspend fun getPredictedDataFromLocalSource(businessId: String): PredictedData?

    fun getFileObjectIdFromPredictedData(businessId: String): Observable<String>

    suspend fun setAmountAmended(request: SetAmountAmendedApiRequest, businessId: String): Completable

    fun setAmountAmend(request: SetAmountAmendedApiRequest, businessId: String): Completable

    fun clearPredictionData(businessId: String): Completable
}
