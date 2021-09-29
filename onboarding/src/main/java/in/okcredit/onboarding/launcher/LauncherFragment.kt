package `in`.okcredit.onboarding.launcher

import `in`.okcredit.onboarding.R
import `in`.okcredit.onboarding.analytics.OnboardingAnalytics
import `in`.okcredit.onboarding.data.OnboardingPreferencesImpl
import `in`.okcredit.onboarding.databinding.LauncherFragmentBinding
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import tech.okcredit.android.base.extensions.alpha
import tech.okcredit.android.base.extensions.animateAlpha
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import javax.inject.Inject

class LauncherFragment : Fragment(R.layout.launcher_fragment), LauncherContract.View {

    @Inject
    lateinit var viewModel: Lazy<LauncherContract.Presenter>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var onboardingAnalytics: Lazy<OnboardingAnalytics>

    @Inject
    lateinit var onboardingPreferences: Lazy<OnboardingPreferencesImpl>

    private val binding: LauncherFragmentBinding by viewLifecycleScoped(
        LauncherFragmentBinding::bind
    )

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // making sure that group always starts with 0 alpha
        binding.groupSplash.alpha(0f)
        binding.groupSplash.animateAlpha(1f, 1_000)
    }

    override fun onResume() {
        super.onResume()
        viewModel.get().attachView(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel.get().detachView()
    }

    override fun gotoHome() {
        // TODO Move home into global graph
        legacyNavigator.get().goToHome(requireActivity())
        requireActivity().finishAffinity()
    }

    override fun goToLanguageSelectionScreen() {
        legacyNavigator.get().gotoWelcomeLanguageSelectionScreen(requireContext())
        requireActivity().finishAffinity()
    }

    override fun setupAppLock() {
        legacyNavigator.get().goToSystemAppLockScreenFromLogin(requireContext())
        requireActivity().finishAffinity()
    }

    override fun authenticateViaNewAppLock() {
        legacyNavigator.get().goToSystemAppLockScreenOnAppResume(requireContext())
        requireActivity().finishAffinity()
    }

    override fun authenticateViaOldAppLOck() {
        legacyNavigator.get().goToCustomLockScreen(this, CUSTOM_APP_LOCK_AUTHENTICATED)
    }

    override fun goToEnterBusinessName() {
        NavHostFragment.findNavController(this).navigate(LauncherFragmentDirections.businessNameScreen())
    }

    override fun showError() {
        Toast.makeText(context, R.string.err_default, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CUSTOM_APP_LOCK_AUTHENTICATED) {
            onboardingPreferences.get().setAppWasInBackgroundFor20Minutes(false)
            gotoHome()
        }
    }

    override fun goToEnterMobileScreen() {
        NavHostFragment.findNavController(this).navigate(R.id.login)
    }

    companion object {
        private const val CUSTOM_APP_LOCK_AUTHENTICATED = 111
    }
}
