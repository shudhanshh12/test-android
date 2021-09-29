package `in`.okcredit.backend.contract

import `in`.okcredit.shared.usecase.Result
import io.reactivex.Observable
import tech.okcredit.android.auth.Credential

interface Authenticate {

    fun execute(req: Credential): Observable<Result<Pair<Boolean, Boolean>>>
}
