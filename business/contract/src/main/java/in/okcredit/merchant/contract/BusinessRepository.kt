package `in`.okcredit.merchant.contract

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface BusinessRepository {

    fun scheduleSyncBusiness(businessId: String): Completable

    fun executeSyncBusiness(businessId: String): Completable

    fun scheduleSyncBusinessCategoriesAndBusinessTypes(businessId: String): Completable

    fun getCategories(): Observable<List<Category>>

    fun getBusinessTypes(): Observable<List<BusinessType>>

    fun updateBusiness(updateBusinessRequest: UpdateBusinessRequest, businessId: String): Completable

    fun clearLocalData(): Completable
    fun checkNewNumberExists(mobile: String, businessId: String): Single<NumberCheckResponse>

    // when user change language , we need to get api data once again in that language
    fun refreshLanguageInCategories(businessId: String): Completable

    suspend fun isMerchantActivated(): Boolean

    fun getBusinessList(): Observable<List<Business>>
}
