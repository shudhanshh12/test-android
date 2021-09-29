package `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet

import `in`.okcredit.shared.base.BaseBottomSheetWithViewEvents
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.databinding.FragmentBottomSheetUploadBinding
import `in`.okcredit.user_migration.presentation.analytics.UserMigrationEventTracker
import `in`.okcredit.user_migration.presentation.analytics.UserMigrationEventTracker.Objects.User_Migration_Bottom_Sheet
import `in`.okcredit.user_migration.presentation.ui.UserMigrationActivity
import `in`.okcredit.user_migration.presentation.ui.upload_option_bottomsheet.UploadOptionBottomSheetContract.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UploadOptionBottomSheet :
    BaseBottomSheetWithViewEvents<State, ViewEvent, Intent>(
        "UploadOptionBottomSheet"
    ) {

    private val deleteUploadSubject: PublishSubject<Unit> = PublishSubject.create()

    private val binding: FragmentBottomSheetUploadBinding by viewLifecycleScoped(FragmentBottomSheetUploadBinding::bind)

    companion object {
        const val FILE_TYPE = "file_type"
        const val TAG = "UploadOptionBottomSheet"
        fun show(
            fragmentManger: FragmentManager,
            fileType: String = "pdf",
        ) {
            val args = Bundle().apply {
                putString(FILE_TYPE, fileType)
            }

            val fragment = UploadOptionBottomSheet()
            fragment.arguments = args
            fragment.show(fragmentManger, TAG)
        }
    }

    @Inject
    lateinit var userMigrationEventTracker: Lazy<UserMigrationEventTracker>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return FragmentBottomSheetUploadBinding.inflate(inflater, container, false).root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListener()
    }

    private fun initClickListener() {
        binding.uploadPdf.setOnClickListener {
            userMigrationEventTracker.get().trackBottomSheetInteracted("Upload Pdf Button")
            deleteUploadSubject.onNext(Unit)
            goToUserMigrationActivity("pdf")
            dismiss()
        }
//      Commented, currently we are showing `upload pdf` option changed it later
//
//        binding.uploadImages.setOnClickListener {
//            userMigrationEventTracker.get().trackUserMigrationBottomSheetInteracted("Upload Images Button")
//            deleteUploadSubject.onNext(Unit)
//            goToUserMigrationActivity("images")
//            dismiss()
//        }
//
//        binding.camera.setOnClickListener {
//            userMigrationEventTracker.get().trackUserMigrationBottomSheetInteracted("Camera Button")
//            dismiss()
//        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            deleteUploadSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.DeleteAllUploads
                }
        )
    }

    override fun render(state: State) {
        binding.uploadPdf.isVisible = state.canShowUploadPdf
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.TrackPdfEntryPointViewed -> userMigrationEventTracker.get()
                .trackObjectViewed(User_Migration_Bottom_Sheet)
        }
    }

    private fun goToUserMigrationActivity(fileType: String) {
        val intent = android.content.Intent(requireActivity(), UserMigrationActivity::class.java)
        intent.putExtra(FILE_TYPE, fileType)
        requireActivity().startActivity(intent)
    }
}
