package tech.okcredit.contacts.ui

import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import `in`.okcredit.shared.usecase.Result
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.launch
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.rxjava.SchedulerProvider
import tech.okcredit.contacts.R
import tech.okcredit.contacts.analytics.ContactsTracker
import tech.okcredit.contacts.contract.ContactsRepository
import tech.okcredit.contacts.databinding.AddOkcreditContactInappBottomSheetBinding
import tech.okcredit.contacts.store.preference.ContactPreference
import tech.okcredit.contacts.usecase.AddOkCreditContact
import tech.okcredit.contacts.usecase.GetOkCreditContact
import tech.okcredit.feature_help.contract.GetSupportNumber
import javax.inject.Inject

class AddOkCreditContactInAppBottomSheet : ExpandedBottomSheetDialogFragment() {

    private val binding: AddOkcreditContactInappBottomSheetBinding by viewLifecycleScoped(
        AddOkcreditContactInappBottomSheetBinding::bind
    )

    @Inject
    internal lateinit var schedulerProvider: Lazy<SchedulerProvider>

    @Inject
    internal lateinit var contactPreference: Lazy<ContactPreference>

    @Inject
    internal lateinit var contactsTracker: Lazy<ContactsTracker>

    @Inject
    internal lateinit var contactsRepository: Lazy<ContactsRepository>

    @Inject
    internal lateinit var addOkCreditContact: Lazy<AddOkCreditContact>

    @Inject
    internal lateinit var getOkCreditContact: Lazy<GetOkCreditContact>

    @Inject
    internal lateinit var getSupportNumber: Lazy<GetSupportNumber>

    private var name: String = ""
    private var number: String = ""

    companion object {
        private const val TAG = "AddOkCreditContactInAppBottomSheet"
        const val ADD_CONTACT_REQUEST = 1001
        const val DEFAULT_CONTACT_NAME = "OKCredit"
        const val INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED = "finishActivityOnSaveCompleted"

        fun show(fragmentManager: FragmentManager) {
            AddOkCreditContactInAppBottomSheet().show(fragmentManager, TAG)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return AddOkcreditContactInappBottomSheetBinding.inflate(layoutInflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch { contactPreference.get().setContactInappDisplayed(false) }
        contactsTracker.get().trackInAppDisplayed()
        initClickListener()
        getOkCreditContact()
    }

    // TODO Moved this to Presenter
    private fun getOkCreditContact() {
        getOkCreditContact.get().execute(Unit)
            .subscribeOn(schedulerProvider.get().io())
            .observeOn(schedulerProvider.get().ui())
            .filter { it !is Result.Progress }
            .subscribe {
                when (it) {
                    is Result.Success -> {
                        binding.apply {
                            name = it.value.name
                            number = it.value.number
                            tvName.text = name
                            tvNumber.text = number
                        }
                    }
                    else -> {
                        val helpNumber = getSupportNumber.get().supportNumber
                        binding.apply {
                            name = DEFAULT_CONTACT_NAME
                            number = helpNumber
                            tvName.text = name
                            tvNumber.text = number
                        }
                    }
                }
            }
    }

    private fun initClickListener() {
        binding.mbSaveContact.setOnClickListener {
            contactsTracker.get().trackInAppInteracted()
            addOkCreditContact.get().execute()
                .subscribeOn(schedulerProvider.get().io())
                .observeOn(schedulerProvider.get().ui())
                .filter { it !is Result.Progress }
                .subscribe {
                    when (it) {
                        is Result.Success -> {
                            if (it.value) {
                                dismiss()
                            } else {
                                openSaveContactDeviceScreen(name, number)
                            }
                        }
                        else -> dismiss()
                    }
                }
        }
    }

    private fun openSaveContactDeviceScreen(name: String, number: String) {
        try {
            val contactIntent =
                Intent(ContactsContract.Intents.Insert.ACTION)
            contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE
            contactIntent
                .putExtra(ContactsContract.Intents.Insert.NAME, name)
                .putExtra(ContactsContract.Intents.Insert.PHONE, number)
                .putExtra(INTENT_KEY_FINISH_ACTIVITY_ON_SAVE_COMPLETED, true)

            startActivityForResult(contactIntent, ADD_CONTACT_REQUEST)
        } catch (e: Exception) {
            shortToast(getString(R.string.err_default))
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getResult(requestCode, resultCode)
    }

    private fun getResult(requestCode: Int, resultCode: Int) {
        if (requestCode != ADD_CONTACT_REQUEST) return
        when (resultCode) {
            Activity.RESULT_OK -> {
                contactsTracker.get().trackOkCreditContactSaved(ContactsTracker.PropertyValue.MANUAL)
                contactsRepository.get().scheduleAcknowledgeContactSaved().subscribe()
            }
            else -> {
                contactsTracker.get().trackOkCreditContactSkipped()
            }
        }
        dismiss()
    }
}
