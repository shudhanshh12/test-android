package tech.okcredit.android.base.extensions

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import tech.okcredit.android.base.json.GsonUtils

// unique name for any object in form of `ClassName(object hex code)`
val Any.className: String
    get() = "${javaClass.simpleName}(${Integer.toHexString(hashCode())})"

// class name for any object
val Any.classType: String
    get() = javaClass.simpleName

// json for any object
fun Any.json(): String = GsonUtils.gson().toJson(this)

fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}

inline fun <A : Any, B : Any> ifLet(a: A?, b: B?, block: (A, B) -> Unit) {
    a?.let { b?.let { block(a, b) } }
}

inline fun <A : Any, B : Any, C : Any> ifLet(a: A?, b: B?, c: C?, block: (A, B, C) -> Unit) {
    a?.let { b?.let { c?.let { block(a, b, c) } } }
}

fun <T> List<T>.isEqual(second: List<T>): Boolean {
    if (this.size != second.size) {
        return false
    }

    return this.zip(second).all { (x, y) -> x == y }
}

fun String?.ifNullOrBlank(defaultValue: () -> String?): String? =
    if (isNullOrBlank()) defaultValue() else this

@Suppress("NOTHING_TO_INLINE")
inline fun Retrofit.Builder.delegatingCallFactory(
    delegate: dagger.Lazy<OkHttpClient>,
): Retrofit.Builder = callFactory {
    delegate.get().newCall(it)
}
