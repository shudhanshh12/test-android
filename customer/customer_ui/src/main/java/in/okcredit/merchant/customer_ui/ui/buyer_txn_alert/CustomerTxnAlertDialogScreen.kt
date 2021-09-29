package `in`.okcredit.merchant.customer_ui.ui.buyer_txn_alert

import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.analytics.CustomerEventTracker
import `in`.okcredit.merchant.customer_ui.databinding.CustomerTxnAlertDialogScreenBinding
import `in`.okcredit.merchant.customer_ui.ui.buyer_txn_alert.CustomerTxnAlertDialogContract.Intent
import `in`.okcredit.merchant.customer_ui.ui.buyer_txn_alert.CustomerTxnAlertDialogContract.State
import `in`.okcredit.merchant.customer_ui.utils.CustomerTraces
import `in`.okcredit.shared.base.BaseBottomDialogScreen
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.perf.metrics.AddTrace
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class CustomerTxnAlertDialogScreen :
    BaseBottomDialogScreen<State>("CustomerTxnAlertDialogScreen"),
    CustomerTxnAlertDialogContract.Navigator {

    private val binding: CustomerTxnAlertDialogScreenBinding by viewLifecycleScoped(CustomerTxnAlertDialogScreenBinding::bind)
    private val allowSubject: PublishSubject<Unit> = PublishSubject.create()
    private val denySubject: PublishSubject<Unit> = PublishSubject.create()
    private val dismissSubject: PublishSubject<Unit> = PublishSubject.create()
    private var customerId: String? = null
    private var popUpShowed = false

    @Inject
    internal lateinit var tracker: Lazy<CustomerEventTracker>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    override fun onStart() {
        super.onStart()
        handleOutsideClick()
    }

    private fun handleOutsideClick() {
        val outsideView = dialog?.window?.decorView?.findViewById<View>(com.google.android.material.R.id.touch_outside)
        outsideView?.setOnClickListener {
            dismissSubject.onNext(Unit)
            tracker.get().trackPopUpClicked(
                customerId,
                CustomerEventTracker.RELATION_CUSTOMER,
                CustomerEventTracker.RELATIONSHIP_SCREEN,
                CustomerEventTracker.CONTEXTUAL_TYPE,
                CustomerEventTracker.DISMISS
            )

            dismissDialog()
        }
    }

    @AddTrace(name = CustomerTraces.RENDER_ADD_NUMBER_POPUP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return CustomerTxnAlertDialogScreenBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            accept.setOnClickListener {
                allowSubject.onNext(Unit)
            }
            cancel.setOnClickListener {
                denySubject.onNext(Unit)
            }
        }

        view.viewTreeObserver.addOnGlobalLayoutListener {
            val behavior: BottomSheetBehavior<*> = showBottomSheetFullyExpanded()
            disableDraggingInBottomSheet(behavior)
        }

        binding.permissionTakingGroup.visibility = View.VISIBLE
        binding.permissionResultGroup.visibility = View.GONE
        binding.permissionResultDone.setOnClickListener {
            dismiss()
        }
    }

    internal fun showBottomSheetFullyExpanded(): BottomSheetBehavior<*> {
        val dialog = dialog as BottomSheetDialog?
        val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 0
        return behavior
    }

    internal fun disableDraggingInBottomSheet(behavior: BottomSheetBehavior<*>) {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {}
            override fun onStateChanged(view: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        })
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            allowSubject
                .map {
                    Intent.AllowAction
                },
            denySubject
                .map {
                    Intent.DenyAction
                },
            dismissSubject.map {
                Intent.Dismiss
            }
        )
    }

    override fun render(state: State) {
        customerId = state.customerId
        customerId?.let {
            if (popUpShowed.not()) {
                popUpShowed = true
                trackPopUpShown()
            }
        }
        setName(state.name)
        setPhoneNumber(state.mobile)
        setProfilePic(state.profilePic, state.name)
        setMessage(state.amount, state.name, state.type)
    }

    private fun trackPopUpShown() {
        tracker.get().trackCustomerTxnAlertPopUpDisplayed(
            customerId,
            CustomerEventTracker.RELATION_CUSTOMER,
            CustomerEventTracker.RELATIONSHIP_SCREEN,
            CustomerEventTracker.CONTEXTUAL_TYPE
        )
    }

    private fun setMessage(amount: Long, name: String?, type: Int) {
        name?.let {
            if (type == 1) {
                binding.message.text = Html.fromHtml(getString(R.string.buyer_txn_alert_credit, it, amount.toString()))
            } else if (type == 2) {
                binding.message.text = Html.fromHtml(getString(R.string.buyer_txn_alert_payment, it, amount.toString()))
            }
        }
    }

    private fun setProfilePic(profilepic: String?, name: String?) {
        name?.let {
            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    it.substring(0, 1).toUpperCase(),
                    ColorGenerator.MATERIAL.getColor(it)
                )
            if (profilepic != null) {

                GlideApp
                    .with(binding.profilepic.context)
                    .load(profilepic)
                    .placeholder(defaultPic)
                    .error(defaultPic)
                    .fallback(defaultPic)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(binding.profilepic)
            } else {
                binding.profilepic.setImageDrawable(defaultPic)
            }
        }
    }

    private fun setPhoneNumber(mobile: String?) {
        mobile?.let {
            binding.phone.text = mobile
        }
    }

    private fun setName(name: String?) {
        name?.let {
            binding.name.text = name
        }
    }

    override fun goToLogin() {
        activity?.runOnUiThread {
            legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
        }
    }

    override fun dismissDialog() {
        dismiss()
    }

    override fun showDenyPopUp() {

        activity?.runOnUiThread {
            tracker.get().trackPopUpClicked(
                customerId,
                CustomerEventTracker.RELATION_CUSTOMER,
                CustomerEventTracker.RELATIONSHIP_SCREEN,
                CustomerEventTracker.CONTEXTUAL_TYPE,
                CustomerEventTracker.DENY
            )

            binding.permissionTakingGroup.visibility = View.GONE
            binding.permissionResultGroup.visibility = View.VISIBLE
            binding.permissionHeading.text = getString(R.string.permission_denied)
            binding.profilepic.setImageDrawable(context?.getDrawable(R.drawable.ic_check_circle_border))
        }
    }

    override fun showAllowPopUp() {
        activity?.runOnUiThread {

            tracker.get().trackPopUpClicked(
                customerId,
                CustomerEventTracker.RELATION_CUSTOMER,
                CustomerEventTracker.RELATIONSHIP_SCREEN,
                CustomerEventTracker.CONTEXTUAL_TYPE,
                CustomerEventTracker.ALLOW
            )
            binding.permissionTakingGroup.visibility = View.GONE
            binding.permissionResultGroup.visibility = View.VISIBLE
            binding.permissionHeading.text = getString(R.string.permission_given)
            binding.profilepic.setImageDrawable(context?.getDrawable(R.drawable.ic_check_circle_border))
        }
    }
}
