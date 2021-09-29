package `in`.okcredit.collection_ui.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import android.content.pm.PackageManager
import io.reactivex.Observable
import javax.inject.Inject

// return permission granded info of a requested permission
class IsPermissionGranted @Inject constructor(
    private val context: Context
) : UseCase<String, Boolean> {
    override fun execute(req: String): Observable<Result<Boolean>> {
        val res = context.checkCallingOrSelfPermission(req)
        val isPermissionAllowed = (res == PackageManager.PERMISSION_GRANTED)
        return UseCase.wrapObservable(Observable.just(isPermissionAllowed))
    }
}
