package `in`.okcredit.onboarding.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Languages(
    val languageCode: String = "",
    val languageTitle: String = "",
    val languageSubTitle: String = "",
    val backgroundColor: Int = 0,
    var selected: Boolean = false,
    val letterText: String = "",
    val letterDrawable: Int = 0,
    val viewType: Int
) : Parcelable
