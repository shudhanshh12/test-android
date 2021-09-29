package `in`.okcredit.payment.ui.payment_error_screen

import `in`.okcredit.payment.PaymentActivity
import `in`.okcredit.payment.R
import `in`.okcredit.payment.contract.PaymentResultListener
import `in`.okcredit.payment.databinding.PaymentErrorFragmentBinding
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import java.util.concurrent.TimeUnit

class PaymentErrorFragment :
    BaseFragment<PaymentErrorContract.State, PaymentErrorContract.ViewEvents, PaymentErrorContract.Intent>(
        "PaymentErrorFragment"
    ) {

    companion object {
        const val ARG_PAYMENT_ERROR_TYPE = "error_type"
        const val ARG_ACCOUNT_ID = "account_id"
        const val ARG_ERROR_MSG = "message"
    }

    private var callbackListener: PaymentResultListener? = null

    private val binding: PaymentErrorFragmentBinding by viewLifecycleScoped(
        PaymentErrorFragmentBinding::bind
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return PaymentErrorFragmentBinding.inflate(layoutInflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
        setCallbackListener()
    }

    private fun setCallbackListener() {
        callbackListener = requireActivity() as PaymentActivity
    }

    private fun setClickListeners() {
        binding.ivCross.setOnClickListener {
            requireActivity().finish()
        }
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(PaymentErrorContract.Intent.Load),
            binding.mbRetry.clicks()
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .map {
                    PaymentErrorContract.Intent.OnRetry
                }
        )
    }

    override fun render(state: PaymentErrorContract.State) {
        when (state.errorType) {
            PaymentErrorType.OTHER -> {
                binding.apply {
                    tvErrorTitle.text = getString(R.string.payment_other_error)
                    if (state.errorMessage.isNotEmpty()) {
                        tvErrorSubTitle.visible()
                        tvErrorSubTitle.text = state.errorMessage
                    }
                    ivError.setBackgroundResource(R.drawable.payment_error_other)
                }
            }
            PaymentErrorType.NETWORK -> {
                binding.apply {
                    tvErrorTitle.text = getString(R.string.payment_network)
                    ivError.setBackgroundResource(R.drawable.payment_error_network)
                }
            }
        }
    }

    override fun handleViewEvent(event: PaymentErrorContract.ViewEvents) {
        when (event) {
            is PaymentErrorContract.ViewEvents.OnRetry -> {
                callbackListener?.onRetryClicked()
            }
        }
    }

    override fun loadIntent(): UserIntent? {
        return PaymentErrorContract.Intent.Load
    }
}
