package merchant.okcredit.gamification.ipl.game.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.data.IplRepository
import merchant.okcredit.gamification.ipl.model.TeamScore
import javax.inject.Inject

class GetTeamScore @Inject constructor(private val iplRepository: Lazy<IplRepository>) {

    fun execute(matchId: String, teamId: String): Observable<Result<TeamScore>> {
        return UseCase.wrapObservable(iplRepository.get().getTeamScore(matchId, teamId))
    }
}
