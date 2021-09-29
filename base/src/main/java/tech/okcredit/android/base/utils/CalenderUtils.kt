package tech.okcredit.android.base.utils

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import tech.okcredit.android.base.R
import java.lang.ref.WeakReference
import java.util.*

class CalenderUtils {
    companion object {
        fun getCalendarId(weakReference: WeakReference<Context?>): Long? {

            var context = weakReference.get()
            context?.let {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_CALENDAR
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    val projection = arrayOf(
                        CalendarContract.Calendars._ID,
                        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
                    )

                    var calCursor = context.contentResolver.query(
                        CalendarContract.Calendars.CONTENT_URI,
                        projection,
                        CalendarContract.Calendars.VISIBLE + " = 1 AND " + CalendarContract.Calendars.IS_PRIMARY + "=1",
                        null,
                        CalendarContract.Calendars._ID + " ASC"
                    )

                    if (calCursor != null && calCursor.count <= 0) {
                        calCursor = context.contentResolver.query(
                            CalendarContract.Calendars.CONTENT_URI,
                            projection,
                            CalendarContract.Calendars.VISIBLE + " = 1",
                            null,
                            CalendarContract.Calendars._ID + " ASC"
                        )
                    }

                    if (calCursor != null) {
                        if (calCursor.moveToFirst()) {
                            val calName: String
                            val calID: String
                            val nameCol = calCursor.getColumnIndex(projection[1])
                            val idCol = calCursor.getColumnIndex(projection[0])
                            calName = calCursor.getString(nameCol)
                            calID = calCursor.getString(idCol)
                            calCursor.close()
                            return calID.toLong()
                        }
                    }
                    return null
                }
                return null
            }

            return null
        }

        fun setEvent(
            calID: Long?,
            weakReference: WeakReference<Context?>,
            description: String?,
            selectedDateInMillis: Long?,
            customerDeepLink: String
        ) {

            val context = weakReference.get()
            context?.let {
                calID?.let {
                    val startMillis: Long = Calendar.getInstance().run {
                        set(
                            DateTimeUtils.getYear(selectedDateInMillis),
                            DateTimeUtils.getMonth(selectedDateInMillis),
                            DateTimeUtils.getDateFromMillis(selectedDateInMillis),
                            10,
                            0,
                            0
                        )
                        timeInMillis
                    }
                    val endMillis: Long = Calendar.getInstance().run {
                        set(
                            DateTimeUtils.getYear(selectedDateInMillis),
                            DateTimeUtils.getMonth(selectedDateInMillis),
                            DateTimeUtils.getDateFromMillis(selectedDateInMillis),
                            10,
                            15,
                            0
                        )

                        timeInMillis
                    }

                    val values = ContentValues().apply {
                        put(CalendarContract.Events.DTSTART, startMillis)
                        put(CalendarContract.Events.DTEND, endMillis)
                        put(CalendarContract.Events.TITLE, context.getString(R.string.collect_due_from, description))
                        put(
                            CalendarContract.Events.DESCRIPTION,
                            context.getString(R.string.collect_due_from, description) + " " + customerDeepLink
                        )
                        put(CalendarContract.Events.CALENDAR_ID, calID)
                        put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Calcutta")
                    }
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_CALENDAR
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        val uri: Uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)!!

                        // get the event ID that is the last element in the Uri
                        val eventID: Long = uri.lastPathSegment!!.toLong()

                        val values1 = ContentValues().apply {
                            put(CalendarContract.Reminders.MINUTES, 5)
                            put(CalendarContract.Reminders.EVENT_ID, eventID)
                            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                        }
                        val uri1: Uri =
                            context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values1)!!
                    }
                }
            }
        }
    }
}
