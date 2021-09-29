package tech.okcredit.sdk.store

import `in`.okcredit.merchant.contract.GetActiveBusinessId
import `in`.okcredit.merchant.suppliercredit.utils.Utils
import android.content.Context
import androidx.room.EmptyResultSetException
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxCompletable
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ImageCache
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.sdk.DbEntityMapper
import tech.okcredit.sdk.store.database.BillDatabase
import tech.okcredit.sdk.store.database.BillDatabaseDao
import tech.okcredit.sdk.store.database.DBBill
import tech.okcredit.sdk.store.database.DbBillDoc
import tech.okcredit.sdk.store.database.LocalBill
import javax.inject.Inject

class BillLocalSourceImpl @Inject constructor(
    private val billDatabaseDao: Lazy<BillDatabaseDao>,
    private val context: Lazy<Context>,
    private val billPreferences: Lazy<BillPreferences>,
    private val imageCache: Lazy<ImageCache>,
    private val getActiveBusinessId: Lazy<GetActiveBusinessId>,
) : BillLocalSource {

    override fun areBillsPresent(accountId: String): Observable<Boolean> {
        return getActiveBusinessId.get().execute().flatMapObservable { businessId ->
            billDatabaseDao.get().areBillsPresent(accountId, businessId).subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
        }
    }

    override fun getAllBillsForAccount(
        accountId: String,
        startTimeInMs: String,
        endTimeInMs: String,
        businessId: String
    ): Observable<List<LocalBill>> {
        return billDatabaseDao.get().getAllBillsForAccount(accountId, startTimeInMs, endTimeInMs, businessId)
            .map { list ->
                Utils.mapList(list, DbEntityMapper.getBillsWithDoc(businessId))
            }
            .map {
                it.forEach { localBill ->
                    localBill.localBillDocList.forEach {
                        it.apply {
                            imageUrl = imageCache.get().getImage(url)
                        }
                    }
                }
                it
            }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getStartTime(businessId: String): Single<Long> {
        return billDatabaseDao.get().getStartTime(businessId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker()).onErrorResumeNext {
                if (it is EmptyResultSetException) {
                    return@onErrorResumeNext Single.just(0L)
                } else throw it
            }
    }

    override fun putBills(dbBillList: MutableList<DBBill>, dbDocList: MutableList<DbBillDoc>): Completable {
        return Completable.fromAction { billDatabaseDao.get().createTransaction(dbBillList, dbDocList) }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun clearAllTables(): Completable {
        return rxCompletable {
            billPreferences.get().clear()
            BillDatabase.INSTANCE?.clearAllTables()
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun deleteBill(billId: String): Completable {
        return Completable.fromAction { billDatabaseDao.get().deleteBill(billId) }
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getBill(billId: String, businessId: String): Observable<LocalBill> {
        return billDatabaseDao.get().getBill(billId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker()).map {
                DbEntityMapper.getBillsWithDoc(businessId).convert(it)
            }
            .map {
                it.localBillDocList.forEach {
                    it.apply {
                        imageUrl = imageCache.get().getImage(url)
                    }
                }
                it
            }
    }

    override fun updateSeenTime(accountId: String, currentTimestamp: String, businessId: String): Completable {
        return if (billDatabaseDao.get().isAccountPresent(accountId)) {
            billDatabaseDao.get().updateSeenTime(accountId, currentTimestamp).subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
        } else {
            billDatabaseDao.get().insertAccount(accountId, currentTimestamp, businessId).subscribeOn(ThreadUtils.database())
                .observeOn(ThreadUtils.worker())
        }
    }

    override fun getUnseenBillCount(accountId: String, startTime: Long, businessId: String): Observable<Int> {
        return billDatabaseDao.get().getDistinctLastSeenTime(accountId).flatMap {
            if (it.isEmpty().not()) {
                billDatabaseDao.get().getUnseenBillCount(accountId, it[0], startTime, businessId).map {
                    it[0]
                }
            } else {
                Observable.just(0)
            }
        }.subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun putNewBillDocsInDb(localBillDocList: List<DbBillDoc>): Completable {
        return billDatabaseDao.get().putBillDocs(localBillDocList)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun updateNote(note: String, billId: String): Completable {
        return billDatabaseDao.get().updateNote(note, billId)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun deleteBillDoc(id: String): Completable {
        return billDatabaseDao.get().deleteBillDoc(id)
            .subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker())
    }

    override fun getLastSeenTime(accountId: String): Single<String> {
        return billDatabaseDao.get().getDistinctLastSeenTime(accountId).subscribeOn(ThreadUtils.database())
            .observeOn(ThreadUtils.worker()).firstOrError().flatMap {
                if (it.isEmpty()) {
                    Single.just("0")
                } else {
                    Single.just(it[0])
                }
            }.onErrorResumeNext {
                if (it is EmptyResultSetException) {
                    return@onErrorResumeNext Single.just("0")
                } else throw it
            }
    }

    override fun getUnreadBillCounts(businessId: String): Observable<Map<String, Int>> =
        getBillAdoptionTime(DateTimeUtils.currentDateTime().millis, businessId).flatMap { adoptionTime ->
            billDatabaseDao.get().getDistinctUnreadBillCounts(adoptionTime, businessId).map { list ->
                list.associateBy({ it.accountId }, { it.count })
            }
        }

    override fun getUnreadBillCount(accountId: String, businessId: String): Observable<Int> =
        getBillAdoptionTime(DateTimeUtils.currentDateTime().millis, businessId).flatMap { adoptionTime ->
            billDatabaseDao.get().getDistinctUnreadBillCount(accountId, adoptionTime, businessId)
        }

    override fun getTotalBillCount(accountId: String, businessId: String): Observable<Int> =
        billDatabaseDao.get().getDistinctTotalBillCount(accountId, businessId)

    override fun setBillAdoptionTime(millis: Long, businessId: String) =
        billPreferences.get().setBillAdoptionTime(millis, businessId)

    override fun getBillAdoptionTime(defaultTime: Long, businessId: String): Observable<Long> =
        billPreferences.get().getBillAdoptionTime(defaultTime, businessId)

    override fun insertOrUpdateBills(bill: DBBill, docList: List<DbBillDoc>?) =
        billDatabaseDao.get().insertOrUpdateBills(bill, docList)
}
