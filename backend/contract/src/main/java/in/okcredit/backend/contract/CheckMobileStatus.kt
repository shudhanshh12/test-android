package `in`.okcredit.backend.contract

import io.reactivex.Single

interface CheckMobileStatus {

    fun execute(mobile: String?): Single<Boolean>
}
