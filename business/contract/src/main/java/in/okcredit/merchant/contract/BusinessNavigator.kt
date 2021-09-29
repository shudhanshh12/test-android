package `in`.okcredit.merchant.contract

import androidx.fragment.app.FragmentManager

interface BusinessNavigator {

    fun showBusinessTypeDialog(
        fragmentManager: FragmentManager,
        businessTypes: List<BusinessType>,
        listener: BusinessTypeListener? = null,
        selectedBusinessTypeId: String = ""
    )

    fun showSwitchBusinessDialog(
        fragmentManager: FragmentManager,
        source: String
    )

    fun showCreateBusinessDialog(
        fragmentManager: FragmentManager
    )
}
