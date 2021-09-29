package `in`.okcredit.merchant.core.common

import `in`.okcredit.merchant.core.model.Customer

sealed class CoreException : Throwable() {
    object TransactionNotFoundException : CoreException()
    object IllegalArgumentException : CoreException()
    data class MobileConflict(val conflict: Customer) : CoreException()
    data class DeletedCustomer(val conflict: Customer) : CoreException()
    data class DeletePermissionDenied(val errorMessage: String) : CoreException()
    object MobileUpdateAccessDenied : CoreException()
    object InvalidName : CoreException()
}
