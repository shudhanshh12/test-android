package tech.okcredit.contacts.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import dagger.Lazy
import tech.okcredit.android.base.activity.OkcActivity
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.contacts.R
import tech.okcredit.contacts.analytics.ContactsTracker
import tech.okcredit.contacts.databinding.AddOkcreditContactActivityBinding
import javax.inject.Inject

class AddOkcreditContactTransparentActivity : OkcActivity() {

    private val binding: AddOkcreditContactActivityBinding
        by viewLifecycleScoped(AddOkcreditContactActivityBinding::inflate)

    @Inject
    internal lateinit var contactsTracker: Lazy<ContactsTracker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(binding.root)

        openPhoneBookForSavingContacts(
            intent.getStringExtra(CONTACT_NAME),
            intent.getStringExtra(PHONE_NUMBER)
        )
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode == Activity.RESULT_OK
                && requestCode == ADD_CONTACT_REQUEST -> {
                contactsTracker.get().trackOkCreditContactSaved(ContactsTracker.PropertyValue.MANUAL)
                finish()
            }
            else -> {
                contactsTracker.get().trackOkCreditContactSkipped()
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {

        fun getIntent(
            context: Context,
            contactName: String?,
            phoneNumber: String?,
        ): Intent {
            return Intent(context, AddOkcreditContactTransparentActivity::class.java)
                .putExtra(CONTACT_NAME, contactName)
                .putExtra(PHONE_NUMBER, phoneNumber)
        }

        const val PHONE_NUMBER = "phone_number"
        const val CONTACT_NAME = "contact_name"
        const val ADD_CONTACT_REQUEST = 10011
        const val INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED = "finishActivityOnSaveCompleted"
    }

    private fun openPhoneBookForSavingContacts(
        contactName: String?,
        phoneNumber: String?
    ) {
        if (contactName != null && phoneNumber != null) {
            try {
                val contactIntent =
                    Intent(ContactsContract.Intents.Insert.ACTION)
                contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
                contactIntent
                    .putExtra(ContactsContract.Intents.Insert.NAME, contactName)
                    .putExtra(ContactsContract.Intents.Insert.PHONE, phoneNumber)
                    .putExtra(INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true)

                startActivityForResult(contactIntent, ADD_CONTACT_REQUEST)
            } catch (e: Exception) {
                contactsTracker.get().trackFailedToOpenPhoneBook(e.message)
                finish()
            }
        } else {
            contactsTracker.get().trackFailedToOpenPhoneBook(
                "Contact or Phone Number Not Found"
            )
            finish()
        }
    }
}
