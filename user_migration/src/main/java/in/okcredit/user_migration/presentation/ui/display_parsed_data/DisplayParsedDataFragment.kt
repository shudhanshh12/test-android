package `in`.okcredit.user_migration.presentation.ui.display_parsed_data

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.user_migration.R
import `in`.okcredit.user_migration.databinding.FragmentShowParseDataBinding
import `in`.okcredit.user_migration.presentation.analytics.UserMigrationEventTracker
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.DisplayParsedDataContract.*
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.models.CustomerUiTemplate
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.views.DisplayParsedDataController
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.views.ItemViewCustomerList
import `in`.okcredit.user_migration.presentation.ui.display_parsed_data.views.ItemViewFileName
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import tech.okcredit.android.base.extensions.getColorFromAttr
import tech.okcredit.android.base.extensions.getNavigationResultLiveData
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.extensions.visible
import tech.okcredit.app_contract.LegacyNavigator
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DisplayParsedDataFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "ShowProgressUploadingScreen",
        R.layout.fragment_show_parse_data
    ),
    ItemViewFileName.ItemViewFileListener,
    ItemViewCustomerList.ItemViewCustomerListener {

    private val openPdfFileSubject: PublishSubject<String> = PublishSubject.create()
    private val onEditDetailPublishSubject: PublishSubject<CustomerUiTemplate> = PublishSubject.create()

    private val binding: FragmentShowParseDataBinding by viewLifecycleScoped(FragmentShowParseDataBinding::bind)

    @Inject
    lateinit var itemViewFileController: Lazy<DisplayParsedDataController>

    @Inject
    lateinit var legacyNavigator: Lazy<LegacyNavigator>

    @Inject
    lateinit var userMigrationEventTracker: Lazy<UserMigrationEventTracker>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userMigrationEventTracker.get()
            .trackObjectViewed(UserMigrationEventTracker.Objects.User_Migration_Display_Parsed_Customer_Screen)
        init()
        initCLickListener()
    }

    private fun init() {
        binding.pdfFilesUploadView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = itemViewFileController.get().adapter
        }
        getNavigationResultLiveData()?.observe(
            viewLifecycleOwner,
            Observer {
                pushIntent(Intent.UpdateCustomer(it as CustomerUiTemplate))
            }
        )
    }

    private fun initCLickListener() {
        binding.toolbar.setNavigationOnClickListener {
            this.findNavController().popBackStack()
        }
    }

    override fun render(state: State) {
        binding.tvCustomersCount.text = requireContext().getString(
            R.string.customers_count,
            state.totalCustomerCount.toString()
        )
        setViewVisibility(state)
        itemViewFileController.get().setData(state.models)
    }

    private fun setViewVisibility(state: State) = binding.apply {
        if (state.fileParserLoading || state.parserError || state.networkError) {
            btnUiContainer.gone()
        } else {
            btnUiContainer.visible()
        }
        if (state.createCustomerLoading) {
            pbCreateCustomer.visible()
            disableSubmit()
        } else {
            pbCreateCustomer.gone()
            enabledSubmit()
        }

        if (state.totalCustomerCount == 0) {
            disableSubmit()
        } else {
            enabledSubmit()
        }
    }

    override fun loadIntent(): UserIntent {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(

            openPdfFileSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.OpenPdfFile(it)
                },

            onEditDetailPublishSubject
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .map {
                    Intent.EditDetails(it)
                },

            binding.submit.clicks()
                .throttleFirst(1, TimeUnit.SECONDS)
                .map {
                    userMigrationEventTracker.get()
                        .trackDisplayParsedCustomerScreenInteracted("Submit Button")
                    Intent.Submit(getCurrentState().models)
                }
        )
    }

    private fun openPdfFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().applicationContext.packageName + ".provider",
                file
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: Exception) {
            shortToast(R.string.pdf_not_found)
        }
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.ShowError -> {
                shortToast(event.error)
            }
            is ViewEvent.OpenPdfFile -> openPdfFile(event.file)
            is ViewEvent.GoToHome -> goToHome()
            is ViewEvent.GoToEditDetailsDialog -> goToEditDetailsDialog(event.customer)
        }
    }

    private fun goToHome() {
        requireActivity().finish()
        legacyNavigator.get().goToHome(requireActivity())
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

    override fun showEditDetailsDialog(customer: CustomerUiTemplate) {
        onEditDetailPublishSubject.onNext(customer)
    }

    private fun goToEditDetailsDialog(customer: CustomerUiTemplate) {
        findNavController().navigate(DisplayParsedDataFragmentDirections.goToEditDetailsBottomsheet(customer))
    }

    override fun removeCustomer(customer: CustomerUiTemplate) {
        pushIntent(Intent.UpdateCustomer(customer))
    }

    override fun showSolveErrorMessage() {
        shortToast(R.string.error_message_solve_issue_before_adding)
    }

    override fun onPdfLinkClicked(fileName: String) {
        openPdfFileSubject.onNext(fileName)
    }
}
