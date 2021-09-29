package tech.okcredit.contacts.usecase

import android.content.Context
import android.provider.ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP
import com.tomash.androidcontacts.contactgetter.main.FieldType
import com.tomash.androidcontacts.contactgetter.main.contactsGetter.ContactsGetterBuilder
import dagger.Lazy
import dagger.Reusable
import tech.okcredit.android.base.extensions.isGreaterThanZero
import tech.okcredit.android.base.extensions.itOrBlank
import tech.okcredit.android.base.utils.MobileUtils
import tech.okcredit.contacts.ContactUtils
import tech.okcredit.contacts.contract.model.Contact
import javax.inject.Inject

@Reusable
class GetPhoneBookContacts @Inject constructor(
    private val context: Lazy<Context>,
) {

    fun execute(): List<Contact> {
        val allContacts = ContactsGetterBuilder(context.get())
            .addField(FieldType.PHONE_NUMBERS, FieldType.NAME_DATA)
            .setSortOrder(CONTACT_LAST_UPDATED_TIMESTAMP)
            .buildList()

        val contactModelList = mutableListOf<Contact>()

        for (contactItem in allContacts) {
            for (phoneItem in contactItem.phoneList) {
                val phone: String = MobileUtils.normalize(phoneItem.mainData)
                if (MobileUtils.isPhoneNumberValid(phone)) {
                    val mobile = normalizePhone(phone)
                    if (contactModelList.find { it.mobile == mobile } == null) {
                        val name = contactItem.compositeName.itOrBlank()
                        val picUri = contactItem.photoUri?.toString().itOrBlank()
                        val phoneBookId = ContactUtils.generateContactId(name, mobile)
                        val timestamp = getLastModificationDate(contactItem.lastModificationDate)
                        contactModelList.add(
                            Contact(
                                phonebookId = phoneBookId,
                                name = name,
                                mobile = mobile,
                                picUri = picUri,
                                timestamp = timestamp,
                                synced = false,
                            )
                        )
                    }
                }
            }
        }
        return contactModelList
    }

    private fun normalizePhone(phoneNumber: String): String {
        return phoneNumber.replace("[^0-9]".toRegex(), "")
    }

    private fun getLastModificationDate(lastModificationDate: Long): Long {
        return if (lastModificationDate.isGreaterThanZero()) {
            lastModificationDate
        } else {
            System.currentTimeMillis()
        }
    }
}
