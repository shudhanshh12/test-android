package merchant.okcredit.supplier.contract

import io.reactivex.Completable

interface PutNotificationReminderAction {
    fun execute(notificationId: String, status: Int): Completable
}
