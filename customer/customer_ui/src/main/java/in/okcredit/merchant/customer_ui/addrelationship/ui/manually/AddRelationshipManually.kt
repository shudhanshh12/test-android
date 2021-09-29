package `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.addrelationship.analytics.AddRelationshipEventTracker
import `in`.okcredit.merchant.customer_ui.addrelationship.enum.AddRelationshipFailedError
import `in`.okcredit.merchant.customer_ui.addrelationship.enum.AddRelationshipFailedError.*
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContacts
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.contacts.AddRelationshipFromContactsContract.Companion.ADD_CUSTOMER
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.AddRelationshipCyclicError
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.manually.AddRelationshipManuallyContract.*
import `in`.okcredit.merchant.customer_ui.databinding.FragmentAddRelationshipManuallyBinding
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.lifecycleScope
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.coroutines.delay
import tech.okcredit.android.base.extensions.afterTextChangedDebounce
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.MobileUtils
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import javax.inject.Inject

class AddRelationshipManually : BaseFragment<State, ViewEvent, Intent>(
    "AddRelationshipManually",
    R.layout.fragment_add_relationship_manually
) {

    companion object {

        const val ARG_ADD_RELATIONSHIP_TYPE = "arg_add_relationship_type"
        const val REQUEST_KEY = "request_key"
        const val ARG_NAME = "arg_name"
        const val ARG_MOBILE = "arg_mobile"
        const val ARG_PROFILE_IMAGE = "arg_profile_image"
        const val ARG_SOURCE = "arg_source_relationship_manually"
        const val ARG_DEFAULT_MODE = "arg_default_mode"
        const val ARG_OPEN_FOR_RESULT = "arg_open_for_result_manually"

        fun newInstance(
            addRelationshipType: Int,
            name: String = "",
            source: String,
            defaultMode: String = "Manual",
            openForResult: Boolean = false
        ): AddRelationshipManually {
            val args = Bundle().apply {
                putInt(ARG_ADD_RELATIONSHIP_TYPE, addRelationshipType)
                putString(ARG_NAME, name)
                putString(ARG_SOURCE, source)
                putString(ARG_DEFAULT_MODE, defaultMode)
                putBoolean(ARG_OPEN_FOR_RESULT, openForResult)
            }
            return AddRelationshipManually().apply {
                arguments = args
            }
        }
    }

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var addRelationshipTracker: Lazy<AddRelationshipEventTracker>

    internal val binding: FragmentAddRelationshipManuallyBinding by viewLifecycleScoped(
        FragmentAddRelationshipManuallyBinding::bind
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpResultListener()
    }

    private fun setUpResultListener() {
        parentFragmentManager.setFragmentResultListener(
            REQUEST_KEY,
            this,
            FragmentResultListener { requestKey, result ->
                onFragmentResult(requestKey, result)
            }
        )
    }

    private fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            REQUEST_KEY -> {
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    val name = result.getString(ARG_NAME)
                    val mobileNumber = result.getString(ARG_MOBILE)
                    val profileImage = result.getString(ARG_PROFILE_IMAGE)
                    pushIntent(Intent.NameChanged(name ?: ""))
                    pushIntent(Intent.MobileChanged(mobileNumber ?: ""))
                    pushIntent(Intent.SetProfileImage(profileImage))
                }
            }
            else -> {
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
    }

    private fun clickListeners() {
        binding.apply {
            nameEditText.afterTextChangedDebounce(300L) { name ->
                if (name.isEmpty()) {
                    pushIntent(Intent.TrackAddCustomerSelectName)
                }
                pushIntent(Intent.NameChanged(name))
            }
            numberEditText.afterTextChangedDebounce(300L) { mobile ->
                if (mobile.isEmpty()) {
                    pushIntent(Intent.TrackAddCustomerSelectMobile)
                }
                onPhoneNumberTextChanged(mobile)
            }

            confirm.setOnClickListener {
                pushIntent(Intent.ConfirmButtonClicked)
                nameEditText.requestFocus()
            }

            backButton.setOnClickListener {
                activity?.finish()
            }

            addFromContacts.setOnClickListener {
                tracker.get().trackImportContactClicked(
                    "Add Relation",
                    "Contact Screen",
                    source = getCurrentState().source,
                    relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer" else "Supplier",
                    defaultMode = getCurrentState().defaultMode
                )
                if (!Permission.isContactPermissionAlreadyGranted(requireContext())) {
                    tracker.get().trackContactsPermissionPopUp(
                        "Add Relation",
                        "Add Relationship Manually",
                        source = getCurrentState().source,
                        relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer" else "Supplier",
                        defaultMode = getCurrentState().defaultMode
                    )
                    Permission.requestContactPermission(
                        requireActivity(),
                        object : IPermissionListener {
                            override fun onPermissionGrantedFirstTime() {
                            }

                            override fun onPermissionGranted() {
                                gotoAddRelationshipFromContacts()
                            }

                            override fun onPermissionDenied() {
                            }

                            override fun onPermissionPermanentlyDenied() {
                                addRelationshipTracker.get().trackViewPermissionDialog(
                                    flow = "Add Relation",
                                    relation = if (getCurrentState().relationshipType == ADD_CUSTOMER) "Customer"
                                    else "Supplier",
                                    source = getCurrentState().source,
                                    defaultMode = "Manual"
                                )
                            }
                        }
                    )
                } else {
                    gotoAddRelationshipFromContacts()
                }
            }
        }
    }

    private fun onPhoneNumberTextChanged(mobile: String) {
        var isPasted = false
        if (mobile.length > 11) {
            val parsedMobile = MobileUtils.parseMobile(mobile)
            binding.numberEditText.setText(parsedMobile)
            binding.numberEditText.setSelection(parsedMobile.length)
            isPasted = true
        } else if (mobile.length > 10) {
            var mobileString = mobile
            mobileString = mobileString.replaceFirst("^0+(?!$)".toRegex(), "")

            if (mobileString.length == 10) {
                binding.numberEditText.setText(mobileString)
                binding.numberEditText.setSelection(10)
            } else {
                binding.numberEditText.setText(mobile.substring(0, 10))
                binding.numberEditText.setSelection(10)
            }
            isPasted = true
        }
        pushIntent(
            Intent.MobileChanged(
                if (isPasted) {
                    binding.numberEditText.text.toString()
                } else {
                    mobile
                }
            )
        )
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun loadIntent(): UserIntent = Intent.Load

    override fun render(state: State) {
        binding.loaderGroup.isVisible = state.isLoading
        checkForHeaderText(state)
        checkForRelationshipName(state)
        checkForMobileName(state)
    }

    private fun checkForMobileName(state: State) {
        binding.numberEditText.setText(state.mobile)
        binding.numberEditText.setSelection(state.mobile.length)
    }

    private fun checkForRelationshipName(state: State) {
        binding.nameEditText.setText(state.name)
        binding.nameEditText.setSelection(state.name.length)
    }

    private fun checkForHeaderText(state: State) {
        if (state.relationshipType == ADD_CUSTOMER) {
            binding.selectCustomerTitle.text = getString(R.string.t_001_addrel_ab_title_add_cust_manually)
        } else {
            binding.selectCustomerTitle.text = getString(R.string.t_001_addrel_ab_title_add_supp_manually)
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
            is ViewEvent.SetResultRelationshipAddedSuccessfully -> setResult()
        }
    }

    private fun setResult() {
        activity?.setResult(Activity.RESULT_OK)
    }

    private fun showAddRelationshipFailedDialog(
        id: String?,
        name: String?,
        mobile: String?,
        profile: String?,
        errorType: AddRelationshipFailedError,
        exception: Throwable,
    ) {
        addRelationshipTracker.get().trackAddTransactionFailed(
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

    private fun gotoSupplierFragment(supplierId: String?) {
        if (supplierId == null) return
        requireActivity().finish()
        legacyNavigator.get().goToSupplierScreen(requireActivity(), supplierId)
    }

    private fun gotoCustomerFragment(customerId: String?) {
        if (customerId == null) return
        legacyNavigator.get().goToCustomerScreen(requireActivity(), customerId, "Add Relationship V2")
        requireActivity().finish()
    }

    internal fun gotoAddRelationshipFromContacts() {
        getCurrentState().relationshipType?.also { relationshipType ->
            val fragment = AddRelationshipFromContacts.newInstance(
                relationshipType,
                false,
                "Add Manually Fragment",
                defaultMode = getCurrentState().defaultMode
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
