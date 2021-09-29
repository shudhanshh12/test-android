package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.BusinessRepositoryImpl
import `in`.okcredit.merchant.contract.GetBusiness
import dagger.Lazy
import javax.inject.Inject

class GetBusinessImpl @Inject constructor(
    private val repository: Lazy<BusinessRepositoryImpl>,
) : GetBusiness {
    override fun execute(businessId: String) = repository.get().getBusiness(businessId)
}
