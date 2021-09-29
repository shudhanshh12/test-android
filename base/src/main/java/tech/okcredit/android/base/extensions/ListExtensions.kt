package tech.okcredit.android.base.extensions

fun <E> MutableList<E>.addIfNotExists(element: E) {
    if (this.contains(element).not()) {
        this.add(element)
    }
}
