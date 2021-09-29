package merchant.okcredit.gamification.ipl.view.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.gamification.ipl.data.IplRepository
import javax.inject.Inject

class SetEducationView @Inject constructor(private val iplRepository: Lazy<IplRepository>) {

    fun execute(): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(
            iplRepository.get().setGamesEducationView()
        )
    }
}
