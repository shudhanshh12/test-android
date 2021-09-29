package tech.okcredit.help.helpHome.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.help.utils.PhoneBookUtils
import javax.inject.Inject

class AddOkcreditNumberToContact @Inject constructor(val context: Lazy<Context>) {
    fun execute(mobile: String): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(PhoneBookUtils.addOkCreditNumberToContact(context.get(), mobile))
    }
}
