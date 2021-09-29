package merchant.okcredit.user_stories.homestory

import `in`.okcredit.shared.base.BaseLayout
import `in`.okcredit.shared.base.UserIntent
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.airbnb.epoxy.ModelView
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener
import dagger.Lazy
import io.reactivex.Observable
import merchant.okcredit.user_stories.R
import merchant.okcredit.user_stories.analytics.UserStoriesTracker
import merchant.okcredit.user_stories.contract.HomeStoryNavigation
import merchant.okcredit.user_stories.contract.model.UserStories
import merchant.okcredit.user_stories.databinding.HomeUserStoryLayoutBinding
import merchant.okcredit.user_stories.homestory.epoxy.HomeUserStoryController
import tech.okcredit.android.base.extensions.getString
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.visible
import javax.inject.Inject

@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class HomeUserStoryLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseLayout<HomeUserStoryContract.State>(context, attrs, defStyleAttr), HomeUserStoryContract.Interactor {

    private val binding = HomeUserStoryLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        viewModel.setNavigation(this)
        userStoryController.get().setAddStoryClickListener(::onAddStoryClicked)
        userStoryController.get().setMyStoryClickedListener(::onMyStoryClicked)
        userStoryController.get().setUserStoryClickListener(::onViewStoryClicked)
        binding.storyRecyclerview.setController(controller = userStoryController.get())
    }

    @Inject
    lateinit var userStoryController: Lazy<HomeUserStoryController>

    @Inject
    lateinit var homeStoryNavigation: Lazy<HomeStoryNavigation>

    @Inject
    lateinit var userStoryEventTracker: Lazy<UserStoriesTracker>

    override fun userIntents(): Observable<UserIntent> {
        return Observable.ambArray()
    }

    override fun render(state: HomeUserStoryContract.State) {
        checkFeatureEnable(state)
    }

    private fun checkFeatureEnable(state: HomeUserStoryContract.State) {
        if (!state.isUserStoryEnabled) {
            binding.storyRecyclerview.gone()
            binding.divider.gone()
            binding.dummy.visible()
        } else {
            renderStories(state)
        }
    }

    private fun renderStories(state: HomeUserStoryContract.State) {
        state.homeUserStory?.let {
            binding.storyRecyclerview.visible()
            binding.divider.visible()
            binding.dummy.gone()
            userStoryController.get().setData(state.homeUserStory)
        }
    }

    override fun loadIntent(): UserIntent? {
        return HomeUserStoryContract.Intent.Load
    }

    private fun onAddStoryClicked() {
        getCurrentState().activeMerchantId.let {
            userStoryEventTracker.get()
                .trackEventAddStoryClick(it, "customer_tab", "top")
        }
        Dexter.withContext(context).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.CAMERA
        ).withListener(custom)
            .check()
    }

    private fun onMyStoryClicked() {
        homeStoryNavigation.get().goToMyStoryScreen(context)
    }

    private val permissionListener = object : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {

            userStoryEventTracker.get().trackEventStoryPermissionLoaded(
                getCurrentState().activeMerchantId,
                "camera"
            )

            userStoryEventTracker.get().trackEventStoryPermissionLoaded(
                getCurrentState().activeMerchantId,
                "storage"
            )
            userStoryEventTracker.get().trackEventStoryPermissionLoaded(
                getCurrentState().activeMerchantId,
                "contacts"
            )

            if (report.areAllPermissionsGranted()) {
                homeStoryNavigation.get()
                    .goToAddStoryScreen(
                        context, getCurrentState().activeMyStoryCount,
                        getCurrentState().activeMerchantId
                    )

                userStoryEventTracker.get().trackEventStoryPermissionClicked(
                    getCurrentState().activeMerchantId,
                    "camera",
                    "yes"
                )
                userStoryEventTracker.get().trackEventStoryPermissionClicked(
                    getCurrentState().activeMerchantId,
                    "storage",
                    "yes"
                )

                userStoryEventTracker.get().trackEventStoryPermissionClicked(
                    getCurrentState().activeMerchantId,
                    "contacts",
                    "yes"
                )
            } else {
                userStoryEventTracker.get().trackEventStoryPermissionClicked(
                    getCurrentState().activeMerchantId,
                    "camera",
                    "No"
                )
                userStoryEventTracker.get().trackEventStoryPermissionClicked(
                    getCurrentState().activeMerchantId,
                    "storage",
                    "No"
                )

                userStoryEventTracker.get().trackEventStoryPermissionClicked(
                    getCurrentState().activeMerchantId,
                    "contacts",
                    "No"
                )
            }
        }

        override fun onPermissionRationaleShouldBeShown(
            permissions: MutableList<PermissionRequest>?,
            token: PermissionToken?,
        ) {
            token?.continuePermissionRequest()
        }
    }
    private val snackbarMultiplePermissionsListener: MultiplePermissionsListener =
        SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
            .with(binding.root, getString(R.string.permission_denied))
            .withOpenSettingsButton(getString(R.string.settings))
            .withCallback(object : Snackbar.Callback() {
                override fun onShown(snackbar: Snackbar) {
                    userStoryEventTracker.get().trackEventStoryPermissionClicked(
                        getCurrentState().activeMerchantId,
                        "camera",
                        "No"
                    )
                    userStoryEventTracker.get().trackEventStoryPermissionClicked(
                        getCurrentState().activeMerchantId,
                        "storage",
                        "No"
                    )
                }

                override fun onDismissed(snackbar: Snackbar, event: Int) {}
            })
            .build()
    private val custom = CompositeMultiplePermissionsListener(snackbarMultiplePermissionsListener, permissionListener)

    private fun onViewStoryClicked(userStory: UserStories, position: Int) {
        getCurrentState().activeMerchantId.let {
            userStoryEventTracker.get().trackEventUserStoryClick(
                userStory.storyId, userStory.totalStories,
                userStory.totalSeen, it,
                "customer_tab",
                userStory.relationship,
                userStory.id,
                position,
                userStory.storyType
            )
        }

        homeStoryNavigation.get().goToViewUserStoryScreen(context, userStory)
    }
}
