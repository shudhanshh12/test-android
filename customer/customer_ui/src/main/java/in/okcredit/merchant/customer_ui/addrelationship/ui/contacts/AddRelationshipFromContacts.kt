package `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.addrelationship.AddRelationshipActivity
import `in`.okcredit.merchant.customer_ui.addrelationship.analytics.AddRelationshipEventTracker
import `in`.okcredit.merchant.customer_ui.addrelationship.enum.AddRelationshipFailedError
import `in`.okcredit.merchant.customer_ui.addrelationship.enum.AddRelationshipFailedError.*
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContactsContract.*
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContactsContract.Companion.ADD_CUSTOMER
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views.AddManuallyView
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.views.ContactItemView
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.AddRelationshipCyclicError
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually.AddRelationshipManually
import `in`.okcredit.merchant.customer_ui.databinding.FragmentAddRelationshipFromContactsBinding
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import tech.okcredit.android.base.extensions.*
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class AddRelationshipFromContacts :
    BaseFragment<State, ViewEvent, Intent>(
        "AddRelationshipFromContacts",
        R.layout.fragment_add_relationship_from_contacts
    ),
    ContactItemView.ContactListener,
    AddManuallyView.AddManuallyListener {

    companion object {
        const val ARG_ADD_RELATIONSHIP_TYPE = "arg_add_relationship_type"
        const val ARG_CAN_SHOW_ADD_MANUALLY_OPTION = "arg_can_show_add_manually_option"
        const val ARG_SOURCE = "arg_source_relationship_from_contacts"
        const val ARG_OPEN_FOR_RESULT = "arg_open_for_result_relationship_from_contacts"
        const val ARG_DEFAULT_MODE = "arg_default_mode"

        fun newInstance(
            addRelationshipType: Int,
            canShowAddManuallyOption: Boolean = true,
            source: String,
            openForResult: Boolean = false,
            defaultMode: String = "Contact",
        ): AddRelationshipFromContacts {
            val args = bundleOf(
                ARG_ADD_RELATIONSHIP_TYPE to addRelationshipType,
                ARG_CAN_SHOW_ADD_MANUALLY_OPTION to canShowAddManuallyOption,
                ARG_SOURCE to source,
                ARG_DEFAULT_MODE to defaultMode,
                ARG_OPEN_FOR_RESULT to openForResult
            )
            return AddRelationshipFromContacts().apply {
                arguments = args
            }
        }
    }

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var tracker: Lazy<AddRelationshipEventTracker>

    @Inject
    lateinit var controller: Lazy<AddRelationshipFromContactsController>

    val binding: FragmentAddRelationshipFromContactsBinding by viewLifecycleScoped(
        FragmentAddRelationshipFromContactsBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        binding.apply {
            contactRecyclerview.apply {
                adapter = controller.get().adapter
                layoutManager = LinearLayoutManager(activity)
            }
            searchImg.setOnClickListener {
                pushIntent(Intent.ShowSearchInput(true))
            }
            btnClose.setOnClickListener {
                searchInput.setText("")
                pushIntent(Intent.SearchQuery(""))
                hideSoftKeyboard()
                pushIntent(Intent.ShowSearchInput(false))
            }
            backButton.setOnClickListener {
                activity?.finish()
            }
            searchInput
                .afterTextChangedDebounce(300L) {
                    pushIntent(Intent.SearchQuery(it))
                }
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun loadIntent(): UserIntent = Intent.Load

    override fun render(state: State) {
        controller.get().setData(state.addRelationshipEpoxyModels)

        if (state.canShowSearchInput) {
            showSearchInput()
        } else {
            hideSearchInput()
        }
        checkForHeaderText(state)
    }

    private fun checkForHeaderText(state: State) {
        if (state.relationshipType == ADD_CUSTOMER) {
            binding.selectCustomerTitle.text = getString(R.string.t_001_addrel_ab_title_add_cust)
        } else {
            binding.selectCustomerTitle.text = getString(R.string.t_001_addrel_ab_title_add_supp)
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.AddRelationshipFailed -> showAddRelationshipFailedDialog(
                event.id,
                event.name,
                event.mobile,
                event.profile,
                event.errorType,
                event.exception
            )
            is ViewEvent.GoToCustomerFragment -> gotoCustomerFragment(event.customerId)
            is ViewEvent.GoToSupplierFragment -> gotoSupplierFragment(event.supplierId)
            is ViewEvent.ShowError -> shortToast(event.message)
            is ViewEvent.ReturnResult -> setFragmentResult(event.contactModel)
            is ViewEvent.SetResultRelationshipAddedSuccessfully -> setActivityResult()
        }
    }

    private fun setActivityResult() {
        activity?.setResult(Activity.RESULT_OK)
    }

    // TODO(Harshit) Cleanup
    private fun showAddRelationshipFailedDialog(
        id: String?,
        name: String?,
        mobile: String?,
        profile: String?,
        errorType: AddRelationshipFailedError,
        exception: Throwable,
    ) {
        tracker.get().trackAddTransactionFailed(
            reason = "Conflict",
            type = "Conflict: ${errorType.value}",
            exception = exception.toString(),
            flow = "Add Relation",
            relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer" else "Supplier",
            source = getCurrentState().source,
            defaultMode = getCurrentState().defaultMode
        )
        when (errorType) {
            MOBILE_CONFLICT_ACCOUNT_WITH_CUSTOMER -> showMobileConflictAccountWithCustomer(
                id,
                name,
                mobile,
                profile,
                errorType
            )
            DELETED_CUSTOMER_CYCLIC_ACCOUNT -> showDeletedCustomerCyclicAccount(
                id,
                name,
                mobile,
                profile,
                errorType
            )
            DELETED_SUPPLIER_CYCLIC_ACCOUNT -> showDeletedSupplierCyclicAccount(
                id,
                name,
                mobile,
                profile,
                errorType
            )
            ACTIVE_CUSTOMER_CYCLIC_ACCOUNT -> showActiveCustomerCyclicAccount(
                id,
                name,
                mobile,
                profile,
                errorType
            )
            ACTIVE_SUPPLIER_CYCLIC_ACCOUNT -> showActiveSupplierCyclicAccount(
                id,
                name,
                mobile,
                profile,
                errorType
            )
            MOBILE_CONFLICT_ACCOUNT_WITH_SUPPLIER -> showMobileConflictAccountWithSupplier(
                id,
                name,
                mobile,
                profile,
                errorType
            )
        }
    }

    private fun showMobileConflictAccountWithSupplier(
        id: String?,
        name: String?,
        mobile: String?,
        profile: String?,
        errorType: AddRelationshipFailedError,
    ) {
        if (id == null) return
        hideSoftKeyboard()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(500L)
            AddRelationshipCyclicError.showDialog(
                fragmentManager = childFragmentManager,
                title = getString(R.string.t_001_addrel_cant_add_supplier_popup_title),
                name = name,
                number = mobile,
                profileImg = profile,
                description = getString(R.string.t_001_addrel_cant_add_customer_popup_sub_txt),
                canShowMoveCta = false,
                viewRelationshipType = AddRelationshipCyclicError.SUPPLIER,
                relationshipId = id,
                moveRelationshipType = null,
                shouldRequireReactivation = false,
                source = getCurrentState().source,
                typeOfConflict = errorType.value,
                defaultMode = getCurrentState().defaultMode,
            )
        }
    }

    private fun showMobileConflictAccountWithCustomer(
        id: String?,
        name: String?,
        mobile: String?,
        profile: String?,
        errorType: AddRelationshipFailedError,
    ) {
        if (id == null) return
        hideSoftKeyboard()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(500L)
            AddRelationshipCyclicError.showDialog(
                fragmentManager = childFragmentManager,
                title = getString(R.string.t_001_addrel_cant_add_customer_popup_title),
                name = name,
                number = mobile,
                profileImg = profile,
                description = getString(R.string.t_001_addrel_cant_add_supplier_popup_sub_txt),
                canShowMoveCta = false,
                viewRelationshipType = AddRelationshipCyclicError.CUSTOMER,
                relationshipId = id,
                moveRelationshipType = null,
                shouldRequireReactivation = false,
                source = getCurrentState().source,
                typeOfConflict = errorType.value,
                defaultMode = getCurrentState().defaultMode,
            )
        }
    }

    private fun showDeletedSupplierCyclicAccount(
        id: String?,
        name: String?,
        mobile: String?,
        profile: String?,
        errorType: AddRelationshipFailedError,
    ) {
        if (id == null) return
        hideSoftKeyboard()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(500L)
            AddRelationshipCyclicError.showDialog(
                fragmentManager = childFragmentManager,
                title = getString(R.string.t_001_addrel_cant_add_customer_popup_title),
                name = name,
                number = mobile,
                profileImg = profile,
                description = getString(R.string.t_001_addrel_cant_add_customer_popup_sub_txt),
                canShowMoveCta = false,
                viewRelationshipType = AddRelationshipCyclicError.SUPPLIER,
                relationshipId = id,
                moveRelationshipType = null,
                shouldRequireReactivation = true,
                source = getCurrentState().source,
                typeOfConflict = errorType.value,
                defaultMode = getCurrentState().defaultMode,
            )
        }
    }

    private fun showDeletedCustomerCyclicAccount(
        id: String?,
        name: String?,
        mobile: String?,
        profile: String?,
        errorType: AddRelationshipFailedError,
    ) {
        if (id == null) return
        hideSoftKeyboard()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(500L)
            AddRelationshipCyclicError.showDialog(
                fragmentManager = childFragmentManager,
                title = getString(R.string.t_001_addrel_cant_add_supplier_popup_title),
                name = name,
                number = mobile,
                profileImg = profile,
                description = getString(R.string.t_001_addrel_cant_add_supplier_popup_sub_txt),
                canShowMoveCta = true,
                viewRelationshipType = AddRelationshipCyclicError.CUSTOMER,
                relationshipId = id,
                moveRelationshipType = AddRelationshipCyclicError.SUPPLIER,
                shouldRequireReactivation = true,
                source = getCurrentState().source,
                typeOfConflict = errorType.value,
                defaultMode = getCurrentState().defaultMode,
            )
        }
    }

    private fun showActiveSupplierCyclicAccount(
        id: String?,
        name: String?,
        mobile: String?,
        profile: String?,
        errorType: AddRelationshipFailedError,
    ) {
        if (id == null) return
        hideSoftKeyboard()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(500L)
            AddRelationshipCyclicError.showDialog(
                fragmentManager = childFragmentManager,
                title = getString(R.string.t_001_addrel_cant_add_customer_popup_title),
                name = name,
                number = mobile,
                profileImg = profile,
                description = getString(R.string.t_001_addrel_cant_add_customer_popup_sub_txt),
                canShowMoveCta = false,
                viewRelationshipType = AddRelationshipCyclicError.SUPPLIER,
                relationshipId = id,
                moveRelationshipType = null,
                source = getCurrentState().source,
                typeOfConflict = errorType.value,
                defaultMode = getCurrentState().defaultMode,
            )
        }
    }

    private fun showActiveCustomerCyclicAccount(
        id: String?,
        name: String?,
        mobile: String?,
        profile: String?,
        errorType: AddRelationshipFailedError,
    ) {
        if (id == null) return
        hideSoftKeyboard()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(500L)
            AddRelationshipCyclicError.showDialog(
                fragmentManager = childFragmentManager,
                title = getString(R.string.t_001_addrel_cant_add_supplier_popup_title),
                name = name,
                number = mobile,
                profileImg = profile,
                description = getString(R.string.t_001_addrel_cant_add_supplier_popup_sub_txt),
                canShowMoveCta = true,
                viewRelationshipType = AddRelationshipCyclicError.CUSTOMER,
                relationshipId = id,
                moveRelationshipType = AddRelationshipCyclicError.SUPPLIER,
                source = getCurrentState().source,
                typeOfConflict = errorType.value,
                defaultMode = getCurrentState().defaultMode,
            )
        }
    }

    private fun showSearchInput() {
        binding.apply {
            selectCustomerGrp.gone()
            searchInputGrp.visible()
            searchInput.requestFocus()
            showSoftKeyboard(searchInput)
        }
    }

    private fun hideSearchInput() {
        binding.apply {
            selectCustomerGrp.visible()
            searchInputGrp.gone()
            hideSoftKeyboard()
        }
    }

    private fun gotoSupplierFragment(supplierId: String) {
        requireActivity().finish()
        legacyNavigator.get().goToSupplierScreen(requireActivity(), supplierId)
    }

    private fun gotoCustomerFragment(customerId: String) {
        requireActivity().finish()
        legacyNavigator.get().goToCustomerScreen(requireActivity(), customerId, "Add Relationship V2")
    }

    private fun setFragmentResult(contact: AddRelationshipEpoxyModels.ContactModel) {
        parentFragmentManager.setFragmentResult(
            AddRelationshipManually.REQUEST_KEY,
            bundleOf(
                AddRelationshipManually.ARG_NAME to contact.name,
                AddRelationshipManually.ARG_MOBILE to contact.mobile,
                AddRelationshipManually.ARG_PROFILE_IMAGE to contact.profileImage
            )
        )
        parentFragmentManager.popBackStack()
    }

    override fun onAddManuallyClicked(name: String) {
        getCurrentState().relationshipType?.also {
            val fragment = AddRelationshipManually.newInstance(
                it,
                name,
                source = getCurrentState().source,
                defaultMode = getCurrentState().defaultMode
            )
            (requireActivity() as? AddRelationshipActivity)?.supportFragmentManager?.beginTransaction()
                ?.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                ?.replace(R.id.fragment_container_view, fragment)
                ?.commit()
        }
    }

    override fun onContactItemClicked(
        relationshipId: String?,
        mobile: String
    ) {
        tracker.get().trackSelectContact(
            flow = "Add Relation",
            relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer"
            else "Supplier",
            source = getCurrentState().source,
            defaultMode = getCurrentState().defaultMode
        )
        when {
            getCurrentState().isFragmentOpenForResult -> {
                pushIntent(Intent.ReturnResult(mobile))
            }
            relationshipId != null -> {
                pushIntent(Intent.RedirectToLedgerScreen(relationshipId))
            }
            else -> {
                pushIntent(Intent.AddRelationship(mobile))
            }
        }
    }
}
