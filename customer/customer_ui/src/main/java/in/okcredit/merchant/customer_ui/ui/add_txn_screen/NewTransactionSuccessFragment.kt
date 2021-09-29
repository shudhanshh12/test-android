package `in`.okcredit.merchant.customer_ui.ui.add_txn_screen

import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.CreditTransactionSuccessFragmentBinding
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment.findNavController
import kotlinx.coroutines.delay
import merchant.okcredit.accounting.model.Transaction
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class NewTransactionSuccessFragment : Fragment(R.layout.credit_transaction_success_fragment) {

    private lateinit var savedStateHandle: SavedStateHandle

    private val binding: CreditTransactionSuccessFragmentBinding by viewLifecycleScoped(
        CreditTransactionSuccessFragmentBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedStateHandle =
            findNavController(this@NewTransactionSuccessFragment).previousBackStackEntry!!.savedStateHandle
        savedStateHandle.set(ANIMATION_FINISHED, false)
        val txnType = arguments?.getLong(ARG_TXN_TYPE) ?: Transaction.CREDIT
        val amount = arguments?.getLong(ARG_TXN_AMOUNT) ?: 0
        val customerMobilePresent = arguments?.getBoolean(ARG_MOBILE_PRESENT) ?: false

        if (txnType == Transaction.CREDIT) {
            binding.textCreditAmount.text = if (amount > 0) {
                getString(R.string.credit_added_of_amount, CurrencyUtil.formatV2((amount)))
            } else {
                getString(R.string.credit_added)
            }
        } else {
            binding.textCreditAmount.text = if (amount > 0) {
                getString(R.string.payment_added_of_amount, CurrencyUtil.formatV2((amount)))
            } else {
                getString(R.string.payment_added)
            }
        }

        binding.textSmsSent.isVisible = customerMobilePresent

        scale(binding.imageSuccess)

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(2000)
            val navController = findNavController(this@NewTransactionSuccessFragment)
            savedStateHandle.set(ANIMATION_FINISHED, true)
            navController.popBackStack()
        }
    }

    private fun scale(view: View) {
        val anim = ObjectAnimator.ofFloat(view, "scaleX", 1.37f)
        anim.duration = 600
        anim.repeatCount = ObjectAnimator.INFINITE
        anim.repeatMode = ObjectAnimator.REVERSE
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()

        val anim2 = ObjectAnimator.ofFloat(view, "scaleY", 1.37f)
        anim2.duration = 600 // duration 3 seconds
        anim2.repeatCount = ObjectAnimator.INFINITE
        anim2.repeatMode = ObjectAnimator.REVERSE
        anim2.interpolator = LinearInterpolator()
        anim2.start()
    }

    companion object {
        const val ARG_TXN_AMOUNT = "arg_txn_amount"
        const val ARG_TXN_TYPE = "arg_txn_type"
        const val ARG_MOBILE_PRESENT = "arg_customer_mobile_present"
        const val ANIMATION_FINISHED: String = "ANIMATION_FINISHED"
    }
}
