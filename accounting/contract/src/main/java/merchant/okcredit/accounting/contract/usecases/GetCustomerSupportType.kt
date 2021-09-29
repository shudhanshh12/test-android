package merchant.okcredit.accounting.contract.usecases

import io.reactivex.Single
import merchant.okcredit.accounting.contract.model.SupportType

interface GetCustomerSupportType {
    fun execute(): Single<SupportType>
}
