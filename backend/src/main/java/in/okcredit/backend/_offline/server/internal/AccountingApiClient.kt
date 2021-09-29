package `in`.okcredit.backend._offline.server.internal

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AccountingApiClient {
    @GET("backup/all")
    fun getBackUp(): Single<Response<Report?>?>?

    @GET("report/account-statement")
    fun getSupplierStatementReport(
        @Query("after") fromTimestamp: Long,
        @Query("before") toTimestamp: Long,
        @Query("account_type") value: Int
    ): Single<Response<Report?>?>?
}
