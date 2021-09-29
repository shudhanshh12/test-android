package tech.okcredit.android.communication

import io.reactivex.Single

interface GetCustomerBinding {
    fun getCustomerNameAndImage(customerId: String): Single<NotificationPersonProfile>
}
