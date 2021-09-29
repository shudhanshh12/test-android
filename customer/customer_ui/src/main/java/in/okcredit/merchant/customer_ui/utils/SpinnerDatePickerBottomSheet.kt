package `in`.okcredit.merchant.customer_ui.utils

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.databinding.SpinnerDatePickerBottomSheetBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class SpinnerDatePickerBottomSheet :
    ExpandedBottomSheetDialogFragment(),
    SpinnerDatePicker.OnDateSetListener {

    private lateinit var binding: SpinnerDatePickerBottomSheetBinding

    private val selectedCalendar = Calendar.getInstance()

    private var onDateSelectedListener: OnDateSetListener? = null

    private val dateFormat by lazy { SimpleDateFormat.getDateInstance(DateFormat.MEDIUM) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.BottomSheetMaterialDialogStyle)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SpinnerDatePickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDatePicker()

        binding.buttonConfirm.text = getString(R.string.confirm_date, dateFormat.format(selectedCalendar.time))
        binding.buttonConfirm.setOnClickListener {
            onDateSelectedListener?.onDateSelected(selectedCalendar)
            dismiss()
        }

        binding.tvToday.setOnClickListener {
            val todayCalendar = Calendar.getInstance()
            val currentYear = todayCalendar.get(Calendar.YEAR)
            val currentMonth = todayCalendar.get(Calendar.MONTH)
            val currentDayOfMonth = todayCalendar.get(Calendar.DAY_OF_MONTH)
            binding.datePicker.updateDate(currentYear, currentMonth, currentDayOfMonth)
        }
    }

    private fun initDatePicker() {
        if (arguments?.containsKey(SELECTED_CALENDAR) == true) {
            val calendar = arguments?.getSerializable(SELECTED_CALENDAR) as Calendar
            selectedCalendar.timeInMillis = calendar.timeInMillis
        }

        val year = selectedCalendar.get(Calendar.YEAR)
        val monthOfYear = selectedCalendar.get(Calendar.MONTH)
        val dayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH)
        binding.datePicker.initDate(year, monthOfYear, dayOfMonth, this)
        binding.datePicker.setMaxDate(Calendar.getInstance().timeInMillis)
    }

    fun setDateSelectedListener(onDateSelectedListener: OnDateSetListener) {
        this.onDateSelectedListener = onDateSelectedListener
    }

    override fun onDateSelected(selectedDate: Calendar) {
        selectedCalendar.timeInMillis = selectedDate.timeInMillis
        binding.buttonConfirm.text = getString(R.string.confirm_date, dateFormat.format(selectedCalendar.time))
    }

    companion object {
        const val SELECTED_CALENDAR = "selected_calendar"

        @JvmStatic
        fun getInstance(selectedDate: Calendar?) = SpinnerDatePickerBottomSheet().apply {
            if (selectedDate != null) {
                val bundle = Bundle()
                bundle.putSerializable(SELECTED_CALENDAR, selectedDate)
                arguments = bundle
            }
        }
    }

    interface OnDateSetListener {
        fun onDateSelected(selectedDate: Calendar)
    }
}
