package `in`.okcredit.collection_ui.ui.passbook.add_to_khata

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.analytics.OnlineCollectionTracker
import `in`.okcredit.collection_ui.databinding.DialogAddToKhataBinding
import `in`.okcredit.collection_ui.dialogs.SuccessDialog
import `in`.okcredit.fileupload._id.GlideApp
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.setBooleanVisibility
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddToKhataDialog :
    BaseBottomSheetWithViewEvents<AddToKhataContract.State, AddToKhataContract.ViewEvent, AddToKhataContract.Intent>("AddToKhataDialog") {

    private val binding: DialogAddToKhataBinding by viewLifecycleScoped(DialogAddToKhataBinding::bind)

    @Inject
    lateinit var onlineCollectionTracker: OnlineCollectionTracker

    companion object {
        const val TAG = "AddToKhataDialog"

        const val ARG_SOURCE = "arg_source"

        fun newInstance(source: String): AddToKhataDialog {
            val args = Bundle()
            args.putString(ARG_SOURCE, source)
            val fragment = AddToKhataDialog()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DialogAddToKhataBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cancel.setOnClickListener {
            onlineCollectionTracker.trackClickEventCancelTagging(getCurrentState().collectionOnlinePayment?.id ?: "", getCurrentState().source)
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View>(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun loadIntent(): UserIntent {
        return AddToKhataContract.Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.confirm.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    val customer = getCurrentState().customer
                    val collectionOnlinePayment = getCurrentState().collectionOnlinePayment
                    onlineCollectionTracker.trackClickEventConfirmTagging(
                        collectionOnlinePayment?.id,
                        customer?.id,
                        customer?.mobile,
                        getCurrentState().source,
                    )
                    AddToKhataContract.Intent.TagCustomer
                }
        )
    }

    override fun render(state: AddToKhataContract.State) {
        setProfile(state.customer?.description, state.customer?.profileImage)
        setMobileNumber(state.customer?.mobile)
        setDescription(state.collectionOnlinePayment?.amount?.toLong() ?: 0L, state.customer?.description)
        setLoader(state.isLoading)
    }

    private fun setLoader(isLoading: Boolean) {
        if (isLoading) {
            showLoader()
        } else {
            hideLoader()
        }
    }

    private fun setProfile(name: String?, profilepic: String?) {
        binding.name.text = name
        name?.let {
            var text = if (it.length > 1) it.substring(0, 1) else it
            text = if (text.isEmpty() || text.toCharArray()[0].isDigit()) {
                "+"
            } else {
                text.uppercase(Locale.getDefault())
            }
            val defaultPic = TextDrawable
                .builder()
                .buildRound(
                    text,
                    ColorGenerator.MATERIAL.getColor(it)
                )
            if (profilepic != null) {
                GlideApp
                    .with(requireContext())
                    .load(profilepic)
                    .placeholder(defaultPic)
                    .error(defaultPic)
                    .fallback(defaultPic)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(binding.profileImage)
            } else {
                binding.profileImage.setImageDrawable(defaultPic)
            }
        }
    }

    private fun setDescription(amount: Long, customerName: String?) {
        binding.description.text = context?.getString(
            R.string.add_to_khata_description,
            CurrencyUtil.formatV2(amount),
            customerName
        )
    }

    private fun setMobileNumber(mobile: String?) {
        binding.number.text = mobile
        binding.number.setBooleanVisibility(mobile.isNullOrEmpty().not())
    }

    override fun handleViewEvent(event: AddToKhataContract.ViewEvent) {
        when (event) {
            AddToKhataContract.ViewEvent.OnSuccess -> showSuccessDialog(getString(R.string.successfully_added))
            is AddToKhataContract.ViewEvent.OnError -> context?.shortToast(event.msg)
        }
    }

    private fun hideLoader() {
        binding.confirm.visible()
        binding.loader.gone()
    }

    private fun showLoader() {
        binding.confirm.gone()
        binding.loader.visible()
    }

    private fun showSuccessDialog(msg: String) {
        val customer = getCurrentState().customer
        val collectionOnlinePayment = getCurrentState().collectionOnlinePayment
        onlineCollectionTracker.trackOnSuccessfullTagging(
            collectionOnlinePayment?.id,
            customer?.id,
            customer?.mobile,
            collectionOnlinePayment?.createdTime,
            getCurrentState().source
        )
        val dialog = SuccessDialog.newInstance(msg)
        dialog.show(parentFragmentManager, SuccessDialog.TAG)
        dismiss()
    }
}
