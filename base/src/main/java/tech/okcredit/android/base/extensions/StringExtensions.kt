package tech.okcredit.android.base.extensions

fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")

fun String?.isNotNullOrBlank() = !this.isNullOrBlank()

fun String?.itOrBlank(): String = this ?: ""

fun String.encloseWithPercentageSymbol() = "%$this%"
