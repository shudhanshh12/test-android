package `in`.okcredit.communication_inappnotification.local

import `in`.okcredit.communication_inappnotification.contract.DisplayStatus
import androidx.room.TypeConverter
import com.skydoves.balloon.ArrowOrientation

class DisplayStatusMapper {
    @TypeConverter
    fun statusToString(status: DisplayStatus) = status.name

    @TypeConverter
    fun stringToStatus(statusString: String) = DisplayStatus.valueOf(statusString)
}

class ArrowOrientationMapper {
    @TypeConverter
    fun arrowOrientationToString(arrowOrientation: ArrowOrientation) = arrowOrientation.name

    @TypeConverter
    fun stringToArrowOrientation(string: String) = ArrowOrientation.valueOf(string)
}
