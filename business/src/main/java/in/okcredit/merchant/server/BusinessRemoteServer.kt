package `in`.okcredit.merchant.server

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.NumberCheckResponse
import `in`.okcredit.merchant.server.internal.ApiMessages
import `in`.okcredit.merchant.store.database.BusinessCategory
import io.reactivex.Completable
import io.reactivex.Single
import `in`.okcredit.merchant.store.database.Business as DbBusiness
import `in`.okcredit.merchant.store.database.BusinessType as DbBusinessType

interface BusinessRemoteServer {
    fun getBusiness(businessId: String): Single<DbBusiness>

    fun getCategories(businessId: String): Single<List<BusinessCategory>>

    fun getBusinessTypes(businessId: String): Single<List<DbBusinessType>>

    fun updateBusiness(request: ApiMessages.UpdateBusinessRequest, businessId: String): Completable

    fun checkNewNumber(mobile: String, businessId: String): Single<NumberCheckResponse>

    suspend fun isMerchantActivated(): ApiMessages.IsMerchantActivated

    suspend fun createBusiness(name: String, businessId: String): Business
}
