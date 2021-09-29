package `in`.okcredit.merchant.customer_ui.ui.subscription.add

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.SubscriptionSuccessScreenBinding
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.delay
import tech.okcredit.android.base.animation.AnimationUtils
import tech.okcredit.android.base.extensions.viewLifecycleScoped

class SubscriptionSuccessScreen : Fragment(R.layout.subscription_success_screen) {

    private lateinit var savedStateHandle: SavedStateHandle

    private val binding: SubscriptionSuccessScreenBinding by viewLifecycleScoped(
        SubscriptionSuccessScreenBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedStateHandle =
            NavHostFragment.findNavController(this@SubscriptionSuccessScreen).previousBackStackEntry!!.savedStateHandle
        savedStateHandle.set(ANIMATION_FINISHED, false)

        AnimationUtils.scale(binding.imageTick)

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(2000)
            val navController = NavHostFragment.findNavController(this@SubscriptionSuccessScreen)
            savedStateHandle.set(ANIMATION_FINISHED, true)
            navController.popBackStack()
        }
    }

    companion object {
        const val ANIMATION_FINISHED: String = "ANIMATION_FINISHED"
    }
}
