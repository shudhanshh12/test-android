package merchant.okcredit.gamification.ipl.game.usecase

import `in`.okcredit.merchant.contract.BusinessType
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.data.IplRepository
import javax.inject.Inject

class GetBusinessTypes @Inject constructor(private val iplRepository: Lazy<IplRepository>) {

    fun execute(): Observable<List<`in`.okcredit.merchant.contract.BusinessType>> {
        return iplRepository.get().getMerchantBusinessTypes()
    }
}
