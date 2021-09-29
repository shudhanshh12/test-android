package `in`.okcredit.merchant.usecase

import `in`.okcredit.merchant.contract.Business
import `in`.okcredit.merchant.contract.BusinessRepository
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Single
import javax.inject.Inject

@Reusable
class GetBusinessList @Inject constructor(
    private val businessRepository: Lazy<BusinessRepository>,
) {
    fun execute(): Single<List<Business>> {
        return businessRepository.get().getBusinessList().firstOrError()
    }
}
