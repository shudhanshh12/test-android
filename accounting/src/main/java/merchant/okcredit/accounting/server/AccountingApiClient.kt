package merchant.okcredit.accounting.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header

interface AccountingApiClient {

    @GET("backup/all")
    fun getBackUp(
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<ApiMessages>

    data class ApiMessages(
        @SerializedName("merchant_id") val merchantId: String,
        @SerializedName("report_url") val reportUrl: String
    )
}
