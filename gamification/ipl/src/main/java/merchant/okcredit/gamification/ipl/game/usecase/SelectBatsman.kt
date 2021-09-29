package merchant.okcredit.gamification.ipl.game.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.gamification.ipl.data.IplRepository
import merchant.okcredit.gamification.ipl.game.data.server.model.response.OnboardingDetails
import javax.inject.Inject

class SelectBatsman @Inject constructor(private val iplRepository: Lazy<IplRepository>) {

    fun execute(matchId: String, choiceId: String): Observable<Result<OnboardingDetails>> {
        return UseCase.wrapSingle(
            rxSingle { iplRepository.get().selectBatsman(matchId, choiceId) }
        )
    }
}
