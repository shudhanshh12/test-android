package `in`.okcredit.merchant.customer_ui.ui.subscription.add.frequency

import `in`.okcredit.merchant.customer_ui.R
import `in`.okcredit.merchant.customer_ui.data.server.model.request.DayOfWeek
import `in`.okcredit.merchant.customer_ui.data.server.model.response.SubscriptionFrequency
import `in`.okcredit.merchant.customer_ui.databinding.AddSubscriptionFrequencyBottomSheetBinding
import `in`.okcredit.shared.base.ExpandedBottomSheetDialogFragment
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import tech.okcredit.android.base.extensions.getColorCompat
import tech.okcredit.android.base.extensions.getDrawableCompact
import tech.okcredit.android.base.extensions.gone
import tech.okcredit.android.base.extensions.longToast
import tech.okcredit.android.base.extensions.visible
import java.io.Serializable
import java.util.*

class AddSubscriptionFrequencyBottomSheet : ExpandedBottomSheetDialogFragment() {

    private lateinit var binding: AddSubscriptionFrequencyBottomSheetBinding

    private var selectedFrequency: SubscriptionFrequency? = null
    private var listener: FrequencySubmitListener? = null
    private var daysInWeek: MutableSet<DayOfWeek>? = null

    private var checkedDate = 0

    private lateinit var monthController: MonthController

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.BottomSheetMaterialDialogStyle)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AddSubscriptionFrequencyBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpMonthView()
        setUpWeekView()
        setUpFrequencySelector()

        // setup default or previously selected frequency
        val frequency =
            arguments?.getSerializable(SELECTED_FREQUENCY) as SubscriptionFrequency? ?: SubscriptionFrequency.DAILY
        updateFrequency(frequency)
        updateViewForSelectedFrequency()
        if (frequency == SubscriptionFrequency.MONTHLY) {
            checkForMonthlySelectedDate()
        } else if (frequency == SubscriptionFrequency.WEEKLY) {
            checkForWeeklySelectedDays()
        }

        binding.buttonConfirm.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            listener?.frequencySelected(
                selectedFrequency!!,
                if (selectedFrequency!! == SubscriptionFrequency.WEEKLY) daysInWeek?.toList() else null,
                if (selectedFrequency!! == SubscriptionFrequency.MONTHLY) getStartDateFromCheckedDate() else null
            )
            dismiss()
        }
    }

    private fun checkForWeeklySelectedDays() {
        if (selectedFrequency != SubscriptionFrequency.WEEKLY) return
        val daysInWeek =
            arguments?.getSerializable(WEEK_SELECTED_DAYS) as List<DayOfWeek>?
        if (!daysInWeek.isNullOrEmpty()) {
            this.daysInWeek = mutableSetOf()
            this.daysInWeek?.addAll(daysInWeek)
            updateViewForSelectedDaysInWeek()
        }
    }

    private fun updateViewForSelectedDaysInWeek() {
        binding.apply {
            buttonMonday.isChecked = false
            buttonTuesday.isChecked = false
            buttonWednesday.isChecked = false
            buttonThursday.isChecked = false
            buttonFriday.isChecked = false
            buttonSaturday.isChecked = false
            buttonSunday.isChecked = false
            daysInWeek?.forEach {
                when (it) {
                    DayOfWeek.MONDAY -> buttonMonday.isChecked = true
                    DayOfWeek.TUESDAY -> buttonTuesday.isChecked = true
                    DayOfWeek.WEDNESDAY -> buttonWednesday.isChecked = true
                    DayOfWeek.THURSDAY -> buttonThursday.isChecked = true
                    DayOfWeek.FRIDAY -> buttonFriday.isChecked = true
                    DayOfWeek.SATURDAY -> buttonSaturday.isChecked = true
                    DayOfWeek.SUNDAY -> buttonSunday.isChecked = true
                }
            }
        }
    }

    private fun checkForMonthlySelectedDate() {
        if (selectedFrequency != SubscriptionFrequency.MONTHLY) return
        val startDate = arguments?.getLong(MONTHLY_SELECTED_DATE)
        if (startDate != null && startDate > 0L) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = startDate
            }
            checkedDate = calendar.get(Calendar.DAY_OF_MONTH)
            updateViewForSelectedDateOfMonth()
        }
    }

    private fun updateViewForSelectedDateOfMonth() {
        monthController.setCheckedDate(checkedDate)
    }

    private fun validateInputs(): Boolean {
        if (selectedFrequency == null) {
            longToast(R.string.msg_select_frequency)
            return false
        }

        if (selectedFrequency == SubscriptionFrequency.MONTHLY) {
            if (checkedDate == 0) {
                longToast(R.string.msg_select_date)
                return false
            }
        }

        if (selectedFrequency == SubscriptionFrequency.WEEKLY) {
            if (daysInWeek.isNullOrEmpty()) {
                longToast(R.string.msg_select_days)
                return false
            }
        }

        return true
    }

    private fun getStartDateFromCheckedDate(): Long {
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_MONTH) >= checkedDate) {
            calendar.add(Calendar.MONTH, 1)
        }
        calendar.apply {
            set(Calendar.DAY_OF_MONTH, checkedDate)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun setUpWeekView() {
        binding.apply {
            buttonMonday.setOnCheckedChangeListener { _, isChecked ->
                if (daysInWeek == null) daysInWeek = mutableSetOf()
                if (isChecked) {
                    daysInWeek?.add(DayOfWeek.MONDAY)
                } else {
                    daysInWeek?.remove(DayOfWeek.MONDAY)
                }
            }

            buttonTuesday.setOnCheckedChangeListener { _, isChecked ->
                if (daysInWeek == null) daysInWeek = mutableSetOf()
                if (isChecked) {
                    daysInWeek?.add(DayOfWeek.TUESDAY)
                } else {
                    daysInWeek?.remove(DayOfWeek.TUESDAY)
                }
            }

            buttonWednesday.setOnCheckedChangeListener { _, isChecked ->
                if (daysInWeek == null) daysInWeek = mutableSetOf()
                if (isChecked) {
                    daysInWeek?.add(DayOfWeek.WEDNESDAY)
                } else {
                    daysInWeek?.remove(DayOfWeek.WEDNESDAY)
                }
            }

            buttonThursday.setOnCheckedChangeListener { _, isChecked ->
                if (daysInWeek == null) daysInWeek = mutableSetOf()
                if (isChecked) {
                    daysInWeek?.add(DayOfWeek.THURSDAY)
                } else {
                    daysInWeek?.remove(DayOfWeek.THURSDAY)
                }
            }

            buttonFriday.setOnCheckedChangeListener { _, isChecked ->
                if (daysInWeek == null) daysInWeek = mutableSetOf()
                if (isChecked) {
                    daysInWeek?.add(DayOfWeek.FRIDAY)
                } else {
                    daysInWeek?.remove(DayOfWeek.FRIDAY)
                }
            }

            buttonSaturday.setOnCheckedChangeListener { _, isChecked ->
                if (daysInWeek == null) daysInWeek = mutableSetOf()
                if (isChecked) {
                    daysInWeek?.add(DayOfWeek.SATURDAY)
                } else {
                    daysInWeek?.remove(DayOfWeek.SATURDAY)
                }
            }

            buttonSunday.setOnCheckedChangeListener { _, isChecked ->
                if (daysInWeek == null) daysInWeek = mutableSetOf()
                if (isChecked) {
                    daysInWeek?.add(DayOfWeek.SUNDAY)
                } else {
                    daysInWeek?.remove(DayOfWeek.SUNDAY)
                }
            }
        }
    }

    private fun setUpFrequencySelector() {
        binding.buttonDaily.setOnClickListener {
            updateFrequency(SubscriptionFrequency.DAILY)
            updateViewForSelectedFrequency()
        }

        binding.buttonWeekly.setOnClickListener {
            updateFrequency(SubscriptionFrequency.WEEKLY)
            updateViewForSelectedFrequency()
        }

        binding.buttonMonthly.setOnClickListener {
            updateFrequency(SubscriptionFrequency.MONTHLY)
            updateViewForSelectedFrequency()
        }
    }

    private fun updateFrequency(frequency: SubscriptionFrequency) {
        if (frequency == selectedFrequency) return
        // un-select current selected frequency
        when (selectedFrequency) {
            SubscriptionFrequency.DAILY -> {
                binding.buttonDaily.icon = null
                binding.buttonDaily.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.white))
                binding.buttonDaily.setTextColor(getColorCompat(R.color.grey700))
                binding.buttonDaily.strokeColor = ColorStateList.valueOf(getColorCompat(R.color.grey400))
            }
            SubscriptionFrequency.WEEKLY -> {
                binding.buttonWeekly.icon = null
                binding.buttonWeekly.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.white))
                binding.buttonWeekly.setTextColor(getColorCompat(R.color.grey700))
                binding.buttonWeekly.strokeColor = ColorStateList.valueOf(getColorCompat(R.color.grey400))
            }
            SubscriptionFrequency.MONTHLY -> {
                binding.buttonMonthly.icon = null
                binding.buttonMonthly.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.white))
                binding.buttonMonthly.setTextColor(getColorCompat(R.color.grey700))
                binding.buttonMonthly.strokeColor = ColorStateList.valueOf(getColorCompat(R.color.grey400))
            }
        }

        // select new selected frequency
        when (frequency) {
            SubscriptionFrequency.DAILY -> {
                binding.buttonDaily.icon = getDrawableCompact(R.drawable.ic_tick_green)
                binding.buttonDaily.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.green_lite))
                binding.buttonDaily.setTextColor(getColorCompat(R.color.green_primary))
                binding.buttonDaily.strokeColor = ColorStateList.valueOf(getColorCompat(R.color.green_primary))
            }
            SubscriptionFrequency.WEEKLY -> {
                binding.buttonWeekly.icon = getDrawableCompact(R.drawable.ic_tick_green)
                binding.buttonWeekly.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.green_lite))
                binding.buttonWeekly.setTextColor(getColorCompat(R.color.green_primary))
                binding.buttonWeekly.strokeColor = ColorStateList.valueOf(getColorCompat(R.color.green_primary))
            }
            SubscriptionFrequency.MONTHLY -> {
                binding.buttonMonthly.icon = getDrawableCompact(R.drawable.ic_tick_green)
                binding.buttonMonthly.backgroundTintList = ColorStateList.valueOf(getColorCompat(R.color.green_lite))
                binding.buttonMonthly.setTextColor(getColorCompat(R.color.green_primary))
                binding.buttonMonthly.strokeColor = ColorStateList.valueOf(getColorCompat(R.color.green_primary))
            }
        }
        selectedFrequency = frequency
    }

    private fun updateViewForSelectedFrequency() {
        when (selectedFrequency) {
            SubscriptionFrequency.DAILY -> {
                binding.apply {
                    weeklyGroup.gone()
                    gridMonth.gone()
                }
            }
            SubscriptionFrequency.WEEKLY -> {
                binding.apply {
                    weeklyGroup.visible()
                    gridMonth.gone()
                }
            }
            SubscriptionFrequency.MONTHLY -> {
                binding.apply {
                    weeklyGroup.gone()
                    gridMonth.visible()
                }
            }
        }
        // reset selected date and days in weekly mode
        checkedDate = 0
        daysInWeek = null
        updateViewForSelectedDaysInWeek()
        updateViewForSelectedDateOfMonth()
    }

    private fun setUpMonthView() {
        binding.gridMonth.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            monthController = MonthController()
            monthController.spanCount = 7
            monthController.setListener(::dateClicked)
            adapter = monthController.adapter
            monthController.requestModelBuild()
        }
    }

    private fun dateClicked(date: Int?) {
        if (date == null) return
        checkedDate = date
        monthController.setCheckedDate(checkedDate)
    }

    fun setListener(frequencySubmitListener: FrequencySubmitListener?) {
        this.listener = frequencySubmitListener
    }

    interface FrequencySubmitListener {
        fun frequencySelected(selectedFrequency: SubscriptionFrequency, daysInWeek: List<DayOfWeek>?, startDate: Long?)
    }

    companion object {
        @JvmStatic
        fun getInstance(selectedFrequency: SubscriptionFrequency?, daysInWeek: List<DayOfWeek>?, startDate: Long?) =
            AddSubscriptionFrequencyBottomSheet().apply {
                val bundle = Bundle()
                selectedFrequency?.let {
                    bundle.putSerializable(SELECTED_FREQUENCY, it)
                }
                daysInWeek?.let {
                    bundle.putSerializable(WEEK_SELECTED_DAYS, it as Serializable)
                }
                startDate?.let {
                    bundle.putLong(MONTHLY_SELECTED_DATE, it)
                }
                arguments = bundle
            }

        private const val SELECTED_FREQUENCY = "selected_frequency"
        private const val WEEK_SELECTED_DAYS = "weekly_selected_days"
        private const val MONTHLY_SELECTED_DATE = "monthly_selected_date"
    }
}
