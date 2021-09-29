package merchant.okcredit.accounting.contract.usecases

import merchant.okcredit.accounting.contract.model.SupportType

interface GetCustomerSupportData {
    fun get24x7String(): String
    fun getCustomerSupportNumber(supportType: SupportType): String
    fun getCustomerCareCallNumber(): String
    fun getCustomerCareChatNumber(): String
}
