package `in`.okcredit.collection.contract

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object OnlineCollectionNotification {

    private val bus = PublishSubject.create<OnlinePayment>()

    fun send(data: OnlinePayment) {
        bus.onNext(data)
    }

    fun toObservable(): Observable<OnlinePayment> {
        return bus
    }

    data class OnlinePayment(
        val customerId: String?,
        val amount: Long,
        val createTime: Long,
    )
}
