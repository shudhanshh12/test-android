package `in`.okcredit.user_migration.presentation.ui.file_pick.screen

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.databinding.FragmentFileManagerBinding
import `in`.okcredit.user_migration.presentation.analytics.UserMigrationEventTracker
import `in`.okcredit.user_migration.presentation.analytics.UserMigrationEventTracker.Objects
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.FilePickContract.*
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.FilePickerFragmentDirections.goToShowProgressUpload
import `in`.okcredit.user_migration.presentation.ui.file_pick.screen.views.ItemViewFile
import `in`.okcredit.user_migration.presentation.ui.upload_status.model.ListFilesPath
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.*
import tech.okcredit.base.permission.IPermissionListener
import tech.okcredit.base.permission.Permission.requestStoragePermission
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FilePickerFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "UploadFileListScreen",
        R.layout.fragment_file_manager
    ),
    ItemViewFile.ItemViewFileListener {

    internal val binding: FragmentFileManagerBinding by viewLifecycleScoped(FragmentFileManagerBinding::bind)

    private val selectedFileSubject: PublishSubject<String> = PublishSubject.create()

    @Inject
    lateinit var filePickController: Lazy<FilePickController>

    @Inject
    lateinit var userMigrationEventTracker: Lazy<UserMigrationEventTracker>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userMigrationEventTracker.get()
            .trackObjectViewed(Objects.User_Migration_Local_File_Listing_Screen)
        init()
        initClickListener()
        requestStoragePermission()
    }

    private fun init() {
        binding.listFilesRecyclerview.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = filePickController.get().adapter
        }

        filePickController.get().adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                binding.listFilesRecyclerview.scrollToPosition(0)
            }
        })
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            userMigrationEventTracker.get().trackObjectViewed(Objects.STORAGE_PERMISSION)
            requestStoragePermission(
                activity as AppCompatActivity,
                object : IPermissionListener {
                    override fun onPermissionGrantedFirstTime() {
                    }

                    override fun onPermissionGranted() {
                        userMigrationEventTracker.get().trackStoragePermissionInteracted("Granted")
                        pushIntent(Intent.RefreshFiles)
                    }

                    override fun onPermissionDenied() {
                        userMigrationEventTracker.get().trackStoragePermissionInteracted("Denied")
                        requireActivity().finish()
                    }

                    override fun onPermissionPermanentlyDenied() {
                        userMigrationEventTracker.get().trackStoragePermissionInteracted("Denied")
                        requireActivity().finish()
                    }
                }
            )
        }
    }

    private fun initClickListener() {
        binding.submit.setOnClickListener {
            userMigrationEventTracker.get().trackLocalFileListingScreenInteracted("Submit Button")
            pushIntentWithDelay(Intent.CheckNetwork, 200)
        }

        binding.svSearchFile.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                pushIntent(Intent.SearchFiles(query))
                hideSoftKeyboard()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                pushIntent(Intent.SearchFiles(newText))
                return true
            }
        })

        binding.toolbar.setNavigationOnClickListener {
            hideSoftKeyboard()
            requireActivity().finish()
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            selectedFileSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.SetSelectedFile(it)
                },
        )
    }

    override fun render(state: State) {
        binding.apply {
            noFileFoundGroup.isVisible = state.noFileFound
            listFilesRecyclerview.isVisible = state.noFileFound.not()
        }

        setSubmitVisibility(state)
        filePickController.get().setData(state)
    }

    private fun setSubmitVisibility(state: State) {
        if (state.selectedLocalFiles.show) {
            enabledSubmit()
        } else {
            disableSubmit()
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            ViewEvent.GotoUploadStatusScreen -> {
                navigate(goToShowProgressUpload(ListFilesPath(getCurrentState().selectedLocalFiles.filePaths)))
            }
            is ViewEvent.ShowError -> shortToast(event.error)
        }
    }

    override fun onFileSelected(filePath: String) {
        userMigrationEventTracker.get().trackLocalFileListingScreenInteracted("File Item")
        selectedFileSubject.onNext(filePath)
    }

    private fun enabledSubmit() {
        binding.submit.apply {
            elevation = resources.getDimension(R.dimen.view_4dp)
            backgroundTintList =
                ColorStateList.valueOf(getColorFromAttr(R.attr.colorPrimary))
            isEnabled = true
        }
    }

    private fun disableSubmit() {
        binding.submit.apply {
            elevation = 0f
            backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.grey400)
            isEnabled = false
        }
    }
}
