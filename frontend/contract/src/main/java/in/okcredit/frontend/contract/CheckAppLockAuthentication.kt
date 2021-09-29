package `in`.okcredit.frontend.contract

import `in`.okcredit.frontend.contract.data.AppResume
import io.reactivex.Single

interface CheckAppLockAuthentication {
    fun execute(): Single<AppResume>
}
