package tech.okcredit.bill_management_ui.selected_bills.selectedimage

import `in`.okcredit.shared.base.BaseFragment
import `in`.okcredit.shared.base.UserIntent
import `in`.okcredit.shared.utils.CommonUtils
import `in`.okcredit.shared.utils.exhaustive
import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.camera.camera_preview_images.CameraImagesPreview
import com.jakewharton.rxbinding3.view.clicks
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.viewLifecycleScoped
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.bill_management_ui.R
import tech.okcredit.bill_management_ui.databinding.SelectedImageFragmentBinding
import tech.okcredit.bill_management_ui.selected_bills.selectedimage.SelectedImageContract.*
import tech.okcredit.bills.BILL_INTENT_EXTRAS
import tech.okcredit.camera_contract.CapturedImage
import tech.okcredit.sdk.analytics.BillTracker
import tech.okcredit.sdk.models.RawBill
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SelectedImageFragment :
    BaseFragment<State, ViewEvent, Intent>(
        "SelectedImageScreen",
        R.layout.selected_image_fragment
    ),
    CameraImagesPreview.PreviewInteractor,
    DialogInterface.OnCancelListener,
    DatePickerDialog.OnDateSetListener {

    private var oldDateTime: Long = 0
    private var defaultDateChange: Boolean = false
    private val onChangeDate: PublishSubject<DateTime> = PublishSubject.create()

    private val deleteImageSubject: PublishSubject<Pair<CapturedImage, ArrayList<CapturedImage>>> =
        PublishSubject.create()
    private val stateChangeSubject: PublishSubject<Boolean> = PublishSubject.create()
    private var datePickerDialog: DatePickerDialog? = null

    var addNoteStarted = false
    var firstFocus = true

    @Inject
    internal lateinit var billTracker: Lazy<BillTracker>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cameraPreview.setListener(this)

        binding.dateTextNew.setOnClickListener {
            billTracker.get().trackDateClicked()

            if (activity != null) {
                if (!requireActivity().isFinishing) {
                    if (datePickerDialog == null) {
                        initDatePicker(getCurrentState())
                    }
                    datePickerDialog?.show()
                }
            }
        }

        binding.addNoteInputField.addTextChangedListener {
            pushIntent(Intent.UpdateNote(it?.toString()))
            if (addNoteStarted.not() && it?.toString()?.length ?: 0 > 0) {
                addNoteStarted = true
                billTracker.get().trackAddNoteStarted("Add Bill", "Fab", "Bill Management")
            }
        }
        binding.done.clicks().throttleFirst(300, TimeUnit.MILLISECONDS).doOnNext {
            if (!binding.addNoteInputField.text.isNullOrBlank()) {
                billTracker.get()
                    .trackAddNoteCompleted(
                        "Add Bill",
                        "Fab",
                        "Bill Management",
                        binding.addNoteInputField.text.toString()
                    )
            }
            pushIntent(
                Intent.OnDoneClicked(
                    RawBill(
                        binding.addNoteInputField.text.toString(),
                        getCurrentState().imageList,
                        getCurrentState().date
                    )
                )
            )
        }.subscribe()

        binding.delete.setOnClickListener {
            val deletedImage = binding.cameraPreview.deleteImage()
            deletedImage?.let {
                deleteImageSubject.onNext(it to getCurrentState().imageList)
            }
        }
        binding.addNoteInputField.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus && firstFocus) {
                firstFocus = false
                billTracker.get().trackAddNoteClicked("Add Bill", "Fab", "Bill Management")
            }
        }
    }

    // Dismissing billDate dialog
    override fun onCancel(p0: DialogInterface?) {
        datePickerDialog?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (datePickerDialog != null && datePickerDialog?.isShowing == true) {
            datePickerDialog?.dismiss()
        }
    }

    private fun initDatePicker(state: State) {
        datePickerDialog = DatePickerDialog(
            requireContext(),
            this,
            state.date.year,
            state.date.monthOfYear.minus(1),
            state.date.dayOfMonth
        )
        if (datePickerDialog?.datePicker != null) {
            datePickerDialog?.datePicker?.maxDate = CommonUtils.currentDateTime().millis
        }

        datePickerDialog?.setButton(DatePickerDialog.BUTTON_POSITIVE, getString(R.string.ok), datePickerDialog)
        datePickerDialog?.setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), datePickerDialog)
        datePickerDialog?.setOnCancelListener(this)
    }

    // On Date Changed
    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)

        val newDate = DateTime(calendar.timeInMillis)
        billTracker.get().trackDateUpdate("Bill Management", oldDateTime, newDate.millis)
        defaultDateChange = true
        onChangeDate.onNext(newDate)
        datePickerDialog?.dismiss()
    }

    private val binding: SelectedImageFragmentBinding by viewLifecycleScoped(SelectedImageFragmentBinding::bind)

    override fun loadIntent(): UserIntent? {
        return Intent.Load
    }

    override fun userIntents(): Observable<UserIntent> {
        return Observable.mergeArray(
            onChangeDate
                .throttleFirst(400, TimeUnit.MILLISECONDS)
                .map {
                    Intent.OnChangeDate(it)
                },
            stateChangeSubject
                .throttleFirst(400, TimeUnit.MILLISECONDS)
                .map {
                    Intent.StateChange(it)
                },
            deleteImageSubject
                .throttleFirst(400, TimeUnit.MILLISECONDS)
                .map {
                    Intent.DeleteImage(it)
                }

        )
    }

    override fun render(state: State) {
        state.imageList.let {
            if (it.size > 0) {
                binding.cameraPreview.setImages(it)
            }
        }

        if (state.submitLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.done.visibility = View.INVISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.done.visibility = View.VISIBLE
        }

        binding.dateTextNew.postDelayed(
            {
                oldDateTime = state.date.millis
                if (view != null) {
                    binding.dateTextNew.setText(DateTimeUtils.formatDateOnly(state.date), TextView.BufferType.NORMAL)
                }
            },
            400
        )
    }

    override fun handleViewEvent(event: ViewEvent) {
        when (event) {
            is ViewEvent.GoBack -> onImageAddedSuccessfully(event.billId)
        }.exhaustive
    }

    private fun onImageAddedSuccessfully(billId: String) {
        billTracker.get().trackAddBillSuccess(
            getCurrentState().imageList.size,
            getCurrentState().date,
            getCurrentState().note,
            billId,
            defaultDateChange,
            "New",
            flow = "Add Bill",
        )
        val intent = android.content.Intent()
        intent.putExtra(BILL_INTENT_EXTRAS.STATUS.KEY, BILL_INTENT_EXTRAS.STATUS.IMAGE_SUCCESS)
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    override fun onLastImageDeletion() {
        activity?.onBackPressed()
    }

    override fun onFirstItemLeftScrolled() {
        activity?.onBackPressed()
    }

    override fun onCameraClicked() {
        billTracker.get().trackAddMoreBillManagement(
            "Add Bill"
        )
        activity?.onBackPressed()
    }
}
