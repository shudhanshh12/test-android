package tech.okcredit.contract

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface MerchantPrefSyncStatus {
    fun checkMerchantPrefSync(): Single<Boolean>
    fun execute(): Completable
    fun checkFingerPrintAvailability(): Observable<Boolean>
    fun checkFingerPrintEnable(): Observable<Boolean>
}
