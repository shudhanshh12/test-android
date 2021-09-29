package `in`.okcredit.cashback.datasource.local

import `in`.okcredit.cashback.datasource.remote.apiClient.CashbackMessageDetailsDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface CashbackLocalCacheSource {

    fun setCashbackMessageDetailsCache(cashbackMessageDetailsDto: CashbackMessageDetailsDto): Completable

    fun getCachedCashbackMessageDetails(): Single<CashbackMessageDetailsDto>

    fun getCachedCashbackMessageDetailsTimestamp(): Observable<Long>

    fun invalidateCache(): Completable

    fun clear(): Completable
}
