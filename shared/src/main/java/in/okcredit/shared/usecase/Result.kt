package `in`.okcredit.shared.usecase

sealed class Result<T> {
    data class Progress<T>(val value: Int = 0, val maxValue: Int = 100) : Result<T>()
    data class Success<T>(val value: T) : Result<T>()
    data class Failure<T>(val error: Throwable) : Result<T>()
}
