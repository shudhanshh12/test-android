package `in`.okcredit.collection.contract

import io.reactivex.Completable

interface SendCollectionEvent {
    fun execute(customerId: String?, eventName: String): Completable // possible event name - reminder/merchant_qr

    companion object {
        const val EVENT_MERCHANT_QR = "merchant_qr"
        const val EVENT_REMINDER = "reminder"
    }
}
