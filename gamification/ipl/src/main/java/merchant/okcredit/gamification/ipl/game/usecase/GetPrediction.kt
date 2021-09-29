package merchant.okcredit.gamification.ipl.game.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.gamification.ipl.data.IplRepository
import merchant.okcredit.gamification.ipl.game.data.server.model.response.PredictionResponse
import javax.inject.Inject

class GetPrediction @Inject constructor(private val iplRepository: Lazy<IplRepository>) {

    fun execute(matchId: String): Observable<Result<PredictionResponse>> {
        return UseCase.wrapSingle(
            rxSingle { iplRepository.get().getPrediction(matchId) }
        )
    }
}
