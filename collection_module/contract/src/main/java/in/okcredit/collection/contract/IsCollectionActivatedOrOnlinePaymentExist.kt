package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface IsCollectionActivatedOrOnlinePaymentExist {
    fun execute(): Observable<Boolean>
}
