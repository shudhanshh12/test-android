package `in`.okcredit.onboarding.contract.autolang

import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Language(
    val languageCode: String = "",
    val languageTitle: String = "",
    val languageSubTitle: String = "",
    @ColorInt val backgroundColor: Int = 0,
    @ColorInt val fontColor: Int = 0,
    @DrawableRes val letterDrawable: Int = 0
) : Parcelable
