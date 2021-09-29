package `in`.okcredit.shared.usecase

import `in`.okcredit.shared.network.NetworkRepository
import dagger.Lazy
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetConnectionStatus @Inject constructor(private val repository: Lazy<NetworkRepository>) {

    fun execute(): Observable<Result<Boolean>> {
        return UseCase.wrapObservable(repository.get().getConnectionStatus())
    }

    fun executeUnwrapped(): Observable<Boolean> = repository.get().getConnectionStatus()

    fun executeWithTimeout(): Observable<Boolean> = repository.get().getConnectionStatus()
        .timeout(1, TimeUnit.SECONDS)
    // if this will emitted internet is connected within in 2 sec then it should failed
}
