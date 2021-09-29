package merchant.okcredit.supplier.contract

import io.reactivex.Completable

interface PutNotificationReminder {
    fun execute(accountId: String): Completable
}
