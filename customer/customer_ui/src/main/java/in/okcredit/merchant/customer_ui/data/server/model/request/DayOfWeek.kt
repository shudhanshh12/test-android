package `in`.okcredit.merchant.customer_ui.data.server.model.request

import android.text.SpannableStringBuilder
import com.google.gson.annotations.SerializedName
import java.text.DateFormatSymbols
import java.util.*

enum class DayOfWeek(val value: Int) {
    @SerializedName("0")
    MONDAY(0),

    @SerializedName("1")
    TUESDAY(1),

    @SerializedName("2")
    WEDNESDAY(2),

    @SerializedName("3")
    THURSDAY(3),

    @SerializedName("4")
    FRIDAY(4),

    @SerializedName("5")
    SATURDAY(5),

    @SerializedName("6")
    SUNDAY(6);

    companion object {
        @JvmStatic
        fun getDay(value: Int) = when (value) {
            MONDAY.value -> MONDAY
            TUESDAY.value -> TUESDAY
            WEDNESDAY.value -> WEDNESDAY
            THURSDAY.value -> THURSDAY
            FRIDAY.value -> FRIDAY
            SATURDAY.value -> SATURDAY
            SUNDAY.value -> SUNDAY
            else -> MONDAY
        }
    }
}

fun List<DayOfWeek>.toFormattedString(): String {
    if (this.isNullOrEmpty()) return ""
    val weekDays = DateFormatSymbols.getInstance().shortWeekdays
    val spannableStringBuilder = SpannableStringBuilder()
    if (this.contains(DayOfWeek.SUNDAY)) spannableStringBuilder.append(weekDays[Calendar.SUNDAY])
    if (this.contains(DayOfWeek.MONDAY)) {
        if (spannableStringBuilder.isNotEmpty()) spannableStringBuilder.append(", ")
        spannableStringBuilder.append(weekDays[Calendar.MONDAY])
    }
    if (this.contains(DayOfWeek.TUESDAY)) {
        if (spannableStringBuilder.isNotEmpty()) spannableStringBuilder.append(", ")
        spannableStringBuilder.append(weekDays[Calendar.TUESDAY])
    }
    if (this.contains(DayOfWeek.WEDNESDAY)) {
        if (spannableStringBuilder.isNotEmpty()) spannableStringBuilder.append(", ")
        spannableStringBuilder.append(weekDays[Calendar.WEDNESDAY])
    }
    if (this.contains(DayOfWeek.THURSDAY)) {
        if (spannableStringBuilder.isNotEmpty()) spannableStringBuilder.append(", ")
        spannableStringBuilder.append(weekDays[Calendar.THURSDAY])
    }
    if (this.contains(DayOfWeek.FRIDAY)) {
        if (spannableStringBuilder.isNotEmpty()) spannableStringBuilder.append(", ")
        spannableStringBuilder.append(weekDays[Calendar.FRIDAY])
    }
    if (this.contains(DayOfWeek.SATURDAY)) {
        if (spannableStringBuilder.isNotEmpty()) spannableStringBuilder.append(", ")
        spannableStringBuilder.append(weekDays[Calendar.SATURDAY])
    }
    return spannableStringBuilder.toString()
}
