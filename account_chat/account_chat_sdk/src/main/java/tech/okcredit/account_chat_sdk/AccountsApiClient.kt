package tech.okcredit.account_chat_sdk

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import tech.okcredit.account_chat_sdk.models.FireBaseToken

interface AccountsApiClient {

    @GET("GetFirebaseToken")
    fun getToken(
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Single<Response<FireBaseToken>>
}
