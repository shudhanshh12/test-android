package `in`.okcredit.merchant.suppliercredit.server.internal.common

import `in`.okcredit.merchant.suppliercredit.Supplier

object SupplierCreditServerErrors {
    class InvalidName : Exception()

    class InvalidMobile : Exception()

    class ActiveCyclicAccount(private val error: Error?) : Exception() {
        fun getInfo(): Error? {
            return error
        }
    }

    class DeletedCyclicAccount(private val error: Error?) : Exception() {
        fun getInfo(): Error? {
            return error
        }
    }

    class MobileConflict(private val supplier: Supplier) : Exception() {
        fun getSupplier(): Supplier {
            return supplier
        }
    }

    class InvalidAmount : Exception()

    class InvalidTransaction : Exception()

    data class Error(
        val id: String,
        val name: String,
        val mobile: String?,
        val profile: String?
    )
}
