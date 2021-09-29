package `in`.okcredit.backend._offline.database

import `in`.okcredit.backend._offline.common.Utils
import `in`.okcredit.backend._offline.database.internal.DbEntities
import `in`.okcredit.backend._offline.database.internal.DbEntityMapper
import `in`.okcredit.backend._offline.database.internal.DueInfoDao
import `in`.okcredit.backend._offline.model.DueInfo
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.CompletableSource
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import tech.okcredit.android.base.utils.ThreadUtils
import javax.inject.Inject

class DueInfoRepo @Inject constructor(private val dueInfoDao: Lazy<DueInfoDao>) {

    fun getAllCustomerDueInfo(businessId: String): Observable<MutableList<DueInfo>> {
        return dueInfoDao.get().getDueInfoList(businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .flatMap {
                if (it.isNotEmpty()) {
                    val list = ArrayList<DueInfo>()
                    it.forEach {
                        list.add(DbEntityMapper.DUE_INFO(businessId).convert(it)!!)
                    }

                    return@flatMap Observable.just(list)
                } else {
                    return@flatMap Observable.just(ArrayList<DueInfo>())
                }
            }
    }

    fun getDueInfoForCustomer(customerId: String, businessId: String): Observable<DueInfo> {
        return dueInfoDao.get().getDueInfoForCustomer(customerId, businessId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
            .map { DbEntityMapper.DUE_INFO(businessId).convert(it) }
    }

    fun isDueInfoExists(customerId: String): Single<Boolean> {
        return dueInfoDao.get().isDueInfoExists(customerId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun insertDueInfo(dueInfo: DbEntities.DueInfo): Completable {
        return dueInfoDao.get().insertDueInfo(dueInfo)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun updateCustomDueDateSet(
        isCustomDateSet: Boolean,
        customerId: String,
        dueDateActive: Boolean,
        dateTime: DateTime,
    ): Completable {
        return dueInfoDao.get().updateCustomDueDateSet(isCustomDateSet, customerId, dueDateActive, dateTime)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun clearDueDateForCustomer(customerId: String): Completable {
        return dueInfoDao.get().clearDueDateForCustomer(customerId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun insertAllDueInfo(
        dueInfos: List<`in`.okcredit.backend._offline.server.internal.DueInfo>,
        businessId: String,
    ): Completable {
        val dueInfosArray =
            Utils.mapList(dueInfos, DbEntityMapper.DUE_INFO_API_AND_DB_CONVERTER(businessId)).toTypedArray()
        return dueInfoDao.get().insertDueInfo(*dueInfosArray)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun clear(businessId: String? = null): CompletableSource {
        val completable = if (businessId.isNullOrEmpty()) {
            dueInfoDao.get().deleteAll()
        } else {
            dueInfoDao.get().clearDueInfo(businessId)
        }
        return completable
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    fun insertDueInfo(dueInfo: DueInfo, businessId: String): Completable {
        val dbEntityDueInfo = DbEntityMapper.DUE_INFO(businessId).reverse().convert(dueInfo)!!
        return insertDueInfo(dbEntityDueInfo)
    }
}
