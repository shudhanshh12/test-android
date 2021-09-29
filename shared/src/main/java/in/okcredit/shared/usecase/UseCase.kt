package `in`.okcredit.shared.usecase

import `in`.okcredit.shared.utils.dropBreadcrumb
import `in`.okcredit.shared.utils.getFilteredBreadcrumbException
import androidx.annotation.Keep
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import kotlinx.coroutines.rx2.rxSingle
import tech.okcredit.base.exceptions.ExceptionUtils
import timber.log.Timber

// Warning: File Name changes for this class can break filtering UseCase exception.
@Keep
interface UseCase<RequestT, ResponseT> {
    fun execute(req: RequestT): Observable<Result<ResponseT>>

    companion object {

        fun <ResponseT> wrapObservable(s: Observable<ResponseT>): Observable<Result<ResponseT>> {
            return s.map<Result<ResponseT>> { Result.Success(it) }
                .startWith(Result.Progress())
                .dropBreadcrumb() // Added for Better crash logs. it helps call stack pointing to the point where the chain was created
                .onErrorReturn {
                    logUseCaseErrors(it)
                    Result.Failure((it as CompositeException).exceptions[0])
                }
        }

        fun <ResponseT> wrapCoroutine(block: suspend () -> ResponseT): Observable<Result<ResponseT>> {
            return rxSingle<Result<ResponseT>> {
                val response = block.invoke()
                return@rxSingle Result.Success(response)
            }.onErrorReturn {
                logUseCaseErrors(it)
                return@onErrorReturn Result.Failure(it)
            }.toObservable()
                .startWith(Result.Progress())
        }

        fun <ResponseT> wrapSingle(s: Single<ResponseT>): Observable<Result<ResponseT>> {
            return wrapObservable<ResponseT>(s.flatMapObservable { Observable.just(it) })
        }

        fun wrapCompletable(c: Completable): Observable<Result<Unit>> {
            return wrapSingle(
                c.andThen(Single.just(Unit))
            )
        }

        private fun logUseCaseErrors(it: Throwable) {
            Timber.e(it)
            ExceptionUtils.logUsecaseError(getFilteredBreadcrumbException(it, "UseCase Error"))
        }

        fun <ResponseT> wrapMaybe(maybe: Maybe<ResponseT>): Observable<Result<ResponseT>> {
            return wrapObservable(maybe.flatMapObservable { Observable.just(it) })
        }
    }
}
