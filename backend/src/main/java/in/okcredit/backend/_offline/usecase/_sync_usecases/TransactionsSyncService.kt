package `in`.okcredit.backend._offline.usecase._sync_usecases

import io.reactivex.Completable
import io.reactivex.Single
import org.joda.time.DateTime

interface TransactionsSyncService {
    fun isSyncedAtLeastOnce(): Single<Boolean>

    fun getLastSyncTime(businessId: String): Single<Long>

    fun setLastSyncTime(time: DateTime, businessId: String): Completable

    fun clearLastSyncTime(): Completable
}
