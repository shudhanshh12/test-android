package `in`.okcredit.backend.contract

import io.reactivex.Completable

interface Signout {

    fun isInProgress(): Boolean

    fun execute(password: String?): Completable

    fun logout(): Completable
}
