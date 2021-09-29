package `in`.okcredit.user_migration.presentation.ui.upload_status.screen

import `in`.okcredit.fileupload.user_migration.domain.model.UploadStatus
import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.databinding.FileUploadingFragmentBinding
import `in`.okcredit.user_migration.presentation.analytics.UserMigrationEventTracker
import `in`.okcredit.user_migration.presentation.ui.upload_status.model.ListFilesPath
import `in`.okcredit.user_migration.presentation.ui.upload_status.screen.UploadFileStatusContract.*
import `in`.okcredit.user_migration.presentation.ui.upload_status.screen.UploadFileStatusFragmentDirections.goToDisplayParsedDataScreen
import `in`.okcredit.user_migration.presentation.ui.upload_status.views.ItemUploadingStatus
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.jetbrains.annotations.Nullable
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.hideSoftKeyboard
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UploadFileStatusFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "UploadFileStatusScreen",
        R.layout.file_uploading_fragment
    ),
    @Nullable ItemUploadingStatus.FileUploadStatusListener {

    private val cancelUploadSubject: PublishSubject<UploadStatus> = PublishSubject.create()
    private val retryUploadSubject: PublishSubject<UploadStatus> = PublishSubject.create()
    private val clickSubject: PublishSubject<UserIntent> = PublishSubject.create()

    internal val binding: FileUploadingFragmentBinding by viewLifecycleScoped(FileUploadingFragmentBinding::bind)

    @Inject
    lateinit var uploadFileStatusController: Lazy<UploadFileStatusController>

    @Inject
    lateinit var userMigrationEventTracker: Lazy<UserMigrationEventTracker>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userMigrationEventTracker.get()
            .trackObjectViewed(UserMigrationEventTracker.Objects.User_Migration_File_Uploading_Status_Screen)
        init()
        initClickListener()
    }

    private fun init() {
        binding.pdfFilesUploadView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = uploadFileStatusController.get().adapter
        }
    }

    private fun initClickListener() {
        binding.tvAddMoreFiles.setOnClickListener {
            userMigrationEventTracker.get()
                .trackFileUploadingStatusScreenInteracted("Add More Files Button")
            goBack()
        }
        binding.toolbar.setNavigationOnClickListener { goBack() }
        binding.submitUploadPath.setOnClickListener {
            userMigrationEventTracker.get().trackFileUploadingStatusScreenInteracted("Submit Button")
            clickSubject.onNext(Intent.SubmitButtonClicked)
        }
    }

    private fun goBack() {
        this.findNavController().popBackStack()
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            cancelUploadSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.CancelUpload(it)
                },

            retryUploadSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.RetryUpload(it)
                },
            clickSubject

        )
    }

    override fun render(state: State) {
        uploadFileStatusController.get().setState(state)

        if (state.isEnabledSubmitButton) {
            enabledSubmit()
        } else {
            disableSubmit()
        }
    }

    override fun cancelUpload(uploadStatus: UploadStatus?) {
        userMigrationEventTracker.get()
            .trackFileUploadingStatusScreenInteracted("Cancel Button")
        cancelUploadSubject.onNext(uploadStatus!!)
    }

    override fun retryUpload(uploadStatus: UploadStatus?) {
        userMigrationEventTracker.get()
            .trackFileUploadingStatusScreenInteracted("Retry Button")
        retryUploadSubject.onNext(uploadStatus!!)
    }

    private fun enabledSubmit() {
        binding.submitUploadPath.apply {
            elevation = resources.getDimension(R.dimen.view_4dp)
            backgroundTintList =
                ColorStateList.valueOf(getColorFromAttr(R.attr.colorPrimary))
            isEnabled = true
        }
    }

    private fun disableSubmit() {
        binding.submitUploadPath.apply {
            elevation = 0f
            backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.grey400)
            isEnabled = false
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoToDisplayParsedDataScreen -> {
                goToDisplayParsedCustomerScreen()
            }

            is ViewEvent.GoToBack -> {
                goBack()
            }
        }
    }

    private fun goToDisplayParsedCustomerScreen() {
        hideSoftKeyboard()
        findNavController().navigate(
            goToDisplayParsedDataScreen(
                ListFilesPath(getCurrentState().listOfUploadedFileUrls),
                ListFilesPath(getCurrentState().listOfFileNames)
            )
        )
    }
}
