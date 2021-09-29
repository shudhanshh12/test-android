package `in`.okcredit.merchant.core.common

import io.reactivex.Completable
import java.util.*

object CoreUtils {
    fun generateRandomId() = UUID.randomUUID().toString()

    fun validateName(name: String?): Completable {
        return if (name.isNullOrEmpty() || name.length <= 30) {
            Completable.complete()
        } else {
            Completable.error(CoreException.InvalidName)
        }
    }
}
