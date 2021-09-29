package `in`.okcredit.shared.dialogs

import `in`.okcredit.shared.R
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import `in`.okcredit.shared.databinding.DialogDatePickerBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.joda.time.DateTime
import tech.okcredit.android.base.extensions.setBooleanVisibility
import tech.okcredit.android.base.extensions.shortToast
import tech.okcredit.android.base.utils.DateTimeUtils

class DateRangeDialog : ExpandedBottomSheetDialogFragment() {

    companion object {
        const val TAG = "DateRangeDialog"
    }

    lateinit var binding: DialogDatePickerBinding

    private var isFromSelected = true
    private var fromDate = DateTime.now()
    private var toDate = DateTime.now()
    private var listener: Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.RoundedCornerBottomSheet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogDatePickerBinding.inflate(inflater, container, false)
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
        binding.fromDate.text = DateTimeUtils.formatDateOnly(fromDate)
        binding.toDate.text = DateTimeUtils.formatDateOnly(toDate)
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            if (isFromSelected) {
                fromDate = DateTime(year, month.plus(1), dayOfMonth, 0, 0)
                binding.fromDate.text = DateTimeUtils.formatDateOnly(fromDate)
            } else {
                val selectedDate = DateTime(year, month.plus(1), dayOfMonth, 0, 0)
                if (selectedDate.isBefore(fromDate)) {
                    context?.shortToast("Date cannot be before start date")
                    return@setOnDateChangeListener
                }
                toDate = selectedDate
                binding.toDate.text = DateTimeUtils.formatDateOnly(toDate)
            }
        }
        binding.calendarView.maxDate = DateTime.now().millis
        binding.fromSelector.setOnClickListener {
            isFromSelected = true
            binding.fromIndicator.setBooleanVisibility(true)
            binding.toIndicator.setBooleanVisibility(false)
            binding.calendarView.setDate(fromDate.millis, true, true)
        }

        binding.toSelector.setOnClickListener {
            isFromSelected = false
            binding.fromIndicator.setBooleanVisibility(false)
            binding.toIndicator.setBooleanVisibility(true)
            binding.calendarView.setDate(toDate.millis, true, false)
        }

        binding.done.setOnClickListener {
            listener?.onDateRangeChanged(fromDate, toDate)
            dismiss()
        }
        binding.clear.setOnClickListener {
            listener?.onClearDateRange()
            dismiss()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    interface Listener {
        fun onDateRangeChanged(start: DateTime, end: DateTime)
        fun onClearDateRange()
    }
}
