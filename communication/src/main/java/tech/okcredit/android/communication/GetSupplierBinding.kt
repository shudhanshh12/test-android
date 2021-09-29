package tech.okcredit.android.communication

import io.reactivex.Single

interface GetSupplierBinding {
    fun getSupplierNameAndImage(supplierId: String): Single<NotificationPersonProfile>
}
