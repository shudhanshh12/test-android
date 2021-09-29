package `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.addrelationship.analytics.AddRelationshipEventTracker
import `in`.okcredit.merchant.customer_ui.addrelationship.ui.cyclic.AddRelationshipCyclicErrorContract.*
import `in`.okcredit.merchant.customer_ui.databinding.DialogAddRelationshipCyclicErrorBinding
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import dagger.Lazy
import io.reactivex.Observable
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class AddRelationshipCyclicError :
    BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>("AddRelationshipCyclicError") {

    companion object {
        const val CUSTOMER = 0
        const val SUPPLIER = 1
        const val ARG_TITLE = "arg_title"
        const val ARG_DESCRIPTION = "arg_description"
        const val ARG_NAME = "arg_name"
        const val ARG_PROFILE_IMAGE = "arg_profile"
        const val ARG_NUMBER = "arg_number"
        const val ARG_TWO_CTA_ENABLED = "arg_two_cta_enabled"
        const val ARG_VIEW_RELATIONSHIP_TYPE = "arg_view_relationship_type"
        const val ARG_MOVE_RELATIONSHIP_TYPE = "arg_move_relationship_type"
        const val ARG_RELATIONSHIP_ID = "arg_releationship_id"
        const val ARG_SHOULD_REQUIRE_REACTIVATION = "arg_should_require_reactivation"
        const val ARG_TYPE_OF_CONFLICT = "arg_type_of_conflict"
        const val ARG_SOURCE = "arg_source"
        const val ARG_DEFAULT_MODE = "arg_default_mode"

        const val TAG = "AddSupplierDeletedCustomerDialog"

        fun showDialog(
            fragmentManager: FragmentManager,
            title: String,
            description: String,
            name: String?,
            profileImg: String?,
            number: String?,
            canShowMoveCta: Boolean,
            viewRelationshipType: Int,
            moveRelationshipType: Int? = null,
            relationshipId: String,
            shouldRequireReactivation: Boolean = false,
            typeOfConflict: String,
            source: String,
            defaultMode: String = "Manual",
        ) {
            val frag = AddRelationshipCyclicError()
            val args = Bundle()
            args.apply {
                putString(ARG_TITLE, title)
                putString(ARG_DESCRIPTION, description)
                putString(ARG_NAME, name)
                putString(ARG_PROFILE_IMAGE, profileImg)
                putString(ARG_NUMBER, number)
                putBoolean(ARG_TWO_CTA_ENABLED, canShowMoveCta)
                putInt(ARG_VIEW_RELATIONSHIP_TYPE, viewRelationshipType)
                putString(ARG_RELATIONSHIP_ID, relationshipId)
                putString(ARG_TYPE_OF_CONFLICT, typeOfConflict)
                putString(ARG_SOURCE, source)
                if (moveRelationshipType != null) {
                    putInt(ARG_MOVE_RELATIONSHIP_TYPE, moveRelationshipType)
                }
                putBoolean(ARG_SHOULD_REQUIRE_REACTIVATION, shouldRequireReactivation)
                putString(ARG_DEFAULT_MODE, defaultMode)
            }
            frag.arguments = args
            frag.show(fragmentManager, TAG)
        }
    }

    private lateinit var binding: DialogAddRelationshipCyclicErrorBinding

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var tracker: Lazy<AddRelationshipEventTracker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogAddRelationshipCyclicErrorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
    }

    private fun clickListeners() {
        binding.moveToSupplier.setOnClickListener {
            if (getCurrentState().moveRelationshipType == SUPPLIER) {
                tracker.get().trackAddRelationshipConflictCTAClicked(
                    type = getCurrentState().typeOfConflict,
                    cta = "Move Relation",
                    flow = "Add Relation",
                    relation = if (getCurrentState().viewRelationshipType == CUSTOMER) "Customer" else "Supplier",
                    source = getCurrentState().source,
                    defaultMode = getCurrentState().defaultMode
                )
                pushIntent(Intent.MoveToSupplier)
            } else {
                pushIntent(Intent.MoveToCustomer)
            }
            dismiss()
        }
        binding.viewSupplier.setOnClickListener {
            tracker.get().trackAddRelationshipConflictCTAClicked(
                type = getCurrentState().typeOfConflict,
                cta = "View Relation",
                flow = "Add Relation",
                relation = if (getCurrentState().viewRelationshipType == CUSTOMER) "Customer" else "Supplier",
                source = getCurrentState().source,
                defaultMode = getCurrentState().defaultMode
            )
            if (getCurrentState().relationshipId == null) return@setOnClickListener
            if (getCurrentState().shouldRequireReactivation) {
                checkForReactivation()
            } else {
                redirectToLedger()
            }
            activity?.finish()
        }
    }

    private fun redirectToLedger() {
        if (getCurrentState().viewRelationshipType == CUSTOMER) {
            gotoCustomerFragment(getCurrentState().relationshipId!!)
        } else {
            gotoSupplierFragment(getCurrentState().relationshipId!!)
        }
    }

    private fun checkForReactivation() {
        if (getCurrentState().viewRelationshipType == CUSTOMER) {
            goToCustomerFragmentForReactivation(getCurrentState().relationshipId!!)
        } else {
            gotoSupplierFragmentForReactivation(getCurrentState().relationshipId!!)
        }
    }

    private fun gotoSupplierFragmentForReactivation(relationshipId: String) {
        legacyNavigator.get().startingSupplierScreenForReactivation(
            requireContext(),
            relationshipId,
            getCurrentState().name
        )
        activity?.finish()
    }

    private fun goToCustomerFragmentForReactivation(relationshipId: String) {
        legacyNavigator.get().startingCustomerScreenForReactivation(
            requireContext(),
            relationshipId,
            getCurrentState().name
        )
        activity?.finish()
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.empty()
    }

    override fun loadIntent(): UserIntent = Intent.Load

    override fun render(state: State) {
        binding.title.text = state.headerText
        binding.description.text = state.descriptionText
        binding.name.text = state.name
        binding.number.text = state.mobile
        checkForMoveCta(state)
        checkForViewRelationshipCta(state)
        checkForProfileImage(state)
    }

    private fun checkForProfileImage(state: State) {
        val defaultPic = TextDrawable
            .builder()
            .buildRound(
                state.name.substring(0, 1).toUpperCase(),
                ColorGenerator.MATERIAL.getColor(state.name)
            )

        GlideApp.with(requireContext())
            .load(state.profile)
            .placeholder(defaultPic)
            .circleCrop()
            .error(defaultPic)
            .fallback(defaultPic)
            .thumbnail(0.25f)
            .into(binding.profileImage)
    }

    private fun checkForViewRelationshipCta(state: State) {
        if (state.viewRelationshipType == CUSTOMER) {
            binding.viewSupplier.text = getString(R.string.t_001_addrel_cta_view_cust)
        } else {
            binding.viewSupplier.text = getString(R.string.t_001_addrel_cta_view_supp)
        }
    }

    private fun checkForMoveCta(state: State) {
        binding.moveToSupplier.isVisible = state.canShowMoveCta
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GotoMoveToSupplierFlow -> gotoMoveToSupplierScreen(event.supplierId)
        }
    }

    private fun gotoSupplierFragment(supplierId: String) {
        requireActivity().finish()
        legacyNavigator.get().goToSupplierScreen(requireActivity(), supplierId)
    }

    private fun gotoCustomerFragment(customerId: String) {
        legacyNavigator.get().goToCustomerScreen(requireActivity(), customerId, "Add Relationship V2")
        requireActivity().finish()
    }

    private fun gotoMoveToSupplierScreen(supplierId: String?) {
        if (supplierId == null) return
        legacyNavigator.get().goToMoveToSupplierScreen(requireActivity(), supplierId)
        requireActivity().finish()
    }
}
