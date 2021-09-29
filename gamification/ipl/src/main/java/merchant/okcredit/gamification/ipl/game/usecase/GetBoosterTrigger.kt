package merchant.okcredit.gamification.ipl.game.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.data.IplRepository
import merchant.okcredit.gamification.ipl.model.Booster
import javax.inject.Inject

class GetBoosterTrigger @Inject constructor(private val iplRepository: Lazy<IplRepository>) {

    fun execute(matchId: String): Observable<Result<Booster>> {
        return UseCase.wrapObservable(iplRepository.get().getBoosterTrigger(matchId))
    }
}
