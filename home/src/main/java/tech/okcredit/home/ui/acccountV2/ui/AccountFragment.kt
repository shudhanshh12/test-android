package tech.okcredit.home.ui.acccountV2.ui

import `in`.okcredit.analytics.Tracker
import `in`.okcredit.backend.utils.CurrencyUtil
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.ScreenName
import `in`.okcredit.web.WebExperiment.Companion.WEBVIEW_LIBRARY_URL
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.snackbar.Snackbar
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.supplier.contract.SupplierNavigator
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission
import tech.okcredit.home.R
import tech.okcredit.home.databinding.AccountFragmentBinding
import tech.okcredit.home.ui.acccountV2.analytics.AccountEventProperties.ACCOUNT_SCREEN
import tech.okcredit.home.ui.acccountV2.analytics.AccountEventProperties.DOWNLOAD_BACKUP
import tech.okcredit.home.ui.acccountV2.analytics.AccountEventProperties.VIEW_ACCOUNT_STATEMENT
import tech.okcredit.home.ui.acccountV2.analytics.AccountEventTacker
import tech.okcredit.home.ui.acccountV2.ui.AccountContract.*
import tech.okcredit.userSupport.SupportRepository
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AccountFragment : BaseFragment<State, ViewEvent, Intent>(
    "AccountScreen",
    R.layout.account_fragment
) {

    internal val binding: AccountFragmentBinding by viewLifecycleScoped(AccountFragmentBinding::bind)
    var snackbar: Snackbar? = null

    companion object {
        const val SOURCE = "ACCOUNT SCREEN"
        fun newInstance() = AccountFragment()
    }

    @Inject
    internal lateinit var accountEventTracker: Lazy<AccountEventTacker>

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    @Inject
    lateinit var userSupport: Lazy<SupportRepository>

    @Inject
    internal lateinit var supplierNavigator: Lazy<SupplierNavigator>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey50
                )
            )
        )
        initListeners()
        binding.rootLayout.setTracker(performanceTracker)
    }

    override fun onPause() {
        super.onPause()
        clearVectorDrawableAnimationCallbacks(binding.ivDownloading)
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            Observable.just(
                Intent.CheckSupplierCreditFeatureAndObserveWorkerStatus(WeakReference(viewLifecycleOwner))
            ),
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun initListeners() {
        binding.apply {
            cardCustomer.setOnClickListener {
                accountEventTracker.get().trackAccountEvents(VIEW_ACCOUNT_STATEMENT, ACCOUNT_SCREEN)
                pushIntent(Intent.CustomerKhataClick)
            }
            cardSupplier.setOnClickListener { pushIntent(Intent.SupplierKhataClick) }
            cardDownload.setOnClickListener {
                if (getCurrentState().isLoading.not()) {
                    accountEventTracker.get().trackAccountEvents(DOWNLOAD_BACKUP, ACCOUNT_SCREEN)
                    checkPermission()
                }
            }

            contextualHelp.setScreenNameValue(
                ScreenName.AccountScreen.value, tracker.get(), userSupport.get(),
                legacyNavigator.get()
            )

            cardWebLib.setOnClickListener {
                pushIntent(Intent.WebLibraryClick)
            }
        }
    }

    override fun render(state: State) {
        binding.toolbarTitle.text = state.merchantName
        binding.lastUpdatedAtGroup.isVisible = state.shouldShowLastUpdatedAtText

        state.accountSummary?.let {
            CurrencyUtil.renderV2(it.balance, binding.tvValueCustomerKhata, 0)
            binding.tvCustomerKhataAdv.text = if (it.balance > 0) {
                getString(R.string.you_give)
            } else {
                getString(R.string.you_get)
            }
            binding.tvCustomerCount.text =
                resources.getQuantityString(R.plurals.number_of_customers, it.customerCount, it.customerCount)
        }

        state.supplierSummary?.let {
            CurrencyUtil.renderV2(it.supplierBalance, binding.tvSupplierKhataAmount, 0)
            binding.tvSupplierKhataAdv.text = if (it.supplierBalance > 0) {
                getString(R.string.you_get)
            } else {
                getString(R.string.you_give)
            }
            binding.tvSupplierCount.text =
                resources.getQuantityString(R.plurals.number_of_suppliers, it.supplierCount, it.supplierCount)
        }

        binding.cardWebLib.isVisible = state.isWebTestingActivated

        binding.groupDownloading.isVisible = state.isLoading
        if (state.isLoading) {
            startVectorDrawableAnimation(binding.ivDownloading)
        } else {
            clearVectorDrawableAnimationCallbacks(binding.ivDownloading)
        }
        binding.groupDownloaded.isVisible = state.isDownloaded
    }

    private fun startVectorDrawableAnimation(view: ImageView) {
        val vectorDrawable = AnimatedVectorDrawableCompat.create(requireContext(), R.drawable.ic_animated_download)
        vectorDrawable?.let {
            view.setImageDrawable(vectorDrawable)
            vectorDrawable.start()
            val callback = getVectorDrawableAnimationLooperCallback(vectorDrawable)
            vectorDrawable.registerAnimationCallback(callback)
        }
    }

    private fun getVectorDrawableAnimationLooperCallback(
        animatedVectorDrawableCompat: AnimatedVectorDrawableCompat
    ): Animatable2Compat.AnimationCallback {
        return object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                animatedVectorDrawableCompat.start()
            }
        }
    }

    private fun clearVectorDrawableAnimationCallbacks(view: ImageView) {
        when (val drawable = view.drawable) {
            is AnimatedVectorDrawableCompat -> drawable.clearAnimationCallbacks()
            is AnimatedVectorDrawable ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    drawable.clearAnimationCallbacks()
                }
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToAccountStatementScreen -> legacyNavigator.get()
                .goToAccountStatementScreen(requireContext(), SOURCE)
            is ViewEvent.StartDownload -> pushIntent(Intent.OnReportUrlGenerated)
            is ViewEvent.ReportGenerationFailed -> showErrorSnackbar(event.isInternetIssue)
            is ViewEvent.GoToSupplierStatementScreen -> supplierNavigator.get()
                .goToSupplierAccountStatement(requireActivity())
            ViewEvent.DeepLinkStartDownload -> checkPermission()
            ViewEvent.GoToWebWebViewScreen -> legacyNavigator.get()
                .goToWebViewScreen(requireActivity(), WEBVIEW_LIBRARY_URL)
        }
    }

    private fun showErrorSnackbar(internetIssue: Boolean) {
        val stringId = if (internetIssue) R.string.no_internet_msg else R.string.report_generation_failed
        view?.snackbar(getString(stringId), Snackbar.LENGTH_SHORT)?.show()
    }

    private fun checkPermission() {

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Permission.requestStoragePermission(
                requireActivity() as AppCompatActivity,
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {
                    }

                    override fun onPermissionGranted() {
                        Observable
                            .timer(500, TimeUnit.MILLISECONDS)
                            .subscribe {
                                pushIntent(Intent.DownloadReport)
                            }
                    }

                    override fun onPermissionDenied() {
                    }
                }
            )
        } else {
            pushIntent(Intent.DownloadReport)
        }
    }
}
