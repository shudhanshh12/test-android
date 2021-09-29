package merchant.okcredit.accounting.usecases

import merchant.okcredit.accounting.contract.AccountingRepository
import merchant.okcredit.accounting.contract.model.SupportType
import merchant.okcredit.accounting.contract.usecases.GetCustomerSupportData
import javax.inject.Inject

class GetCustomerSupportDataImpl @Inject constructor(private val accountingRepository: AccountingRepository) :
    GetCustomerSupportData {

    override fun getCustomerSupportNumber(supportType: SupportType): String {
        return if (supportType == SupportType.CALL)
            getCustomerCareCallNumber()
        else
            getCustomerCareChatNumber()
    }

    override fun get24x7String(): String = accountingRepository.get24x7String()

    override fun getCustomerCareCallNumber() = accountingRepository.getCustomerCareCallNumber()

    override fun getCustomerCareChatNumber() = accountingRepository.getCustomerCareChatNumber()
}
