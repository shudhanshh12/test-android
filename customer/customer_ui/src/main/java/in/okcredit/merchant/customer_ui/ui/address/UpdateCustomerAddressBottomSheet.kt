package `in`.okcredit.merchant.customer_ui.ui.address

import `in`.okcredit.merchant.customer_ui.databinding.BottomSheetUpdateCustomerAddressBinding
import `in`.okcredit.merchant.customer_ui.ui.address.UpdateCustomerAddressContract.*
import `in`.okcredit.shared.R
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class UpdateCustomerAddressBottomSheet :
    BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>("UpdateCustomerAddress") {

    private val binding: BottomSheetUpdateCustomerAddressBinding by viewLifecycleScoped(
        BottomSheetUpdateCustomerAddressBinding::bind
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return BottomSheetUpdateCustomerAddressBinding.inflate(inflater, container, false).root
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetMaterialDialogStyleWithKeyboard
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            editTextAnswer.doAfterTextChanged {
                pushIntent(Intent.AddressChanged(it.toString()))
            }

            buttonSubmit.setOnClickListener {
                pushIntent(Intent.SubmitTapped)
            }

            editTextAnswer.requestFocus()
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: State) {
        if (state.currentAddress != binding.editTextAnswer.text.toString()) {
            binding.editTextAnswer.setText(state.currentAddress)
            binding.editTextAnswer.setSelection(state.currentAddress?.length ?: 0)
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.AddressUpdated -> dismissAllowingStateLoss()
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    companion object {
        @JvmStatic
        fun getInstance(customerId: String, existingAddress: String? = null) =
            UpdateCustomerAddressBottomSheet().apply {
                val bundle = Bundle()
                bundle.putString(ARG_CUSTOMER_ID, customerId)
                existingAddress?.let {
                    bundle.putString(ARG_CURRENT_ADDRESS, existingAddress)
                }
                arguments = bundle
            }

        const val ARG_CUSTOMER_ID = "customer_id"
        const val ARG_CURRENT_ADDRESS = "current_address"
    }
}
