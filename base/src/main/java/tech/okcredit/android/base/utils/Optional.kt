package tech.okcredit.android.base.utils

sealed class Optional<out T> {

    data class Present<T>(val `object`: T) : Optional<T>()

    object Absent : Optional<Nothing>()
}

fun <T> T?.ofNullable() = if (this == null) {
    Optional.Absent
} else {
    Optional.Present(this)
}
