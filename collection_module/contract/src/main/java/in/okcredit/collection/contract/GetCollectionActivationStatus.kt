package `in`.okcredit.collection.contract

import io.reactivex.Observable

interface GetCollectionActivationStatus {

    fun execute(): Observable<Boolean>
}
