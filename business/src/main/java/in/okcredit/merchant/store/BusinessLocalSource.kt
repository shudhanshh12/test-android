package `in`.okcredit.merchant.store

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.merchant.contract.Category
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import `in`.okcredit.merchant.store.database.Business as DbMerchant
import `in`.okcredit.merchant.store.database.BusinessCategory as DbCategory
import `in`.okcredit.merchant.store.database.BusinessType as DbBusinessType

interface BusinessLocalSource {
    fun getBusiness(businessId: String): Observable<Business>

    fun getCategories(): Observable<List<Category>>

    fun getBusinessTypes(): Observable<List<BusinessType>>

    fun updateBusiness(business: Business): Completable

    fun saveBusiness(merchant: DbMerchant): Completable

    fun saveCategories(categories: List<DbCategory>): Completable

    fun saveBusinessTypes(businessTypes: List<DbBusinessType>): Completable

    fun cancelWorker(): Completable

    fun deleteBusinessTable(): Completable

    fun deleteCategoriesTable(): Completable

    suspend fun setBusinessActivated(activated: Boolean)

    suspend fun getBusinessActivated(): Boolean

    fun getBusinessIdList(): Flow<List<String>>

    fun getBusinessList(): Observable<List<Business>>

    @Deprecated("To be used only for multiple accounts migration")
    fun getBusinessIdForMultipleAccountsMigration(): String?
}
