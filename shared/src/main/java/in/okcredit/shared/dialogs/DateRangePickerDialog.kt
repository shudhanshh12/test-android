package `in`.okcredit.shared.dialogs

import `in`.okcredit.shared.R
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.archit.calendardaterangepicker.customviews.DateRangeCalendarView
import org.joda.time.DateTime
import java.util.*

object DateRangePickerDialog {
    interface Listener {
        fun onDone(startDate: DateTime, endDate: DateTime)
    }

    @JvmStatic
    fun show(context: Context, startDate: DateTime, endDate: DateTime, listener: Listener): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)

        val dialogView = LayoutInflater.from(context).inflate(R.layout.date_range_dialog, null)
        builder.setView(dialogView)
        dialogView.setPadding(0, 0, 0, 0)

        val alertDialog = builder.create()
        val calendar = dialogView.findViewById<DateRangeCalendarView>(R.id.calendar)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

        var selectedStartDate: DateTime = startDate
        var selectedEndDate: DateTime = endDate

        calendar.setCalendarListener(object : DateRangeCalendarView.CalendarListener {
            override fun onFirstDateSelected(startDate: Calendar) {
                selectedStartDate = DateTime(startDate.timeInMillis).withTimeAtStartOfDay()
                selectedEndDate = DateTime(startDate.timeInMillis).withTimeAtStartOfDay().plusDays(1).minusMillis(1)
            }

            override fun onDateRangeSelected(startDate: Calendar, endDate: Calendar) {
                selectedStartDate = DateTime(startDate.timeInMillis).withTimeAtStartOfDay()
                selectedEndDate = DateTime(endDate.timeInMillis).withTimeAtStartOfDay().plusDays(1).minusMillis(1)
            }
        })

        val startSelectionDate = Calendar.getInstance()
        startSelectionDate.timeInMillis = startDate.millis
        val endSelectionDate = Calendar.getInstance()
        endSelectionDate.timeInMillis = endDate.millis
        calendar.setSelectedDateRange(startSelectionDate, endSelectionDate)

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnOk.setOnClickListener {
            listener.onDone(selectedStartDate, selectedEndDate)
            alertDialog.dismiss()
        }

        return alertDialog
    }
}
