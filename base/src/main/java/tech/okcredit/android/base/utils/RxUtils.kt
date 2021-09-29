package tech.okcredit.android.base.utils

import io.reactivex.SingleEmitter
import io.reactivex.subjects.PublishSubject
import java.lang.Exception

fun <T> SingleEmitter<T>.onSuccessIfNotDisposed(value: T) {
    if (!isDisposed) onSuccess(value)
}

fun <T> SingleEmitter<T>.onErrorIfNotDisposed(exception: Exception) {
    if (!isDisposed) onError(exception)
}

fun <T> PublishSubject<T>.onCompleteIfObserving() {
    if (hasObservers()) onComplete()
}

fun <T> PublishSubject<T>.onErrorIfObserving(throwable: Throwable) {
    if (hasObservers()) onError(throwable)
}
