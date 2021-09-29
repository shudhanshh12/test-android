package merchant.okcredit.user_stories.server

import `in`.okcredit.merchant.contract.BUSINESS_ID_HEADER
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface UserStoriesApiClient {
    @GET("v1/mystatus")
    fun getMyStory(
        @Query(value = "start_time") startTime: Long?,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<UserStoriesApiMessage.UserStatusListResponse<UserStoriesApiMessage.MyStory>>>

    @GET("v1/status")
    fun getOtherStory(
        @Query(value = "start_time") startTime: Long?,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<UserStoriesApiMessage.UserStatusListResponse<UserStoriesApiMessage.OthersStory>>>

    @Multipart
    @POST("/v1/status")
    fun postStory(
        @Part("request_id") request_id: RequestBody,
        @Part media: MultipartBody.Part,
        @Part("caption") caption: RequestBody?,
        @Part("upload_time") upload_time: RequestBody,
        @Header(BUSINESS_ID_HEADER) businessId: String,
    ): Single<Response<UserStoriesApiMessage.UserStatusResponse<UserStoriesApiMessage.MyStory>>>
}
