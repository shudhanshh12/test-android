package tech.okcredit.help.utils

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import io.reactivex.Completable
import tech.okcredit.android.base.utils.ThreadUtils
import tech.okcredit.base.exceptions.ExceptionUtils
import timber.log.Timber
import java.util.ArrayList

object PhoneBookUtils {
    fun addOkCreditNumberToContact(context: Context, mobile: String): Completable {
        return Completable
            .fromAction {
                val displayName = "OKCredit"

                val ops = ArrayList<ContentProviderOperation>()

                ops.add(
                    ContentProviderOperation
                        .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build()
                )

                // ------------------------------------------------------ Names
                ops.add(
                    ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract
                                .CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.StructuredName
                                .DISPLAY_NAME,
                            displayName
                        )
                        .build()
                )

                // ------------------------------------------------------ Mobile Number
                ops.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract
                                .CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobile)
                        .withValue(
                            ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                        )
                        .build()
                )

                // Asking the Contact provider to create a new contact
                try {
                    context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to add contact")
                    ExceptionUtils.logException("Error: addOkCreditNumberToContact", e)
                }
            }
            .subscribeOn(ThreadUtils.newThread())
    }
}
