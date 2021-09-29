package `in`.okcredit.shared.utils

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import tech.okcredit.base.exceptions.ExceptionUtils

fun Disposable.addTo(autoDisposable: AutoDisposable) {
    autoDisposable.add(this)
}

fun Disposable.addTo(autoDisposable: LayoutAutoDispose) {
    autoDisposable.add(this)
}

val <T> T.exhaustive: T
    get() = this

class BreadcrumbException : Exception()

fun <T> Observable<T>.dropBreadcrumb(): Observable<T> {
    val breadcrumb = BreadcrumbException()
    return this.onErrorResumeNext { error: Throwable ->
        throw CompositeException(error, breadcrumb)
    }
}

// Hack: Filtering StackTrace for avoiding firebase grouping.
// Removing CompositeException, BreadcrumbException and All Unnecessary RxChain which is added with exception.
fun getFilteredBreadcrumbException(it: Throwable, tag: String): Exception {
    if (it is CompositeException) {
        if (it.exceptions.size == 2) {
            val exception = Exception("$tag: ${it.cause.cause?.message}")
            var isOnErrorReached = false
            exception.stackTrace = it.exceptions[1].stackTrace.filterIndexed { index, stacktrace ->
                if (stacktrace.methodName == "onError" || stacktrace.methodName == "onNext") {
                    isOnErrorReached = true
                }
                return@filterIndexed index > 1 && isOnErrorReached.not() && stacktrace.className != "UseCase.kt"
            }.toTypedArray()
            ExceptionUtils.logUsecaseError(exception)
            return exception
        }
    }
    return Exception(tag, it)
}
