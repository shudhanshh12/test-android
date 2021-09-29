package `in`.okcredit.merchant.customer_ui.ui.customer

import `in`.okcredit.backend._offline.model.DueInfo
import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.DialogDueDatePickerBinding
import `in`.okcredit.merchant.customer_ui.utils.calender.MonthView.CapturedDate
import `in`.okcredit.merchant.customer_ui.utils.calender.OKCDate
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import tech.okcredit.android.base.utils.DateTimeUtils
import tech.okcredit.android.base.utils.ThreadUtils
import java.net.InetAddress

class DueDatePickerDialog : ExpandedBottomSheetDialogFragment() {

    private lateinit var binding: DialogDueDatePickerBinding
    internal var internetDisposable: Disposable? = null
    private var dueInfo: DueInfo? = null

    companion object {
        const val TAG = "DueDatePickerDialog"
        var selectedDueDate: CapturedDate? = null
    }

    private lateinit var interactionListener: InteractionListener

    interface InteractionListener {
        fun onOkClicked(
            capturedDate: CapturedDate,
            suggestedDaysSpan: String
        )

        fun onCancelClicked()
        fun onClearClicked()
        fun onOutsideClicked()
    }

    fun addInteractionListnener(interactionListener: InteractionListener): DueDatePickerDialog {
        this.interactionListener = interactionListener
        return this
    }

    override fun onPause() {
        super.onPause()
        interactionListener.onCancelClicked()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogDueDatePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog?
            val bottomSheet = dialog!!.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0
        }
        binding.monthView.refresh()
        binding.monthView.setOnDateSelectListener { capturedDate ->
            if (capturedDate.dateStatus == CapturedDate.DateStatus.ADDED) {
                selectedDueDate = capturedDate
            }
            interactionListener.onOkClicked(capturedDate, "")
        }
        binding.monthView.setOnDateClearListner { capturedDate -> interactionListener.onOkClicked(capturedDate, "") }
        dueInfo?.let {
            binding.monthView.setDueInfo(it)
            if (it.isDueActive && it.activeDate != null) {
                if (DateTimeUtils.isCurrentDateCrossed(it.activeDate))
                    binding.monthView.setReleventDate(it.activeDate)
            }
            if (it.isDueActive.not()) {
                binding.reminderTitle.text =
                    getString(R.string.sms_reminder_will_be_sent_to_your_customer_on_selected_reminder_date)
            } else {
                binding.reminderTitle.text =
                    getString(
                        R.string.selected_due_date_title,
                        DateTimeUtils.getFormat2(
                            requireContext(),
                            it.activeDate
                        )
                    )
            }
        }

        binding.clear.setOnClickListener { v: View? ->
            internetDisposable = isInternetAvailable().subscribe {
                if (it) {
                    interactionListener.onClearClicked()
                    if (binding.monthView.selectedDate != null) {
                        binding.monthView.selectedDate.dateStatus = CapturedDate.DateStatus.DELETED
                        interactionListener.onOkClicked(binding.monthView.selectedDate, "")
                    } else {
                        interactionListener.onCancelClicked()
                    }
                } else {
                    Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.fiveDay.setOnClickListener {
            internetDisposable = isInternetAvailable().subscribe { aBoolean: Boolean ->
                if (aBoolean) {
                    val capturedDate = CapturedDate(
                        OKCDate(DateTimeUtils.getFifthDateFromNow(), DateTimeUtils.getFifthDateTimeInMillis()),
                        CapturedDate.DateStatus.ADDED
                    )
                    selectedDueDate = capturedDate
                    interactionListener.onOkClicked(capturedDate, "5")
                } else {
                    Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.fifteenDays.setOnClickListener {
            // NEW DATE
            internetDisposable = isInternetAvailable().subscribe { aBoolean: Boolean ->
                if (aBoolean) {
                    val capturedDate = CapturedDate(
                        OKCDate(DateTimeUtils.getFifteenDateFromNow(), DateTimeUtils.getFifteenDateTimeInMillis()),
                        CapturedDate.DateStatus.ADDED
                    )
                    selectedDueDate = capturedDate
                    interactionListener.onOkClicked(capturedDate, "15")
                } else {
                    Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.tenDays.setOnClickListener {
            internetDisposable = isInternetAvailable().subscribe { aBoolean: Boolean ->
                if (aBoolean) {
                    val capturedDate = CapturedDate(
                        OKCDate(DateTimeUtils.getTenthDateFromNow(), DateTimeUtils.getTenthDateTimeInMillis()),
                        CapturedDate.DateStatus.ADDED
                    )
                    selectedDueDate = capturedDate
                    interactionListener.onOkClicked(capturedDate, "10")
                } else {
                    Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.thirtyDays.setOnClickListener {
            internetDisposable = isInternetAvailable().subscribe { aBoolean: Boolean ->
                if (aBoolean) {
                    val capturedDate = CapturedDate(
                        OKCDate(DateTimeUtils.getThirtyDateFromNow(), DateTimeUtils.getThirtyDateTimeInMillis()),
                        CapturedDate.DateStatus.ADDED
                    )
                    selectedDueDate = capturedDate
                    interactionListener.onOkClicked(capturedDate, "30")
                } else {
                    Toast.makeText(context, "No Internet", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val d: Dialog = super.onCreateDialog(savedInstanceState)
        d.setOnShowListener { dialog ->
            val d: BottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet: FrameLayout =
                d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behaviour = BottomSheetBehavior.from(bottomSheet)
            behaviour.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        handleUserExit()
                        dismiss()
                    }
                }
            })
        }
        return d
    }

    private fun isInternetAvailable(): Observable<Boolean> {
        return Observable.fromCallable {
            try {
                val ipAddr: InetAddress = InetAddress.getByName("google.com")
                !ipAddr.equals("")
            } catch (e: Exception) {
                false
            }
        }.subscribeOn(ThreadUtils.api()).observeOn(AndroidSchedulers.mainThread())
    }

    internal fun handleUserExit() {
        interactionListener.onOutsideClicked()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        handleUserExit()
    }

    override fun onDetach() {
        internetDisposable?.dispose()
        super.onDetach()
    }

    fun setUI(value: DueInfo?) {
        dueInfo = value
    }
}
