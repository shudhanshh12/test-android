package `in`.okcredit.user_migration.presentation.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import tech.okcredit.user_migration.contract.models.GetPredictedDataApiRequest
import tech.okcredit.user_migration.contract.models.ParsedMigrationFileResponse
import tech.okcredit.user_migration.contract.models.ParsedMigrationRequest
import tech.okcredit.user_migration.contract.models.PredictedData
import tech.okcredit.user_migration.contract.models.SetAmountAmendedApiRequest
import tech.okcredit.user_migration.contract.models.create_customer_transaction.CreateCustomerRequest

interface UserMigrationApiClient {

    @Headers("Content-Type: application/json", "Content-Encoding: gzip")
    @POST("v1/parseFile")
    fun getParsedMigrationFileData(
        @Body request: ParsedMigrationRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<List<ParsedMigrationFileResponse>>>

    @Headers("Content-Type: application/json", "Content-Encoding: gzip")
    @POST("v1/createCustomersAndTransactions")
    fun getCustomerAndTransaction(
        @Body request: CreateCustomerRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Void>>

    @Headers("Content-Type: application/json", "Content-Encoding: gzip")
    @POST("v1/image/parse")
    suspend fun getPredictedData(
        @Body request: GetPredictedDataApiRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): PredictedData

    @Headers("Content-Type: application/json", "Content-Encoding: gzip")
    @POST("v1/image/parse/amend")
    fun setPredictedAmountAmended(
        @Body request: SetAmountAmendedApiRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Completable
}
