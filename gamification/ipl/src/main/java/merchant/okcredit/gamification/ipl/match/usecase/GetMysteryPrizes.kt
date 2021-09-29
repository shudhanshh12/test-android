package merchant.okcredit.gamification.ipl.match.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.gamification.ipl.data.IplRepository
import merchant.okcredit.gamification.ipl.game.data.server.model.response.MysteryPrizeModel
import javax.inject.Inject

class GetMysteryPrizes @Inject constructor(private val iplRepository: Lazy<IplRepository>) {

    fun execute(): Observable<Result<List<MysteryPrizeModel>>> {
        return UseCase.wrapSingle(rxSingle { iplRepository.get().getMysteryPrizes().filter { !it.isClaimed() } })
    }
}
