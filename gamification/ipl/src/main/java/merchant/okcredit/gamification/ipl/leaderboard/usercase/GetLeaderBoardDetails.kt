package merchant.okcredit.gamification.ipl.leaderboard.usercase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.gamification.ipl.data.IplRepository
import merchant.okcredit.gamification.ipl.game.data.server.model.response.LeaderBoardResponse
import javax.inject.Inject

class GetLeaderBoardDetails @Inject constructor(private val iplRepository: Lazy<IplRepository>) {

    fun execute(key: String): Observable<Result<LeaderBoardResponse>> {
        return UseCase.wrapSingle(rxSingle { iplRepository.get().getLeaderBoardDetails(key) })
    }
}
