package merchant.okcredit.gamification.ipl.game.usecase

import `in`.okcredit.shared.usecase.Result
import `in`.okcredit.shared.usecase.UseCase
import android.content.Context
import com.tomash.androidcontacts.contactgetter.entity.PhoneNumber
import com.tomash.androidcontacts.contactgetter.main.ContactDataFactory
import com.tomash.androidcontacts.contactgetter.main.contactsGetter.ContactsGetterBuilder
import com.tomash.androidcontacts.contactgetter.main.contactsSaver.ContactsSaverBuilder
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

class AddOkCreditContact @Inject constructor(
    private val context: Lazy<Context>,
) {

    fun execute(OkName: String, okcWhatsAppNumber: String): Observable<Result<Unit>> {
        return UseCase.wrapCompletable(

            Completable.create { completable ->
                if (isNumberAlreadyInPhoneBook(okcWhatsAppNumber)) {
                    completable.onComplete()
                } else {
                    try {
                        val data = PhoneNumber(okcWhatsAppNumber, OkName)
                        val phone = listOf(data)
                        val contact = ContactDataFactory.createEmpty().apply {
                            compositeName = OkName
                            phoneList = phone
                        }
                        ContactsSaverBuilder(context.get()).saveContact(contact)
                        completable.onComplete()
                    } catch (e: Exception) {
                        completable.onError(e)
                        RecordException.recordException(e as Throwable)
                    }
                }
            }

        )
    }

    private fun isNumberAlreadyInPhoneBook(number: String): Boolean {
        val query = ContactsGetterBuilder(context.get())
            .withPhone(number)
            .firstOrNull()
        return query != null
    }
}
