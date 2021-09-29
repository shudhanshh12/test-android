package tech.okcredit.home.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.android.auth.usecases.IsPasswordSet
import javax.inject.Inject

class CheckPassword @Inject constructor(private val isPasswordSet: Lazy<IsPasswordSet>) {
    fun execute(): Observable<Result<Boolean>> {
        return UseCase.wrapSingle(isPasswordSet.get().execute())
    }
}

sealed class PinDialog {
    class EnterNewPin : PinDialog()
    class UpdatePin : PinDialog()
}
