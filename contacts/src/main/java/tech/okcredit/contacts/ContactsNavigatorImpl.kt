package tech.okcredit.contacts

import androidx.fragment.app.FragmentManager
import tech.okcredit.contacts.contract.ContactsNavigator
import tech.okcredit.contacts.ui.AddOkCreditContactInAppBottomSheet
import javax.inject.Inject

class ContactsNavigatorImpl @Inject constructor() : ContactsNavigator {
    override fun showAddOkCreditContactInAppBottomSheet(fragmentManager: FragmentManager) {
        AddOkCreditContactInAppBottomSheet.show(fragmentManager)
    }
}
