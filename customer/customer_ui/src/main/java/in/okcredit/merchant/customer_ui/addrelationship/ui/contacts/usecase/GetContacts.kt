package `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.usecase

import `in`.okcredit.home.IGetRelationsNumbersAndBalance
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipEpoxyModels.ContactModel
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.toContactModel
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.contacts.contract.ContactsRepository
import javax.inject.Inject

class GetContacts @Inject constructor(
    private val contactsRepository: Lazy<ContactsRepository>,
    private val relationsNumbersAndBalance: Lazy<IGetRelationsNumbersAndBalance>,
) {

    fun execute(searchQuery: String?): Observable<Pair<List<ContactModel>, String?>> {
        return Observable.combineLatest(
            getContactsFromPhoneBook(searchQuery),
            getRelationNumberAndBalance()
        ) { contactList, relationNumberModel ->
            contactList.map { contact ->
                val findRelationModel = relationNumberModel.firstOrNull {
                    it.mobile == contact.mobile
                }
                if (findRelationModel != null) {
                    contact.toContactModel(
                        id = findRelationModel.relationshipId,
                        balance = findRelationModel.balance,
                        relationshipType = findRelationModel.relationshipType
                    )
                } else {
                    contact.toContactModel()
                }
            } to searchQuery
        }
    }

    private fun getContactsFromPhoneBook(searchQuery: String?) =
        contactsRepository.get().getContactsAndSyncWithPhonebook(searchQuery).map { it.contactList }

    private fun getRelationNumberAndBalance() =
        relationsNumbersAndBalance.get().execute()
}
