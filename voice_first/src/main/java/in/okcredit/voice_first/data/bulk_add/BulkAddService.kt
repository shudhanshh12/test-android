package `in`.okcredit.voice_first.data.bulk_add

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import `in`.okcredit.voice_first.data.bulk_add.entities.ParseTransactionRequest
import `in`.okcredit.voice_first.data.bulk_add.entities.ParseTransactionResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface BulkAddApiService {

    @POST("/v1/transaction/parse")
    suspend fun parseTransaction(
        @Header(BUSINESS_ID_HEADER) businessId: String,
        @Body text: ParseTransactionRequest
    ): ParseTransactionResponse
}
