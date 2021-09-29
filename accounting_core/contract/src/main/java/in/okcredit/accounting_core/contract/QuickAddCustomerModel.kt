package `in`.okcredit.accounting_core.contract

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuickAddCustomerModel(
    val name: String,
    val mobile: String? = null,
    val profileImage: String? = null,
    val customerId: String? = null,
    val supplierId: String? = null,
    val shouldReactivate: Boolean = false
) : Parcelable {

    fun isSupplier() = !supplierId.isNullOrBlank()

    fun isExistingCustomer() = !customerId.isNullOrBlank()
}
