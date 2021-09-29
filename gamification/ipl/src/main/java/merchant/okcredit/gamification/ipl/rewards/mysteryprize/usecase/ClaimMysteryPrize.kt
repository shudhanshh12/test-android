package merchant.okcredit.gamification.ipl.rewards.mysteryprize.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.gamification.ipl.data.IplRepository
import okhttp3.ResponseBody
import javax.inject.Inject

class ClaimMysteryPrize @Inject constructor(private val iplRepository: Lazy<IplRepository>) {

    fun execute(prizeId: String): Observable<Result<ResponseBody>> {
        return UseCase.wrapSingle(rxSingle { iplRepository.get().claimMysteryPrize(prizeId) })
    }
}
