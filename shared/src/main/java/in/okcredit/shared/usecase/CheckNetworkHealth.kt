package `in`.okcredit.shared.usecase

import android.content.Context
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import tech.okcredit.android.base.extensions.isConnectedToInternet
import tech.okcredit.base.network.NetworkError
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Deprecated(message = "Use GetConnectionStatus usecase instead")
class CheckNetworkHealth @Inject constructor(private val context: Context) : UseCase<Unit, Unit> {

    companion object {
        private const val INTERVAL_DEFAULT = 10L
        private const val INTERVAL_NOT_CONNECTED = 1L
    }

    override fun execute(req: Unit): Observable<Result<Unit>> {
        val interval = BehaviorSubject.createDefault(INTERVAL_NOT_CONNECTED)
        val networkError = NetworkError(cause = RuntimeException("not connected to internet"))

        return interval.switchMap { Observable.interval(it, TimeUnit.SECONDS) }
            .map {
                if (context.isConnectedToInternet()) {
                    Result.Success(Unit)
                } else {
                    Result.Failure<Unit>(networkError)
                }
            }
            .doOnNext {
                if (it is Result.Success) {
                    interval.onNext(INTERVAL_DEFAULT)
                } else if (it is Result.Failure) {
                    interval.onNext(INTERVAL_NOT_CONNECTED)
                }
            }.startWith(Result.Success(Unit))
            .distinctUntilChanged()
    }

    fun isConnectedToInternet() = context.isConnectedToInternet()
}
