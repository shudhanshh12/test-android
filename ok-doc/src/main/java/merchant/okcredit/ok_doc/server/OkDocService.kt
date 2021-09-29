package merchant.okcredit.ok_doc.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import merchant.okcredit.ok_doc.contract.model.ImageDoc
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface OkDocService {
    @GET("v1/image/{media_id}")
    suspend fun getImageUrl(
        @Path("media_id") mediaId: String,
        @Header(BUSINESS_ID_HEADER) businessId: String
    ): Response<ImageDoc>
}
