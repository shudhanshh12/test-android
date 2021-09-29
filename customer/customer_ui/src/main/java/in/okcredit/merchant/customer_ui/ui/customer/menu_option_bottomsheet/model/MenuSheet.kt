package `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.model

import `in`.okcredit.merchant.customer_ui.ui.customer.menu_option_bottomsheet.MenuOptionsBottomSheet
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MenuSheet(
    val menuOptionsToShow: List<MenuOptionsBottomSheet.Companion.MenuOptions> = mutableListOf(),
    val menuOptionsToHide: List<MenuOptionsBottomSheet.Companion.MenuOptions> = mutableListOf(),
) : Parcelable
