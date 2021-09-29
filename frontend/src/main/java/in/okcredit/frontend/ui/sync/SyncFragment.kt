package `in`.okcredit.frontend.ui.sync

import `in`.okcredit.accounting_core.contract.SyncState
import `in`.okcredit.analytics.Tracker
import `in`.okcredit.frontend.R
import `in`.okcredit.frontend.ui._dialogs.SyncRestartDialog
import `in`.okcredit.frontend.ui.sync.SyncContract.*
import `in`.okcredit.frontend.ui.sync.SyncContract.Companion.KEY_SKIP_SELECT_BUSINESS_SCREEN
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.RenderMode
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import kotlinx.android.synthetic.main.sync_fragment.*
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.snackbar
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.app_contract.LegacyNavigator
import tech.okcredit.base.Traces
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncFragment : BaseFragment<State, ViewEvent, Intent>("SyncScreen", R.layout.sync_fragment) {

    private var alert: Snackbar? = null

    @Inject
    internal lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    internal lateinit var tracker: Lazy<Tracker>

    private var alertDialog: AlertDialog? = null
    private var skipSelectBusinessScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skipSelectBusinessScreen = arguments?.getBoolean(KEY_SKIP_SELECT_BUSINESS_SCREEN) == true
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        root_view.setTracker(performanceTracker)
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            btn_retry_file
                .clicks()
                .map {
                    if (getCurrentState().syncState != SyncState.NETWORK_ERROR) {
                        tracker.get().trackSyncRestart("Button")
                    }
                }
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map { Intent.Retry }
        )
    }

    @AddTrace(name = Traces.RENDER_SYNC_SCREEN)
    override fun render(state: State) {
        when (state.syncState) {
            SyncState.WAITING -> {
                syncing_title.visible()
                syncing_desc.gone()
                sync_error_image.gone()
                btn_retry_file.gone()
                bottom_container.gone()
                progress.gone()
                processing_text.gone()
                animation_view.visible()

                syncing_title.text = getString(R.string.syncing_your_data)
            }
            SyncState.GENERATE_FILE, SyncState.DOWNLOADING, SyncState.PROCESSING -> {
                syncing_title.visible()
                syncing_desc.gone()
                sync_error_image.gone()
                btn_retry_file.gone()
                bottom_container.gone()
                progress.gone()
                processing_text.visible()
                animation_view.visible()

                syncing_title.text = getString(R.string.syncing_your_data)
            }
            SyncState.NETWORK_ERROR -> {
                if (state.isSyncRetryVisible) {
                    btn_retry_file.visible()
                    bottom_container.gone()
                    syncing_desc.text = getString(R.string.please_click_connect_internet)
                } else {
                    btn_retry_file.gone()
                    bottom_container.visible()
                    syncing_desc.text = getString(R.string.please_connect_internet)
                }
                syncing_title.visible()
                syncing_desc.visible()
                sync_error_image.visible()
                progress.visible()
                processing_text.gone()
                animation_view.gone()

                syncing_title.text = getString(R.string.syncing_paused)
                syncing_desc.text = getString(R.string.please_connect_internet)
                sync_error_image.setImageDrawable(getDrawableCompact(R.drawable.ic_sync_network_error))

                if (state.taskProgress > 0) {
                    download_progress_text.text =
                        getString(R.string.percentage_completed, state.taskProgress.toString())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progress.setProgress(state.taskProgress, true)
                    } else {
                        progress.progress = state.taskProgress
                    }
                } else {
                    progress.gone()
                }
            }
            SyncState.SYNC_GENERIC_ERROR, SyncState.FILE_COMPRESSION_ERROR, SyncState.FILE_DOWONLOAD_ERROR -> {
                renderErrorState()
            }
            else -> {
            }
        }

        // show/hide alert
        if (state.networkError or state.isAlertVisible) {
            alert = when {
                state.networkError -> view?.snackbar(
                    getString(R.string.home_no_internet_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                state.isAlertVisible -> view?.snackbar(state.alertMessage, Snackbar.LENGTH_INDEFINITE)
                else -> view?.snackbar(getString(R.string.err_default), Snackbar.LENGTH_INDEFINITE)
            }
            alert?.show()
        } else {
            alert?.dismiss()
        }

        if (state.error) {
            renderErrorState()
        }
    }

    private fun renderErrorState() {
        syncing_title.visible()
        syncing_desc.visible()
        sync_error_image.visible()
        btn_retry_file.visible()
        bottom_container.gone()
        progress.gone()
        processing_text.gone()
        animation_view.gone()

        syncing_title.text = getString(R.string.syncing_failed)
        syncing_desc.text = getString(R.string.err_default)
        sync_error_image.setImageDrawable(getDrawableCompact(R.drawable.ic_sync_network_error))
    }

    override fun onBackPressed(): Boolean {
        alertDialog = SyncRestartDialog.show(activity) {
            tracker.get().trackSyncRestart("Popup")
            pushIntent(Intent.Retry)
            alertDialog?.cancel()
        }

        return true
    }

    override fun onResume() {
        super.onResume()

        animation_view.setRenderMode(RenderMode.HARDWARE)
        animation_view.enableMergePathsForKitKatAndAbove(true)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.e("<<<<SyncScreen onLowMemory")
    }

    override fun onDestroy() {
        super.onDestroy()

        if (alertDialog != null && alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
        }
    }

    private fun gotoLogin() {
        legacyNavigator.get().goToLoginScreenForAuthFailure(requireActivity())
    }

    private fun goHome() {
        legacyNavigator.get().goToHome(requireActivity())
        activity?.finishAffinity()
    }

    private fun goToSelectBusiness() {
        findNavController().navigate(R.id.action_sync_screen_to_selectBusinessFragment)
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoHome -> goHome()
            is ViewEvent.GotoLogin -> gotoLogin()
            ViewEvent.GoToSelectBusinessOrHome -> {
                if (skipSelectBusinessScreen) goHome() else goToSelectBusiness()
            }
        }
    }
}
