@file:JvmName("ErrorUtils")
@file:JvmMultifileClass

package tech.okcredit.android.base.error

import io.reactivex.exceptions.CompositeException

// checks if the given error is of type T or is caused by an error of type T
inline fun <reified T : Throwable> Throwable?.check(): Boolean = extract<T>() != null

// extracts error of type F from the given error, if available
inline fun <reified T : Throwable> Throwable?.extract(): T? {
    if (this == null) return null
    var currentError = this
    while (currentError != null) {
        if (currentError is T) return currentError
        currentError = currentError.cause
    }
    return null
}

// check() checks if the given CompositeException contain the given error
inline fun <reified T : Throwable> CompositeException?.extract(): T? {
    if (this == null) return null
    for (throwable in exceptions) {
        var currentError = throwable
        while (currentError != null) {
            if (currentError is T) return currentError
            currentError = currentError.cause
        }
    }
    return null
}
