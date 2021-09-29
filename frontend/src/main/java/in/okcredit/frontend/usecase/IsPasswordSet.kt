package `in`.okcredit.frontend.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import io.reactivex.Observable
import io.reactivex.Single
import tech.okcredit.android.auth.AuthService
import tech.okcredit.android.base.utils.ThreadUtils
import javax.inject.Inject

class IsPasswordSet @Inject constructor(private val authService: AuthService) : UseCase<Unit, Boolean> {

    override fun execute(req: Unit): Observable<Result<Boolean>> {
        return UseCase.wrapSingle(
            Single.fromCallable { authService.isPasswordSet() }.subscribeOn(ThreadUtils.api())
        )
    }
}
