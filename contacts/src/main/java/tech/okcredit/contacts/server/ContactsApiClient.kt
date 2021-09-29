package tech.okcredit.contacts.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.*
import tech.okcredit.contacts.server.data.CheckedResponse

interface ContactsApiClient {
    @Headers("Content-Type: application/json", "Content-Encoding: gzip")
    @PUT("contacts/sync")
    suspend fun uploadContact(
        @Body request: UploadContactRequest,
        @Header(BUSINESS_ID_HEADER) businessId: String
    )

    @GET("contacts/{deviceId}/network")
    suspend fun getCheckedResponse(
        @Path("deviceId") deviceId: String,
        @Query("start_ts") startTime: Long,
        @Query("last_id") lastId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): CheckedResponse

    @GET
    fun getOkCreditContact(
        @Url url: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<OkCreditContactResponse>>

    @POST
    fun acknowledgeContactSaved(
        @Url url: String,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<Void>>

    companion object {
        const val OKC_CONTACT_END_POINT = "okcNumber"
        const val ACKNOWLEDGE_CONTACT_SAVED_END_POINT = "mapping"
    }
}
