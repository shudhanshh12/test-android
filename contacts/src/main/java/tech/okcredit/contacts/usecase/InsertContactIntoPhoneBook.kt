package tech.okcredit.contacts.usecase

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import dagger.Lazy
import dagger.Reusable
import io.reactivex.Completable
import tech.okcredit.android.base.crashlytics.RecordException
import javax.inject.Inject

@Reusable
class InsertContactIntoPhoneBook @Inject constructor(
    private val context: Lazy<Context>,
) {

    fun execute(name: String, mobile: String) = Completable.fromAction {
        try {
            val ops = ArrayList<ContentProviderOperation>()
            val rawContactID: Int = ops.size

            ops.add(
                ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                    .withValue(RawContacts.ACCOUNT_TYPE, null)
                    .withValue(RawContacts.ACCOUNT_NAME, null)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(
                        Data.MIMETYPE,
                        CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build()
            )

            ops.add(
                ContentProviderOperation.newInsert(Data.CONTENT_URI)
                    .withValueBackReference(Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Phone.NUMBER, mobile)
                    .withValue(CommonDataKinds.Phone.TYPE, CommonDataKinds.Phone.TYPE_MOBILE)
                    .build()
            )

            context.get().contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: Exception) {
            RecordException.recordException(e)
        }
    }
}
