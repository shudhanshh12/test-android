package `in`.okcredit.merchant.store

import `in`.okcredit.merchant.contract.BusinessType
import `in`.okcredit.merchant.store.database.Business
import `in`.okcredit.merchant.store.database.BusinessCategory
import `in`.okcredit.merchant.store.database.BusinessDao
import `in`.okcredit.merchant.store.database.DbEntityMapper
import `in`.okcredit.merchant.store.database.DbEntityMapper.toBusiness
import `in`.okcredit.merchant.store.sharedprefs.BusinessPreferences
import `in`.okcredit.merchant.utils.Utils
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import tech.okcredit.android.base.coroutines.DispatcherProvider
import tech.okcredit.android.base.preferences.Scope
import tech.okcredit.android.base.utils.ThreadUtils
import timber.log.Timber
import javax.inject.Inject
import `in`.okcredit.merchant.store.database.BusinessType as DbBusinessType

class BusinessLocalSourceImpl @Inject constructor(
    private val businessDao: Lazy<BusinessDao>,
    private val businessPreferences: Lazy<BusinessPreferences>,
    private val dispatcherProvider: Lazy<DispatcherProvider>,
) : BusinessLocalSource {

    companion object {
        const val PREF_MERCHANT_ACTIVATED = "pref merchant activated"
    }

    override fun getBusiness(businessId: String): Observable<`in`.okcredit.merchant.contract.Business> {
        return businessDao.get().getBusiness(businessId)
            .subscribeOn(ThreadUtils.database())
            .map { it.toBusiness() }
    }

    override fun getCategories(): Observable<List<`in`.okcredit.merchant.contract.Category>> {
        return businessDao.get().getCategories()
            .subscribeOn(ThreadUtils.database())
            .map<List<`in`.okcredit.merchant.contract.Category>> { categories ->
                Utils.mapList(categories, DbEntityMapper.CATEGORY.reverse())
            }
            .toObservable()
            .doOnError {
                Timber.i(it)
            }
    }

    override fun getBusinessTypes(): Observable<List<BusinessType>> {
        return businessDao.get().getBusinessTypes()
            .subscribeOn(ThreadUtils.database())
            .map<List<BusinessType>> { categories ->
                Utils.mapList(categories, DbEntityMapper.BUSINESS.reverse())
            }
            .toObservable()
            .doOnError {
                Timber.i(it)
            }
    }

    override fun saveBusiness(business: Business): Completable {
        return Completable.fromAction {
            businessDao.get().saveMerchant(business)
        }.subscribeOn(ThreadUtils.database())
    }

    override fun saveCategories(categories: List<BusinessCategory>): Completable {
        return Completable.fromAction {
            businessDao.get().saveCategories(categories)
        }.subscribeOn(ThreadUtils.database())
    }

    override fun saveBusinessTypes(businessTypes: List<DbBusinessType>): Completable {
        return Completable.fromAction {
            businessDao.get().saveBusinessTypes(businessTypes)
        }.subscribeOn(ThreadUtils.database())
    }

    override fun cancelWorker(): Completable {
        return Completable.complete()
    }

    override fun updateBusiness(business: `in`.okcredit.merchant.contract.Business): Completable {
        return Completable.complete()
    }

    override fun deleteBusinessTable(): Completable {
        return Completable.fromAction {
            businessDao.get().deletedMerchant()
            businessDao.get().deletedMerchantCategories()
        }.subscribeOn(ThreadUtils.database())
    }

    override fun deleteCategoriesTable(): Completable {
        return Completable.fromAction {
            Timber.i("refreshLanguageInCategories deleted")
            businessDao.get().deletedMerchantCategories()
        }.subscribeOn(ThreadUtils.database())
    }

    override suspend fun setBusinessActivated(activated: Boolean) {
        businessPreferences.get().set(PREF_MERCHANT_ACTIVATED, activated, Scope.Individual)
    }

    override suspend fun getBusinessActivated(): Boolean {
        return withContext(dispatcherProvider.get().io()) {
            businessPreferences.get().getBoolean(PREF_MERCHANT_ACTIVATED, Scope.Individual).first()
        }
    }

    override fun getBusinessIdList(): Flow<List<String>> {
        return businessDao.get().getBusinessIdList()
    }

    override fun getBusinessList(): Observable<List<`in`.okcredit.merchant.contract.Business>> {
        return businessDao.get().getBusinessList().subscribeOn(ThreadUtils.database())
            .map { list ->
                list.map {
                    it.toBusiness()
                }
            }
    }

    override fun getBusinessIdForMultipleAccountsMigration(): String? {
        return businessDao.get().getBusinessIdForMultipleAccountsMigration().firstOrNull()
    }
}
