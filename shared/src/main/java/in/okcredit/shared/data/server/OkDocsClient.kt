package `in`.okcredit.shared.data.server

import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OkDocsClient {

    @Multipart
    @POST("file/upload")
    fun uploadDBFile(@Part file: MultipartBody.Part): Single<Response<Unit>>
}
