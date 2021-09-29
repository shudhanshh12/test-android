package `in`.okcredit.collection_ui.ui.passbook.refund

import `in`.okcredit.collection_ui.R
import `in`.okcredit.collection_ui.databinding.RefundConsentBottomSheetBinding
import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.Nullable
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import java.util.concurrent.TimeUnit

class RefundConsentBottomSheet :
    BaseBottomSheetWithViewEvents<RefundConsentContract.State, RefundConsentContract.ViewEvents, RefundConsentContract.Intent>(
        "RefundConsentBottomSheet"
    ) {

    companion object {
        const val TAG = "RefundConsentBottomSheet"
        const val ARG_PAYOUT_ID = "payout_id"
        const val ARG_TXN_ID = "txn_id"
        const val ARG_PAYMENT_ID = "payment_id"
        const val ARG_COLLECTION_TYPE = "collection_type"
        fun newInstance(
            payoutId: String,
            txnId: String,
            paymentId: String,
            collectionType: String,
        ): RefundConsentBottomSheet {
            val bundle = Bundle().apply {
                putString(ARG_PAYOUT_ID, payoutId)
                putString(ARG_TXN_ID, txnId)
                putString(ARG_PAYMENT_ID, paymentId)
                putString(ARG_COLLECTION_TYPE, collectionType)
            }
            return RefundConsentBottomSheet().apply {
                arguments = bundle
            }
        }
    }

    private val binding: RefundConsentBottomSheetBinding by viewLifecycleScoped(
        RefundConsentBottomSheetBinding::bind
    )

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return RefundConsentBottomSheetBinding.inflate(inflater, container, false).root
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            binding.mbRefund.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    RefundConsentContract.Intent.InitiateRefund
                },
            binding.mbCancel.clicks()
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    RefundConsentContract.Intent.Cancel
                }

        )
    }

    override fun render(state: RefundConsentContract.State) {
        if (state.showLoader) {
            binding.apply {
                mbRefund.text = ""
                ivLoading.visible()
                ivLoading.clearAnimation()
                ivLoading.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.payment_rotate
                    )
                )
            }
        } else {
            binding.apply {
                ivLoading.clearAnimation()
                ivLoading.gone()
                mbRefund.text = getString(R.string.refund)
            }
        }
    }

    override fun handleViewEvent(event: RefundConsentContract.ViewEvents) {
        when (event) {
            RefundConsentContract.ViewEvents.RefundSuccessful -> dismissAllowingStateLoss()
            is RefundConsentContract.ViewEvents.ShowError -> shortToast(event.message)
            RefundConsentContract.ViewEvents.Cancel -> dismissAllowingStateLoss()
        }
    }

    override fun loadIntent(): UserIntent? {
        return RefundConsentContract.Intent.Load
    }
}
