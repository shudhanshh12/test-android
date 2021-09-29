package merchant.okcredit.suppliercredit.contract

interface GetIsSupplierAddTransactionRestricted {

    suspend fun execute(businessId: String, supplierId: String): Boolean
}
