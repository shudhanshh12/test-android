package merchant.okcredit.gamification.ipl.game.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.rx2.rxSingle
import merchant.okcredit.gamification.ipl.data.IplRepository
import merchant.okcredit.gamification.ipl.game.data.server.model.response.ActiveMatches
import javax.inject.Inject

class GetActiveMatches @Inject constructor(private val iplRepository: Lazy<IplRepository>) {

    fun execute(): Observable<Result<ActiveMatches>> {
        return UseCase.wrapSingle(
            rxSingle { iplRepository.get().getActiveMatches() }
        )
    }
}
