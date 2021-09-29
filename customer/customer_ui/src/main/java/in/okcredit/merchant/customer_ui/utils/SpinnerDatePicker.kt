package `in`.okcredit.merchant.customer_ui.utils

import `in`.okcredit.merchant.customer_ui.databinding.SpinnerDatePickerLayoutBinding
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.shawnlin.numberpicker.NumberPicker
import java.text.DateFormat
import java.text.DateFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class SpinnerDatePicker @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    companion object {
        private const val DATE_FORMAT = "MM/dd/yyyy"
        private const val DEFAULT_START_YEAR = 1900
        private const val DEFAULT_END_YEAR = 2100
    }

    private var mNumberOfMonths = 0

    private lateinit var mTempDate: Calendar

    private lateinit var mMinDate: Calendar

    private lateinit var mMaxDate: Calendar

    private lateinit var mCurrentDate: Calendar

    private var mOnDateChangedListener: OnDateSetListener? = null

    private var mShortMonths = arrayOf<String>()

    private val mDateFormat: DateFormat = SimpleDateFormat(DATE_FORMAT)

    private val binding = SpinnerDatePickerLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private val onChangeListener = NumberPicker.OnValueChangeListener { picker, oldVal, newVal ->
        mTempDate.timeInMillis = mCurrentDate.timeInMillis
        // take care of wrapping of days and months to update greater fields
        when (picker.id) {
            binding.dayPicker.id -> {
                val maxDayOfMonth = mTempDate.getActualMaximum(Calendar.DAY_OF_MONTH)
                if (oldVal == maxDayOfMonth && newVal == 1) {
                    mTempDate.add(Calendar.DAY_OF_MONTH, 1)
                } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                    mTempDate.add(Calendar.DAY_OF_MONTH, -1)
                } else {
                    mTempDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal)
                }
            }
            binding.monthPicker.id -> {
                if (oldVal == 11 && newVal == 0) {
                    mTempDate.add(Calendar.MONTH, 1)
                } else if (oldVal == 0 && newVal == 11) {
                    mTempDate.add(Calendar.MONTH, -1)
                } else {
                    mTempDate.add(Calendar.MONTH, newVal - oldVal)
                }
            }
            binding.yearPicker.id -> {
                mTempDate[Calendar.YEAR] = newVal
            }
            else -> {
                throw IllegalArgumentException()
            }
        }
        // now set the date to the adjusted one
        setDate(
            mTempDate[Calendar.YEAR],
            mTempDate[Calendar.MONTH],
            mTempDate[Calendar.DAY_OF_MONTH]
        )
        updateSpinners()
        notifyDateChanged()
    }

    init {
        setCurrentLocale(Locale.getDefault())

        // day
        binding.dayPicker.formatter = NumberPicker.getTwoDigitFormatter()
        binding.dayPicker.setOnLongPressUpdateInterval(100)
        binding.dayPicker.setOnValueChangedListener(onChangeListener)

        // month
        binding.monthPicker.minValue = 0
        binding.monthPicker.maxValue = mNumberOfMonths - 1
        binding.monthPicker.displayedValues = mShortMonths
        binding.monthPicker.setOnLongPressUpdateInterval(200)
        binding.monthPicker.setOnValueChangedListener(this.onChangeListener)

        // year
        binding.yearPicker.setOnLongPressUpdateInterval(100)
        binding.yearPicker.setOnValueChangedListener(this.onChangeListener)
        binding.yearPicker.formatter = NumberPicker.Formatter { return@Formatter it.toString() }

        // set the min date giving priority of the minDate over startYear
        mTempDate.clear()
        mTempDate.set(DEFAULT_START_YEAR, 0, 1)
        setMinDate(mTempDate.timeInMillis)

        // set the max date giving priority of the maxDate over endYear
        mTempDate.clear()
        mTempDate.set(DEFAULT_END_YEAR, 11, 31)

        setMaxDate(mTempDate.timeInMillis)

        // initialize to current date
        mCurrentDate.timeInMillis = System.currentTimeMillis()
        initDate(
            mCurrentDate[Calendar.YEAR],
            mCurrentDate[Calendar.MONTH],
            mCurrentDate.get(Calendar.DAY_OF_MONTH),
            null
        )
    }

    /**
     * Initialize the state. If the provided values designate an inconsistent
     * date the values are normalized before updating the spinners.
     *
     * @param year The initial year.
     * @param monthOfYear The initial month <strong>starting from zero</strong>.
     * @param dayOfMonth The initial day of the month.
     * @param onDateChangedListener How user is notified date is changed by
     *            user, can be null.
     */
    fun initDate(
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int,
        onDateChangedListener: OnDateSetListener?
    ) {
        setDate(year, monthOfYear, dayOfMonth)
        updateSpinners()

        mOnDateChangedListener = onDateChangedListener
    }

    /**
     * Set the callback that indicates the date has been adjusted by the user.
     *
     * @param callback How user is notified date is changed by
     * user, can be null.
     */
    fun setOnDateSetListener(callback: OnDateSetListener?) {
        mOnDateChangedListener = callback
    }

    /**
     * Update the current date.
     *
     * @param year The year.
     * @param month The month which is **starting from zero**.
     * @param dayOfMonth The day of the month.
     */
    fun updateDate(year: Int, month: Int, dayOfMonth: Int) {
        if (!isNewDate(year, month, dayOfMonth)) {
            return
        }
        setDate(year, month, dayOfMonth)
        updateSpinners()
        notifyDateChanged()
    }

    private fun isNewDate(year: Int, month: Int, dayOfMonth: Int): Boolean {
        return mCurrentDate[Calendar.YEAR] != year || mCurrentDate[Calendar.MONTH] != month || mCurrentDate[Calendar.DAY_OF_MONTH] != dayOfMonth
    }

    private fun setCurrentLocale(locale: Locale) {
        mTempDate = if (!this::mTempDate.isInitialized) Calendar.getInstance(locale) else getCalendarForLocale(mTempDate, locale)
        mMinDate = if (!this::mMinDate.isInitialized) Calendar.getInstance(locale) else getCalendarForLocale(mMinDate, locale)
        mMaxDate = if (!this::mMaxDate.isInitialized) Calendar.getInstance(locale) else getCalendarForLocale(mMaxDate, locale)
        mCurrentDate = if (!this::mCurrentDate.isInitialized) Calendar.getInstance(locale) else getCalendarForLocale(mCurrentDate, locale)

        mNumberOfMonths = mTempDate.getActualMaximum(Calendar.MONTH) + 1
        mShortMonths = DateFormatSymbols().shortMonths
    }

    private fun setDate(year: Int, month: Int, dayOfMonth: Int) {
        mCurrentDate.set(year, month, dayOfMonth)
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.timeInMillis = mMinDate.timeInMillis
        } else if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.timeInMillis = mMaxDate.timeInMillis
        }
    }

    /**
     * Gets a calendar for locale bootstrapped with the value of a given calendar.
     *
     * @param oldCalendar The old calendar.
     * @param locale The locale.
     */
    private fun getCalendarForLocale(oldCalendar: Calendar?, locale: Locale): Calendar {
        return if (oldCalendar == null) {
            Calendar.getInstance(locale)
        } else {
            val currentTimeMillis = oldCalendar.timeInMillis
            val newCalendar = Calendar.getInstance(locale)
            newCalendar.timeInMillis = currentTimeMillis
            newCalendar
        }
    }

    private fun updateSpinners() {
        // set the spinner ranges respecting the min and max dates
        when (mCurrentDate) {
            mMinDate -> {
                binding.dayPicker.minValue = mCurrentDate[Calendar.DAY_OF_MONTH]
                binding.dayPicker.maxValue = mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
                binding.dayPicker.wrapSelectorWheel = false
                binding.monthPicker.displayedValues = null
                binding.monthPicker.minValue = mCurrentDate[Calendar.MONTH]
                binding.monthPicker.maxValue = mCurrentDate.getActualMaximum(Calendar.MONTH)
                binding.monthPicker.wrapSelectorWheel = false
            }
            mMaxDate -> {
                binding.dayPicker.minValue = mCurrentDate.getActualMinimum(Calendar.DAY_OF_MONTH)
                binding.dayPicker.maxValue = mCurrentDate[Calendar.DAY_OF_MONTH]
                binding.dayPicker.wrapSelectorWheel = false
                binding.monthPicker.displayedValues = null
                binding.monthPicker.minValue = mCurrentDate.getActualMinimum(Calendar.MONTH)
                binding.monthPicker.maxValue = mCurrentDate[Calendar.MONTH]
                binding.monthPicker.wrapSelectorWheel = false
            }
            else -> {
                binding.dayPicker.minValue = 1
                binding.dayPicker.maxValue = mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
                binding.dayPicker.wrapSelectorWheel = true
                binding.monthPicker.displayedValues = null
                binding.monthPicker.minValue = 0
                binding.monthPicker.maxValue = 11
                binding.monthPicker.wrapSelectorWheel = true
            }
        }

        // make sure the month names are a zero based array
        // with the months in the month spinner
        val displayedValues = mShortMonths.copyOfRange(
            binding.monthPicker.minValue,
            binding.monthPicker.maxValue + 1
        )
        binding.monthPicker.displayedValues = displayedValues

        // year spinner range does not change based on the current date
        binding.yearPicker.minValue = mMinDate.get(Calendar.YEAR)
        binding.yearPicker.maxValue = mMaxDate.get(Calendar.YEAR)
        binding.yearPicker.wrapSelectorWheel = false

        // set the spinner values
        binding.yearPicker.value = mCurrentDate[Calendar.YEAR]
        binding.monthPicker.value = mCurrentDate[Calendar.MONTH]
        binding.dayPicker.value = mCurrentDate[Calendar.DAY_OF_MONTH]
    }

    /**
     * Notifies the listener, if such, for a change in the selected date.
     */
    private fun notifyDateChanged() {
        mOnDateChangedListener?.onDateSelected(mCurrentDate)
    }

    fun setMinDate(minDate: Long) {
        mTempDate.timeInMillis = minDate
        if (mTempDate[Calendar.YEAR] == mMinDate[Calendar.YEAR] &&
            mTempDate[Calendar.DAY_OF_YEAR] == mMinDate[Calendar.DAY_OF_YEAR]
        ) {
            // Same day, no-op.
            return
        }
        mMinDate.timeInMillis = minDate
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.timeInMillis = mMinDate.timeInMillis
        }
        updateSpinners()
    }

    fun getMinDate(): Calendar? {
        val minDate = Calendar.getInstance()
        minDate.timeInMillis = mMinDate.timeInMillis
        return minDate
    }

    fun setMaxDate(maxDate: Long) {
        mTempDate.timeInMillis = maxDate
        if (mTempDate[Calendar.YEAR] == mMaxDate[Calendar.YEAR] &&
            mTempDate[Calendar.DAY_OF_YEAR] == mMaxDate[Calendar.DAY_OF_YEAR]
        ) {
            // Same day, no-op.
            return
        }
        mMaxDate.timeInMillis = maxDate
        if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.timeInMillis = mMaxDate.timeInMillis
        }
        updateSpinners()
    }

    fun getMaxDate(): Calendar? {
        val maxDate = Calendar.getInstance()
        maxDate.timeInMillis = mMaxDate.timeInMillis
        return maxDate
    }

    /**
     * Parses the given `date` and in case of success sets the result
     * to the `outDate`.
     *
     * @return True if the date was parsed.
     */
    private fun parseDate(date: String, outDate: Calendar): Boolean {
        return try {
            outDate.time = mDateFormat.parse(date) ?: Date()
            true
        } catch (e: ParseException) {
            e.printStackTrace()
            false
        }
    }

    interface OnDateSetListener {
        fun onDateSelected(selectedDate: Calendar)
    }
}
