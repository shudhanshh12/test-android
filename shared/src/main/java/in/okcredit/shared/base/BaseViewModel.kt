package `in`.okcredit.shared.base

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

abstract class BaseViewModel<S : UiState, P : UiState.Partial<S>, E : BaseViewEvent> protected constructor(
    initialState: S
) : BasePresenter<S, P>(initialState), PresenterWithViewEvents<S, E> {

    private val viewEventsRelay: PublishRelay<E> = PublishRelay.create()

    override fun viewEvent(): Observable<E> = viewEventsRelay

    protected fun emitViewEvent(event: E) = viewEventsRelay.accept(event)

    fun <ResponseT> wrap(observable: Observable<ResponseT>) = UseCase.wrapObservable(observable)

    fun wrap(completable: Completable) = UseCase.wrapCompletable(completable)

    fun <ResponseT> wrap(maybe: Maybe<ResponseT>) = UseCase.wrapMaybe(maybe)

    fun <ResponseT> wrap(single: Single<ResponseT>): Observable<Result<ResponseT>> = UseCase.wrapSingle(single)

    fun <ResponseT> wrap(block: suspend () -> ResponseT): Observable<Result<ResponseT>> = UseCase.wrapCoroutine(block)
}
