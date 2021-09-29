package `in`.okcredit.backend.contract

interface GetIsCustomerAddTransactionRestricted {

    suspend fun execute(businessId: String, customerId: String): Boolean
}
